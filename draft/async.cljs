(ns node.async
  (:require [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defprotocol IError)
(extend-type js/Error IError)

(defn error?
  [x]
  (satisfies? IError x))


(defn callback->chan
  "Takes node async API style `fn` vector of `params`
  & applies them + generated callback function. Result
  passed to a callback are put onto `output` channel
  (which is either provided, or otherwise created).
  If optional `auto-close` is `true` `channel` will be
  closed after callback results are put on it.
  `output` channel is returned as result."
  ([fn]
   (callback->chan fn [] (async/chan) false))
  ([fn params]
   (callback->chan fn params false (async/chan)))
  ([fn params auto-close]
   (callback->chan fn params auto-close (async/chan)))
  ([fn params auto-close output]
   (apply fn (conj params (fn [error result]
                            (async/put! output
                                        (cond (error? error) error
                                              error (js/Error. error)
                                              :else result))
                            (if auto-close (async/close! output)))))
   channel))

(defn passback
  ([out error fn params]
   (passback out error params false))
  ([out error fn params close?]
   (apply fn (conj params (fn [falure result]
                            (if failure
                              (async/put! error failure)
                              (async/put! out result))
                            (when auto-close (async/close! out)))))))

(defn error->chan
  "Takes an error and puts it onto `output` channel. If
  `auto-close` is `true` then `output` will be closed after.
  `output` channel maybe passed, if not one is generated.
  `output` channel is returned as result."
  ([error] (error->chan error false (async/chan)))
  ([error auto-close] (error->chan error auto-close (async/chan)))
  ([error auto-close output]
   (async/put! output (if (error? error) error (Error. error)))
   (if auto-close (async/end! output))
   output))
