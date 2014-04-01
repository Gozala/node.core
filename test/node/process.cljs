(ns test.node.process
  (:require [cemerick.cljs.test :as test]
            [clojure.string :as string]
            [node.process :refer [process]]
            [node.path :as path]
            [node.utils :refer [json->edn]])
  (:require-macros [cemerick.cljs.test
                    :refer [is are deftest done with-test
                            thrown? run-tests testing
                            test-var]]))


(def posix? (not= :win32 (:platform @process)))

(deftest process-type
  (is (satisfies? IDeref process)
      "node.process/process is an atom"))


(deftest process-api
  (are [field type?] (type? (field @process))
       :command-line-arguments vector?
       :command-line-arguments #(every? string? %)
       :exec-arguments vector?
       :exec-arguments #(every? string? %)
       :exec-path string?
       :version string?
       :versions map?
       :versions #(every? keyword? (keys %))
       :config map?
       :config #(every? keyword? (keys %))
       :architecture #{:arm :ia32 :x64}
       :platform #{:darwin :freebsd :linux :sunos :win32}
       :title string?
       :working-directory string?
       :max-next-depth integer?
       :mask integer?
       :group-id (if posix? integer? nil?)
       :user-id (if posix? integer? nil?)
       :groups (if posix? vector? nil?)))

(defn assert-values
  [process node]
  (are [field getter] (= (field @process) (getter node))
       :command-line-arguments #(vec (.-argv %))
       :exec-arguments #(vec (.-execArgv %))
       :exec-path #(.-execPath %)
       :version #(.-version %)
       :versions #(json->edn (.-versions %))
       :config #(json->edn (.-config %))
       :architecture #(keyword (.-arch %))
       :platform #(keyword (.-platform %))
       :title #(.-title %)
       :working-directory #(.cwd %)
       :max-next-depth #(.-maxTickDepth %)
       :mask #(.umask %)
       :group-id #(if posix? (.getgid %))
       :user-id #(if posix? (.getuid %))
       :groups #(if posix? (json->edn (.getgroups %)))))


(deftest process-values
  (assert-values process js/process))

(deftest process-static-fields
  (are [field value]
       (thrown? js/Error (swap! process assoc field value))
       :command-line-arguments ["hello"]
       :exec-arguments ["hello"]
       :exec-path "5"
       :version "5"
       :versions nil
       :versions {}
       :config {}
       :architecture :arm
       :platform :win32))

(deftest process-changes
  (are [field value access]
       (let [before (field @process)
             ;; Store value since it may be expression
             ;; and we want single execution.
             after value

             _! (swap! process assoc field after)
             changed? (= after (access js/process))

             _! (swap! process assoc field before)

             changed-back? (= before (access js/process))]
         (and (not= before after)
              changed?
              changed-back? ))

       :title "testify" #(.-title %)

       :working-directory
       (path/directory (:working-directory @process))
       #(.cwd %)

       :max-next-depth
       (+ (:max-next-depth @process) 17)
       #(.-maxTickDepth %)

       :mask
       8r0777
       #(.umask %)))

(deftest node-changes
  (are [field change value]
       (let [before (field @process)
             ;; Store value since it may be expression
             ;; and we want single execution.
             after value

             _! (change js/process after)
             changed? (= after (field @process))

             _! (change js/process before)

             changed-back? (= before (field @process))]
         (and (not= before after)
              changed?
              changed-back? ))

       :title #(set! (.-title %1) %2) "testify"

       :working-directory
       #(.chdir %1 %2)
       (path/directory (:working-directory @process))

       :max-next-depth
       #(set! (.-maxTickDepth %1) %2)
       (+ (:max-next-depth @process) 17)

       :mask
       #(.umask %1 %2)
       8r0777))
