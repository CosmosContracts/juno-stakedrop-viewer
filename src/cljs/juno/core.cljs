(ns juno.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.core :as reagent]
            [re-frame.core :as rf :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [juno.events]
            [juno.subs] ;; require so the compiler knows about them
            [juno.views]
            [devtools.core :as devtools])
  (:import [goog History]
           [goog.history EventType]))


;; debugging in dev 
(devtools/install! [:formatters])
(enable-console-print!)

;; init the app-db  with a placeholder
(dispatch-sync [:initialise-db])

;; secretary is a bit old hat, but it's quick to get running with
;; if you were building a bigger app, consider bidi
; (defroute "/" [] (dispatch [:set-showing :all]))
; (defroute "/:filter" [filter] (dispatch [:set-showing (keyword filter)]))

; (defonce history
;   (doto (History.)
;     (events/listen EventType.NAVIGATE
;                    (fn [event] (secretary/dispatch! (.-token event))))
;     (.setEnabled true)))


;; -- Entry Point -------------------------------------------------------------

(defn render
  []
  ;; root view for the entire UI.
  (reagent/render [juno.views/app]
                  (.getElementById js/document "app")))

(defn ^:dev/after-load clear-cache-and-render!
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code. We force a UI update by clearing
  ;; the Reframe subscription cache.
  (rf/clear-subscription-cache!)
  (render))

(defn ^:export main
  []
  (render))
