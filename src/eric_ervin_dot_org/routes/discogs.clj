(ns eric-ervin-dot-org.routes.discogs
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [doctype html5]]
            [clojure.java.jdbc :as sql]
            [eric-ervin-dot-org.representation :refer [html-style-css map-html-table-td map-html-table-tr]]))


(defn reports-html [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/discogs.db"}
                                   qry (:query qry-map)
                                   header (:header qry-map)
                                   results (sql/query db-spec [qry] {:as-arrays? true})
                                   report-rows (map map-html-table-tr (rest results))]
                               (html5  {:lang "en"}

                                       [:head html-style-css] 
                                       [:body
                                        [:table 
                                         [:tr (map #(html [:th %]) header)]
                                         report-rows]])))


(def discogs-root-template "
     <!DOCTYPE html>
     <html lang=\"en\">
     <head>
     <style> table,th,td {
                      border: 1px solid black;
                      border-collapse: collapse;
                      padding: 3px;
                      text-align: center;}
                               
              td {text-align: left}
     </style>
     </head>
     <body>
     <div id=\"header\"><h3>My Record Collection</h3>
     </div>
     <div id=\"releases\">
     <h4>Releases</h4>
     <table>
     <thead>
     <tr>
     <th></th>
     <th scope=\"col\">By Title</th>
     <th scope=\"col\">By Artist</th>
     <th scope=\"col\">By Label</th>
     <th scope=\"col\">By Release Year</th></tr>
     </thead>
     <tbody>
     <tr>
     <th>All</th><td><a href=\"/discogs/releases?sort=title\">HTML</a></td>
     <td><a href=\"/discogs/releases?sort=artist\">HTML</a></td>
     <td><a href=\"/discogs/releases?sort=label\">HTML</a></td>
     <td><a href=\"/discogs/releases?sort=year\">HTML</a></td></tr>
     </tbody>
     </table>
     </div>
     <div id=\"reports\"><h4>Reports</h4>
     <table>
     <thead><tr>
     <th scope=\"col\">Report</th><th scope=\"col\">Format</th></tr></thead>
     <tbody>
     <tr><td>Count by Artist</td><td><a href=\"/discogs/reports?rpt=artist_count\">HTML</a></td></tr>
     <tr><td>Count by Label</td><td><a href=\"/discogs/reports?rpt=label_count\">HTML</a></td></tr>
     <tr><td>Count by Year Released</td><td><a href=\"/discogs/reports?rpt=year_count\">HTML</a></td></tr>
     </tbody>
     </table></div></body></html>")
  
  



(defn releases-query [ctx] 
  (let [sort (get-in ctx [:request :params "sort"])
        select-fields "title, artist, label, year"
        qry-str (if (some? sort) (str "Select " select-fields " from release order by " sort)
                                 (str "Select " select-fields " from release order by artist"))
        qry-map {:header ["Title" "Artist" "Label" "Release Year"]
                 :query qry-str}]       
   (reports-html qry-map)))




(defresource res-discogs [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render discogs-root-template))

(defn report-query [ctx] 
       (if-let [qry-map (condp = (get-in ctx [:request :params "rpt"])  
                              "artist_count" 
                              {:header ["Artist" "Count"] 
                               :query "Select artist, count(*) as count from release group by artist order by count(*) DESC"}
                              "label_count"
                              {:header ["Label" "Count"] 
                               :query "Select label, count(*) as count from release group by label order by count(*) DESC"}
                              "year_count"
                              {:header ["Year Released" "Count"] 
                               :query "Select year, count(*) as count from release group by year order by count(*) DESC"}
                              "year_month_added"
                              {:header ["Year Added" "Month Added" "Count"]
                               :query "Select substr(dateadded,0,5), substr(dateadded,6,2), Count(*) 
                                       from release group by substr(dateadded,0,5), substr(dateadded,6,2)
                                       order by substr(dateadded,0,5) DESC, substr(dateadded,6,2) DESC"})] 
         
         (reports-html qry-map)))

(defresource res-releases [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok releases-query)
(defresource res-reports [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok report-query)

(defroutes discogs-routes  
  (ANY "/discogs" [] res-discogs)
  (ANY "/discogs/releases" [] res-releases)
  (ANY "/discogs/reports" [] res-reports))
  
  
