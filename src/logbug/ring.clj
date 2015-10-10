; Copyright © 2013 - 2015 Thomas Schank <DrTom@schank.ch> and contributers

(ns logbug.ring
  (:require
    [logbug.thrown]
    [clojure.tools.logging :as logging]
    [clj-logging-config.log4j :as logging-config]
    ))

;### exceptions ###############################################################

; naive approach, but good enough for debugging when there aren't
; many concurrent requests which throw exceptions
(def ^:dynamic *e* nil)

(def ^:dynamic *report-level* :warn)

(defn- report-and-rethrow [ns e request]
  (when-not (= *e* e)
    (def ^:dynamic *e* e)
    (logging/log ns *report-level* nil (str "LOGBUG-RING-WRAPPER WEBSTACK-EXCEPTION "
                                            {:message (.getMessage e)
                                             :request request
                                             :exception (logbug.thrown/stringify e)})))
  (throw e))


;### wrapper ##################################################################

(defn wrap-handler-with-logging*
  "Wraps a ring handler. Logs request and response including
  the level of wraps. Also logs exceptions including the request
  with level *report-level* set to :warn by default."
  ([handler ns loglevel]
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
           response))
       (catch Exception e
         (report-and-rethrow ns e request))))))


(defmacro wrap-handler-with-logging
  "Convenience macro which calls wrap-handler-with-logging*.
  The macro ensures that the current namespace is used from
  where this macro is used."
  [handler]
  `(wrap-handler-with-logging* ~handler ~*ns* :debug))


;### threading macro ##########################################################

(defmacro o-> [interleaved & handlers]
  `(-> ~@(interleave handlers (repeat interleaved))))
