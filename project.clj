(defproject lsyscljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths  ["src/clj" "src/cljs"]
  :dependencies  [[org.clojure/clojure "1.5.1"]
                  [org.clojure/clojurescript "0.0-2234"]
                  [om "0.6.4"]
                  [org.clojure/core.async "0.1.267.0-0d7780-alpha"]]
  :plugins       [[lein-cljsbuild "1.0.3"]]
  :cljsbuild     {:builds
                  [{;; CLJS source code path
                    :source-paths ["src/cljs"]
                    ;; Google Closure (CLS) options configuration
                    :compiler {;; CLS generated JS script filename
                               :output-to "lsystems.js"
                               :output-dir "out"
                               ;; minimal JS optimization directive
                               :optimizations :none
                               ;; generated JS code prettyfication
                               :pretty-print true
                               :source-map true}}]})
