(ns juno.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]))

(s/def :account/address string?)
(s/def :account/balance string?)
(s/def :account/denom string?)
(s/def :juno/account (s/keys :req [:account/address :account/balance :account/denom]))
(s/def :app/loading boolean?)
(s/def :app/error-str string?)
(s/def ::db (s/keys :req [:juno/account :app/loading :app/error-str]))

;; very simple default app-db
(def default-db
  {:juno/account
   {:account/address ""
    :account/balance ""
    :account/denom "JUNO"}
   :app/loading false
   :app/error-str ""})

;; -- Local Storage  ----------------------------------------------------------
;;
;; on app startup, reload the data from when the program was last run.
;; essentially a noddy way of trying to ensure multiple clicks can be filtered out
(def ls-key "juno-reframe") ;; localstore key

;; put juno balance into local storage
(defn account->local-store
  "Puts account into localStorage"
  [app-db]
  (let [account (get app-db :juno/account)]
    (.setItem js/localStorage ls-key (str account))))     ;; sorted-map written as an EDN map


;; -- cofx Registrations  -----------------------------------------------------

;; Use `reg-cofx` to register a "coeffect handler" which will inject the data
;; stored in localstore.
(re-frame/reg-cofx
  :local-store-account
  (fn [cofx _]
      ;; put the localstore balance into the coeffect under :local-store-balance
    (let [ls-data (some->> (.getItem js/localStorage ls-key)
                           (cljs.reader/read-string)) ;; EDN map -> map
          ]
      (assoc cofx :local-store-account
             ls-data)))) 
