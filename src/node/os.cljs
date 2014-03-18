(ns node.os
  (:require [cljs.core.async :as async]))

(def *os* (js/process.binding "os"))
(def *process* js/process)

(def
  ^{:doc "Operating system platform"}
  platform
  (atom (keyword (.-platform js/process))))

(def
  ^{:doc "True if windows otherwise false"}
  windows?
  (atom (= @platform :win32)))

(def
  ^{:doc "Endianness of the CPU, either `:BE` or `:LE`"}
  endianness
  (atom (keyword (.getEndianness *os*))))


(def
  ^{:doc "Returns the hostname of the operating system"}
  host-name
  (atom (.getHostname *os*)))

(def
  ^{:doc "Returns the operating system name"}
  type
  (atom (keyword (.getOSType *os*))))

(def
  ^{:doc "Returns the operating system CPU architecture"}
  architecture
  (atom (keyword (.-arch *process*))))

(def
  ^{:doc "A constant defining the appropriate End-of-line marker
    for the operating system."}
  eol
  (atom (if @windows? "\r\n" "\n")))

(def
  ^{:doc "Returns the operating system release."}
  release
  (atom (.getOSRelease *os*)))

(def
  ^{:doc "Returns the total amount of system memory in bytes."}
  total-system-memory
  (atom (.getTotalMem *os*)))



(def
  ^{:doc "Returns a vector of maps containing information about each CPU/core
    installed: model, speed (in MHz), and times (an object containing the
    number of milliseconds the CPU/core spent in: user, nice, sys, idle,
    and irq)."}
  cpus
  (atom (vec (map (fn [x] {:model (.-model x)
                      :speed: (.-speed x)})
             (vec (.getCPUs *os*))))))

