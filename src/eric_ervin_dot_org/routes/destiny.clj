(ns eric-ervin-dot-org.routes.destiny
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [doctype html5]]
            [clojure.java.jdbc :as sql]
            [eric-ervin-dot-org.representation :refer [html-style-css map-html-table-td map-html-table-tr]]))





(defn reports-html [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/destiny.db"}
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

(defresource res-destiny [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] (html5  {:lang "en"}
                                          [:head html-style-css] 
                                     
                                          [:body
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
                                             
                                             [:tr [:td "Compatible with Villains, Command"][:td [:a {:href "/destiny/reports?rpt=villain_command_compatible"} "HTML"]]]
                                             [:tr [:td "Count by Affiliation/Faction"][:td [:a {:href "/destiny/reports?rpt=affiliation_faction_count"} "HTML"]]]
                                             [:tr [:td "Count by Rarity"][:td [:a {:href "/destiny/reports?rpt=rarity_count"} "HTML"]]]
                                             [:tr [:td "Count by Set"][:td [:a {:href "/destiny/reports?rpt=set_count"} "HTML"]]]
                                             [:tr [:td "Highest Cost Support/Event/Upgrade"][:td [:a {:href "/destiny/reports?rpt=high_cost"} "HTML"]]]
                                             [:tr [:td "Rarity Legendary Cards"][:td [:a {:href "/destiny/reports?rpt=legendary"} "HTML"]]]
                                             [:tr [:td "Rarity Rare Cards"][:td [:a {:href "/destiny/reports?rpt=rare"} "HTML"]]]
                                             [:tr [:td "Type Character Cards"][:td [:a {:href "/destiny/reports?rpt=type_character"} "HTML"]]]
                                             [:tr [:td "Type Upgrade Cards"][:td [:a {:href "/destiny/reports?rpt=type_upgrade"} "HTML"]]]]]])))

(defn cards-query [ctx] 
  (let [affil (get-in ctx [:request :params "affil"])
        fact (get-in ctx [:request :params "fact"])
        select-fields "cardsetcode, position, name, typename, isunique, raritycode, affiliation, factioncode, cminpoints, cmaxpoints, chealth, csides,imgsrc"
        qry-str (cond (and (nil? affil) (nil? fact))  (str "Select " select-fields " from card")
                      (and (nil? affil) (some? fact)) (str "Select " select-fields " from card where faction = \"" fact "\"")
                      (and (some? affil) (nil? fact)) (str "Select " select-fields " from card where affiliation = \"" affil "\"")
                      :else (str "Select " select-fields " from card where affiliation = \"" affil "\" and faction = \"" fact "\""))
        qry-map {:header ["Set" "Pos" "Name" "Type" "Unique" "Rarity" "Affil" "Faction" 
                          "Min<br>Cost" "Max<br>Cost" "Health" "Sides" "Img Source"]
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
                               {:header ["Set" "Pos" "Name" "Type" "Is<br>Unique" "Rarity" "Cost" "Sides" "Image"] 
                                :query "Select cardsetcode, position, name, typename, isunique, raritycode, ccost, csides, imgsrc
                                        from card where ccost is not null 
                                        order by ccost desc"}
                               "legendary" 
                               {:header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is<br>Unique" "Rarity" "Cost" "Sides" "Image"] 
                                :query "Select cardsetcode, position, name, typename, affiliation, factioncode, isunique, raritycode, ccost, csides, imgsrc 
                                        from card where rarity = \"Legendary\" 
                                        "}
                               "rare" 
                               {:header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is<br>Unique" "Rarity" "Cost" "Sides" "Image"] 
                                :query "Select cardsetcode, position, name, typename, affiliation, factioncode, isunique, raritycode, ccost, csides, imgsrc 
                                        from card where rarity = \"Rare\" 
                                        "}
                               "villain_command_compatible"
                               {:header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is<br>Unique" "Rarity" "Cost" "Sides" "Image"] 
                                :query "Select cardsetcode, position, name, typename, affiliation, factioncode, isunique, raritycode, ccost, csides, imgsrc 
                                        from card where (affiliation = \"Villain\" or affiliation = \"Neutral\" ) 
                                                    and (faction = \"Command\" or faction = \"General\") 
                                        "}
                               "type_character"
                               {:header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is<br>Unique" "Rarity" "MinPoints" "MaxPoints" "Health", "Sides" "Image"] 
                                :query "Select cardsetcode, position, name, typename, affiliation, factioncode, isunique, raritycode, cminpoints, cmaxpoints, chealth, csides, imgsrc 
                                        from card where typename = \"Character\" 
                                "}
                                
                               "type_upgrade"
                               {:header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is<br>Unique" "Rarity" "Cost" "Sides" "Image"] 
                                :query "Select cardsetcode, position, name, typename, affiliation, faction, isunique, raritycode, ccost, csides, imgsrc 
                                        from card where typename = \"Upgrade\" 
                                "})]
                               
                          
                          
                                        
                                   
         (reports-html qry-map)))

(defresource res-cards [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-query)
(defresource res-reports [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok report-query)

(defroutes destiny-routes  
  (ANY "/destiny" [] res-destiny)
  (ANY "/destiny/cards" [] res-cards)
  (ANY "/destiny/reports" [] res-reports))
  
          
