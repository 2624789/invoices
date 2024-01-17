(ns invoice-items)

(defn- has-ret?
  "Returns true if the invoice item has at least one ret_fuente 1%"
  [item]
  (some #(= (:retention/rate %) 1)
        (filter #(= (:retention/category %) :ret_fuente)
                (:retentionable/retentions item))))

(defn- has-iva?
  "Returns true if the invoice item has at least one iva 19%"
  [item]
  (some #(= (:tax/rate %) 19)
        (filter #(= (:tax/category %) :iva) (:taxable/taxes item))))

(defn- has-iva-or-ret?
  [item]
  (or (has-iva? item) (has-ret? item)))

(defn- has-iva-and-ret?
  [item]
  (and (has-iva? item) (has-ret? item)))

(defn find-invoice-items
  "Find all invoice items that satisfy the following conditions:
  - have at least one :iva 19%
  - have at least one :ret_fuente 1%
  - satisfy EXACTLY one of the above two conditions"
  [items]
  (->> items
       (filter has-iva-or-ret?)
       (remove has-iva-and-ret?)))

(defn- discount-factor [{:keys [discount-rate]
                         :or   {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

;; TODO: Safe handle negative values
(defn subtotal
  "calculates the subtotal of an invoice-item taking a discount-rate into account"
  [{:keys [precise-quantity precise-price discount-rate]
    :as   item
    :or   {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))
