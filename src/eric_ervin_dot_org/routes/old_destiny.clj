(ns eric-ervin-dot-org.routes.old_destiny
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]))

(defn cards-html [qry] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/destiny.db"}
                             all-cards (sql/query db-spec [qry])
                             card-rows (map #(html [:tr [:td (:cardset %)] [:td (:position %)][:td (:name %)][:td (:typename %)]
                                                    [:td (:isunique %)][:td (:affiliation %)][:td (:faction %)][:td (:cminpoints %)][:td (:cmaxpoints %)]]) all-cards)]
                         (html [:h6 [:style "table,th,td {
                                     border: 1px solid black;
                                     border-collapse: collapse;
                                     padding: 5px;
                                     }
                                     "]; 
                                [:table 
                                 [:tr [:th "Set"] [:th "Position"] [:th "Name"][:th "Type"][:th "Is Unique"][:th "Affiliation"] [:th "Faction"][:th "Min Cost"][:th "Max Cost"]]
                                 card-rows]])))

(defn cards-all-html [ctx]
     (cards-html "Select * from card"))

(defn cards-villain-command-html [ctx]
     (cards-html "Select * from card where affiliation = \"Villain\" and faction = \"Command\""))

(defn cards-hero-command-html [ctx]
     (cards-html "Select * from card where affiliation = \"Hero\" and faction = \"Command\""))

(defn cards-neutral-command-html [ctx]
     (cards-html "Select * from card where affiliation = \"Neutral\" and faction = \"Command\""))

(defn cards-villain-force-html [ctx]
     (cards-html "Select * from card where affiliation = \"Villain\" and faction = \"Force\""))

(defn cards-hero-force-html [ctx]
     (cards-html "Select * from card where affiliation = \"Hero\" and faction = \"Force\""))

(defn cards-neutral-force-html [ctx]
     (cards-html "Select * from card where affiliation = \"Neutral\" and faction = \"Force\""))

(defn cards-villain-rogue-html [ctx]
     (cards-html "Select * from card where affiliation = \"Villain\" and faction = \"Rogue\""))

(defn cards-hero-rogue-html [ctx]
     (cards-html "Select * from card where affiliation = \"Hero\" and faction = \"Rogue\""))

(defn cards-neutral-rogue-html [ctx]
     (cards-html "Select * from card where affiliation = \"Neutral\" and faction = \"Rogue\""))

(defn cards-villain-general-html [ctx]
     (cards-html "Select * from card where affiliation = \"Villain\" and faction = \"General\""))

(defn cards-hero-general-html [ctx]
     (cards-html "Select * from card where affiliation = \"Hero\" and faction = \"General\""))

(defn cards-neutral-general-html [ctx]
     (cards-html "Select * from card where affiliation = \"Neutral\" and faction = \"General\""))

(defresource res-destiny [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] (html [:style "table,th,td {
                                          border: 1px solid black;
                                         border-collapse: collapse;
                                         padding: 15px;
                                         }
                                         "]
                                         
                                        [:h4 "All Cards"]
                                        [:table
                                                             
                                         [:tr [:th "HTML"] [:td [:a {:href "/destiny/cards/html"} "Cards"]]]]
                                        [:h4 "Subsets of Cards"]
                                        [:table
                                         [:tr [:th ""] [:th "Villain"][:th "Hero"][:th "Neutral"]]
                                         [:tr [:th "Command"] [:td [:a {:href "/destiny/cards/villain/command/html"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards/hero/command/html"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards/neutral/command/html"} "HTML"]]]
                                         [:tr [:th "Force"] [:td [:a {:href "/destiny/cards/villain/force/html"} "HTML"]]
                                                            [:td [:a {:href "/destiny/cards/hero/force/html"} "HTML"]]
                                                            [:td [:a {:href "/destiny/cards/neutral/force/html"} "HTML"]]]
                                         [:tr [:th "Rogue"] [:td [:a {:href "/destiny/cards/villain/rogue/html"} "HTML"]]
                                                            [:td [:a {:href "/destiny/cards/hero/rogue/html"} "HTML"]]
                                                            [:td [:a {:href "/destiny/cards/neutral/rogue/html"} "HTML"]]]
                                         [:tr [:th "General"] [:td [:a {:href "/destiny/cards/villain/general/html"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards/hero/general/html"} "HTML"]]
                                                              [:td [:a {:href "/destiny/cards/neutral/general/html"} "HTML"]]]])))
                                    
  

(defresource res-cards-all-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-all-html)
(defresource res-cards-villain-command-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-villain-command-html)
(defresource res-cards-hero-command-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-hero-command-html)
(defresource res-cards-neutral-command-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-neutral-command-html)
(defresource res-cards-villain-force-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-villain-force-html)
(defresource res-cards-hero-force-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-hero-force-html)
(defresource res-cards-neutral-force-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-neutral-force-html)
(defresource res-cards-villain-rogue-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-villain-rogue-html)
(defresource res-cards-hero-rogue-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-hero-rogue-html)
(defresource res-cards-neutral-rogue-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-neutral-rogue-html)
(defresource res-cards-villain-general-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-villain-general-html)
(defresource res-cards-hero-general-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-hero-general-html)
(defresource res-cards-neutral-general-html [ctx] :allowed-methods [:get :options] :available-media-types ["text/html"] :handle-ok cards-neutral-general-html)


(defroutes destiny-routes  
  (ANY "/destiny" [] res-destiny)
  (ANY "/destiny/cards/html" [] res-cards-all-html)
  (ANY "/destiny/cards/villain/command/html" [] res-cards-villain-command-html)
  (ANY "/destiny/cards/hero/command/html" [] res-cards-hero-command-html)
  (ANY "/destiny/cards/neutral/command/html" [] res-cards-neutral-command-html)
  (ANY "/destiny/cards/villain/force/html" [] res-cards-villain-force-html)
  (ANY "/destiny/cards/hero/force/html" [] res-cards-hero-force-html)
  (ANY "/destiny/cards/neutral/force/html" [] res-cards-neutral-force-html)
  (ANY "/destiny/cards/villain/rogue/html" [] res-cards-villain-rogue-html)
  (ANY "/destiny/cards/hero/rogue/html" [] res-cards-hero-rogue-html)
  (ANY "/destiny/cards/neutral/rogue/html" [] res-cards-neutral-rogue-html)
  (ANY "/destiny/cards/villain/general/html" [] res-cards-villain-general-html)
  (ANY "/destiny/cards/hero/general/html" [] res-cards-hero-general-html)
  (ANY "/destiny/cards/neutral/general/html" [] res-cards-neutral-general-html))
  
            
