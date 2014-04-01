(ns node.process
  (:require [node.utils :refer [json->edn
                                reflect-atom-fields!
                                pipe-events-onto-atom!]]
            [node.process.patch]
            [clojure.string :as string]))

(def ^:private *process* js/process)
(def ^:private *env* (.-env *process*))
(def ^:private windows? (= "win32" (.-platform *process*)))

(def ^:private
  *static-fields*
  {;; The PID of the process.
   :pid (.-pid *process*)
   ;; A vector containing the command line arguments.
   ;; The first element will be 'node', the second element
   ;; will be the name of the JavaScript file. The next elements
   ;; will be any additional command line arguments.
   :command-line-arguments (vec (.-argv *process*))
   ;; This is the set of node-specific command line options from the
   ;; executable that started the process. These options do not show
   ;; up in process.argv, and do not include the node executable, the
   ;; name of the script, or any options following the script name.
   ;; These options are useful in order to spawn child processes with
   ;; the same execution environment as the parent.
   ;; fore example:
   ;;
   ;; node --harmony script.js --version
   ;;
   ;; (:exec-arguments @node.process/process)
   ;; ;; => ["--harmony"]
   ;; (:command-line-arguments @node.process/process)
   ;; ;; => ["/usr/local/bin/node" "script.js" "--version"]
   :exec-arguments (vec (.-execArgv *process*))
   ;; This is the absolute pathname of the executable that started
   ;; the process.
   :exec-path (.-execPath *process*)
   ;; A compiled-in property that exposes NODE_VERSION.
   :version (.-version *process*)
   ;; A property exposing version strings of node and its dependencies.
   :versions (json->edn (.-versions *process*))
   ;; A map representation of the configure options that were used
   ;; to compile the current node executable. This is the same as
   ;; the "config.gypi" file that was produced when running the
   ;; ./configure script.
   :config (json->edn (.-config *process*))
   ;; What processor architecture you're running on: :arm, :ia32,
   ;; or :x64.
   :architecture (keyword (.-arch *process*))
   ;; What platform you're running on: :darwin, :freebsd, :linux,
   ;; :sunos or :win32
   :platform (keyword (.-platform *process*))
   ;; Operating system's default directory for temp files.
   :temp-directory (if windows?
                     (or (.-TEMP *env*)
                         (.-TMP *env*)
                         (str (or (.-SystemRoot *env*)
                                  (.-windir *env*))
                              "\\temp"))
                     (or (.-TMPDIR *env*)
                         (.-TMP *env*)
                         (.-TEMP *env*)
                         "/tmp"))})


(def ^:private
  *mutable-fields*
  (merge {;; Title displayed in 'ps'.
          :title (.-title *process*)
          ;; Current working directory of the process.
          :working-directory (.cwd *process*)
          ;; Callbacks passed to (core.async/put! node.scheduler/next task) will
          ;; usually be called at the end of the current flow of execution,
          ;; and are thus approximately as fast as calling a function
          ;; synchronously. Left unchecked, this would starve the event loop,
          ;; preventing any I/O from occurring.
          :max-next-depth (.-maxTickDepth *process*)
          ;; Process's file mode creation mask. Child processes inherit the mask
          ;; from the parent process.
          :mask (.umask *process*)}
         (when-not windows?
           ;; Note: this function is only available on POSIX platforms
           ;; (i.e. not Windows)
           {;; Gets the group identity of the process. (See getgid(2).)
            ;; This is the numerical group id, not the group name.
            :group-id (.getgid *process*)
            ;; Gets the user identity of the process. (See getuid(2).)
            ;; This is the numerical userid, not the username.
            :user-id (.getuid *process*)
            ;; A vector with the supplementary group IDs. POSIX leaves it
            ;; unspecified if the effective group ID is included but node.js
            ;; ensures it always is.
            :groups (json->edn (.getgroups *process*))})))

(def ^:private *static-field-names* (keys *static-fields*))
(def ^:private *mutable-field-names* (keys *mutable-fields*))

(def process
  (atom (merge *static-fields* *mutable-fields*)
        :validator #(= (select-keys % *static-field-names*)
                       *static-fields*)))

;; Setup an event listeners to reflect changes on the
;; node process object onto `process` atom.
(pipe-events-onto-atom!
 *process*
 process
 {:title "title"
  :working-directory "cwd"
  :user-id "uid"
  :group-id "gid"
  :groups "groups"
  :max-next-depth "maxTickDepth"
  :mask "umask"})

;; Setup a watcher on a process atom to reflect changes
;; to a mutable fields on actual node process.
(reflect-atom-fields!
 process
 {:title #(set! (.-title *process*) %)
  :working-directory #(.chdir *process* %)
  :user-id #(.setuid *process* %)
  :group-id #(.setgid *process* %)
  :groups #(.setgroups *process* (clj->js %))
  :max-next-depth #(set! (.-maxTickDepth *process*) %)
  :mask #(.umask *process* %)})


(defn abort!
  "This causes node to emit an abort. This will cause node to exit
  and generate a core file."
  []
  (.abort *process*))

(defn exit!
  "Ends the process with the specified code. If omitted, exit uses
  the 'success' code 0."
  ([] (exit! 0))
  ([code] (.exit *process* code)))

(defn kill!
  "Send a signal to a process. process|pid is either process atom
  or the pid  (process id). Optionally a signal keyword maybe given
  to describe signal to be send. Signal names are keywords like
  :sigint or :sighup. If omitted, the signal will be :sigterm.

  Will throw an error if target does not exist, and as a special
  case, a signal of 0 can be used to test for the existence of a
  process.

  Note that just because the name of this function is process/kill!,
  it is really just a signal sender, like the kill system call. The
  signal sent may do something other than kill the target process."
  ([process|pid]
   (.kill *process*
          (if (integer? process|pid)
            process|pid
            (:pid @process|pid))))
  ([process|pid signal]
     (.kill *process*
            (if (integer? process|pid)
              process|pid
              (:pid @process|pid))

            (string/upper-case (name signal)))))
