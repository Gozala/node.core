(ns node.utils)

(defn json->edn
  "Takes JSON object and translates it to EDN."
  [json]
  ;; Note that we need to serailazie and parse json
  ;; because `js->clj` does not really handles some
  ;; node objects well.
  (js->clj (js/JSON.parse (js/JSON.stringify json))
           :keywordize-keys true))


(defn reflect-atom-fields!
  "Takes target atom and sets up a watcher on it, dispatching
  onto handlers hash when associated field is changed"
  [target handlers]
  (let [fields (keys handlers)]
    (add-watch
     target
     nil
     (fn [_ _ previous current]
       (doseq [field fields]
         (when-not (= (field previous) (field current))
           ((field handlers) (field current))))))))

(defn pipe-events-onto-atom!
  "Takes node's EventEmitter object and reflects
  emitted values onto target atom's fields. Given
  feild->event mapping is field name to event type
  mapping used"
  [emitter target feild->event]
  (doseq [field (keys feild->event)]
    (.on emitter
         (field feild->event)
         #(let [value (json->edn %)]
            (when-not (= value (field @target))
              (swap! target assoc field value))))))

(def
  ^{:doc "Function takes `target` object and `field` string arguments.
    In side effect it deletes `field` from the given `target`"}
  delete!
  (js/Function "target" "field" "delete target[field]; target;"))
