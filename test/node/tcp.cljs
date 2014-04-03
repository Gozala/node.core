(ns test.node.tcp
  (:require [cemerick.cljs.test :as test]
            [cljs.core.async :as async]
            [node.tcp :as tcp]
            [node.utils :refer [json->edn]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cemerick.cljs.test
                    :refer [is are deftest done with-test
                            thrown? run-tests testing
                            test-var]]))


(def *port* 12345)


(deftest ^:async ping-pong
  (let [server (tcp/listen! *port*)]
    (go (loop [socket (async/<! (:accept server))]
          #_ (print "server accept" socket)
          (when-not (nil? socket)
            (is (#{"127.0.0.1" "localhost"}
                               (:host (tcp/local-address socket))))
            (is (= *port* (:port (tcp/local-address socket))))

            (go (loop [chunk (async/<! (:in socket))]
                  #_ (print "server <!" chunk)
                  (if (nil? chunk)
                    ;; If nil is received input is ended
                    ;; so we end output.
                    (do
                      #_ (print "server close!" )
                      (tcp/close! (:out socket))
                      #_ (tcp/close! server))
                    ;; otherwise assert that ping is received
                    ;; and respond with "pong"
                    (do
                      (is (= (str chunk) "PING"))
                      (async/>! (:out socket) "PONG")
                      #_ (print "server >!" "PONG")
                      (recur (async/<! (:in socket)))))))))))

  (let [n (atom 0)
        max 1000
        client (tcp/connect! *port*)]
    (go (async/>! (:out client) "PING")
        #_ (print "client >! PING" @n)
        (loop [chunk (async/<! (:in client))]
          #_ (print "client <!" chunk)
          (if (nil? chunk)
            (do
              #_ (print "client disconnected done")
              (is (= @n (inc max)))
              (done))
            (do
              (is (= (str chunk) "PONG"))
              (swap! n inc)
              (async/>! (:out client) "PING")
              #_ (print "client >!" "PING" @n)
              (when (= @n max)
                (tcp/close! (:out client)))

              (recur (async/<! (:in client)))))))))