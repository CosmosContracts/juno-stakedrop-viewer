(ns juno.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :account
 (fn [db _]
   (:juno/account db)))

(reg-sub
 :account/address
 :<- [:account]
 (fn [account _]
   (get account :account/address)))

(reg-sub
 :account/balance
 :<- [:account]
 (fn [account _]
   (get account :account/balance)))

(reg-sub
 :account/denom
 :<- [:account]
  (fn [account _]
   (get account :account/denom)))

(reg-sub
 :app/error-str
 (fn [db _]
   (:app/error-str db)))
