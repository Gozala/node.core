(ns node.path
  (:require [clojure.string :as string]))

(def ^:private windows? (= "win32" (.-platform js/process)))
(def ^:private *path* (js/require "path"))
(def ^:private *separator* (if (= (.-sep *path*) "\\") #"\\" #"/"))

(defn normalize
  "Normalize a string path, taking care of '..' and '.' parts.

  When multiple slashes are found, they're replaced by a single one;
  when the path contains a trailing slash, it is preserved. On Windows
  backslashes are used."
  [path]
  (.normalize *path* path))

(defn build
  "Creates a path given a base path and any number of sub-path extensions.
  If base is an absolute path, the result is an absolute path, otherwise
  the result is a relative path.

  The base and sub arguments must be strings, otherwise exception is
  thrown.

  The build function builds a path without checking the validity of the
  path or accessing the filesystem."
  [base & sub-paths]
  (apply (.-join *path*) base sub-paths))

(defn split
  "Returns the vector of path elements that constitute path

    (node.path/split \"/Users/Clojure\") ;=> [\"\" \"Users\" \"Clojure\"]"
  [path]
  (string/split (normalize path) *separator*))


(def
  ^{:doc "Resolves to to an absolute path.

    If to isn't already absolute from arguments are prepended in right to left
    order, until an absolute path is found. If after using all from paths still
    no absolute path is found, the current working directory is used as well.
    The resulting path is normalized, and trailing slashes are removed unless
    the path gets resolved to the root directory. Non-string arguments are
    ignored.

    Another way to think of it is as a sequence of cd commands in a shell.

      (node.path/resolve \"foo/bar\" \"/tmp/file/\" \"..\" \"a/../subfile\")
    "}
  resolve
  (.-resolve *path*))

(defn relative
  [from to]
  "Solve the relative path from `from` to `to`.

  At times we have two absolute paths, and we need to derive the relative path
  from one to the other. This is actually the reverse transform of
  `node.path/resolve` which means we see that:

    (= (node.path/resolve from (node.path/relative from to))
       (node.path/resolve to))
  "
  (.relative *path* from to))

(def
  ^{:doc "Return the directory name of a path.
    Similar to the Unix `dirname` command."}
  directory
  (.-dirname *path*))

(defn file
  "Return the last portion of a path.
  Similar to the Unix `basename` command."
  [path]
  (.basename *path* path))

(def
  ^{:doc "Return the extension of the path, from the last `.` to end of string
    in the last portion of the path. If there is no `.` in the last portion of
    the path or the first character of it is `.`, then it returns an empty
    string."}
  extension
  (.-extname *path*))

(def ^:private
  *split-device*
  #"([a-zA-Z]:|[\\/]{2}[^\\/]+[\\/]+[^\\/]+)?([\\/])?([\s\S]*?)$")

(defn- windows-absolute?
  [path]
  (let [[_ device other] (re-matches *split-device* path)
        unc? (and device (not= (aget device 1) ":"))]
    (boolean (or other unc?))))

(defn- positx-absolute?
  [path]
  (= (aget path 0) \/))

(def
  ^{:doc "Returns true if path is an absolute path, false otherwise. The path
    argument can be a path for any platform. If path is not a legal path
    string false is returned. This function does not access the filesystem."}
  absolute?
  (if windows? windows-absolute? positx-absolute?))
