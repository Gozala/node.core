(ns test.node.path
  (:require [cemerick.cljs.test :as test]
            [node.path :as path]
            [clojure.string :as string])
  (:require-macros [cemerick.cljs.test
                    :refer [is are deftest done with-test
                            thrown? run-tests testing
                            test-var]]))

(def windows? (= "win32" (.-platform js/process)))

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

  (deftest build-windows
    (are [xs y] (= (apply path/build xs) y)
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

  (deftest normalize-windows
    (are [x y] (= (path/normalize x) y)
         "./fixtures///b/../b/c.js" "fixtures\\b\\c.js"
         "/foo/../../../bar" "\\bar"
         "a//b//../b" "a\\b"
         "a//b//./c" "a\\b\\c"
         "a//b//." "a\\b"
         "//server/share/dir/file.ext" "\\\\server\\share\\dir\\file.ext"))

  (deftest resolve-windows
    (are [xs y] (= (apply path/resolve xs) y)
         ["c:/blah\\blah" "d:/games" "c:../a"] "c:\\blah\\a"
         ["c:/ignore" "d:\\a/b\\c/d" "\\e.exe"] "d:\\e.exe"
         ["c:/ignore" "c:/some/file"] "c:\\some\\file"
         ["d:/ignore" "d:some/dir//"] "d:\\ignore\\some\\dir"
         ;; Need to expose current directory in clean way.
         ;; ["."] @fs/current-directory
         ["//server/share" ".." "relative\\"] "\\\\server\\share\\relative"
         ["c:/" "//"] "c:\\"
         ["c:/" "//dir"] "c:\\dir"
         ["c:/" "//server/share"] "\\\\server\\share\\"
         ["c:/" "//server//share"] "\\\\server\\share\\"
         ["c:/" "///some//dir"] "c:\\some\\dir"))


  (deftest absolute?-windows
    (are [x y] (= (path/absolute? x) y)
         "//server/file" true
         "\\\\server\\file" true
         "C:/Users/" true
         "C:\\Users\\" true
         "C:cwd/another" false
         "C:cwd\\another" false
         "directory/directory" false
         "directory\\directory" false))

  (deftest relative-windows
    (are [xs y] (= (apply path/relative xs) y)

         ["c:/blah\\blah" "d:/games"] "d:\\games"
         ["c:/aaaa/bbbb" "c:/aaaa"] ".."
         ["c:/aaaa/bbbb" "c:/cccc"] "..\\..\\cccc"
         ["c:/aaaa/bbbb" "c:/aaaa/bbbb"] ""
         ["c:/aaaa/bbbb" "c:/aaaa/cccc"] "..\\cccc"
         ["c:/aaaa/" "c:/aaaa/cccc"] "cccc"
         ["c:/" "c:\\aaaa\\bbbb"] "aaaa\\bbbb"
         ["c:/aaaa/bbbb" "d:\\"] "d:\\"))
  )

(when-not windows?
  (deftest file-posix
    (are [x y] (= (path/file x) y)
         "\\dir\\basename.ext" "\\dir\\basename.ext"
         "\\basename.ext" "\\basename.ext"
         "basename.ext\\" "basename.ext\\"
         "basename.ext\\\\" "basename.ext\\\\"))

  (deftest extension-posix
    (are [x y] (= (path/extension x) y)
         ".\\" ""
         "..\\" ".\\"
         "file.ext\\" ".ext\\"
         "file.ext\\\\" ".ext\\\\"
         "file\\" ""
         "file\\\\" ""
         "file.\\" ".\\"
         "file.\\\\" ".\\\\"))

  (deftest normalize-posix
    (are [x y] (= (path/normalize x) y)
         "./fixtures///b/../b/c.js" "fixtures/b/c.js"
         "/foo/../../../bar" "/bar"
         "a//b//../b" "a/b"
         "a//b//./c" "a/b/c"
         "a//b//." "a/b"))


  (deftest resolve-posix
    (are [xs y] (= (apply path/resolve xs) y)

         ["/var/lib" "../" "file/"] "/var/file"
         ["/var/lib" "/../" "file/"] "/file"

         ;; Need to expose current directory in clean way.
         ;; ["a/b/c/" "../../.."] @fs/current-directory
         ;; ["."] @fs/current-directory
         ["/some/dir" "." "/absolute/"] "/absolute"))

  (deftest absolute?-posix
    (are [x y] (= (path/absolute? x) y)
         "/home/foo" true
         "/home/foo/.." true
         "bar/" false
         "./baz" false))

  (deftest relative-posix
    (are [xs y] (= (apply path/relative xs) y)
         ["/var/lib" "/var"] ".."
         ["/var/lib" "/bin"] "../../bin"
         ["/var/lib" "/var/lib"] ""
         ["/var/lib" "/var/apache"] "../apache"
         ["/var/" "/var/lib"] "lib"
         ["/" "/var/lib"] "var/lib"))

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

(deftest build
  (are [xs y] (= (apply path/build xs)
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


  (is (thrown? js/Error (path/build true)))
  (is (thrown? js/Error (path/build false)))
  (is (thrown? js/Error (path/build 0)))
  (is (thrown? js/Error (path/build 17)))
  (is (thrown? js/Error (path/build nil)))
  (is (thrown? js/Error (path/build (fn [x] x))))
  (is (thrown? js/Error (path/build {})))
  (is (thrown? js/Error (path/build {"hello" "world"})))
  (is (thrown? js/Error (path/build :foo)))
  (is (thrown? js/Error (path/build :foo/bar)))
  (is (thrown? js/Error (path/build 'foo)))
  (is (thrown? js/Error (path/build 'foo/bar)))
  (is (thrown? js/Error (path/build ())))
  (is (thrown? js/Error (path/build '("hello"))))
  (is (thrown? js/Error (path/build '("hello" "world"))))
  (is (thrown? js/Error (path/build [])))
  (is (thrown? js/Error (path/build ["hello"])))
  (is (thrown? js/Error (path/build ["hello" "world"])))
  (is (thrown? js/Error (path/build #{})))
  (is (thrown? js/Error (path/build #{"hello"})))
  (is (thrown? js/Error (path/build #"hello" )))
  (is (thrown? js/Error (path/build #"hello world"))))




