(ns eric-ervin-dot-org.routes.gematria
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]
            [clojure.java.jdbc :as sql]))
            ;;[eric-ervin-dot-org.representation :refer [html-style-css map-html-table-td map-html-table-tr]]))


(def gematria-root-template "<!DOCTYPE html>
    <html lang=\"en\">
    <head>
    <title>Gematria</title>
    <style>table,th,td {
                 border: 1px solid black;
                 border-collapse: collapse;
                 padding: 3px;
                 text-align: center
                 }
           td {text-align: left}</style>
    </head>
    <body>
    <div id=\"header\">
    <h1>Gematria</h1>
    <p>See also:<a href=\"https://en.wikipedia.org/wiki/Gematria\">Wikipedia</a></p>
    </div>
    <div id=\"word_form\">
    <p>Calculate the numerical value of a word.</p>
    <form action=\"/gematria/search\" method=\"get\">
    <input id=\"id_word_input\" name=\"word\" type=\"text\">
    <input type=\"submit\" value=\"Calculate\"></form>
    </div>
    <div id=\"value_form\">
    <p>Search the 10,000 most common English words by numerical value.</p>
    <form action=\"/gematria/search\" method=\"get\">
    <input id=\"id_value_input\" name=\"value\" type=\"text\">
    <input type=\"submit\" value=\"Search\">
    </form>
    </div>
    </body>
    </html>")

(def gematria-word-template 
     "<!DOCTYPE html>
      <html lang=\"en\">
      <head>
          <title>Gematria</title>
          <style>table,th,td {
                 border: 1px solid black;
                 border-collapse: collapse;
                 padding: 3px;
                 text-align: center
          }
                 td {text-align: left}
      </style></head>
      <body>
      <div id=\"header\">
      <table id = 'id_word_value_table'>
      <tr>{{#wrd-vec}}<th>{{.}}</th>{{/wrd-vec}}</tr>
      <tr>{{#wrd-result}}<td>{{.}}</td>{{/wrd-result}}</tr></table>
  </div>
  <br><br>
  <div id=\"etc\">
  <p>Others with same value</p>
  <br>
  <table id = 'id_other_word_table'>
  <tr><th>Word</th><th>Value</th></tr>
   {{#other-results}}
   <tr>{{#result}}<td>{{.}}</td>{{/result}}</tr>
   {{/other-results}}
  </table>
  </div>
  </body>
  </html>")

(def gematria-value-template "
     <!DOCTYPE html>
     <html lang=\"en\">
     <head>
     <title>Gematria</title>
     <style>table,th,td {
                 border: 1px solid black;
                 border-collapse: collapse;
                 padding: 3px;
                 text-align: center
                 }              
                 td {text-align: left}
                 
      </style>
      </head>
      <body>
      <table id = 'id_word_value_table'>
      <tr>
      <th>Word</th><th>Value</th></tr>
      {{#results}}
      <tr>{{#result}}<td>{{.}}</td>{{/result}}</tr>
      {{/results}}
      </table>
      </body>
      </html>")

(defn calculate-word-value [wrd]
  (let [word wrd
        lc-word (clojure.string/lower-case wrd)
        values (map #(- (int %) 96) lc-word)
        value-pairs (map #(vec [% (- (int %) 96)]) lc-word)
        total-value (apply + values)]
     {:word word
      :values (vec values)
      :value-pairs (vec value-pairs)
      :totalvalue total-value}))


(defn query-table [query] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/gematria.db"}
                                qry query
                                results (sql/query db-spec [qry] {:as-arrays? true})
                                result-rows (map #(hash-map :result %) (rest results))]
                            
                            result-rows))


(defn calculate-word-html [wrd]
  (let [wrd-map (calculate-word-value wrd)
        word-result (conj (:values wrd-map) (:totalvalue wrd-map))
        query (str "Select word, wordvalue from gematria where wordvalue = \"" (:totalvalue wrd-map) "\" order by word")
        other-results (query-table query) 
        output-map {:wrd-vec (conj (vec wrd) "total")
                    :wrd-result word-result
                    :other-results other-results}]
    (render gematria-word-template output-map))) 
    

(defn find-by-value-html [vl]
  (let [query (str "Select word, wordvalue from gematria where wordvalue = \"" vl "\" order by word")
        results (query-table query)
        output-map {:results results}] 
    (render gematria-value-template output-map)))




(defresource res-search [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] 
                           (let [params (get-in ctx [:request :params])]
                             (cond 
                               (contains? params "word") (calculate-word-html (params "word"))
                               (contains? params "value") (find-by-value-html (params "value"))))))
                               


(defresource res-gematria [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render gematria-root-template))

(defroutes gematria-routes  
  (ANY "/gematria" [] res-gematria)
  (ANY "/gematria/search" [] res-search))

  
  
