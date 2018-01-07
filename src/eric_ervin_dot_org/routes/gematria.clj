(ns eric-ervin-dot-org.routes.gematria
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]))

(def html-style-css [:style "table,th, td {
                                          border: 1px solid black;
                                          border-collapse: collapse;
                                          padding: 3px;
                                          text-align: center;
                                         }
                             td {text-align: left;}
                     "])




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
  
(defn calculate-word-html [wrd]
  (let [wrd-map (calculate-word-value wrd)
        html-header [:tr (map #(html [:th %]) (conj (vec wrd) "total"))]
        html-result [:tr (map #(html [:td %]) (conj (:values wrd-map) (:totalvalue wrd-map)))]] 
  
      (html html-style-css [:table html-header html-result])))



(defresource res-word [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] 
                          (let [wrd (get-in ctx [:request :params "word"])]
                           (calculate-word-html wrd))))



(defresource res-gematria [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] (html html-style-css
                                        [:div {:id "header"}
                                         [:h2 "Gematria"]
                                         [:p "Calculate the numerical value of a word."]
                                         [:p "See also:"[:a {:href "https://en.wikipedia.org/wiki/Gematria"} "Wikipedia"]]]
                                        [:div {:id "form"}
                                          [:form {:action "/gematria/word" :method "get"}
                                           [:input {:type "text" :name "word"}]
                                           [:input {:type "submit" :value "Calculate"}]]]))) 
                                          
                                         

(defroutes gematria-routes
  
  (ANY "/gematria" [] res-gematria)
  (ANY "/gematria/word" [] res-word))

  
  
