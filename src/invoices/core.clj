(ns invoices.core
  (:require [invoice-items :as ii])
  (:use clojure.pprint)
  (:gen-class))

(def invoice (clojure.edn/read-string (slurp "invoice.edn")))

(defn -main
  "Demo features"
  [& args]
  (do (println "Invoice items that satisfy given conditions:\n")
      (pprint (ii/find-invoice-items (:invoice/items invoice)))))
