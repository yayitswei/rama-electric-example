(ns ^{:clj-kondo/ignore true
      :dev/always       true}
  app.client.root
  (:require
    #?@(:clj  [[app.modules.wordcount :refer [->SimpleWordCountModule]]
               [app.helpers.rama :as rama]
               [app.cluster]])
    [hyperfiddle.electric-dom2 :as dom]
    [clojure.string :as str]
    [hyperfiddle.electric :as e])
  #?(:clj (:import
            [com.rpl.rama Path]
            [app.modules.wordcount SimpleWordCountModule])))

#?(:clj (def cluster @app.cluster/cluster))

(e/def pstate-words #?(:clj (rama/pstate cluster SimpleWordCountModule "$$words")))
(e/def pstate-word-counts #?(:clj (rama/pstate cluster SimpleWordCountModule "$$word-counts")))
(e/def words-depot #?(:clj (rama/depot cluster SimpleWordCountModule "*words")))


#?(:clj (defn <-words
          [pstate]
          (rama/subscribe pstate (Path/key (into-array String ["all-words"])))))


#?(:clj (defn <-word-score
          [pstate word]
          (rama/subscribe pstate (Path/key (into-array String [word])))))


(e/defn Word
  [word score]
  (e/client
    (dom/div (dom/props {:class ["flex" "space-x-2"]})
      (dom/div (dom/text score))
      (dom/div (dom/text word)))))


(e/defn Root
  []
  (e/client
    ;; Scoreboard
    (dom/div (dom/props {:class ["flex" "flex-col" "p-6" "space-y-2"]})
      (dom/div
        (dom/text "Scoreboard")
        (e/server
          (let [word-scores (e/for-by identity [word (new (<-words pstate-words))]
                              [word (new (<-word-score pstate-word-counts word))])]
            (e/for-by first [[word score] (sort-by second > word-scores)]
              (Word. word score)))))
      ;; Input
      (dom/input (dom/props {:class ["border"]})
        (dom/on "keydown"
          (e/fn [e]
            (when (= "Enter" (.-key e))
              (let [value (-> e .-target .-value)]
                (when-not (str/blank? value)
                  (e/server (e/offload #(.append words-depot value))))))))))))
