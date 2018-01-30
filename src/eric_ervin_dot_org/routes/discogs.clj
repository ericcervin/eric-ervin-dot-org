(ns eric-ervin-dot-org.routes.discogs
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]
            [eric-ervin-dot-org.representation :refer [html-style-css]]))
            

(defn map-html-table-td [cl]
  (if (some? cl)
      (if (clojure.string/includes? cl "http") (html [:td [:a {:href cl} cl]]) (html [:td cl]))
      (html [:td])))    

(defn map-html-table-tr [mp]
  (html [:tr (map map-html-table-td mp)]))

(defn reports-html [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/discogs.db"}
                                   qry (:query qry-map)
                                   header (:header qry-map)
                                   results (sql/query db-spec [qry] {:as-arrays? true})
                                   report-rows (map map-html-table-tr (rest results))]
                               (html html-style-css
                                 [:table 
                                  [:tr (map #(html [:th %]) header)]
                                  report-rows])))

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
             :handle-ok (fn [ctx] (html html-style-css
                                    [:div {:id "header"}
                                     [:h3 "My Record Collection"]]
                                    
                                    [:div {:id "releases"}
                                   
                                     [:h4 "Releases"]
                                     [:table
                                      [:thead
                                       [:tr [:th ""] [:th {:scope "col"} "By Title"][:th {:scope "col"} "By Artist"][:th {:scope "col"} "By Label"][:th {:scope "col"} "By Release Year"]]]
                                      [:tbody
                                       [:tr [:th "All"]     
                                        [:td [:a {:href "/discogs/releases?sort=title"} "HTML"]]
                                        [:td [:a {:href "/discogs/releases?sort=artist"} "HTML"]]
                                        [:td [:a {:href "/discogs/releases?sort=label"} "HTML"]] 
                                        [:td [:a {:href "/discogs/releases?sort=year"} "HTML"]]]]]]
                                    
                                    [:div {:id "reports"}
                                     [:h4 "Reports"]
                                     [:table
                                      [:thead
                                       [:tr [:th {:scope "col"} "Report"][:th {:scope "col"} "Format"]]]
                                      [:tbody
                                       [:tr [:td "Count by Artist"][:td [:a {:href "/discogs/reports?rpt=artist_count"} "HTML"]]]
                                       [:tr [:td "Count by Label"][:td [:a {:href "/discogs/reports?rpt=label_count"} "HTML"]]]
                                       ;;[:tr [:td "Count by Year/Month Cataloged"][:td [:a {:href "/discogs/reports?rpt=year_month_added"} "HTML"]]]
                                       [:tr [:td "Count by Year Released"][:td [:a {:href "/discogs/reports?rpt=year_count"} "HTML"]]]]]])))

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
  
  
