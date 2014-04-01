(ns test.node.os
  (:require [cemerick.cljs.test :as test]
            [clojure.string :as string]
            [node.os :refer [os]]
            [node.path :as path]
            [node.utils :refer [json->edn]])
  (:require-macros [cemerick.cljs.test
                    :refer [is are deftest done with-test
                            thrown? run-tests testing
                            test-var]]))

(def *os* (js/require "os"))
(def *path* (js/require "path"))

(deftest os-type
  (is (satisfies? IAtom os)
      "node.os/os is an atom"))

(deftest os-api
  (are [field type?] (type? (field @os))
       :platform #{:darwin :freebsd :linux :sunos :win32}
       :endianness #{:BE :LE}
       :host-name string?
       :type keyword?
       :architecture #{:arm :ia32 :x64}
       :eol #{"\r\n" "\n"}
       :release string?
       :total-system-memory integer?
       :path-delimiter #{":" ";"}
       :path-separator #{"\\" "/"}
       :cpus vector?
       :cpus? (fn [x] (every? #(and (map? %)
                                    (string? (:model %))
                                    (integer? (:speed %)))
                              x))
       :network-interfaces map?
       :network-interfaces #(every? keyword? (keys %))
       :network-interfaces #(every? vector? (vals %))))

(deftest os-values
  (are [field getter] (= (field @os) (getter *os*))
       :platform #(keyword (.platform %))
       :endianness #(keyword (.endianness %))
       :host-name #(.hostname %)
       :type #(keyword (.type %))
       :architecture #(keyword (.arch %))
       :eol #(.-EOL %)
       :release #(.release %)
       :total-system-memory #(.totalmem %)
       :path-delimiter #(.-delimiter *path*)
       :path-separator #(.-sep *path*)
       :cpus (fn [x] (vec (map #(select-keys % [:model :speed])
                               (json->edn (.cpus x)))))
       :network-interfaces #(json->edn (.networkInterfaces %))))
