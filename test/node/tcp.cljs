(ns test.node.tcp
  (:require [cemerick.cljs.test :as test])
  (:require-macros [cemerick.cljs.test
                    :refer [is deftest done with-test
                            run-tests testing test-var]]))


(deftest somewhat-less-wat
  (is (= "{}[]" (+ {} []))))
