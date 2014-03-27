(ns node.fs
  (:require [cljs.core.async :as async]
            [node.type :refer [Input Output IO]]
            [node.stream :refer [pipe-events->channel
                                 pipe-stream->channel
                                 pipe-channel->stream]]
            [node.async :refer [passback callback->chan error->chan error?]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(def fs (js/require "fs"))
(def normalize-path (.-_makeLong (js/require "path")))
(def fs-biding (.binding js/process "fs"))
(def *constants* (.binding js/process "constants"))

(def O_RDONLY (.-O_RDONLY *constants*))
(def O_SYNC (.-O_SYNC *constants*))
(def O_RDWR (.-O_RDWR *constants*))
(def O_TRUNC (.-O_TRUNC *constants*))
(def O_CREAT (.-O_CREAT *constants*))
(def O_WRONLY (.-O_WRONLY *constants*))
(def O_EXCL (.-O_EXCL *constants*))
(def O_APPEND (.-O_APPEND *constants*))


(def Buffer (.-Buffer (js/require "buffer")))


(defprotocol IReadable
  (-read [readable options]))

(defprotocol IClosable
  (-close [this]))


(deftype File
  [path flags mode ^:mutable -fd]
  IReadable
  (-read [this options]
         (let [output (Input. (async/chan) (async/chan))
               auto-close (:auto-close options)
               chunk-size (:chunk-size options (* 64 1024))
               buffer (Buffer. chunk-size)
               queue (async/chan)]
           (go
            ;; If -fd isn't set, then we need to open file descriptor
            ;; for an underlaying file and cache it into -fd.
            (when (nil? -fd)
              (passback queue             ;; fd is put on a queue when when open
                        (:error output)   ;; error is put on error channel if open fails
                        fs-biding.open [flags mode])
              (set! -fd (async/<! queue)))

            -fd


            ;; Read chunk and move offset until exhausted. Note that since data is
            ;; put on a channel followup reads won't happen until consumer takes
            ;; chunk off a channel.
            ;; TODO: Abort reads if `(:in output)` is closed, which should be from
            ;; the other end.
            (loop [offset (:offset options 0)]
              (passback queue
                        (:error output)
                        fs-biding.read [-fd buffer 0 chunk-size offset])

              (let [n (async/<! queue)]
                (when (pos? n)
                  (async/>! (:in input) (.slice buffer 0 n))
                  (recur (+ offset n)))))

            ;; Once read loop is complete, close file descriptor if
            ;; auto-close is true.
            (when auto-close
              (passback queue
                        (:error output)
                        fs-biding.close
                        [-fd])
              (async/<! pipe)
              (set! -fd nil))

            ;; Finally close (:in output) to indicate that read is
            ;; complete.
            (async/close! (:in output)))

           ;; Note: If error occurs during IO, that will end up
           ;; on (:error output) handling such race conditions is
           ;; user's concern.
           output))
  ;; Implement `close` method to be compatible with `with-open`
  ;; macro.
  Object
  (close [_]
         (when -fd (.closeSync fs-biding -fd))))

(extend-type string
  IReadable
  (-read [path options]
         (-read (file path options)
                (conj options {:auto-close true}))))


(defn file
  ([path] (file path {}))
  ([path options] (File. (normalize-path path)
                         (read-flags (:flags options :r))
                         (:mode options 0666)
                         nil)))


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


(def x (read "/Users/gozala/.vimrc"))


(defn print!
  [channel]
  (go (loop []
        (async/<! channel)
        (recur))))

(print! (:in x))
