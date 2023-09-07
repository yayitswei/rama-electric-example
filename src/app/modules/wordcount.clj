(ns app.modules.wordcount
  (:require
    [app.helpers.rama :as rama])
  (:import
    [com.rpl.rama Agg CompoundAgg Depot PState Path RamaModule]))


(deftype SimpleWordCountModule
  []

  RamaModule

  (define
    [_ setup topologies]
    (.declareDepot setup "*words" (Depot/random))
    (let [s      (.stream topologies "s")
          source (.source s "*words")]
      (.pstate s "$$word-counts" (PState/mapSchema String Long))
      (.pstate s "$$words" (PState/mapSchema String (PState/setSchema String)))
      (-> source
        (.out (into-array String ["*word"]))
        (.anchor "root")
        (.hashPartition "*word")
        (.compoundAgg "$$word-counts" (CompoundAgg/map (into-array Object ["*word" (Agg/count)])))
        (.hook "root")
        (.globalPartition)
        (.localTransform "$$words" (-> (Path/key (into-array String ["all-words"]))
                                     (.nullToSet)
                                     (.voidSetElem)
                                     (.termVal "*word")))))))

