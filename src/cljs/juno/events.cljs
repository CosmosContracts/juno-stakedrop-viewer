(ns juno.events
  (:require
    [juno.db :refer [default-db account->local-store]]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path after]]
    [cljs.spec.alpha :as s]
    [ajax.core :as ajax]
    [day8.re-frame.http-fx]))

;; switch to this to use the dev backend
(def dev-host "http://localhost:3001")

;; switch to this and run lein prod to build the prod app
;; env vars can be defined in the closures for the compiler
;; will switch to that at some point
(def prod-host "https://stakedrop.junochain.com")

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; now we create an interceptor using `after`
(def check-spec-interceptor (after (partial check-and-throw :juno.db/db)))

(def ->local-store (after account->local-store))

(def account-interceptors [check-spec-interceptor
                           ->local-store])

;; registration.
(reg-event-fx
  :initialise-db
  [(inject-cofx :local-store-account)
   check-spec-interceptor]

  ;; the event handler (function) being registered
  (fn [{:keys [db local-store-account]} _]
    {:db (if (s/valid? :juno/account local-store-account) ;; don't bother if invalid
           (assoc default-db :juno/account local-store-account)
           default-db)}))

;; here's the stuff for handling api reqs

(reg-event-fx
 :account/query-balance
 (fn
   [{db :db} [_ address]]
   (if address
     (let [endpoint (str dev-host "/address/" address)]
      {:http-xhrio {:method :get
                    :uri endpoint
                    :format (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true}) 
                    :on-success [::handle-response]
                    :on-failure [::handle-bad-response]}}))))

(reg-event-db
 ::handle-response
 account-interceptors
 (fn [db [_ {address :address
            balance :balance
            denom :denom
            :as response}]]
   (let [returned-account {:account/address address
                           :account/balance (str balance)
                           ;; display only
                           ;; otherwise use the int type
                           :account/denom denom}]
     (-> db
         (assoc :app/loading false)
         (assoc :juno/account returned-account)))))

(reg-event-db
 ::handle-bad-response
 [check-spec-interceptor]
 (fn [db [_ response]]
   (let
       [err-from-res (str (get-in response [:response :error]))
        error-string (if err-from-res
                       err-from-res
                       "An error occurred. Please check the address you submitted.")]
     (-> db
         (assoc :app/loading false)
         ;; TODO - better error handling
         (assoc :app/error-str error-string)))))
