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
          <p>Rows and rows of pitch classes</p>
        </div>
        <div id=\"pcs\">
          <table>
            <thead>
              <tr><th scope=\"col\">Basic Rows</th><th scope=\"col\">Square</th></tr>
            </thead>
            <tbody>
              <tr><td><a href=\"/serialism/rows/html\">HTML</a></td>
                  <td><a href=\"/serialism/square/html\">HTML</a></td>
              </tr>
            </tbody>
          </table>
        </div>
      </body>
    </html>")


(def serialism-rows-result-template "
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
      <p>(t = ten. e = eleven.)</p>
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

(def serialism-square-result-template "
  <!DOCTYPE html>
  <html lang=\"en\">
    <head>  
      <style> table,th,td {
         border: 1px solid black;
         border-collapse: collapse;
         padding: 3px;
         text-align: center;}
         td {text-align: left;
             width: 20px}
      </style>
      <title>Serialism</title>
    </head>
    <body>
      <p>(t = ten. e = eleven.)</p>
      <table>
        <tbody>
          {{#sq}}
          <tr>
          {{#vl}}
          <td>{{.}}</td>
          {{/vl}}
          </tr>
          {{/sq}}
        </tbody>
      </table>
    </body>
  </html>")


(defn random-dodeca-row [] (vec (shuffle (range 12))))

(defn absolute-pitch-class [pc] (cond (< pc 0) (+ pc 12)
                                      (> pc 12) (- pc 12)
                                      (= pc 12) 0
                                      :else pc))

(defn transpose [n i] (absolute-pitch-class (+ n i)))

(defn transpose-row [rw i] (map #(transpose % i) rw))

(defn shift-to-zero [rw]
     (let [old-row rw
           or0 (rw 0)
           shft (* or0 -1)
           new-row (transpose-row old-row shft)]
          
      new-row))



(defn add_e_and_t [n] (condp = n 10 "t" 11 "e" n))

(defn serial-square [rw] (for [i rw]
                           (hash-map :vl (map add_e_and_t (transpose-row rw (- 12 i))))))

(defn serialism-map [] (let [P0 (vec (shift-to-zero (random-dodeca-row)))
                             R0 (vec (reverse P0))
                             I0 (vec(map #(if (= % 0) 0 (- 12 %)) P0))
                             RI0 (vec(reverse I0))]
                            {:p0 (map add_e_and_t P0)
                             :r0 (map add_e_and_t R0)
                             :i0 (map add_e_and_t I0)
                             :ri0 (map add_e_and_t RI0)
                             :sq (serial-square P0)}))

(defresource res-serialism [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render serialism-root-template))

(defresource res-serialism-rows-html [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render serialism-rows-result-template (serialism-map)))

(defresource res-serialism-square-html [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render serialism-square-result-template (serialism-map)))

(defroutes serialism-routes
  (ANY "/serialism" [] res-serialism)
  (ANY "/serialism/rows/html" [] res-serialism-rows-html)
  (ANY "/serialism/square/html" [] res-serialism-square-html))
  


