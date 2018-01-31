(ns eric-ervin-dot-org.routes.philosophy-usa
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]
            [eric-ervin-dot-org.representation :refer [html-style-css map-html-table-td map-html-table-tr]]))


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
                                   [:div {:id "header"}
                                    [:h3 "Philosopy USA"]
                                    [:p "Philosophy and religious studies degrees completed during the 2014-2015 academic year."] 
                                    [:p "Data taken from the Integrated Postsecondary Education Data System (IPEDS)"]
                                    [:p [:a {:href "https://nces.ed.gov/ipeds/Home/UseTheData"} "https://nces.ed.gov/ipeds/Home/UseTheData"]]]
                                   
                                   [:div {:id "reports"}          
                                    [:h4 "Reports"]
                                    [:table
                                     [:thead
                                      [:tr [:th {:scope "col"} "Report"][:th {:scope "col"} "Format"]]]
                                     [:tbody
                                      [:tr [:td "Philosophy Degrees Completed by Award Level"][:td [:a {:href "/philosophy/reports?rpt=awlevel_count"} "HTML"]]]                                    
                                      [:tr [:td "Philosophy Degrees Completed by Institution"][:td [:a {:href "/philosophy/reports?rpt=inst_count"} "HTML"]]]
                                      [:tr [:td "Philosophy Degrees Completed by State"][:td [:a {:href "/philosophy/reports?rpt=state_count"} "HTML"]]]
                                      [:tr [:td "Philosophy Degrees Completed by Subject Classification"][:td [:a {:href "/philosophy/reports?rpt=cip_count"} "HTML"]]]]]])))
                                     
                                        



(defroutes philosophy-routes  
  (ANY "/philosophy" [] res-philosophy)
  (ANY "/philosophy/reports" [] res-reports))
  
  

