(ns eric-ervin-dot-org.routes.serialism
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]
            [clojure.java.jdbc :as sql]))
            ;;[eric-ervin-dot-org.representation :refer [html-style-css map-html-table-td map-html-table-tr]]))


(def serialism-root-template "
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
        <title>Serialism</title>
      </head>
      <body>
        <div id=\"header\">
          <h1>Serialism</h1>
          <p>Rows and rows of numbers</p>
        </div>
        <div id=\"numbers\">
          <table>
            <thead>
              <tr><th scope=\"col\">Numbers</th></tr>
            </thead>
            <tbody>
              <tr><td><a href=\"/serialism/html\">HTML</a></td></tr>
            </tbody>
          </table>
        </div>
      </body>
    </html>")


(def serialism-result-template "
  <!DOCTYPE html>
  <html lang=\"en\">
    <head>  
      <style> table,th,td {
         border: 1px solid black;
         border-collapse: collapse;
         padding: 3px;
         text-align: center;}
         td {text-align: left}
      </style>
      <title>Serialism</title>
    </head>
    <body>
      <table>
        <tbody>
          <tr><th>P0</th>{{#p0}}<td>{{.}}</td>{{/p0}}</tr>
          <tr><th>R0</th>{{#r0}}<td>{{.}}</td>{{/r0}}</tr>
          <tr><th>I0</th>{{#i0}}<td>{{.}}</td>{{/i0}}</tr>
          <tr><th>RI0</th>{{#ri0}}<td>{{.}}</td>{{/ri0}}</tr>
        </tbody>
      </table>
    </body>
  </html>")

(defn random-dodeca-row [] (vec (shuffle (range 12))))

(defn absolute-pitch-class [pc] (if (< pc 0) (+ pc 12) pc))

(defn shift-to-zero [rw]
     (let [old-row rw
           or0 (rw 0)
           new-row (map absolute-pitch-class (map #(- % or0) old-row))]
      new-row))


(defn serialism-map [] (let [P0 (vec (shift-to-zero (random-dodeca-row)))
                             R0 (vec (reverse P0))
                             I0 (vec(map #(if (= % 0) 0 (- 12 %)) P0))
                             RI0 (vec(reverse I0))]
                         {:p0 (map #(format "%02d" %) P0)
                          :r0 (map #(format "%02d" %) R0)
                          :i0 (map #(format "%02d" %) I0)
                          :ri0 (map #(format "%02d" %)RI0)}))

(defresource res-serialism [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render serialism-root-template))

(defresource res-serialism-html [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render serialism-result-template (serialism-map)))

(defroutes serialism-routes
  (ANY "/serialism" [] res-serialism)
  (ANY "/serialism/html" [] res-serialism-html))


