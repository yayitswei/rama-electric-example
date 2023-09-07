(ns app.helpers.rama
  (:require
    [missionary.core :as m]
    [taoensso.timbre :as timbre])
  (:import
    [com.rpl.rama Depot PState Path ProxyState$Callback RamaModule]
    [com.rpl.rama.test InProcessCluster LaunchConfig]
    [hyperfiddle.electric Failure Pending]))


(defn pstate
  ^PState [^InProcessCluster cluster module ^String name]
  (.clusterPState cluster (.getName module) name))


(defn depot
  ^Depot [^InProcessCluster cluster module ^String name]
  (.clusterDepot cluster (.getName module) name))


(deftype ProxyCallback
  [f]

  ProxyState$Callback

  (change
    [v new _diff _old]
    (when new (f new))))


(defn subscribe
  [^PState pstate ^Path path]
  (->> (m/observe (fn [!]
                    (timbre/debug "Starting subscription")
                    (let [p (.proxyAsync pstate path (->ProxyCallback !))]
                      (fn []
                        (timbre/debug "Closing subscription")
                        (.close @p)))))
    (m/reductions {} (Failure. (Pending.)))
    (m/relieve {})))
