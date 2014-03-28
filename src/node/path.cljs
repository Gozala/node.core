(ns node.path)

(def ^:private *path* (js/require "path"))

(def
  ^{:doc "The platform-specific path delimiter: `;` or `:`"}
  *delimiter*
  (atom (.-delimiter *path*)))

(def
  ^{:doc "The platform-specific file separator: `\\` or `/`"}
  *separator*
  (atom (.-sep *path*)))

(defn normalize
  "Normalize a string path, taking care of '..' and '.' parts.

  When multiple slashes are found, they're replaced by a single one;
  when the path contains a trailing slash, it is preserved. On Windows
  backslashes are used."
  [path]
  (.normalize *path* path))

(def
  ^{:doc "Join all arguments together and normalize the resulting path.
    Arguments must be strings, otherwise exception is thrown"}
  join
  (.-join *path*))

(defn split
  "Returns the vector of path elements that constitute path

    (node.path/split \"/Users/Clojure\") ;=> [\"\" \"Users\" \"Clojure\"]"
  [path]
  (.split path @*separator*))


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

