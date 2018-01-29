(ns eric-ervin-dot-org.routes.serialism
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]))


(def html-style-css [:style "table,th {
                                          border: 1px solid black;
                                          border-collapse: collapse;
                                          padding: 3px;
                                          text-align: center
                                         }
                             td {
                                          border: 1px solid black;
                                          border-collapse: collapse;
                                          padding: 3px;
                                          text-align: center
                                         }
                                         "])


(defn map-html-table-td [cl]
  (if (some? cl)
      (html [:td cl])
      (html [:td])))    

(defn serialism-html [mp]
  (html html-style-css [:table
                        [:tr [:th "P0"](map map-html-table-td (:P0 mp))]
                        [:tr [:th "R0"](map map-html-table-td (:R0 mp))]
                        [:tr [:th "I0"](map map-html-table-td (:I0 mp))]
                        [:tr [:th "RI0"](map map-html-table-td (:RI0 mp))]]))

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


