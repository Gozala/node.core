(ns node.tcp
  (:require [cljs.core.async :as async]
            [node.socket :refer [Socket Server]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [node.macros :refer [await]]))

(def node-net (js/require "net"))


(defn- ->socket
  [socket]
  (let [in (async/chan)
        out (async/chan)
        error (async/chan)
        stabilizer (async/chan)]

    (.on socket "connect" #())
    ;; When socket output is drained put ::drain on stabilizer
    ;; that will resume pumping packets from `out` channel into
    ;; socket
    (.on socket "drain" #(async/put! stabilizer ::drain))
    ;; When socket is closed close `out` channel to notify
    ;; consumer no packets can be written. Even if consumer
    ;; doesn't track this, further attempt to `put!` will be ignored.
    (.on socket "close" #(async/close! out))

    ;; When socket receives data, enque it onto `in` channel.
    (.on socket "data" #(async/put! in %))
    ;; When socket is closed from the remote end close `in` channel.
    (.on socket "end" #(async/close! in))

    ;; Put any errors onto error channel.
    (.on socket "error" #(async/put! error %))

    ;; Pipe data from `out` channel to a node socket. Closing
    ;; `out` will half close (send `FIN` packet) to remote end
    ;; of the socket.
    (go (loop [packet (<! out)]
          (if (nil? packet)
            ;; Send `FIN` if `out` is closed by a consumer.
            (.end socket)
            (do
              ;; Write a packet from `out` channel into a socket. If entire
              ;; packet was not flushed block until drained.
              (if-not (.write socket packet)
                (<! stabilizer))
              ;; And then handle new packet.
              (recur (<! out))))))

    (Socket. in out error socket)))

(defn- ->listener
  [server]
  (let [accept (async/chan)
        error (async/chan)]

    (.on server "error" #(async/put! error %))
    (.on server "connection" #(async/put! accept (->socket %)))
    (.on server "close" #(async/close! accept))

    (Server. accept error server)))

(def close! node.socket/close!)


(defn listen!
  ([port-number]
   (listen! port-number nil))
  ([port-number host-name]
   (let [server (.createServer node-net)]
     (if host-name
       (.listen server port-number host-name)
       (.listen server port-number))

     (->listener server))))

(defn connect!
  ([port-number] (connect! port-number nil))
  ([port-number host-name]
   (let [socket (.connect node-net #js {:port port-number
                                        :host (or host-name "localhost")})]
     (->socket socket))))


(defn forward!
  [input output]
  (go (loop []
        (let [[packet source] (async/alts! [input output])
              more (cond
                    (nil? packet) (async/close! output)

                    (= source input) (or (>! output packet) packet)

                    :else nil)]
          (if-not (nil? more) (recur))))))


(defn print!
  [input id]
  (go (loop [packet (<! input)]
        (print id packet)
        (if-not (nil? packet)
          (recur (<! input))))))


(def x1 (async/chan))
(def x2 (async/chan))

(forward! x1 x2)

(print! x1 :x1)
(print! x2 :x2)

(async/put! x1 6)
(async/close! x2)


(def client (connect! 6000))

(print! (:in client) :client-input)
(print! (:error client) :client-error)

(close! client)


(defn rdp-message
  [packet]
  (let [message (.stringify js/JSON (clj->js packet))
        length (count message)]
    (str length ":" message)))


(async/put! (:out client)
            (rdp-message {:to :root :type :requestTypes}))
(async/put! (:out client)
            (rdp-message {:to :root :type :echo :text "hello world"}))
(async/put! (:out client)
            (rdp-message {:to :root :type :listTabs}))

(:out client)
