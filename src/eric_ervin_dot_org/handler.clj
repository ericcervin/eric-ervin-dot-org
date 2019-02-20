(ns eric-ervin-dot-org.handler
  (:require [liberator.core :refer [resource defresource]]
            [liberator.representation :refer [ring-response]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]
            [clj-time.core :as t]
            [eric-ervin-dot-org.routes.discogs   :refer [discogs-routes]]
            [eric-ervin-dot-org.routes.destiny   :refer [destiny-routes]]
            [eric-ervin-dot-org.routes.gematria   :refer [gematria-routes]]
            [eric-ervin-dot-org.routes.philosophy-usa   :refer [philosophy-routes]]
            [eric-ervin-dot-org.routes.powerball :refer [powerball-routes]]
            [eric-ervin-dot-org.routes.serialism :refer [serialism-routes]]
            [eric-ervin-dot-org.routes.wh-champions :refer [wh-champions-routes]]))
            

(defn wrap-log-request [handler]
  (fn [req]
    (let [log-string (str (:uri req) "\t" (:query-string req) "\t" (:request-method req) "\t" (clj-time.core/now) " UTC")]
     (println log-string)
     (handler req))))


(defresource echo_context [ctx]                           
             :allowed-methods [:get :options]
             :available-media-types ["text/plain"]
             :handle-ok (fn [ctx] (str ctx)))

(defresource echo_request [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/plain"]
             :handle-ok (fn [ctx] (:request ctx)))

(defresource echo_route_param [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/plain"]
             :handle-ok (fn [ctx] (get-in ctx [:request :route-params :etc])))


(def resource-list {:resources [{:name "Destiny"    :path "/destiny"    :last-updated "02/20/2019" :desc "Star Wars Destiny card game data"}
                                {:name "Discogs"    :path "/discogs"    :last-updated "12/23/2018" :desc "Albums I've cataloged"}
                                {:name "Gematria"   :path "/gematria"   :last-updated "N/A" :desc "The numerical value of words"}
                                {:name "Philosophy" :path "/philosophy" :last-updated "12/27/2018" :desc "Philosophy degrees completed during the 2014-2015 academic year"}
                                {:name "Powerball"  :path "/powerball"  :last-updated "N/A" :desc "A source for Powerball numbers to play"}
                                {:name "Serialism"  :path "/serialism"  :last-updated "N/A" :desc "Toying with set theory"}
                                {:name "Warhammer Champions"  :path "/wh_champions"  :last-updated "12/06/2018" :desc "Warhammer Age of Sigmar: Champions card game data"}]})
                     

(def root-template
  
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
  <p>A toy website to release some Clojure into the world..</p>
  <p>Though it's a toy, these are resources I use. (Except that I've quit playing Powerball)</p>
  <p><a href=\"https://github.com/ericcervin/eric-ervin-dot-org\">https://github.com/ericcervin/eric-ervin-dot-org</a></p>
  <p>There is a (twin) sister site written in Python/Flask: <a href = \"http://ericervin.com\">http://ericervin.com</a>
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

(def not-found-template "<!DOCTYPE html><html lang=\"en\"><head><title>Error 404 Not Found</title></head><body>404 - Not Found</body></html>")

(defresource root [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render root-template resource-list))

(defresource not-found [ctx]
      :available-media-types ["text/html"]
      :handle-ok (fn [ctx] (ring-response {:status 404 :body (render not-found-template)})))

(defresource robots [ctx]
             :available-media-types ["text/plain"]
             :handle-ok (str "User-agent: *\nDisallow: /\n"))

(defroutes app-routes
  (ANY "/" [] root)
  
  (ANY "/echo_context" [] echo_context)
  
  (ANY "/echo_request/" [] echo_request)
  
  (ANY "/echo_route_param/:etc" [] echo_route_param)
  
  (GET "/robots.txt" [] robots)
  
  destiny-routes
  
  discogs-routes
  
  gematria-routes
  
  philosophy-routes
  
  powerball-routes
  
  serialism-routes
  
  wh-champions-routes
  
  (ANY "*" [_] not-found))
  
  
(def app
  (wrap-log-request (wrap-params app-routes)))
