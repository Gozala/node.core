(ns node.socket
  (:require [cljs.core.async :as async]))

(defprotocol ICloseable
  (close! [resource]))

(extend-type
  default
  ICloseable
  (close! [channel] (async/close! channel)))


(defprotocol ISocket
  (buffer-size [socket] "")
  (encoding [socket] )
  (local-address [socket])
  (remote-address [socket]))

(defprotocol IServer
  (max-connections [server])
  (address [server]))

(defrecord Address [port host family])

(defn- ->address
  [address]
  (Address. (.-port address)
            (.-address address)
            (keyword (.-family address))))


(defrecord Socket
  [in out error socket]
  ICloseable
  (close! [_] (async/close! out))
  ISocket
  (encoding [_] (.-encoding socket))
  (local-address [_] (->address (.address socket)))
  (remote-address [_] (Address. (.-remotePort socket)
                                (.-remoteAddress socket)
                                nil))
  IEncodeJS
  (-clj->js [_] socket))


(defrecord Server
  [accept error server]
  ICloseable
  (close! [_] (.close server))
  IServer
  (address [_] (->address (.address server)))
  (max-connections [_] (.-maxConnections server))
  IEncodeJS
  (-clj->js [_] server))
