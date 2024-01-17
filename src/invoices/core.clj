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

(defn file->invoice
  "Converts an invoice json file to a clojure map"
  [file-name]
  (json/read-str (slurp file-name)))

(def invoice-from-json (file->invoice "invoice.json"))

;;;;
;; invoice
;;;;
(defn remove-root-key
  [invoice]
  (get invoice "invoice"))

(defn rename-keys
  [invoice key-map]
  (cs/rename-keys invoice key-map))

;;;;
;; issue-date
;;;;
(defn date-string->inst
  [date-str]
  (ci/read-instant-date (cst/join "-" (reverse (cst/split date-str #"/")))))

(defn format-issue-date
  [invoice]
  (update-in invoice [:invoice/issue-date] date-string->inst))

;;;;
;; customer
;;;;
(defn rename-customer-keys
  [invoice]
  (update-in invoice [:invoice/customer]
             cs/rename-keys {"company_name" :customer/name
                             "email" :customer/email}))

;;;;
;; Items keys
;;;;

(defn rename-items-keys
  [items]
  (into [] (map #(cs/rename-keys % {"price" :invoice-item/price
                                    "quantity" :invoice-item/quantity
                                    "sku" :invoice-item/sku
                                    "taxes" :invoice-item/taxes})
                items)))

(defn rename-invoice-items-keys
  [invoice]
  (update-in invoice [:invoice/items] rename-items-keys))

;;;;
;; Taxes keys
;;;;

(defn rename-taxes-keys
  [taxes]
  (into [] (map #(cs/rename-keys % {"tax_category" :tax/category
                                   "tax_rate"     :tax/rate})
               taxes)))

(defn rename-item-taxes
  [item]
  (update-in item [:invoice-item/taxes] rename-taxes-keys))

(defn rename-items-taxes-keys
  [items]
  (into [] (map #(rename-item-taxes %) items)))

(defn rename-invoice-items-taxes-keys
  [invoice]
  (update-in invoice [:invoice/items] rename-items-taxes-keys))

;;;;
;; Taxes Cat
;;;;

(defn format-taxes-cat
  [taxes]
  (into [] (map #(assoc % :tax/category :iva) taxes)))

(defn format-item-taxes-cat
  [item]
  (update-in item [:invoice-item/taxes] format-taxes-cat))

(defn format-items-taxes-cat
  [items]
  (into [] (map #(format-item-taxes-cat %) items)))

(defn format-invoice-items-taxes-cat
  [invoice]
  (update-in invoice [:invoice/items] format-items-taxes-cat))

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
  (into [] (map #(format-item-taxes-rate %) items)))

(defn format-invoice-items-taxes-rate
  [invoice]
  (update-in invoice [:invoice/items] format-items-taxes-rate))

(defn valid-invoice
  [invoice]
  (-> invoice
       (remove-root-key)
       (rename-keys {"issue_date" :invoice/issue-date
                     "customer" :invoice/customer
                     "items" :invoice/items})
       (format-issue-date)
       (rename-customer-keys)
       (rename-invoice-items-keys)
       (rename-invoice-items-taxes-keys)
       (format-invoice-items-taxes-cat)
       (format-invoice-items-taxes-rate)))

(defn -main
  "Demo features"
  [& args]
  (do (println "\nInvoice items that satisfy given conditions:\n")
      (pprint (ii/find-invoice-items (:invoice/items invoice)))
      (println "\nInvoice from JSON file:\n")
      (println (str "is valid?: "
                    (is/is-valid-invoice? (valid-invoice invoice-from-json))))
      (pprint (valid-invoice invoice-from-json))))
