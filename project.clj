(defproject lsyscljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths  ["src/clj" "src/cljs"]
  :dependencies  [[org.clojure/clojure "1.5.1"]
                  [org.clojure/clojurescript "0.0-2202"]]
  :plugins       [[lein-cljsbuild "1.0.3"]]
  :cljsbuild     {:builds
                  [{;; CLJS source code path
                    :source-paths ["src/cljs"]
                    ;; Google Closure (CLS) options configuration
                    :compiler {;; CLS generated JS script filename
                               :output-to "resources/public/js/lsys.js"
                               ;; minimal JS optimization directive
                               :optimizations :whitespace
                               ;; generated JS code prettyfication
                               :pretty-print true}}]})
