(ns eric-ervin-dot-org.routes.serialism
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]
            [eric-ervin-dot-org.representation :refer [html-style-css map-html-table-td map-html-table-tr]]))
  

(defn serialism-html [mp]
  (html html-style-css [:table
                        [:tr [:th "P0"](map map-html-table-td (map #(format "%02d" %) (:P0 mp)))]
                        [:tr [:th "R0"](map map-html-table-td (map #(format "%02d" %) (:R0 mp)))]
                        [:tr [:th "I0"](map map-html-table-td (map #(format "%02d" %) (:I0 mp)))]
                        [:tr [:th "RI0"](map map-html-table-td (map #(format "%02d" %) (:RI0 mp)))]]))

(defn serialism-map [] (let [P0 (vec (shuffle (range 12)))
                             R0 (vec (reverse P0))
                             I0 (vec(map #(if (= % 0) 0 (- 12 %)) P0))
                             RI0 (vec(reverse I0))]
                         {:P0 P0
                          :R0 R0
                          :I0 I0
                          :RI0 RI0}))

(defresource res-serialism [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (serialism-html (serialism-map)))

(defroutes serialism-routes
  (ANY "/serialism" [] res-serialism))


