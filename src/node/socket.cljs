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
  (address [socket])
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
  [in out error node-socket]
  ICloseable
  (close! [_] (async/close! out))
  ISocket
  (encoding [_] (.-encoding node-socket))
  (local-address [_] (->address (.address node-socket)))
  (remote-address [_] (->address (.remoteAddress node-socket)))
  IEncodeJS
  (-clj->js [_] node-socket))


(defrecord Server
  [accept error node-server]
  ICloseable
  (close! [_] (.close node-server))
  IServer
  (address [_] (->address (.address node-server)))
  (max-connections [_] (.-maxConnections node-server))
  IEncodeJS
  (-clj->js [_] node-server))
