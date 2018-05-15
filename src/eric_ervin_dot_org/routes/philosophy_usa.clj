(ns eric-ervin-dot-org.routes.philosophy-usa
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [cljstache.core :refer [render]]
            [clojure.java.jdbc :as sql]))
            ;;[eric-ervin-dot-org.representation :refer [html-style-css map-html-table-td map-html-table-tr]]))


(def philosophy-root-template "
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
  <title>Philosophy USA</title>
</head>
<body>
<div id=\"header\">
  <h1>Philosophy USA</h1>
  <p>Philosophy and religious studies degrees completed during the 2014-2015 academic year.</p>
  <p>Data taken from the Integrated Postsecondary Education Data System (IPEDS)</p>
  <p><a href=\"https://nces.ed.gov/ipeds/Home/UseTheData\">https://nces.ed.gov/ipeds/Home/UseTheData</a></p>
</div>
<div id=\"reports\">
  <h4>Reports</h4>
  <table>
    <thead>
    <tr>
    <th scope=\"col\">Report</th>
    <th scope=\"col\">Format</th>
    </tr>
    </thead>
    <tbody>
    <tr><td>Philosophy Degrees Completed by Award Level</td><td><a href=\"/philosophy/reports?rpt=awlevel_count\">HTML</a></td></tr>
    <tr><td>Philosophy Degrees Completed by Institution</td><td><a href=\"/philosophy/reports?rpt=inst_count\">HTML</a></td></tr>
    <tr><td>Philosophy Degrees Completed by State</td><td><a href=\"/philosophy/reports?rpt=state_count\">HTML</a></td></tr>
    <tr><td>Philosophy Degrees Completed by Subject Classification</td><td><a href=\"/philosophy/reports?rpt=cip_count\">HTML</a></td></tr>
    </tbody>
  </table>
</div>
</body>
</html>")

(def philosophy-reports-template "
<!DOCTYPE html>
<html lang=\"en\">
  <head>
  <title>Philosophy USA</title>
  <style>table,th,td {
               border: 1px solid black;
               border-collapse: collapse;
               padding: 3px;
               text-align: center
  }
               td {text-align: left}
  </style>

  </head>
  <body>
  <table id = 'id_result_table'>
    <thead>
      <tr>{{#header}}<th>{{{.}}}</th>{{/header}}</tr>
    </thead>
    <tbody>
      {{#results}}
      <tr>{{#result}}<td>{{{vl}}}</td>{{/result}}</tr>
      {{/results}}
    </tbody>
  </table>
</body>
</html>")


(defn vector-of-maps [x] (vec (map #(hash-map :vl %) x)))

(defn reports-html [qry-map] (let [db-spec {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "resources/philosophy-usa.db"}
                                   qry (:query qry-map)
                                   header (:header qry-map)
                                   results (sql/query db-spec [qry] {:as-arrays? true})
                                   output-map {:header header
                                               :results (vec (map #(hash-map :result (vector-of-maps %)) (rest results)))}]
                               (render philosophy-reports-template output-map)))



(defn report-query [ctx] 
       (if-let [qry-map (condp = (get-in ctx [:request :params "rpt"])  
                              "state_count" 
                              {:header ["State" "Count"] 
                               :query "Select stabbr, count(*) as count 
                                       from completion cmp 
                                       join institution ins on cmp.inst = ins.unitid
                                       group by stabbr
                                       order by count(*) DESC"}
                              "inst_count" 
                              {:header ["Institution" "Count"] 
                               :query "Select instnm, count(*) as count 
                                       from completion cmp 
                                       join institution ins on cmp.inst = ins.unitid
                                       group by instnm
                                       order by count(*) DESC"}
                              "cip_count" 
                              {:header ["CIP Code" "CIP Title" "Count"] 
                               :query "Select cipcode, ciptitle, count(*) as count 
                                       from completion cmp 
                                       join cipcode chp on cmp.cip = chp.cipcode
                                       group by cipcode, ciptitle
                                       order by count(*) DESC"}
                                
                              "awlevel_count"
                              {:header ["Code" "Level" "Count"] 
                               :query "Select alcode, alvalue, Count(*) 
                                       from alcode join completion
                                       on alcode.alcode = completion.awlevel
                                       group by alcode, alvalue"})]
         
         
         (reports-html qry-map)))

(defresource res-reports [ctx] 
             :allowed-methods [:get :options] 
             :available-media-types ["text/html"] 
             :handle-ok report-query)

(defresource res-philosophy [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render philosophy-root-template))


(defroutes philosophy-routes  
  (ANY "/philosophy" [] res-philosophy)
  (ANY "/philosophy/reports" [] res-reports))
  
  

