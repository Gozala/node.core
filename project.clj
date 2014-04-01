(defproject com.jeditoolkit/node "0.0.1"
  :description "core.async flavored interface to a node standard library"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "https://github.com/gozala/node.core"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173" :scope "provided"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]]

  :source-paths ["src"]

  :plugins [[lein-cljsbuild "1.0.2"]
            [com.cemerick/clojurescript.test "0.3.0"]]
  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds
   [{:source-paths ["src" "test"]
     :compiler {:optimizations :simple
                :pretty-print true
                :foreign-libs [{:file "js/process.js"
                                :provides ["node.process.patch"]}]
                :output-to "target/cljs/simple.js"}}
    #_ {:id "advanced"
     :source-paths ["src" "test"]
     :compiler {:optimizations :advanced
                :pretty-print false
                :static-fns true
                :externs ["externs/index.js"
                          "externs/process.js"
                          "externs/binding.os.js"
                          "externs/require.path.js"
                          "externs/require.net.js"
                          "externs/require.stream.js"
                          "externs/require.events.js"]
                :foreign-libs [{:file "js/process.js"
                                :provides ["node.process.patch"]}]
                ;:source-map "target/cljs/advanced.js.map"
                :output-to "target/cljs/advanced.js"}}]

   :test-commands {"simple" ["node" :node-runner
                             "target/cljs/simple.js"]}})
