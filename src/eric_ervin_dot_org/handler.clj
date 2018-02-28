(ns eric-ervin-dot-org.handler
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]
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


(def resource-list {:resources [{:name "Destiny"    :path "/destiny"    :last-updated "02/28/2018" :desc "Star Wars Destiny card game data"}
                                {:name "Discogs"    :path "/discogs"    :last-updated "12/16/2017" :desc "Albums I've cataloged"}
                                {:name "Gematria"   :path "/gematria"   :last-updated "N/A" :desc "The numerical value of words"}
                                {:name "Philosophy" :path "/philosophy" :last-updated "12/23/2017" :desc "Philosophy degrees completed during the 2014-2015 academic year"}
                                {:name "Powerball"  :path "/powerball"  :last-updated "N/A" :desc "A source for Powerball numbers to play"}
                                {:name "Serialism"  :path "/serialism"  :last-updated "N/A" :desc "Toying with set theory and dodecaphony"}]})
                     

(def root-string
  
  "
  <!DOCTYPE html>
  <html lang=\"en\">
  <head>
  <title>Eric Ervin Dot Org</title>
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
  <h1>Eric Ervin Dot Org</h1>
  <p>A toy website to release some Clojure into the world.</p>
  <p>Though it's a toy, these are resources I use. (Except that I've quit playing Powerball)</p>
  <p><a href=\"https://github.com/ericcervin/eric-ervin-dot-org\">https://github.com/ericcervin/eric-ervin-dot-org</a></p>
  <br>
  </div>
  <div id=\"resources\">
  <table>
  <thead><tr><th scope=\"col\">Resource</th><th scope=\"col\">Description</th><th scope=\"col\">Data Updated</th></tr></thead>
  <tbody>
  {{#resources}}
  <tr><td><a href=\"{{path}}\">{{name}}</a></td><td>{{desc}}</td><td>{{last-updated}}</td></tr>
  {{/resources}}
  </tbody>
  </table>
  </div>
  </body>
  </html>
  ")

(defresource root [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render root-string resource-list)
             :handle-not-found "oops")

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
