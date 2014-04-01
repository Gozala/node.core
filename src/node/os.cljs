(ns node.os
  (:require [cljs.core.async :as async]
            [node.utils :refer [json->edn]]))

(def ^:private *os* (js/process.binding "os"))
(def ^:private *path* (js/require "path"))
(def ^:private *process* js/process)

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
   :architecture (keyword (.-arch *process*))
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
                            :speed (.-speed x)})
                   (vec (.getCPUs *os*))))
   :network-interfaces (json->edn (.getInterfaceAddresses *os*))
   ;; The platform-specific path delimiter: `;` or `:`
   :path-delimiter (.-delimiter *path*)
   ;; The platform-specific file separator: `\\` or `/`
   :path-separator (.-sep *path*)})


(def os (atom *static-fields* :validator (fn [] false)))

