(defproject com.jeditoolkit/node "0.0.1"
  :description "core.async flavored interface to a node standard library"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "https://github.com/gozala/node.core"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138" :scope "provided"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]]
  :source-paths ["src"]

  :plugins [[lein-cljsbuild "1.0.1"]
            [com.cemerick/clojurescript.test "0.3.0"]]

  :cljsbuild
  {:builds
   [{:id "simple-test"
     :source-paths ["src" "test"]
     :compiler {:optimizations :whitespace
                :pretty-print true
                :output-to "target/cljs/test.js"}}
    {:id "advanced-test"
     :source-paths ["src" "test"]
     :compiler {:optimizations :advanced
                :pretty-print false
                :static-fns true
                :output-dir "out"
                :output-to "target/cljs/test.js"
                :source-map "target/cljs/test.js.map"}}]

   :test-commands {"unit-tests" ["node" :node-runner
                                 "target/cljs/test.js"]}})
