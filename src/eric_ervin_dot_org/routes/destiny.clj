(ns eric-ervin-dot-org.routes.destiny
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]))

(def html-style-css [:style "table,th,td {
                                          border: 1px solid black;
                                          border-collapse: collapse;
                                          padding: 3px;
                                         }
                                         "])
                                         

(defn map-html-table-td-row [mp]
  (html [:tr (map #(html [:td (second %)]) mp)]))

(defn cards-html [qry] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/destiny.db"}
                             all-cards (sql/query db-spec [qry])
                             card-rows (map #(html [:tr [:td (:cardset %)] [:td (:position %)][:td (:name %)][:td (:typename %)]
                                                        [:td (:isunique %)][:td (:rarity %)][:td (:affiliation %)][:td (:faction %)]
                                                        [:td (:cminpoints %)][:td (:cmaxpoints %)][:td (:chealth %)][:td [:a {:href (:imgsrc %)} (:imgsrc %)]]]) all-cards)]
                         
                         (html  html-style-css
                                [:h1 [:table 
                                      [:tr [:th "Set"] [:th "Position"] [:th "Name"][:th "Type"][:th "Is Unique"][:th "Rarity"][:th "Affiliation"] [:th "Faction"][:th "Min Cost"][:th "Max Cost"][:th "Health"][:th "Img Source"]]
                                      card-rows]])))



(defn reports-html [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/destiny.db"}
                                   qry (:query qry-map)
                                   header (:header qry-map)
                                   results (sql/query db-spec [qry])
                                   report-rows (map map-html-table-td-row results)]
                               (html html-style-css
                                 [:table 
                                  [:tr (map #(html [:th %]) header)]
                                  report-rows])))

(defresource res-destiny [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] (html html-style-css
                                        [:h4 "Cards"]
                                        [:table
                                         [:tr [:th ""] [:th "All"][:th "Villain"][:th "Hero"][:th "Neutral"]]
                                         [:tr [:th "All"]     [:td [:a {:href "/destiny/cards?"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Villain"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Hero"} "HTML"]] 
                                                              [:td [:a {:href "/destiny/cards?affil=Neutral"} "HTML"]]]
                                         [:tr [:th "Command"] [:td [:a {:href "/destiny/cards?fact=Command"} "HTML"]] 
                                                              [:td [:a {:href "/destiny/cards?affil=Villain&fact=Command"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Hero&fact=Command"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Neutral&fact=Command"} "HTML"]]]
                                         [:tr [:th "Force"]   [:td [:a {:href "/destiny/cards?fact=Force"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Villain&fact=Force"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Hero&fact=Force"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Neutral&fact=Force"} "HTML"]]]
                                         [:tr [:th "Rogue"]   [:td [:a {:href "/destiny/cards?fact=Rogue"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Villain&fact=Rogue"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Hero&fact=Rogue"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Neutral&fact=Rogue"} "HTML"]]]
                                         [:tr [:th "General"] [:td [:a {:href "/destiny/cards?fact=General"} "HTML"]] 
                                                              [:td [:a {:href "/destiny/cards?affil=Villain&fact=General"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Hero&fact=General"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards?affil=Neutral&fact=General"} "HTML"]]]]
                                    
                                    [:h4 "Reports"]
                                    [:table
                                     [:tr [:th "Count by Affiliation/Faction"][:td [:a {:href "/destiny/reports?rpt=affiliation_faction_count"} "HTML"]]]
                                     [:tr [:th "Count by Set"][:td [:a {:href "/destiny/reports?rpt=set_count"} "HTML"]]]])))

(defn cards-query [ctx] 
  (let [affil (get-in ctx [:request :params "affil"])
        fact (get-in ctx [:request :params "fact"])
        qry-str (cond (and (nil? affil) (nil? fact)) "Select * from card"
                      (and (nil? affil) (some? fact)) (str "Select * from card where faction = \"" fact "\"")
                      (and (some? affil) (nil? fact)) (str "Select * from card where affiliation = \"" affil "\"")
                      :else (str "Select * from card where affiliation = \"" affil "\" and faction = \"" fact "\""))]
  
   (cards-html qry-str)))

(defn report-query [ctx] 
       (if-let [qry-map (cond (= (get-in ctx [:request :params "rpt"]) "affiliation_faction_count") 
                              {:header ["Affilliation" "Faction" "Count"] 
                               :query "Select affiliation, faction, count(*) as count from card group by affiliation, faction"}
                              (= (get-in ctx [:request :params "rpt"]) "set_count") 
                              {:header ["Set" "Count"] 
                               :query "Select cardset, count(*) as count from card group by cardset"})]  
         (reports-html qry-map)))

(defresource res-cards [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-query)
(defresource res-reports [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok report-query)

(defroutes destiny-routes  
  (ANY "/destiny" [] res-destiny)
  (ANY "/destiny/cards" [] res-cards)
  (ANY "/destiny/reports" [] res-reports))
  
          
