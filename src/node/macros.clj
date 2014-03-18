(ns node.macros
  (:require [cljs.core.async :as async]))

(defmacro await
  [form]
  `(let [sync# (async/chan)]
     (~@form #(if (nil? %)
                (async/close! sync#)
                (asnc/put! sync# %)))
     sync#))

(defmacro read
  [socket]
  `(<! (:in socket)))

(defmacro write
  [socket packet]
  `(>! (:out socket) packet))
