(defproject eric_ervin_dot_org "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [liberator "0.13"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.xerial/sqlite-jdbc "3.20.0"]
                 [hiccup "1.0.4"]
                 [cljstache "2.0.1"]]    
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler eric-ervin-dot-org.handler/app
         :auto-reload true
         :auto-refresh true
         :port 80}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.5.1"]]}})
  
  
