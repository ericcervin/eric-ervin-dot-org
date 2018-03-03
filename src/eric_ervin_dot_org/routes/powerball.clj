(ns eric-ervin-dot-org.routes.powerball
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [doctype html5]]
            [eric-ervin-dot-org.representation :refer [html-style-css]]))

(def powerball-root-template "
<!DOCTYPE html>
  <html lang=\"en\">
  <head>
  <style>
  table,th,td {
        border: 1px solid black;
        border-collapse: collapse;
        padding: 3px;
        text-align: center
        }
  td {text-align: left}
</style>
<title>Powerball</title>
</head>
<body>
<div id=\"header\">
  <h3>Powerball</h3>
  <p>Two sets of Powerball numbers</p>
</div>
<div id=\"numbers\">
  <table>
    <thead>
      <tr><th scope=\"col\">Numbers</th></tr>
    </thead>
    <tbody>
      <tr><td><a href=\"/powerball/html\">HTML</a></td></tr>
    </tbody>
  </table>
</div>
</body>
</html>")

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
  (html5  {:lang "en"}
          [:head html-style-css] 
           
          [:body        
           [:table  [:tr [:th "1"][:th "2"][:th "3"][:th "4"][:th "5"][:th "pb"]]
            (powerball-row-html)
            (powerball-row-html)]]))
      

(defresource res-powerball [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render powerball-root-template))

(defresource res-powerball-html [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok powerball-html)
                                         

(defroutes powerball-routes
  
  (ANY "/powerball" [] res-powerball)
  (ANY "/powerball/html" [] res-powerball-html))
  
  
