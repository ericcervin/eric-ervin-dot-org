(ns eric-ervin-dot-org.representation
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [doctype html5]]
            [clojure.java.jdbc :as sql]))

(def html-style-css [:style "table,th,td {
                               border: 1px solid black;
                               border-collapse: collapse;
                               padding: 3px;
                               text-align: center
                               }
                             td {text-align: left}"])

(defn map-html-table-td [cl]
  (if (some? cl)
      (if (clojure.string/includes? cl "http") (html [:td [:a {:href cl} cl]]) (html [:td cl]))
      (html [:td ""])))    

(defn map-html-table-tr [mp]  (html [:tr (map map-html-table-td mp)]))
            
