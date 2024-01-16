(ns invoices.core
  (:require [clojure.data.json :as json]
            [invoice-items :as ii]
            [invoice-spec :as is])
  (:use clojure.pprint)
  (:gen-class))

(def invoice (clojure.edn/read-string (slurp "invoice.edn")))

(defn file->invoice
  "Converts an invoice json file to a clojure map"
  [file-name]
  (json/read-str (slurp file-name)))

(def invoice-from-json (file->invoice "invoice.json"))

(defn -main
  "Demo features"
  [& args]
  (do (println "\nInvoice items that satisfy given conditions:\n")
      (pprint (ii/find-invoice-items (:invoice/items invoice)))
      (println "\nInvoice from JSON file:\n")
      (println (str "is valid?: " (is/is-valid-invoice? invoice-from-json)))
      (pprint invoice-from-json)))
