(ns eric-ervin-dot-org.routes.destiny
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
                                          text-align: left
                                         }
                                         "])

(defn map-html-table-td [cl]
  (if (some? cl)
      (if (clojure.string/includes? cl "http") (html [:td [:a {:href cl} cl]]) (html [:td cl]))
      (html [:td])))    

(defn map-html-table-tr [mp]
  (html [:tr (map map-html-table-td mp)]))

(defn reports-html [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/destiny.db"}
                                   qry (:query qry-map)
                                   header (:header qry-map)
                                   results (sql/query db-spec [qry] {:as-arrays? true})
                                   report-rows (map map-html-table-tr (rest results))]
                               (html html-style-css
                                 [:table 
                                  [:tr (map #(html [:th %]) header)]
                                  report-rows])))

(defresource res-destiny [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] (html html-style-css
                                     [:div {:id "header"}
                                      [:h3 "Star Wars Destiny"][:br]]
                                      

                                      
                                      
                                     [:div {:id "cards"}
                                      [:h4 "Cards"]
                                      
                                      [:table
                                       [:thead
                                        [:tr [:th][:th][:th {:colspan "4"} "Affiliation"]]
                                        [:tr [:th ""][:th ""] [:th {:scope "col"} "All"][:th {:scope "col"} "Villain"][:th {:scope "col"} "Hero"][:th {:scope "col"} "Neutral"]]]
                                       [:tbody 
                                        [:tr [:th {:rowspan "5"} "Faction"] [:th {:scope "row"} "All"]     [:td [:a {:href "/destiny/cards?"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Villain"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Hero"} "HTML"]] 
                                         [:td [:a {:href "/destiny/cards?affil=Neutral"} "HTML"]]]
                                        [:tr [:th {:scope "row"} "Command"] [:td [:a {:href "/destiny/cards?fact=Command"} "HTML"]] 
                                         [:td [:a {:href "/destiny/cards?affil=Villain&fact=Command"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Hero&fact=Command"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Neutral&fact=Command"} "HTML"]]]
                                        [:tr [:th {:scope "row"} "Force"]   [:td [:a {:href "/destiny/cards?fact=Force"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Villain&fact=Force"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Hero&fact=Force"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Neutral&fact=Force"} "HTML"]]]
                                        [:tr [:th {:scope "row"} "Rogue"]   [:td [:a {:href "/destiny/cards?fact=Rogue"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Villain&fact=Rogue"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Hero&fact=Rogue"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Neutral&fact=Rogue"} "HTML"]]]
                                        [:tr [:th {:scope "row"} "General"] [:td [:a {:href "/destiny/cards?fact=General"} "HTML"]] 
                                         [:td [:a {:href "/destiny/cards?affil=Villain&fact=General"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Hero&fact=General"} "HTML"]]
                                         [:td [:a {:href "/destiny/cards?affil=Neutral&fact=General"} "HTML"]]]]]]
                                    
                                    [:div {:id "reports"}
                                     [:h4 "Reports"]
                                     [:table
                                      [:tr [:td "Character Type Cards"][:td [:a {:href "/destiny/reports?rpt=character"} "HTML"]]]
                                      [:tr [:td "Compatible with Villain/Command"][:td [:a {:href "/destiny/reports?rpt=villain_command_compatible"} "HTML"]]]
                                      [:tr [:td "Count by Affiliation/Faction"][:td [:a {:href "/destiny/reports?rpt=affiliation_faction_count"} "HTML"]]]
                                      [:tr [:td "Count by Rarity"][:td [:a {:href "/destiny/reports?rpt=rarity_count"} "HTML"]]]
                                      [:tr [:td "Count by Set"][:td [:a {:href "/destiny/reports?rpt=set_count"} "HTML"]]]
                                      [:tr [:td "Highest Cost Support/Event/Upgrade"][:td [:a {:href "/destiny/reports?rpt=high_cost"} "HTML"]]]
                                      [:tr [:td "Legendary Rarity Cards"][:td [:a {:href "/destiny/reports?rpt=legendary"} "HTML"]]]]])))

(defn cards-query [ctx] 
  (let [affil (get-in ctx [:request :params "affil"])
        fact (get-in ctx [:request :params "fact"])
        select-fields "cardset, position, name, typename, isunique, rarity, affiliation, faction, cminpoints, cmaxpoints, chealth, imgsrc"
        qry-str (cond (and (nil? affil) (nil? fact))  (str "Select " select-fields " from card")
                      (and (nil? affil) (some? fact)) (str "Select " select-fields " from card where faction = \"" fact "\"")
                      (and (some? affil) (nil? fact)) (str "Select " select-fields " from card where affiliation = \"" affil "\"")
                      :else (str "Select " select-fields " from card where affiliation = \"" affil "\" and faction = \"" fact "\""))
        qry-map {:header ["Set" "Position" "Name" "Type" "Is Unique" "Rarity" "Affiliation" "Faction" 
                          "Min Cost" "Max Cost" "Health" "Img Source"]
                 :query qry-str}]       
       (reports-html qry-map)))

(defn report-query [ctx] 
       (if-let [qry-map (condp = (get-in ctx [:request :params "rpt"])  
                              "affiliation_faction_count" 
                              {:header ["Affilliation" "Faction" "Count"] 
                               :query "Select affiliation, faction, count(*) as count from card group by affiliation, faction"}
                              "rarity_count" 
                              {:header ["Rarity" "Count"] 
                               :query "Select rarity, count(*) as count from card group by rarity"}
                              "set_count" 
                              {:header ["Set" "Count"] 
                               :query "Select cardset, count(*) as count from card group by cardset"}
                              "high_cost" 
                               {:header ["Set" "Position" "Name" "Type" "Is Unique" "Rarity" "Cost"] 
                                :query "Select cardset, position, name, typename, isunique, rarity, ccost 
                                        from card where ccost is not null 
                                        order by ccost desc"}
                               "legendary" 
                               {:header ["Set" "Position" "Name" "Type" "Affilliation" "Faction" "Is Unique" "Rarity" "Cost"] 
                                :query "Select cardset, position, name, typename, affiliation, faction, isunique, rarity, ccost 
                                        from card where rarity = \"Legendary\" 
                                        "}
                               "villain_command_compatible"
                               {:header ["Set" "Position" "Name" "Type" "Affilliation" "Faction" "Is Unique" "Rarity" "Cost"] 
                                :query "Select cardset, position, name, typename, affiliation, faction, isunique, rarity, ccost 
                                        from card where (affiliation = \"Villain\" or affiliation = \"Neutral\" ) 
                                                    and (faction = \"Command\" or faction = \"General\") 
                                        "}
                               "character"
                               {:header ["Set" "Position" "Name" "Type" "Affilliation" "Faction" "Is Unique" "Rarity" "MinPoints" "MaxPoints" "Health" "Image"] 
                                :query "Select cardset, position, name, typename, affiliation, faction, isunique, rarity, cminpoints, cmaxpoints, chealth, imgsrc 
                                        from card where typename = \"Character\" 
                                "})]
                                        
                                   
         (reports-html qry-map)))

(defresource res-cards [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-query)
(defresource res-reports [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok report-query)

(defroutes destiny-routes  
  (ANY "/destiny" [] res-destiny)
  (ANY "/destiny/cards" [] res-cards)
  (ANY "/destiny/reports" [] res-reports))
  
          
