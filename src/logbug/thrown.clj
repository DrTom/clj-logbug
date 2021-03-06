; Copyright © 2013 - 2020 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>

(ns logbug.thrown
  (:require
    [clojure.stacktrace :as stacktrace]
    [clojure.tools.logging :as logging]
    [clojure.string :as string]
    ))

(defn get-cause [tr]
  (try
    (when-let [c (.getCause tr)]
      [c (get-cause c)])
    (catch Throwable _)))

(defn expand-to-seq [^Throwable tr]
  (->> (cond
         (instance? java.sql.SQLException tr) (iterator-seq (.iterator tr))
         :else (flatten [tr (get-cause tr)]))
       (filter identity)))

(defn trace-string-seq [ex]
  (map (fn [e] (with-out-str (stacktrace/print-trace-element e)))
       (.getStackTrace ex)))

;(trace-string-seq (IllegalStateException. "asdfa"))

(def ^:dynamic *ns-filter-regex* #".*")

(defn reset-ns-filter-regex [regex]
  (def ^:dynamic *ns-filter-regex* regex))

;(reset-ns-filter-regex #".**logbug.*")

(defn filter-trace-string-seq [ex-seq filter-regex]
  (filter
    #(re-matches filter-regex %)
    ex-seq))


(defn to-string
  ([tr]
   (str "  THROWABLE: "
        (with-out-str (stacktrace/print-throwable tr))
        (when (instance? clojure.lang.ExceptionInfo tr)
          (str " " (ex-data tr)))))
  ([tr filter-regex]
   (str (to-string tr)
        [(filter-trace-string-seq (trace-string-seq tr) filter-regex)])))

;(to-string (IllegalStateException. "Just a demo") #".*")

(defn stringify
  ([^Throwable tr]
   (stringify tr *ns-filter-regex* ", "))
  ([^Throwable tr filter-regex join-str]
   (let [fmap #(to-string % filter-regex)]
     (string/join join-str (map fmap (expand-to-seq tr))))))


;(stringify  (IllegalStateException. "Just a demo", (IllegalStateException. "The Cause")))

;(println (stringify (ex-info "Some error "{:x 42}  (IllegalStateException. "The cause", (IllegalStateException. "The root")))))
