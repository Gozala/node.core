(ns test.node.path
  (:require [cemerick.cljs.test :as test]
            [node.path :as path]
            [clojure.string :as string]
            [node.os :as os])
  (:require-macros [cemerick.cljs.test
                    :refer [is are deftest done with-test
                            thrown? run-tests testing
                            test-var]]))

(deftest *delimiter*
  (is (satisfies? IDeref path/*delimiter*)
      "path/*delimiter* is an atom")
  (is (contains? #{":" ";"} @path/*delimiter*)
      "path/*delimiter* is one of the delimiters"))

(deftest *separator*
  (is (satisfies? IDeref path/*separator*)
      "path/*separator* is an atom")
  (is (contains? #{"/" "\\"} @path/*separator*)
      "path/*separator* is one of the separators"))

(def windows? (= @os/platform :win32))


(when windows?
  (deftest file-windows
    (are [x y] (= (path/file x) y)
         "\\dir\\basename.ext" "basename.ext"
         "\\long\\path\\to\\file" "file"
         "\\long\\path\\to\file.txt" "file.txt"
         "\\basename.ext" "basename.ext"
         "basename.ext\\" "basename.ext"
         "basename.ext\\\\" "basename.ext"))

  (deftest extension-windows
    (are [x y] (= (path/extension x) y)
         ".\\" ""
         "..\\" ""
         "file.ext\\" ".ext"
         "file.ext\\\\" ".ext"
         "file\\" ""
         "file\\\\" ""
         "file.\\" "."
         "file.\\\\" "."))

  (deftest directory-windows
    (are [x y] (= (path/directory x) y)
         "c:\\" "c:\\"
         "c:\\foo" "c:\\"
         "c:\\foo\\" "c:\\"
         "c:\\foo\\bar" "c:\\foo"
         "c:\\foo\\bar\\" "c:\\foo"
         "c:\\foo\\bar\\baz" "c:\\foo\\bar"
         "\\" "\\"
         "\\foo" "\\"
         "\\foo\\" "\\"
         "\\foo\\bar" "\\foo"
         "\\foo\\bar\\" "\\foo"
         "\\foo\\bar\\baz" "\\foo\\bar"
         "c:" "c:"
         "c:foo" "c:"
         "c:foo\\" "c:"
         "c:foo\\bar" "c:foo"
         "c:foo\\bar\\" "c:foo"
         "c:foo\\bar\\baz" "c:foo\\bar"
         "\\\\unc\\share" "\\\\unc\\share"
         "\\\\unc\\share\\foo" "\\\\unc\\share\\"
         "\\\\unc\\share\\foo\\" "\\\\unc\\share\\"
         "\\\\unc\\share\\foo\\bar" "\\\\unc\\share\\foo"
         "\\\\unc\\share\\foo\\bar\\" "\\\\unc\\share\\foo"
         "\\\\unc\\share\\foo\\bar\\baz" "\\\\unc\\share\\foo\\bar"))

  (deftest join-windows
    (are [xs y] (= (apply path/join xs) y)
         ["//foo/bar"] "//foo/bar/"
         ["\\/foo/bar"] "//foo/bar/"
         ["\\\\foo/bar"] "//foo/bar/"
         ;; UNC path expected - server and share separate
         ["//foo" "bar"] "//foo/bar/"
         ["//foo/" "bar"] "//foo/bar/"
         ["//foo" "/bar"] "//foo/bar/"
         ;; UNC path expected - questionable
         ["//foo" "" "bar"] "//foo/bar/"
         ["//foo/" "" "bar"] "//foo/bar/"
         ["//foo/" "" "/bar"] "//foo/bar/"
         ;; UNC path expected - even more questionable
         ["" "//foo" "bar"] "//foo/bar/"
         ["" "//foo/" "bar"] "//foo/bar/"
         ["" "//foo/" "/bar"] "//foo/bar/"
         ;; No UNC path expected (no double slash in first component)
         ["\\" "foo/bar"] "/foo/bar"
         ["\\" "/foo/bar"] "/foo/bar"
         ["" "/" "/foo/bar"] "/foo/bar"
         ;; No UNC path expected (no non-slashes in first component - questionable)
         ["//" "foo/bar"] "/foo/bar"
         ["//" "/foo/bar"] "/foo/bar"
         ["\\\\" "/" "/foo/bar"] "/foo/bar"
         ["//"] "/"
         ;; No UNC path expected (share name missing - questionable).
         ["//foo"] "/foo"
         ["//foo/"] "/foo/"
         ["//foo" "/"] "/foo/"
         ["//foo" "" "/"] "/foo/"
         ;; No UNC path expected (too many leading slashes - questionable)
         ["///foo/bar"] "/foo/bar"
         ["////foo" "bar"] "/foo/bar"
         ["\\\\\\/foo/bar"] "/foo/bar"
         ;; Drive-relative vs drive-absolute paths. This merely describes the
         ;; status quo rather than being obviously right
         ["c:"] "c:."
         ["c:."] "c:."
         ["c:" ""] "c:."
         ["" "c:"] "c:."
         ["c:." "/"] "c:./"
         ["c:." "file"] "c:file"
         ["c:" "/"] "c:/"
         ["c:" "file"] "c:/file"))

  )

(when-not windows?
  (deftest file-unix
    (are [x y] (= (path/file x) y)
         "\\dir\\basename.ext" "\\dir\\basename.ext"
         "\\basename.ext" "\\basename.ext"
         "basename.ext\\" "basename.ext\\"
         "basename.ext\\\\" "basename.ext\\\\"))

  (deftest extension-unix
    (are [x y] (= (path/extension x) y)
         ".\\" ""
         "..\\" ".\\"
         "file.ext\\" ".ext\\"
         "file.ext\\\\" ".ext\\\\"
         "file\\" ""
         "file\\\\" ""
         "file.\\" ".\\"
         "file.\\\\" ".\\\\"))

  )

(deftest file
  (are [x y] (= (path/file x) y)
       "" ""
       "/dir/basename.ext" "basename.ext"
       "/long/path/to/a/file" "file"
       "/long/path/to/file.md" "file.md"
       "/basename.ext" "basename.ext"
       "basename.ext" "basename.ext"
       "basename.ext/" "basename.ext"
       "basename.ext//" "basename.ext"
       (str "/a/b/Icon" (char 13)) (str "Icon" (char 13))))

(deftest directory
  (are [x y] (= (path/directory x) y)
       "/a/b/" "/a"
       "/a/b" "/a"
       "/a" "/"
       "" "."
       "/" "/"
       "////" "/"))


(deftest extension
  (are [x y] (= (path/extension x) y)
       "file.txt" ".txt"
       "" ""
       "/path/to/file" ""
       "/path/to/file.ext" ".ext"
       "/path.to/file.ext" ".ext"
       "/path.to/file" ""
       "/path.to/.file" ""
       "/path.to/.file.ext" ".ext"
       "/path/to/f.ext" ".ext"
       "/path/to/..ext" ".ext"
       "file" ""
       "file.ext" ".ext"
       ".file" ""
       ".file.ext" ".ext"
       "/file" ""
       "/file.ext" ".ext"
       "/.file" ""
       "/.file.ext" ".ext"
       ".path/file.ext" ".ext"
       "file.ext.ext" ".ext"
       "file." "."
       "." ""
       "./" ""
       ".file.ext" ".ext"
       ".file" ""
       ".file." "."
       ".file.." "."
       ".." ""
       "../" ""
       "..file.ext" ".ext"
       "..file" ".file"
       "..file." "."
       "..file.." "."
       "..." "."
       "...ext" ".ext"
       "...." "."
       "file.ext/" ".ext"
       "file.ext//" ".ext"
       "file/" ""
       "file//" ""
       "file./" "."
       "file.//" "."))

(deftest join
  (are [xs y] (= (apply path/join xs)
                 (if windows? (string/replace y #"/" "\\") y))
       ["." "x/b" ".." "/b/c.js"] "x/b/c.js"
       ["/." "x/b" ".." "/b/c.js"] "/x/b/c.js"
       ["/foo" "../../../bar"] "/bar"
       ["foo" "../../../bar"] "../../bar"
       ["foo/" "../../../bar"] "../../bar"
       ["foo/x" "../../../bar"] "../bar"
       ["foo/x" "./bar"] "foo/x/bar"
       ["foo/x/" "./bar"] "foo/x/bar"
       ["foo/x/" "." "bar"] "foo/x/bar"
       ["./"] "./"
       ["." "./"] "./"
       ["." "." "."] "."
       ["." "./" "."] "."
       ["." "/./" "."] "."
       ["." "/////./" "."] "."
       ["."] "."
       ["" "."] "."
       ["" "foo"] "foo"
       ["foo" "/bar"] "foo/bar"
       ["" "/foo"] "/foo"
       ["" "" "/foo"] "/foo"
       ["" "" "foo"] "foo"
       ["foo" ""] "foo"
       ["foo/" ""] "foo/"
       ["foo" "" "/bar"] "foo/bar"
       ["./" ".." "/foo"] "../foo"
       ["./" ".." ".." "/foo"] "../../foo"
       ["." ".." ".." "/foo"] "../../foo"
       ["" ".." ".." "/foo"] "../../foo"
       ["/"] "/"
       ["/" "."] "/"
       ["/" ".."] "/"
       ["/" ".." ".."] "/"
       [""] "."
       ["" ""] "."
       [" /foo"] " /foo"
       [" " "foo"] " /foo"
       [" " "."] " "
       [" " "/"] " /"
       [" " ""] " "
       ["/" "foo"] "/foo"
       ["/" "/foo"] "/foo"
       ["/" "//foo"] "/foo"
       ["/" "" "/foo"] "/foo"
       ["" "/" "foo"] "/foo"
       ["" "/" "/foo"] "/foo")


  (is (thrown? js/Error (path/join true)))
  (is (thrown? js/Error (path/join false)))
  (is (thrown? js/Error (path/join 0)))
  (is (thrown? js/Error (path/join 17)))
  (is (thrown? js/Error (path/join nil)))
  (is (thrown? js/Error (path/join (fn [x] x))))
  (is (thrown? js/Error (path/join {})))
  (is (thrown? js/Error (path/join {"hello" "world"})))
  (is (thrown? js/Error (path/join :foo)))
  (is (thrown? js/Error (path/join :foo/bar)))
  (is (thrown? js/Error (path/join 'foo)))
  (is (thrown? js/Error (path/join 'foo/bar)))
  (is (thrown? js/Error (path/join ())))
  (is (thrown? js/Error (path/join '("hello"))))
  (is (thrown? js/Error (path/join '("hello" "world"))))
  (is (thrown? js/Error (path/join [])))
  (is (thrown? js/Error (path/join ["hello"])))
  (is (thrown? js/Error (path/join ["hello" "world"])))
  (is (thrown? js/Error (path/join #{})))
  (is (thrown? js/Error (path/join #{"hello"})))
  (is (thrown? js/Error (path/join #"hello" )))
  (is (thrown? js/Error (path/join #"hello world"))))




