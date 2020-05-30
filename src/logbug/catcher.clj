; Copyright © 2013 - 2020 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>

(ns logbug.catcher
  (:require
    [clojure.stacktrace :as stacktrace]
    [clojure.tools.logging :as logging]
    [logbug.thrown :as thrown]
    [clojure.set :refer [difference]]
    ))

;##############################################################################

(def with-logging-default-opts
  {:level :warn
   :throwable Throwable
   :message-fn #'thrown/stringify
   })

(defmacro with-logging
  "Catches a throwable (default java.lang.Throwable) thrown in expressions,
  logs it (default level :warn, default message formatter
  logbug.thrown/stringify ) and throws it again."
  [opts & expressions]

  (assert
    (empty?
      (difference (-> opts keys set)
                  (-> with-logging-default-opts keys set)))
    "Options must only contain the same keys as with-logging-default-opts")


  (let [opts (merge with-logging-default-opts opts)
        {level :level
         throwable :throwable
         message-fn :message-fn} opts]
    `(try
       ~@expressions
       (catch ~throwable e#
         (logging/log ~*ns* ~level nil (apply ~message-fn [e#]))
         (throw e#)))))


;##############################################################################


(def snatch-default-options
  {:level :warn
   :throwable Exception
   :return-expr nil
   :return-fn nil
   })

(defmacro snatch
  [opts & expressions]

  (assert
    (empty?
      (difference (-> opts keys set)
                  (-> snatch-default-options keys set)))
    "Options must only contain the same keys as snatch-default-options.")

  (assert
    (not (and (:return-expr opts) (:return-fn opts)))
    "Options may not contain values for :return-fn and :return-expr at the same time.")


  (let [opts (merge snatch-default-options opts)
        {level :level
         throwable :throwable
         return-fn :return-fn
         return-expr :return-expr} opts ]
    `(try
       ~@expressions
       (catch ~throwable e#
         (logging/log ~level (thrown/stringify e#))
         (if ~return-fn
           (apply ~return-fn [e#])
           ~return-expr)))))
