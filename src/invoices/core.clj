(ns invoices.core
  (:require [invoice-items :as ii]
            [clojure.data.json :as json])
  (:use clojure.pprint)
  (:gen-class))

(def invoice (clojure.edn/read-string (slurp "invoice.edn")))

(defn file->invoice
  "Converts an invoice json file to a clojure map"
  [file-name]
  (json/read-str (slurp file-name)))

(defn -main
  "Demo features"
  [& args]
  (do (println "\nInvoice items that satisfy given conditions:\n")
      (pprint (ii/find-invoice-items (:invoice/items invoice)))
      (println "\nInvoice from JSON file:\n")
      (pprint (file->invoice "invoice.json"))))
