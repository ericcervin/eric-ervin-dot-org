(ns eric-ervin-dot-org.routes.powerball
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [eric-ervin-dot-org.representation :refer [html-style-css]]))


(defn powerball-row-map []
  (let [white-balls (vec (take 5 (shuffle (range 1 70))))
        pb (+ 1 (rand-int 26))
        pb-r-map (zipmap [:ball-1 :ball-2 :ball-3 :ball-4 :ball-5] white-balls)
        pb-r-map (assoc pb-r-map :pb pb)]
    pb-r-map))       

(defn powerball-row-html []
  (let [pbrm (powerball-row-map)] 
    (html [:tr 
           [:td (format "%02d"  (:ball-1 pbrm))] 
           [:td (format "%02d"  (:ball-2 pbrm))]
           [:td (format "%02d"  (:ball-3 pbrm))]
           [:td (format "%02d"  (:ball-4 pbrm))]
           [:td (format "%02d"  (:ball-5 pbrm))]
           [:td (format "%02d"  (:pb pbrm))]])))
          

(defn powerball-html [_]  
  (html html-style-css        
    [:table  [:tr [:th "1"][:th "2"][:th "3"][:th "4"][:th "5"][:th "pb"]]
             (powerball-row-html)
             (powerball-row-html)]))

(defn powerball-json [_]  
     {:set-1 (powerball-row-map)
      :set-2 (powerball-row-map)})

(defn powerball-text [_]  
  (let [r1 (powerball-row-map)
        r2 (powerball-row-map)]
    
    (str (:ball-1 r1) "\t" (:ball-2 r1) "\t" (:ball-3 r1) "\t" (:ball-4 r1) "\t" (:ball-5 r1) "\t(" (:pb r1) ")\n"
         (:ball-1 r2) "\t" (:ball-2 r2) "\t" (:ball-3 r2) "\t" (:ball-4 r2) "\t" (:ball-5 r2) "\t(" (:pb r2) ")\n")))
      

(defresource res-powerball [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] (html html-style-css
                                        [:div {:id "header"}
                                         [:h3 "Powerball"]
                                         [:p "Two sets of Powerball numbers"]]
                                        [:div {:id "numbers"}
                                         [:table
                                          [:thead
                                           [:th {:scope "col"} "Numbers"]]
                                          [:tbody
                                           [:tr [:td [:a {:href "/powerball/html"} "HTML"]]]
                                           [:tr [:td [:a {:href "/powerball/json"} "JSON"]]]
                                           [:tr [:td [:a {:href "/powerball/text"} "TEXT"]]]]]])))

(defresource res-powerball-html [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok powerball-html)

(defresource res-powerball-json [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["application/json"]
             :handle-ok powerball-json)

(defresource res-powerball-text [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/plain"]
             :handle-ok powerball-text)

                                         

(defroutes powerball-routes
  
  (ANY "/powerball" [] res-powerball)
  (ANY "/powerball/html" [] res-powerball-html)
  (ANY "/powerball/json" [] res-powerball-json)
  (ANY "/powerball/text" [] res-powerball-text))
  
