; Copyright © 2013 - 2015 Thomas Schank <DrTom@schank.ch>

(ns logbug.ring
  (:require
    [clojure.tools.logging :as logging]
    ))

(defmacro wrap-handler-with-logging
  "Wraps a ring handler. Logs request and response with level
  debug in the namespace where this macro is used from."
  [handler]
  `(fn [request#]
     (let [logbug-level# (or (:logbug-level request#) 0 )]
       (logging/log :debug {:logbug-level logbug-level# :request request#})
       (let [response# (~handler (assoc request#
                                        :logbug-level (inc logbug-level#)))]
         (logging/log :debug {:logbug-level logbug-level# :response response#})
         response#))))


