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
        <thead><tr>
        <th scope=\"col\">Report</th><th scope=\"col\">Format</th></tr></thead>
        <tbody>
          {{#reports}}
          <tr><td>{{text}}</td><td><a href=\"/philosophy/reports/{{key}}\">HTML</a></td></tr>
          {{/reports}}
        </tbody>
      </table>
</div>
</body>
</html>")

(def philosophy-reports-template "
<!DOCTYPE html>
<html lang=\"en\">
  <head>
  <title>{{title}}</title>
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
  <h3>{{title}}</h3>
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
                                   title (:title qry-map)
                                   header (:header qry-map)
                                   results (sql/query db-spec [qry] {:as-arrays? true})
                                   output-map {:title title
                                               :header header
                                               :results (vec (map #(hash-map :result (vector-of-maps %)) (rest results)))}]
                                  
                               (render philosophy-reports-template output-map)))


(def report-map              {
                              "state_count" 
                              {:title "Philosophy Degrees Completed by State"
                               :header ["State" "Count"] 
                               :query "Select stabbr, sum(all_cnt) as count 
                                       from completion cmp 
                                       join institution ins on cmp.inst = ins.unitid
                                       group by stabbr
                                       order by sum(all_cnt) DESC"}
                              "inst_count" 
                              {:title "Philosophy Degrees Completed by Institution"
                               :header ["Institution" "Count"] 
                               :query "Select instnm, sum(all_cnt) as count 
                                       from completion cmp 
                                       join institution ins on cmp.inst = ins.unitid
                                       group by instnm
                                       order by sum(all_cnt) DESC"}
                              "cip_count" 
                              {:title "Philosophy Degrees Completed by Subject Classification"
                               :header ["CIP Code" "CIP Title" "Count"] 
                               :query "Select cipcode, ciptitle, sum(all_cnt) as count 
                                       from completion cmp 
                                       join cipcode chp on cmp.cip = chp.cipcode
                                       group by cipcode, ciptitle
                                       order by sum(all_cnt) DESC"}
                                
                              "awlevel_count"
                              {:title "Philosophy Degrees Completed by Award Level"
                               :header ["Code" "Level" "Count"] 
                               :query "Select alcode, alvalue, sum(all_cnt) 
                                       from alcode join completion
                                       on alcode.alcode = completion.awlevel
                                       group by alcode, alvalue"}
                              "u_of_w"
                              {:title "Philosophy Degrees Completed at University of Washington"
                               :header ["Institution" "Degree Level" "Count"] 
                               :query "Select instnm, alvalue, sum(all_cnt) 
                                       from completion cmp 
                                       join institution ins on cmp.inst = ins.unitid
                                       join alcode on cmp.awlevel = alcode.alcode
                                       where instnm LIKE \"University of Washington%\"
                                       and all_cnt > 0
                                       group by instnm, alvalue"}})
                              


(def philosophy-root-map {:reports (map #(hash-map :text (:title (last %)) :key (first %)) (sort-by #(:title (last %)) report-map))})


(defn report-query [ctx] 
       (let [rpt (get-in ctx [:request :route-params :report])
             qry-map (report-map rpt)]
         (if qry-map (reports-html qry-map) "<HTML><HEAD></HEAD><BODY>Invalid report name</BODY>")))

(defresource res-reports [ctx] 
             :allowed-methods [:get :options] 
             :available-media-types ["text/html"] 
             :handle-ok report-query)

(defresource res-philosophy [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (render philosophy-root-template philosophy-root-map))


(defroutes philosophy-routes  
  (ANY "/philosophy" [] res-philosophy)
  (ANY "/philosophy/reports/:report" [] res-reports))
  
  

