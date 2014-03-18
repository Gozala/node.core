(defproject com.jeditoolkit/node "0.0.1"
  :description "core.async flavored interface to a node standard library"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "https://github.com/gozala/node.core"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138" :scope "provided"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]]
  :source-paths ["src"]

  :plugins [[lein-cljsbuild "1.0.1"]
            [com.cemerick/clojurescript.test "0.2.1"]]

  :cljsbuild
  {:builds
   [{:id "simple"
     :source-paths ["src" "test"]
     :compiler {:optimizations :simple
                :pretty-print true
                :output-to "target/cljs/test-simple.js"}}
    {:id "advanced"
     :source-paths ["src" "test"]
     :compiler {:optimizations :advanced
                :pretty-print false
                :static-fns true
                :output-dir "out"
                :output-to "target/cljs/test-advanced.js"
                :source-map "target/cljs/test-advanced.js.map"}}]
   :test-commands {"unit-tests" ["node" :node-runner
                                 "this.literal_js_was_evaluated=true"
                                 "target/cljs/testable.js"]}})
