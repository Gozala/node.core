(ns node.os
  (:require [cljs.core.async :as async]))

(def *os* (js/process.binding "os"))
(def *path* (js/require "path"))
(def *process* js/process)

(def ^:private
  *static-fields*
  {;; Operating system platform
   :platform (keyword (.-platform *process*))
   ;; Endianness of the CPU, either `:BE` or `:LE`
   :endianness (keyword (.getEndianness *os*))
   ;; Hostname of the operating system
   :host-name (.getHostname *os*)
   ;; Operating system name
   :type (keyword (.getOSType *os*))
   ;; Operating system CPU architecture"
   :architecture (atom (keyword (.-arch *process*)))
   ;; A constant defining the appropriate End-of-line marker
   ;; for the operating system.
   :eol (if (= (.-platform *process*) "win32") "\r\n" "\n")
   ;; Operating system release
   :release (.getOSRelease *os*)
   ;; total amount of system memory in bytes.
   :total-system-memory (.getTotalMem *os*)
   ;; vector of maps containing information about each CPU/core
   ;; installed: model, speed (in MHz)
   :cpus (vec (map (fn [x] {:model (.-model x)
                            :speed: (.-speed x)})
                   (vec (.getCPUs *os*))))
   ;; The platform-specific path delimiter: `;` or `:`
   :path-delimiter (.-delimiter *path*)
   ;; The platform-specific file separator: `\\` or `/`
   :separator (.-sep *path*)})


(def runtime (atom *static-fields* :validator (fn [] false)))
