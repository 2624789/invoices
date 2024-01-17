(ns invoices.core
  (:require [clojure.data.json :as json]
            [clojure.instant :as ci]
            [clojure.set :as cs]
            [clojure.string :as cst]
            [invoice-items :as ii]
            [invoice-spec :as is])
  (:use clojure.pprint)
  (:gen-class))

(def invoice (clojure.edn/read-string (slurp "invoice.edn")))

;;;;
;; JSON file
;;;;
(defn file->invoice
  "Converts an invoice json file to a clojure map"
  [file-name]
  (json/read-str (slurp file-name)))

(def invoice-from-json (file->invoice "invoice.json"))

;;;;
;; date-format
;;;;
(defn date-string->inst
  "Receives a date string in format DD/MM/YYYY and returns an instant"
  [date-str]
  (ci/read-instant-date (cst/join "-" (reverse (cst/split date-str #"/")))))

;;;;
;; Items keys
;;;;
(defn rename-items-keys
  [items]
  (mapv #(cs/rename-keys % {"price"   :invoice-item/price
                           "quantity" :invoice-item/quantity
                           "sku"      :invoice-item/sku
                           "taxes"    :invoice-item/taxes})
        items))

;;;;
;; Taxes keys
;;;;

(defn rename-taxes-keys
  [taxes]
  (mapv #(cs/rename-keys % {"tax_category" :tax/category
                            "tax_rate"     :tax/rate})
        taxes))

(defn rename-item-taxes
  [item]
  (update-in item [:invoice-item/taxes] rename-taxes-keys))

;;;;
;; Taxes Cat
;;;;

(defn format-taxes-cat
  [taxes]
  (into [] (map #(assoc % :tax/category :iva) taxes)))

(defn format-item-taxes-cat
  [item]
  (update-in item [:invoice-item/taxes] format-taxes-cat))

;;;;
;; Taxes Rate
;;;;

(defn format-taxes-rate
  [taxes]
  (into [] (map #(update % :tax/rate double) taxes)))

(defn format-item-taxes-rate
  [item]
  (update-in item [:invoice-item/taxes] format-taxes-rate))

(defn format-items-taxes-rate
  [items]
  (mapv #(format-item-taxes-rate %) items))

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
      (update-in [:invoice/items] (partial mapv #(rename-item-taxes %)))
      (update-in [:invoice/items] (partial mapv #(format-item-taxes-cat %)))
      (update-in [:invoice/items] (partial mapv #(format-item-taxes-rate %)))))

(defn -main
  "Demo features"
  [& args]
  (do (println "\nInvoice items that satisfy given conditions:\n")
      (pprint (ii/find-invoice-items (:invoice/items invoice)))
      (println "\nInvoice from JSON file:\n")
      (println (str "is valid?: "
                    (is/is-valid-invoice? (valid-invoice invoice-from-json))))
      (pprint (valid-invoice invoice-from-json))))
