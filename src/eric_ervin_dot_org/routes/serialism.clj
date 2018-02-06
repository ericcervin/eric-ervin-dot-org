(ns eric-ervin-dot-org.routes.serialism
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET OPTIONS]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [doctype html5]]
            [clojure.java.jdbc :as sql]
            [eric-ervin-dot-org.representation :refer [html-style-css map-html-table-td map-html-table-tr]]))


(defn random-dodeca-row [] (vec (shuffle (range 12))))

(defn absolute-pitch-class [pc] (if (< pc 0) (+ pc 12) pc))

(defn shift-to-zero [rw]
     (let [old-row rw
           or0 (rw 0)
           new-row (map absolute-pitch-class (map #(- % or0) old-row))]
      new-row))

(defn serialism-html [mp]
  (html5  {:lang "en"} 
          [:head html-style-css] 
           
          [:body [:table
                  [:tr [:th "P0"](map map-html-table-td (map #(format "%02d" %) (:P0 mp)))]
                  [:tr [:th "R0"](map map-html-table-td (map #(format "%02d" %) (:R0 mp)))]
                  [:tr [:th "I0"](map map-html-table-td (map #(format "%02d" %) (:I0 mp)))]
                  [:tr [:th "RI0"](map map-html-table-td (map #(format "%02d" %) (:RI0 mp)))]]]))

(defn serialism-map [] (let [P0 (vec (shift-to-zero (random-dodeca-row)))
                             R0 (vec (reverse P0))
                             I0 (vec(map #(if (= % 0) 0 (- 12 %)) P0))
                             RI0 (vec(reverse I0))]
                         {:P0 P0
                          :R0 R0
                          :I0 I0
                          :RI0 RI0}))

(defresource res-serialism [ctx]
             :allowed-methods [:get :options]
             :available-media-types ["text/html"]
             :handle-ok (serialism-html (serialism-map)))

(defroutes serialism-routes
  (ANY "/serialism" [] res-serialism))


