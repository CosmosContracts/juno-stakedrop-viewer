(ns juno.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]))

(defn title []
  [:h1 {:class "mt-0 mb-16"}
   "Juno, Smart Contracts"
   [:span.text-color-primary " Evolved."]])

(defn header []
  [:section {:class "hero section center-content"}
   [:div.container-sm
    [:div {:class "hero-inner section-inner"}
     [:div.hero-content
      [title]
      [:div.container-xs
       [:p {:class "m-0 mb-32"}
        "To find out what your stakedrop allocation is, please enter your address below."]]]]]])

(defn address-form []
  (let [address @(subscribe [:account/address]) ;; if they've got a session active
        val (r/atom (if (not (empty? address))
                      address
                      "")) ;; don't really need this but better to be explicit 
        save #(let [v (-> @val str str/trim)]
                (dispatch [:account/query-balance v]))
        stop #(reset! val "")]
    (fn [props]
      [:section {:class "section center-content pt-128 pb-24"}
       [:div.address-form-group
        [:input (merge (dissoc props :on-save :on-stop :title)
                       {:type "text"
                        :value @val
                        :auto-focus true
                        ;; :on-blur save
                        :on-change #(reset! val
                                            (-> % .-target .-value))
                        :on-key-down #(case (.-which %)
                                        13 (save)
                                        27 (stop)
                                        nil)})]
        [:a {:class "button button-primary button-wide-mobile button-sm ml-16"
             :href "#"
             :on-click save}
         "Submit"]]])))

(defn balance-container [str]
  [:section {:class "section center-content pb-128"}
   [:div.container-xs
    [:p {:class "m-0 mb-32"}
     str]]])

(defn balance []
  (let [balance @(subscribe [:account/balance])
        denom @(subscribe [:account/denom])
        error-str @(subscribe [:app/error-str])]
    (if (not (empty? error-str))
      [balance-container error-str]
        (if (and (not (nil? balance))
              (not (empty? balance)))

          [balance-container (str "Your balance is: "
                                  balance
                                  " "
                                   (str/upper-case denom)
                                   ".")]
          [balance-container "Enter an address to query balance."]))))

(defn contributing []
  [:section {:class "section pb-128"}
   [:div.cta
    [:div {:class "cta-inner section-inner cta-split"}
     [:div.cta-slogan 
      [:h3.m-0 "Want to contribute to the Juno network?"]]
     [:div.cta-action
      [:a {:href "https://discord.gg/wHdzjS5vXx"
           :class "button button-success button-wide-mobile"}
       "Dev Discord"]]]]])

(defn footer-top []
  [:div {:class "footer-top space-between text-xxs"}
   [:div.brand
    [:div.footer-brand]]
   [:div.footer-social
    [:ul.list-reset
     [:li
      [:a 
       {:class "button button button-wide-mobile button-sm"
        :href "https://twitter.com/JunoNetwork"
        :target "_blank"
        :rel "noopener noreferrer"}
       "Twitter"]]
     [:li
      [:a
       {:class "button button-primary button-wide-mobile button-sm"
        :href "https://t.me/JunoNetwork"
        :target "_blank"
        :rel "noopener noreferrer"}
       "Telegram"]]]]])

(defn footer-bottom []
  [:div {:class "footer-bottom space-between text-xxs invert-order-desktop"}
   [:nav.footer-nav
    [:ul.list-reset
     [:li
      [:a
       {:href "https://github.com/CosmosContracts/Juno/wiki"
        :target "_blank"
        :rel "noopener noreferrer"}
       "Documentation"]]
     [:li
      [:a
       {:href "https://discord.gg/wHdzjS5vXx"
        :target "_blank"
        :rel "noopener noreferrer"}
       "Support"]]]]
   [:div.footer-copyright
    "Made by "
    [:a {:href "https://junochain.com"}
     "Juno"]
    ". All rights reserved."]])

(defn footer []
  [:footer {:class "site-footer center-content-mobile"}
   [:div.container
    [:div.site-footer-inner
     [footer-top]
     [footer-bottom]]]])

(defn app
  []
  [:main.site-content
   [:div
    [:section#juno-stakedrop-app
     [header]
     [address-form]
     [balance]
     [contributing]]
    [footer]]])
