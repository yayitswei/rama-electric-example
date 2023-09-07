(ns app.cluster
  (:require [mount.core :refer [defstate] :as mount]
            [app.modules.wordcount :refer [->SimpleWordCountModule]])
  (:import [com.rpl.rama.test InProcessCluster LaunchConfig]))

(defstate cluster
  :start
  (do
    (println "Starting Rama cluster…")
    (let [c (InProcessCluster/create)]
      (println "Launching modules…")
      (.launchModule c (->SimpleWordCountModule) (LaunchConfig. 1 1))
      (println "Rama started.")
      c))
  
  :stop (.stop cluster))
