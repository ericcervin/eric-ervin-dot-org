(ns eric-ervin-dot-org.handler
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]
            [eric-ervin-dot-org.routes.powerball :refer [powerball-routes]]
            [eric-ervin-dot-org.routes.discogs   :refer [discogs-routes]]
            [eric-ervin-dot-org.routes.destiny   :refer [destiny-routes]]
            [eric-ervin-dot-org.routes.philosophy-usa   :refer [philosophy-routes]]))

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
            
                     
(defn serialism_rows [_] (let [P0 (vec (shuffle (range 12)))
                               R0 (vec (reverse P0))
                               I0 (vec(map #(if (= % 0) 0 (- 12 %)) P0))
                               RI0 (vec(reverse I0))]
                           {:P0 P0
                            :R0 R0
                            :I0 I0
                            :RI0 RI0}))

(defresource echo_context [ctx]
                           
             :allowed-methods [:get :options]
             :available-media-types ["text/plain"]
             :handle-ok (fn [ctx] (str ctx)))

(defresource echo_request [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/plain"]
             :handle-ok (fn [ctx] (:request ctx)))


(defresource root [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (fn [ctx] (html html-style-css
                                      [:div {:id "header"}
                                         [:h1 "Eric Ervin Dot Org"]
                                         [:p "A toy website to release some Clojure into the world."]
                                         [:p "Though it's a toy, these are resources I use. (Except that I've quit playing Powerball)"]
                                         [:p [:a {:href "https://github.com/ericcervin/eric-ervin-dot-org"} "https://github.com/ericcervin/eric-ervin-dot-org"]][:br]]
                                       
                                      
                                      [:div {:id "resources"}
                                        [:table
                                          [:thead 
                                           [:tr   [:th {:scope "col"} "Resource"] [:th {:scope "col"}"Description"][:th {:scope "col"} "Data Updated"]]]                                  
                                          [:tbody
                                           [:tr   [:td [:a {:href "/destiny"} "Destiny"]][:td "Star Wars Destiny card game data"][:td "12/22/2017"]]
                                           [:tr   [:td [:a {:href "/discogs"} "Discogs"][:td "Albums I've cataloged"][:td "12/16/2017"]]]
                                           [:tr   [:td [:a {:href "/philosophy"} "Philosophy"][:td "Philosophy degrees completed during the 2014-2015 academic year."][:td "12/23/2017"]]] ;]]])))
                                           [:tr   [:td [:a {:href "/powerball"} "Powerball"]][:td " A source for Powerball numbers to play"][:td "N/A"]]]]])))
                                    
                                         
(defresource serialism [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/plain"]
             :handle-ok serialism_rows)





(defroutes app-routes
  (ANY "/" [] root)
  
  (ANY "/echo_context" [] echo_context)
  
  (ANY "/echo_request" [] echo_request)
  
  powerball-routes
  
  (ANY "/serialism" [] serialism)
  
  destiny-routes
  
  discogs-routes
  
  philosophy-routes)

(def app
  (wrap-params app-routes))
