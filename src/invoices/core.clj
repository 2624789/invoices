(ns invoices.core
  (:require [clojure.data.json :as json]
            [clojure.instant :as ci]
            [clojure.set :as cs]
            [clojure.string :as cst]
            [invoice-items :as ii]
            [invoice-spec :as is])
  (:use clojure.pprint)
  (:gen-class))

(def invoice-from-edn (clojure.edn/read-string (slurp "invoice.edn")))

(defn file->invoice
  "Converts an invoice json file to a clojure map"
  [file-name]
  (json/read-str (slurp file-name)))

(def invoice-from-json (file->invoice "invoice.json"))

(defn date-string->inst
  "Receives a date string in format DD/MM/YYYY and returns an instant"
  [date-str]
  (ci/read-instant-date (cst/join "-" (reverse (cst/split date-str #"/")))))

(defn rename-items-keys
  [items]
  (mapv #(cs/rename-keys % {"price"   :invoice-item/price
                           "quantity" :invoice-item/quantity
                           "sku"      :invoice-item/sku
                           "taxes"    :invoice-item/taxes})
        items))

(defn update-taxes
  [item update-function]
  (update-in item [:invoice-item/taxes] update-function))

(defn rename-taxes-keys
  [taxes]
  (mapv #(cs/rename-keys % {"tax_category" :tax/category
                            "tax_rate"     :tax/rate})
        taxes))

(defn format-taxes-cat
  [taxes]
  (mapv #(assoc % :tax/category :iva) taxes))

(defn format-taxes-rate
  [taxes]
  (mapv #(update % :tax/rate double) taxes))

(defn valid-invoice
  [invoice]
  (-> invoice
      (get "invoice")
      (cs/rename-keys {"issue_date" :invoice/issue-date
                       "customer" :invoice/customer
                       "items" :invoice/items})
      (update-in [:invoice/issue-date] date-string->inst)
      (update-in [:invoice/customer]
                 cs/rename-keys {"company_name" :customer/name
                                 "email" :customer/email})
      (update-in [:invoice/items] rename-items-keys)
      (update-in [:invoice/items]
                 (partial mapv #(update-taxes % rename-taxes-keys)))
      (update-in [:invoice/items]
                 (partial mapv #(update-taxes % format-taxes-cat)))
      (update-in [:invoice/items]
                 (partial mapv #(update-taxes % format-taxes-rate)))))

(defn -main
  "Demo features"
  [& args]
  (do (println "\nInvoice items that satisfy given conditions:\n")
      (pprint (ii/find-invoice-items (:invoice/items invoice-from-edn)))
      (println "\nInvoice from JSON file:\n")
      (println (str "is valid?: "
                    (is/is-valid-invoice? (valid-invoice invoice-from-json))))
      (pprint (valid-invoice invoice-from-json))))
