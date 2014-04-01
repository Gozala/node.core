(ns test.node.env
  (:require [cemerick.cljs.test :as test]
            [node.env :refer [env]])
  (:require-macros [cemerick.cljs.test
                    :refer [is are deftest done with-test
                            thrown? run-tests testing
                            test-var]]))

(def *env* (.-env js/process))


(deftest env-api
  (is (satisfies? IDeref env)
      "node.env/env can be dereferenced")

  (is (= (.-env js/process)
         (clj->js env)))

  (let [watcher (fn [_ _ _ _] nil)]
    (add-watch env :foo watcher)
    (remove-watch env :foo)))

(deftest clj-updates
  (let [n (atom 0)
        watcher (fn [_ _ _ _] (swap! n inc))]

    (add-watch env :clj watcher)

    (is (nil? (.-BAR *env*)))
    (is (nil? (:BAR @env)))
    (is (= @n 0))


    (swap! env assoc :BAR "bar")
    (is (= @n 1))
    (is (= (:BAR @env) "bar"))
    (is (= (.-BAR *env*) "bar"))

    (swap! env assoc :BAR "foo")
    (is (= @n 2))
    (is (= (:BAR @env) "foo"))
    (is (= (.-BAR *env*) "foo"))

    (swap! env assoc :BAR "foo")
    (is (= @n 3))

    (swap! env dissoc :BAR)
    (is (nil? (.-BAR *env*)))
    (is (nil? (:BAR @env)))
    (is (= @n 4))

    (is (nil? ((set (keys @env)) :BAR)))
    (is (nil? ((set (.keys js/Object *env*)) "BAR")))

    (swap! env conj {:FOO "hello" :BAR "world"})
    (is (= @n 5))
    (is (= (:FOO @env) "hello"))
    (is (= (.-FOO *env*) "hello"))
    (is (= (:BAR @env) "world"))
    (is (= (.-BAR *env*) "world"))

    (remove-watch env :clj)))

(deftest js-update
  (let [n (atom 0)
        watcher (fn [_ _ _ _] (swap! n inc))]

    (add-watch env :js watcher)

    (set! (.-BAZ *env*) "bar")
    (is (= @n 0))
    (is (:BAZ @env) "bar")
    (is (= @n 1))

    (set! (.-BAZ *env*) "foo")
    (set! (.-BAZ *env*) "baz")
    (is (:BAZ @env) "baz")
    (is (= @n 2))

    (remove-watch env :js)))
