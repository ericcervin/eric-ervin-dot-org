(ns eric-ervin-dot-org.routes.philosophy-usa
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]))

(def html-style-css [:style "table,th,td {border: 1px solid black;
                                          border-collapse: collapse;
                                          padding: 3px;
                                          text-align: left
                     }
                     "])

(defn map-html-table-td [cl]
  (if (some? cl)
      (if (clojure.string/includes? cl "http") (html [:td [:a {:href cl} cl]]) (html [:td cl]))
      (html [:td])))    

(defn map-html-table-tr [mp]
  (html [:tr (map map-html-table-td mp)]))

(defn reports-html [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/philosophy-usa.db"}
                                   qry (:query qry-map)
                                   header (:header qry-map)
                                   results (sql/query db-spec [qry] {:as-arrays? true})
                                   report-rows (map map-html-table-tr (rest results))]
                               (html html-style-css
                                 [:table 
                                  [:tr (map #(html [:th %]) header)]
                                  report-rows])))



(defn report-query [ctx] 
       (if-let [qry-map (condp = (get-in ctx [:request :params "rpt"])  
                              "state_count" 
                              {:header ["State" "Count"] 
                               :query "Select stabbr, count(*) as count 
                                       from completion cmp 
                                       join institution ins on cmp.inst = ins.unitid
                                       group by stabbr
                                       order by count(*) DESC"}
                              "inst_count" 
                              {:header ["Institution" "Count"] 
                               :query "Select instnm, count(*) as count 
                                       from completion cmp 
                                       join institution ins on cmp.inst = ins.unitid
                                       group by instnm
                                       order by count(*) DESC"}
                              "cip_count" 
                              {:header ["CIP Code" "CIP Title" "Count"] 
                               :query "Select cipcode, ciptitle, count(*) as count 
                                       from completion cmp 
                                       join cipcode chp on cmp.cip = chp.cipcode
                                       group by cipcode, ciptitle
                                       order by count(*) DESC"}
                                
                              "awlevel_count"
                              {:header ["Code" "Level" "Count"] 
                               :query "Select alcode, alvalue, Count(*) 
                                       from alcode join completion
                                       on alcode.alcode = completion.awlevel
                                       group by alcode, alvalue"})]
         
         
         (reports-html qry-map)))

(defresource res-reports [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok report-query)



(defresource res-philosophy [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] (html html-style-css
                                        [:h4 "Philosophy and religious studies degrees completed during the 2014-2015 academic year. <br> 
                                              Data taken from the Integrated Postsecondary Education Data System (IPEDS) <br>"]
                                              
                                        [:h4 [:a {:href "https://nces.ed.gov/ipeds/Home/UseTheData"} "https://nces.ed.gov/ipeds/Home/UseTheData"]]
                                        [:h4 "Reports"]
                                    [:table
                                     [:tr [:th "Philosophy Degrees Completed by Award Level"][:td [:a {:href "/philosophy/reports?rpt=awlevel_count"} "HTML"]]]                                    
                                     [:tr [:th "Philosophy Degrees Completed by Institution"][:td [:a {:href "/philosophy/reports?rpt=inst_count"} "HTML"]]]
                                     [:tr [:th "Philosophy Degrees Completed by State"][:td [:a {:href "/philosophy/reports?rpt=state_count"} "HTML"]]]
                                     [:tr [:th "Philosophy Degrees Completed by Subject Classification"][:td [:a {:href "/philosophy/reports?rpt=cip_count"} "HTML"]]]])))
                                     
                                        



(defroutes philosophy-routes  
  (ANY "/philosophy" [] res-philosophy)
  (ANY "/philosophy/reports" [] res-reports))
  
  
