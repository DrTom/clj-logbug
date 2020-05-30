# clj-logbug - Cross-cutting utilities for logging and debugging in Clojure


## Current Version

See https://clojars.org/logbug.

## Usage

### Debug Wrappers

There are two wrapping functionalities:
1. wrap calls to log invocation and results,
2. wrap calls to remember (and later possibly re-apply) the last arguments.

#### Wrap Functions for Debug Logging

This functionality logs invocation and result of a function referenced by
a var.

Example enable:

    (logbug.debug/wrap-with-log-debug #'some-var)

Example log output:

    [DEBUG some-ns clojure-agent-send-off-pool-5] [some-var "invoked" {:args ( ...
    ...
    [DEBUG some-ns clojure-agent-send-off-pool-5] [some-var "returns" {:res ...

Example disable again:

    (logbug.debug/unwrap-with-log-debug #'some-var)


#### Wrap Functions for Reapplying last Arguments


    (logbug.debug/wrap-with-remember-last-argument #'some-var)

    (logbug.debug/get-last-argument #'some-var)

    (logbug.debug/re-apply-last-argument #'some-var)


#### Wrap a Complete Namespace

Example require the debug namespace:

    (require ['logbug.debug :as 'debug])

Example wrap all vars in the current namespace:

    (debug/debug-ns *ns*)

Example disable wrappers in the current namespace:

    (debug/undebug-ns *ns*)

#### Notes

The use of `require` will "remove" existing wrappers. The last arguments are
stored within the `debug` ns and thus they survive `require` etc.



## Breaking Changes from Version 4.x to 5.x

The dependency on the deprecated
[clj-logging-config](https://github.com/malcolmsparks/clj-logging-config)
library has been removed. This removes in turn deeper dependencies and makes
this library much more flexible to use with any logging framework as long as
it works with the de facto standard
[clojure/tools.logging](https://github.com/clojure/tools.logging/).

The API has not changed but calls to `debug/debug-ns` are affected by this.
`debug/debug-ns` did also set the level of the namespace to `DEBUG`. Since
version 5.x the level must be changed explicitly if desired. How to do this
depends on the chosen logging framework.


## Contributors

* Thomas Schank, https://github.com/DrTom
* Matúš Kmiť, https://github.com/nimaai


## License

Copyright © 2013 - 2020 Thomas Schank and contributors.

  clj-logbug may be used under the terms of either the

 * GNU Lesser General Public License (LGPL) v3
   https://www.gnu.org/licenses/lgpl

or the

 * Eclipse Public License (EPL)
   http://www.eclipse.org/org/documents/epl-v10.php


