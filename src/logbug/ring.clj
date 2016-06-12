; Copyright © 2013 - 2016 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>

(ns logbug.ring
  (:require
    [logbug.thrown]
    [clojure.tools.logging :as logging]
    [clj-logging-config.log4j :as logging-config]
    ))


;### wrapper ##################################################################

(defn wrap-handler-with-logging*
  "Wraps a ring handler. Logs request and response including
  the level of wraps."
  ([handler loglevel  ns]
   (fn [request]
     (try
       (let [logbug-level (or (:logbug-level request) 0 )]
         (logging/log ns loglevel nil (str "LOGBUG-RING-WRAPPER "
                                           {:logbug-level logbug-level
                                            :request request}))
         (let [response (handler
                          (assoc request :logbug-level (inc logbug-level)))]
           (logging/log ns loglevel nil (str "LOGBUG-RING-WRAPPER "
                                             {:logbug-level logbug-level
                                              :response response}))
           response))))))

(defmacro wrap-handler-with-logging
  "Convenience macro which calls wrap-handler-with-logging*.
  The macro ensures that the current namespace is used from
  where this macro is used."
  ([handler]
   `(wrap-handler-with-logging* ~handler :debug ~*ns*))
  ([handler loglevel]
   `(wrap-handler-with-logging* ~handler ~loglevel ~*ns*))
  ([handler loglevel ns]
   `(wrap-handler-with-logging* ~handler ~loglevel ~ns)))

