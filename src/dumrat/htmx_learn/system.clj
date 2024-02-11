(ns dumrat.htmx-learn.system
  (:require [integrant.core :as ig]
            [dumrat.htmx-learn.server :as server]))

;; System config
;;------------------------------------------;;
(def config
  {::start-time nil
   ::dev-mode? true
   :server/opts {:port 3000 :join? false}
   ::server {:server-opts (ig/ref :server/opts)
             :dev-mode? (ig/ref ::dev-mode?)}})

(defmethod ig/init-key ::start-time [_ _]
  (java.util.Date.))
(defmethod ig/init-key ::dev-mode? [_ v] v)
(defmethod ig/init-key :server/opts [_ v] v)
(defmethod ig/init-key ::server [_ {:keys [server-opts dev-mode?]}]
  (server/start-server dev-mode? server-opts))

(defmethod ig/halt-key! ::server [_ server]
  (server/stop-server server))

;; System start/stop
;;------------------------------------------;;
(def ^:private system (atom nil))

(defn stop-system! []
  (when @system
    (tap> [`stop-system! "system stop"])
    (ig/halt! @system)
    (reset! system nil)))

(defn start-system! []
  (stop-system!)
  (let [s (reset! system (ig/init config))
        _ (tap> [`start-system! "system startup"])
        _ (tap> s)]
    s))
;;------------------------------------------;;

(comment

  (start-system!)
  (stop-system!)

  (require '[criterium.core :as cc])
  ;; roughly 3ms startup time in my machine for jetty server.
  (cc/quick-bench (start-system!))

  ,,)
