(ns node.type
  (:require [cljs.core.async :as async]))

(defrecord Input [in error])

(defrecord Output [out error])

(defrecord IO [in out error])
