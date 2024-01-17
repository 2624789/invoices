(ns invoice-items-test
  (:require [clojure.test :refer :all]
            [invoice-items :as ii]))

(deftest test-subtotal
  (testing "subtotal with default discount-rate"
    (is (= 10.0 (ii/subtotal {:precise-quantity 1 :precise-price 10}))))

  (testing "subtotal with positive discount-rate"
    (is (= 9.0 (ii/subtotal {:precise-quantity 1 :precise-price 10 :discount-rate 10}))))

  (testing "subtotal with negative discount-rate"
    (is (= 11.0 (ii/subtotal {:precise-quantity 1 :precise-price 10 :discount-rate -10}))))

  (testing "subtotal with zero discount-rate"
    (is (= 10.0 (ii/subtotal {:precise-quantity 1 :precise-price 10 :discount-rate 0}))))

  (testing "subtotal with positive quantity"
    (is (= 18.0 (ii/subtotal {:precise-quantity 2 :precise-price 10 :discount-rate 10}))))

  (testing "subtotal with negative quantity"
    (is (= -18.0 (ii/subtotal {:precise-quantity -2 :precise-price 10 :discount-rate 10}))))

  (testing "subtotal with zero quantity"
    (is (= 0.0 (ii/subtotal {:precise-quantity 0 :precise-price 10 :discount-rate 10}))))

  (testing "subtotal with positive price"
    (is (= 18.0 (ii/subtotal {:precise-quantity 1 :precise-price 20 :discount-rate 10}))))

  (testing "subtotal with negative price"
    (is (= -18.0 (ii/subtotal {:precise-quantity 1 :precise-price -20 :discount-rate 10}))))

  (testing "subtotal with zero price"
    (is (= 0.0 (ii/subtotal {:precise-quantity 1 :precise-price 0 :discount-rate 10})))))
