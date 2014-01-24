(ns node.fs
  (:require [cljs.core.async :as async]
            [node.stream :refer [pipe-events->channel
                                 pipe-stream->channel
                                 pipe-channel->stream]]
            [node.async :refer [callback->chan error->chan error?]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(def fs (js/require "fs"))
(def normalize-path (.-_makeLong (js/require "path")))
(def fs-biding (.binding js/process "fs"))
(def CONSTANTS (.binding js/process "constants"))

(def O_RDONLY (.-O_RDONLY CONSTANTS))
(def O_SYNC (.-O_SYNC CONSTANTS))
(def O_RDWR (.-O_RDWR CONSTANTS))
(def O_TRUNC (.-O_TRUNC CONSTANTS))
(def O_CREAT (.-O_CREAT CONSTANTS))
(def O_WRONLY (.-O_WRONLY CONSTANTS))
(def O_EXCL (.-O_EXCL CONSTANTS))
(def O_APPEND (.-O_APPEND CONSTANTS))


(def Buffer (.-Buffer (js/require "buffer")))


(defprotocol IReadable
  (-read [readable options]))

(defprotocol IClosable
  (-close [this]))

(def ^:private fd* (async/chan))


(deftype FileDescriptor
  [path flags mode ^:mutable -fd]
  IReadable
  (-read [this options]
         (go (let [output (async/chan)
                   auto-close (:auto-close options)
                   chunk-size (:chunk-size options (* 64 1024))
                   offset (atom (:offset options 0))
                   buffer (Buffer. chunk-size)
                   _ -fd
                   fd (or -fd (async/<! (callback->chan fs-biding.open
                                                        [flags mode]
                                                        false
                                                        fds)))]

               ;; Cache file descriptor in a mutable -fd field
               (if-not -fd (set! -fd fd))
               fd
           (if (error? fd)
             (error->chan fd false output)
             (loop []
                 ;; Queue callback result onto input
               (let [x (async/<! (callback->chan fs-biding.read
                                                 [fd buffer 0 chunk-size offset]
                                                 false
                                                 files))]
                 (cond (error? x)
                       (error->chan x false output)

                       (pos? x)
                       (do
                         (swap! offset + x)
                         (async/>! output (.slice buffer 0 x))
                         (recur))

                       :else nil)))


             (if auto-close
               (callback->chan fs-biding.close [fd] true output)
               (async/close! output)))))
         output)
  Object
  (close [_]
         (if -fd (.closeSync fs-biding -fd))))

(extend-type string
  IReadable
  (-read [path options]
         (-read (file path options)
                (conj options {:auto-close true}))))


(defn file
  ([path] (file path {}))
  ([path options] (FileDescriptor. (normalize-path path)
                                   (read-flags (:flags options :r))
                                   (:mode options 0666))))


(defn read-flags
  "Reads flags and converts them to a number"
  [flag]
  (cond (identical? flag "r")
        O_RDONLY

        (identical? flag "r+")
        O_RDWR

        (identical? flag "rs")
        (bit-or O_RDONLY O_SYNC)

        (identical? flag "rs+")
        (bit-or O_RDWR O_SYNC)

        (identical? flag "w")
        (bit-or O_TRUNC O_CREAT O_WRONLY)

        (or (identical? flag "wx")
            (identical? flag "xw"))
        (bit-or O_TRUNC O_CREAT O_WRONLY O_EXCL)

        (identical? flag "w+")
        (bit-or O_TRUNC O_CREAT O_RDWR)

        (or (identical? flag "wx+")
            (identical? flag "xw+"))
        (bit-or O_TRUNC O_CREAT O_RDWR O_EXCL)

        (identical? flag "a")
        (bit-or O_APPEND O_CREAT O_WRONLY)

        (or (identical? flag "ax")
            (identical? flag "xa"))
        (bit-or APPEND O_CREAT O_WRONLY O_EXCL)

        (identical? flag "a+")
        (bit-or O_APPEND O_CREAT O_RDWR)

        (or (identical? flag "ax+")
            (identical? flag "xa+"))
        (bit-or O_APPEND O_CREAT O_RDWR O_EXC)

        (number? flag)
        flag

        (keyword? flag)
        (read-flags (name flag))

        :else
        (throw (js/TypeError. (str "Unknown file open flag: " flag)))))

(defn read
  ([file] (-read file {}))
  ([file options] (-read file options)))


(read "/Users/gozala/.vimrc")
