# node.net

A [ClojreScript][] interface for working with [nodejs networking][] library
in [core.async][] style.

## Examples


Server example

```clojure
(ns clojurescripting.node
  (:require [node.net :as net]
            [core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(defn handle-connection
  [connection]
  (go
   (>! (:output connection) "hello\r\n")
   (loop []
     (let [chunk (<! (:input connection))]
       (when (nil? chunk)
         (async/close! (:output connection))
         (do
           (>! (:output connection) chunk)
           (recur)))))

(let [server (net/listen 8124)]
  (go (loop [_ nil]
        (let [connection (alt!
                          error ([e] (print "Server error:" e))

                          (<! (:connection server))
                          ([c] (handle-connection c)))]
          (if connection
            (recur (handle-connection connection))
            (print "Server stopped"))))))
```

Client example

```clojure

```


[ClojreScript]:https://github.com/clojure/clojurescript
[nodejs networking]:http://nodejs.org/api/net.html
[core.async]:https://github.com/clojure/core.async
