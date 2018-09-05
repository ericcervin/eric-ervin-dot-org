(ns eric-ervin-dot-org.routes.destiny
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]
            [clojure.java.jdbc :as sql]))
            

                            


(def destiny-root-template "
  <!DOCTYPE html>
  <html lang=\"en\">
  <head>
  <title>Destiny</title>
  <style>
  table,th,td {
                               border: 1px solid black;
                               border-collapse: collapse;
                               padding: 3px;
                               text-align: center
                               }
       td {text-align: left}
  </style>
  </head>
  <body>
  <div id=\"header\">
  <h1>Star Wars Destiny</h1>
  <br>
  </div>
  <div id=\"cards\">
  <h4>Cards</h4>
  <table>
  <thead>
  <tr><th><th><th colspan=\"4\">Affiliation</th></tr>
  <tr><th></th><th></th><th scope=\"col\">All</th><th scope=\"col\">Villain</th><th scope=\"col\">Hero</th><th scope=\"col\">Neutral</th></tr></thead>
  <tbody>
  <tr><th rowspan=\"5\">Faction</th><th scope=\"row\">All</th><td><a href=\"/destiny/cards?\">HTML</a></td><td><a href=\"/destiny/cards?affil=Villain\">HTML</a></td><td><a href=\"/destiny/cards?affil=Hero\">HTML</a></td><td><a href=\"/destiny/cards?affil=Neutral\">HTML</a></td></tr>
  {{#factions}}
  <tr>

  <th scope=\"row\">{{.}}</th>
  <td><a href=\"/destiny/cards?fact={{.}}\">HTML</a></td>
  <td><a href=\"/destiny/cards?affil=Villain&amp;fact={{.}}\">HTML</a></td>
  <td><a href=\"/destiny/cards?affil=Hero&amp;fact={{.}}\">HTML</a></td>
  <td><a href=\"/destiny/cards?affil=Neutral&amp;fact={{.}}\">HTML</a></td>
  </tr>
  {{/factions}}
  </tbody>
  </table>
  </div>
  <div id=\"reports\">
  <h4>Reports</h4>
  <table>
  {{#reports}}
  <tr><td>{{text}}</td><td><a href=\"/destiny/reports/{{key}}\">HTML</a></td></tr>
  {{/reports}}
  </table>
  </div>
  </body>
  </html>
  ")


(def destiny-report-template "<!DOCTYPE html>
<html lang=\"en\">
  <head>
    <title>{{title}}</title>
    <style>
      table,th,td {
        border: 1px solid black;
        border-collapse: collapse;
        font-size: small;
        padding: 3px;
        text-align: center
      }
      td {text-align: left}
    </style>
  </head>
  <body>
    <div id=\"report\">
      <h3>{{title}}</h3>
      <table id = \"id_card_table\">
        <thead>
          <tr>{{#header}}<th>{{{.}}}</th>{{/header}}</tr>
        </thead>
        <tbody>
          {{#results}}
          <tr>{{#result}}<td>{{{vl}}}</td>{{/result}}</tr>
          {{/results}}
        </tbody>
      </table>
    </div>
  </body>
</html>")


(defn vector-of-maps [x] (vec (map #(hash-map :vl %) x)))

(defn html-for-http [cl] 
  (if (some? cl)
   (if (clojure.string/includes? cl "http") (str "<A HREF = \"" cl "\">" cl"</A>") cl)
   cl))

(defn html-for-result [res] (map html-for-http res))

(defn reports-html [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/destiny.db"}
                                   qry (:query qry-map)
                                   header (:header qry-map)
                                   title (:title qry-map)
                                   results (sql/query db-spec [qry] {:as-arrays? true})
                                   results (map html-for-result (rest results))
                                   output-map {:title title
                                               :header header
                                               :results (vec (map #(hash-map :result (vector-of-maps %)) results))}]
                               
                               (render destiny-report-template output-map)))




(defn cards-query [ctx] 
  (let [affil (get-in ctx [:request :params "affil"])
        fact (get-in ctx [:request :params "fact"])
        select-fields "cardsetcode, position, name, typename, isunique, raritycode, affiliation, factioncode, cminpoints, cmaxpoints, chealth, ccost, csides,imgsrc"
        qry-str (cond (and (nil? affil) (nil? fact))  (str "Select " select-fields " from card")
                      (and (nil? affil) (some? fact)) (str "Select " select-fields " from card where faction = \"" fact "\"")
                      (and (some? affil) (nil? fact)) (str "Select " select-fields " from card where affiliation = \"" affil "\"")
                      :else (str "Select " select-fields " from card where affiliation = \"" affil "\" and faction = \"" fact "\""))
        qry-map {:title (str affil " " fact " " "Cards")
                 :header ["Set" "Pos" "Name" "Type" "Unique" "Rarity" "Affil" "Faction" 
                          "Min<br>Points" "Max<br>Points" "Health" "Cost" "Sides" "Img Source"]
                 :query qry-str}]       
       (reports-html qry-map)))


(def report-map        {
                        "affiliation_faction_count" 
                            {:title "Count by Affiliation/Faction"
                             :header ["Affilliation" "Faction" "Count"] 
                             :query "Select affiliation, faction, count(*) as count from card group by affiliation, faction"}
                        "set_affiliation_faction_dice_count"  
                            {:title "Count by Set/Affiliation/Faction (cards with dice)"
                             :header ["Set" "Affilliation" "Faction" "Dice Count"] 
                             :query "Select cardset, affiliation, faction, count(*) as count 
                                     from card
                                     where csides IS NOT NULL
                                     group by cardset, affiliation, faction"}
                        "rarity_count" 
                            {:title "Count by Rarity"
                             :header ["Rarity" "Count"] 
                             :query "Select rarity, count(*) as count from card group by rarity"}
                        "set_count" 
                            {:title "Count by Set"
                             :header ["Set" "Count"] 
                             :query "Select cardset, count(*) as count from card group by cardset"}
                        "high_cost" 
                            {:title "Highest Cost Support/Event/Upgrade"
                             :header ["Set" "Pos" "Name" "Type" "Is Unique" "Rarity" "Cost" "Sides" "Image"] 
                             :query "Select cardsetcode, position, name, typename, isunique, raritycode, ccost, csides, imgsrc
                                     from card where ccost is not null 
                                     order by ccost desc"}
                        "legendary" 
                             {:title "Rarity Legendary Cards"
                              :header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is Unique" "Rarity" "Cost" "Sides" "Image"] 
                              :query "Select cardsetcode, position, name, typename, affiliation, factioncode, isunique, raritycode, ccost, csides, imgsrc 
                                      from card where rarity = \"Legendary\" "}
                        "odd_cost" 
                             {:title "Odd Cost Support/Event/Upgrade"
                              :header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is Unique" "Rarity" "Cost" "Sides" "Image"] 
                              :query "Select cardsetcode, position, name, typename, affiliation, factioncode, isunique, raritycode, ccost, csides, imgsrc 
                                      from card where ccost IN (1,3,5)"}
                        "rare" 
                             {:title "Rarity Rare Cards"
                              :header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is Unique" "Rarity" "Cost" "Sides" "Image"] 
                              :query "Select cardsetcode, position, name, typename, affiliation, factioncode, isunique, raritycode, ccost, csides, imgsrc 
                                      from card where rarity = \"Rare\" "}
                        "villain_command_compatible"
                              {:title "Compatible with Villains, Command"
                               :header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is Unique" "Rarity" "Cost" "Sides" "Image"] 
                               :query "Select cardsetcode, position, name, typename, affiliation, factioncode, isunique, raritycode, ccost, csides, imgsrc 
                                       from card where (affiliation = \"Villain\" or affiliation = \"Neutral\" ) 
                                       and (faction = \"Command\" or faction = \"General\") "}
                        "type_character"
                              {:title "Type Character Cards"
                               :header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is Unique" "Rarity" "MinPoints" "MaxPoints" "Health", "Sides" "Image"] 
                               :query "Select cardsetcode, position, name, typename, affiliation, factioncode, isunique, raritycode, cminpoints, cmaxpoints, chealth, csides, imgsrc 
                                       from card where typename = \"Character\" "}
                        "type_upgrade"
                               {:title "Type Upgrade Cards"
                                :header ["Set" "Pos" "Name" "Type" "Affilliation" "Faction" "Is Unique" "Rarity" "Cost" "Sides" "Image"] 
                                :query "Select cardsetcode, position, name, typename, affiliation, faction, isunique, raritycode, ccost, csides, imgsrc 
                                        from card where typename = \"Upgrade\" "}})

(def destiny-root-map {:reports (map #(hash-map :text (:title (last %)) :key (first %)) (sort-by #(:title (last %)) report-map))
                       :factions ["Command" "Force" "Rogue" "General"]})

(defn report-query [ctx] 
       (let [rpt (get-in ctx [:request :route-params :report])
             qry-map (report-map rpt)]
         (if qry-map (reports-html qry-map) "<HTML><HEAD></HEAD><BODY>Invalid report name</BODY>")))


(defresource res-cards [ctx] 
             :allowed-methods [:get :options] 
             :available-media-types ["text/html"] 
             :handle-ok cards-query)

(defresource res-destiny [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render destiny-root-template destiny-root-map))

(defresource res-reports [ctx] 
             :allowed-methods [:get :options] 
             :available-media-types ["text/html"] 
             :handle-ok report-query)

(defroutes destiny-routes  
  (ANY "/destiny" [] res-destiny)
  (ANY "/destiny/cards" [] res-cards)
  (ANY "/destiny/reports/:report" [] res-reports))
  
          
