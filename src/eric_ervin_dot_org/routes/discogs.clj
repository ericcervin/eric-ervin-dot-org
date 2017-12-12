(ns eric-ervin-dot-org.routes.discogs
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]))
            


(defn releases_html [ctx] (let [db-spec {:classname "org.sqlite.JDBC"
                                         :subprotocol "sqlite"
                                         :subname "resources/discogs.db"}
                                all-releases (sql/query db-spec ["Select * from release order by artist"])
                                release-rows (map #(html [:tr [:td (:title %)] [:td (:artist %)][:td (:label %)][:td (:year %)]]) all-releases)]
                            (html [:h6 [:style "table,th,td {
                                        border: 1px solid black;
                                        border-collapse: collapse;
                                        padding: 5px;
                                        }
                                        "]; 
                                   [:table 
                                    [:tr [:th "Title"] [:th "Artist"] [:th "Label"][:th "Year"]]
                                    release-rows]])))


(defresource res-releases-html [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok releases_html)

(defroutes discogs-routes  
  ;;(ANY "/discogs" [] res-discogs)
  (ANY "/discogs/releases/html" [] res-releases-html))
  ;;(ANY "/discogs/releases/json" [] res-releases-json)
  ;;(ANY "/discogs/releases/text" [] res-releases-text))
  
