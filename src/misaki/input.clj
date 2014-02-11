(ns misaki.input
  "Input resource manager library."
  (:require
    [misaki.config :refer [*config*]]
    [misaki.loader :refer [load-ns-functions]])
  (:refer-clojure  :exclude [empty?]))

(def ^{:dynamic true :doc "Input extension's namespace prefix."}
  *input-ns-prefix*
  "misaki.input")

(def ^{:private true :doc "Input queue."} queue (ref []))

(defn get-input-extensions
  "Get input extension's specified public functions.

   @fn-key: default value is `:-main`"
  ([] (get-input-extensions :-main))
  ([fn-key]
   (load-ns-functions *input-ns-prefix* (:input *config*) fn-key)))

(defn add!
  "Add input resource to queue."
  [edn]
  (dosync (alter queue conj edn)))

(defn get!
  "Get input resource from queue."
  []
  (let [edn (first @queue)]
    (dosync (alter queue (comp vec rest)))
    edn))

(defn empty?
  "Returns true if input queue has no resources."
  []
  (clojure.core/empty? @queue))

(defn get-all
  "Returns sequence of all input resources."
  []
  (mapcat (fn [f] (f *config*)) (get-input-extensions :-get-all)))

(defn start-input-extensions!
  "Start inputting with other threads."
  []
  (doseq [f (get-input-extensions)]
    (.start (Thread. (partial f *config*)))))
