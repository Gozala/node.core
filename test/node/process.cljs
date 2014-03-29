(ns test.node.process
  (:require [cemerick.cljs.test :as test]
            [clojure.string :as string]
            [node.process :refer [process]]
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
       :exec-arguments vector?
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

