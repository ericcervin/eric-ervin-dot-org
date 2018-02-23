(ns eric-ervin-dot-org.routes.gematria
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [doctype html5]]
            [cljstache.core :refer [render]]
            [clojure.java.jdbc :as sql]
            [eric-ervin-dot-org.representation :refer [html-style-css map-html-table-td map-html-table-tr]]))


(def gematria-root-template "<!DOCTYPE html>
    <html lang=\"en\"><head><style>table,th,td {}
                               border: 1px solid black;
                               border-collapse: collapse;
                               padding: 3px;
                               text-align: center
                               
                             td {text-align: left}</style>
    </head>
    <body>
    <div id=\"header\">
    <h2>Gematria</h2>
    <p>See also:<a href=\"https://en.wikipedia.org/wiki/Gematria\">Wikipedia</a></p>
    </div>
    <div id=\"word_form\">
    <p>Calculate the numerical value of a word.</p>
    <form action=\"/gematria/word\" method=\"get\">
    <input name=\"word\" type=\"text\">
    <input type=\"submit\" value=\"Calculate\"></form>
    </div>
    <div id=\"value_form\">
    <p>Search the 10,000 most common English words by numerical value.</p>
    <form action=\"/gematria/value\" method=\"get\">
    <input name=\"value\" type=\"text\">
    <input type=\"submit\" value=\"Search\">
    </form>
    </div>
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

(defn query-table [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/gematria.db"}
                                  qry (:query qry-map)
                                  header (:header qry-map)
                                  results (sql/query db-spec [qry] {:as-arrays? true})
                                  report-rows (map map-html-table-tr (rest results))]
                                 (html
                                   [:table 
                                    [:tr (map #(html [:th %]) header)]
                                    report-rows])))

(defn calculate-word-html [wrd]
  (let [wrd-map (calculate-word-value wrd)
        html-header [:tr (map #(html [:th %]) (conj (vec wrd) "total"))]
        html-result [:tr (map #(html [:td %]) (conj (:values wrd-map) (:totalvalue wrd-map)))]] 
    (html5  {:lang "en"}
            [:head html-style-css] 
            [:body [:div {:id "header"}
                      [:table html-header html-result]]
                   [:br][:br]
                   [:div {:id "etc"}
                    [:p "Others with same value"][:br]
                    (query-table {:header ["Word" "Value"] 
                                  :query (str "Select word, wordvalue 
                                           from gematria 
                                           where wordvalue = \"" (:totalvalue wrd-map) "\"
                                           order by word")})]])))

(defresource res-value [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] 
                          (let [val (get-in ctx [:request :params "value"])]
                           (html5  {:lang "en"}
                             [:head html-style-css]
                             [:body
                              (query-table {:header ["Word" "Value"] 
                                            :query (str "Select word, wordvalue 
                                                     from gematria 
                                                     where wordvalue = \"" val "\"
                                                     order by word")})]))))
                                        

(defresource res-word [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] 
                          (let [wrd (get-in ctx [:request :params "word"])]
                           (calculate-word-html wrd))))

(defresource res-gematria [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render gematria-root-template)) 
                                          
(defroutes gematria-routes  
  (ANY "/gematria" [] res-gematria)
  (ANY "/gematria/word" [] res-word)
  (ANY "/gematria/value" [] res-value))

  
  
