; Copyright © 2013 - 2020 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>

(ns logbug.scratch
  (:require
    [logbug.catcher :as catcher]
    [clojure.tools.logging :as logging]
  ))

(when false

  (macroexpand-1
    `(catcher/with-logging {:level :warn} (println "Hello World!")))

  (catcher/with-logging {:level :warn} (throw (ex-info "Blah" {} )))

  (macroexpand-1
    `(snatch
       {:level :debug
        :throwable Throwable
        :return-expr {:x 42}
        }
       (println "Hello World")))

  (catcher/snatch
    {:return-expr 42}
    (throw (ex-info "Blah" {}))
    )

  ;(catcher/snatch
  ;  {:bad-option 42}
  ;  (throw (ex-info "Blah" {}))
  ;  )



  (catcher/snatch
    {:return-expr 42}
    (throw (java.lang.UnknownError. "Blah"))
    )

  (logging/log *ns* :warn nil "some message")


  )


