(ns eric-ervin-dot-org.routes.wh-champions
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]
            [clojure.java.jdbc :as sql]))


(def wh-champions-root-template "<!DOCTYPE html>
<html lang=\"en\">
  <head>
    <title>Warhammer Champions</title>
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
      <h1>Warhammer Champions</h1>
      <br>
    </div>
    <div id=\"cards\">
      <h4>Cards</h4>
      <table>
        <thead>
          <tr>
            <th></th>
            <th></th>
            <th colspan=\"6\">Alliance</th>
          </tr>
          <tr>
            <th></th>
            <th></th>
            <th scope=\"col\">All</th>
            <th scope=\"col\">Any</th>
            <th scope=\"col\">Chaos</th>
            <th scope=\"col\">Death</th>
            <th scope=\"col\">Destruction</th>
            <th scope=\"col\">Order</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <th rowspan=\"6\">Type</th>
            <th scope=\"row\">All</th>
            <td><a href=\"/wh_champions/cards?\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Any\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Chaos\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Death\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Destruction\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Order\">HTML</a></td>
          </tr>
          {{#types}}
          <tr>
            <th scope=\"row\">{{.}}</th>
            <td><a href=\"/wh_champions/cards?type={{.}}\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Any&amp;type={{.}}\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Chaos&amp;type={{.}}\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Death&amp;type={{.}}\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Destruction&amp;type={{.}}\">HTML</a></td>
            <td><a href=\"/wh_champions/cards?ally=Order&amp;type={{.}}\">HTML</a></td>
          </tr>
          {{/types}}
        </tbody>
      </table>
    </div>
  </body>
 </html>")

(def wh-champions-report-template "<!DOCTYPE html>
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

(defn reports-html [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/wh_champions.db"}
                                   qry (:query qry-map)
                                   header (:header qry-map)
                                   title (:title qry-map)
                                   results (sql/query db-spec [qry] {:as-arrays? true})
                                   results (rest results)
                                   output-map {:title title
                                               :header header
                                               :results (vec (map #(hash-map :result (vector-of-maps %)) results))}]
                               
                               (render wh-champions-report-template output-map)))

(defn cards-query [ctx] 
  (let [ally (get-in ctx [:request :params "ally"])
        type (get-in ctx [:request :params "type"])
        select-fields "setName, cardNumber, alliance, category,class, name, rarity"
        order "order by setNum, cardNumber"
        qry-str (cond (and (nil? ally) (nil? type))  (str "Select " select-fields " from card " order)
                      (and (nil? ally) (some? type)) (str "Select " select-fields " from card where category = \"" type "\" " order)
                      (and (some? ally) (nil? type)) (str "Select " select-fields " from card where alliance = \"" ally "\" " order)
                      :else (str "Select " select-fields " from card where alliance = \"" ally "\" and category = \"" type "\" " order))
        qry-map {:title (str ally " " type " " "Cards")
                 :header ["Set" "Number", "Alliance", "Type","Class", "Name", "Rarity"]
                 :query qry-str}]       
       (reports-html qry-map)))


(def wh-champions-root-map {:types ["Unit" "Champion" "Spell" "Blessing" "Ability"]})

(defresource res-cards [ctx] 
             :allowed-methods [:get :options] 
             :available-media-types ["text/html"] 
             :handle-ok cards-query)


(defresource res-wh-champions [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render wh-champions-root-template wh-champions-root-map))


(defroutes wh-champions-routes  
  (ANY "/wh_champions" [] res-wh-champions)
  (ANY "/wh_champions/cards" [] res-cards))

