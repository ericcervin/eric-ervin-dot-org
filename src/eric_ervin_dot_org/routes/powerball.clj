(ns eric-ervin-dot-org.routes.powerball
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]))
            ;;[eric-ervin-dot-org.representation :refer [html-style-css]]))

(def powerball-result-template "
<!DOCTYPE html>
<html lang=\"en\">
<head>
  <style>table,th,td {
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
  <table>
  <tr><th>1</th><th>2</th><th>3</th><th>4</th><th>5</th><th>pb</th></tr>
  <tr><td>{{row-1.ball-1}}</td><td>{{row-1.ball-2}}</td><td>{{row-1.ball-3}}</td><td>{{row-1.ball-4}}</td><td>{{row-1.ball-5}}</td><td>{{row-1.pb}}</td></tr>
  <tr><td>{{row-2.ball-1}}</td><td>{{row-2.ball-2}}</td><td>{{row-2.ball-3}}</td><td>{{row-2.ball-4}}</td><td>{{row-2.ball-5}}</td><td>{{row-2.pb}}</td></tr>
  </table>
</body>
</html>")


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
        white-balls (map #(format "%02d" %) white-balls)
        pb (format "%02d" (+ 1 (rand-int 26)))
        pb-r-map (zipmap [:ball-1 :ball-2 :ball-3 :ball-4 :ball-5] white-balls)
        pb-r-map (assoc pb-r-map :pb pb)]
    pb-r-map))       

(defn powerball-html [_]  
  (let [output-map {:row-1 (powerball-row-map)
                    :row-2 (powerball-row-map)}]
     (render powerball-result-template output-map)))
      

(defresource res-powerball [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render powerball-root-template))

(defresource res-powerball-result [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok powerball-html)
                                         

(defroutes powerball-routes
  
  (ANY "/powerball" [] res-powerball)
  (ANY "/powerball/html" [] res-powerball-result))
  
  
