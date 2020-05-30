; Copyright © 2013 - 2020 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>

(ns logbug.debug
  (:require
    [clojure.test]
    [clojure.tools.logging :as logging]
    [robert.hooke :as hooke]
    ))


;### Log arguments and result #################################################

(defn wrap-with-log-debug [target-var]
  (logging/debug "wrapping for debugging: " target-var)
  (let [wrapper-fn (fn [f & args]
                     (logging/log (-> target-var meta :ns)
                                  :debug nil
                                  [(symbol (str (-> target-var meta :name)))
                                   "invoked" {:args args}])
                     (let [res (apply f args)]
                       (logging/log (-> target-var meta :ns)
                                    :debug nil
                                    [(symbol (str (-> target-var meta :name)))
                                     "returns" {:res res}])
                       res))]
    (hooke/add-hook target-var :logbug_wrap wrapper-fn)))

(defn unwrap-with-log-debug [target-var]
  (logging/debug "unwrapping from debugging: " target-var)
  (hooke/remove-hook target-var :logbug_wrap))


;### Remember arguments of last call ##########################################

(defonce ^:private last-arguments (atom {}))

(defn- var-key [target-var]
  (str (-> target-var meta :ns) "/" (-> target-var meta :name)))

(defn wrap-with-remember-last-argument [target-var]
  (logging/debug "wrapping for remember" target-var)
  (let [swap-in (fn [current args]
                  (conj current
                        {(var-key target-var) args}))
        wrapper-fn (fn [ f & args]
                     (swap! last-arguments swap-in args)
                     (apply f args))]
    (hooke/add-hook target-var :logbug_remember wrapper-fn)))

(defn unwrap-with-remember-last-argument [target-var]
  (logging/debug "unwrapping from remember" target-var)
  (hooke/remove-hook target-var :logbug_remember))

(defn get-last-argument [target-var]
  (@last-arguments (var-key target-var)))

(defn re-apply-last-argument [target-var]
  (apply target-var (get-last-argument target-var)))


;### Wrap vars of a whole ns ##################################################

(defn- ns-wrappables [ns]
  (filter #(clojure.test/function? (var-get %))
          (vals (ns-interns ns))))

(defn debug-ns [ns]
  (doseq [wrappable (ns-wrappables ns)]
    (wrap-with-log-debug wrappable)
    (wrap-with-remember-last-argument wrappable)))

(defn undebug-ns [ns]
  (doseq [wrappable (ns-wrappables ns)]
    (unwrap-with-log-debug wrappable)
    (unwrap-with-remember-last-argument wrappable)))

;### identity-with-logging ns #################################################

(defmacro identity-with-logging
  ([x]
   `(identity-with-logging ~*ns* ~x))
  ([ns x]
   `(let [result# ~x]
      (logging/log ~ns :debug nil (if (seq? result#) (doall (seq result#)) result#))
      result#)))

;### with-logging ns ##########################################################

(defmacro with-logging
  ([func x]
   `(with-logging ~*ns* ~func ~x))
  ([ns func x]
   `(let [result# ~x
          func-result# (~func result#)]
      (logging/log ~ns :debug nil func-result#)
      result#)))

;### interleave ###############################################################

(defmacro I>
  "Like -> but interleaves 'inter' between every form."
  [inter & forms]
  `(-> ~@(interleave forms (repeat inter))))

(defmacro I>>
  "Like ->> but interleaves 'inter' between every form."
  [inter & forms]
  `(->> ~@(interleave forms (repeat inter))))


;###############################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
