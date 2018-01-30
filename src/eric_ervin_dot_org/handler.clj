(ns eric-ervin-dot-org.handler
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [clojure.java.jdbc :as sql]
            [eric-ervin-dot-org.representation :refer [html-style-css]]
            [eric-ervin-dot-org.routes.discogs   :refer [discogs-routes]]
            [eric-ervin-dot-org.routes.destiny   :refer [destiny-routes]]
            [eric-ervin-dot-org.routes.gematria   :refer [gematria-routes]]
            [eric-ervin-dot-org.routes.philosophy-usa   :refer [philosophy-routes]]
            [eric-ervin-dot-org.routes.powerball :refer [powerball-routes]]
            [eric-ervin-dot-org.routes.serialism :refer [serialism-routes]]))

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
                                           [:tr   [:td [:a {:href "/destiny"} "Destiny"]][:td "Star Wars Destiny card game data"][:td "01/08/2018"]]
                                           [:tr   [:td [:a {:href "/discogs"} "Discogs"][:td "Albums I've cataloged"][:td "12/16/2017"]]]
                                           [:tr   [:td [:a {:href "/gematria"} "Gematria"]][:td "The numerical value of words"][:td "N/A"]]
                                           [:tr   [:td [:a {:href "/philosophy"} "Philosophy"][:td "Philosophy degrees completed during the 2014-2015 academic year"][:td "12/23/2017"]]] ;]]])))
                                           [:tr   [:td [:a {:href "/powerball"} "Powerball"]][:td " A source for Powerball numbers to play"][:td "N/A"]]
                                           ]]])))
(defroutes app-routes
  (ANY "/" [] root)
  
  (ANY "/echo_context" [] echo_context)
  
  (ANY "/echo_request" [] echo_request)
  
  destiny-routes
  
  discogs-routes
  
  gematria-routes
  
  philosophy-routes
  
  powerball-routes
  
  serialism-routes)
  
(def app
  (wrap-params app-routes))
