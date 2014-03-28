(ns node.net
  (:require [cljs.core.async :as async]
            [node.stream :refer [pipe-events->channel
                                 pipe-stream->channel
                                 pipe-channel->stream]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def net (js/require "net"))


;; Representation of the network address {:host \"127.0.0.1\" :port 12346 }
(defrecord Address [host port])

(defrecord Connection [input output error
                       local-address
                       remote-address])


(defn- socket->connection
  [socket]
  (let [connection (Connection. (async/chan)
                                (async/chan)
                                (async/chan)
                                (Address. (.-localAddress socket)
                                          (.-localPort socket))
                                (Address (.-remoteAddress socket)
                                         (.-remotePort socket)))]
    ;; Wire stream to a connection channels.
    (pipe-events->channel socket "error" (:error connection))
    (pipe-stream->channel socket (:input connection))
    (pipe-channel->stream (:output connection) socket)

    connection))

(defrecord Client [connection error])

(defn connect
  "Creates a socket connection to a given address"
  ([port] (connect "localhost" port {}))
  ([host port] (connect host port {}))
  ([host port options]
   (let [connection (async/chan)
         error (async/chan)
         socket (.connect net
                          #js {:host host
                               :port port
                               :localAddress (:localAddress options)
                               :allowHalfOpen (:allowHalfOpen options)})]

     (.once socket "connect" #(async/put! connection
                                          (socket->connection socket)))
     (.once socket "close" #(async/close! connection))
     (pipe-events->channel socket "error" error)

     (Client. connection error))))

(defrecord Server
  [connection error address])

(defn listen
  ""
  ([port] (connect nil port {}))
  ([host port] (connect host port {}))
  ([host port options]
   (let [connection (async/chan)
         error (async/chan)
         server (.createServer net
                               #js {:allowHalfOpen (:allow-half-open options)})
         address (.address server)]

     (.on server "connection" #(async/put! connection (socket->connection %)))
     (.once server "close" #(async/close! connection))
     (pipe-events->channel server "error" error)
     (.listen port host (:backlog options))

     (Server. connection error
              (Address. (.-address address)
                        (.-port address))))))
