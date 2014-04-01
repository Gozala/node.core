(ns node.env
  (:require [node.utils :refer [json->edn delete!]]))


(def ^:private *env* js/process.env)

(defn update-env!
  [present]
  (doseq [key (set (concat (keys (json->edn *env*))
                           (keys present)))]
    (let [id (name key)
          before (aget *env* id)
          after (key present)]
      (if-not (= before after)
        (if (nil? after)
          (delete! *env* id)
          (aset *env* id after))))))

;; Unfortunately we can't use plain atom since changes
;; on the actual `process.ENV` in node aren't observable.
;; There for we implement atom interface in order to always
;; return up to date state on dereference.
(deftype Environment
  [^:mutable state metadata validator watches]
  Object
  IAtom

  IEncodeJS
  (-clj->js [_] *env*)

  ISwap
  (-swap!
   [env f]
   (reset! env (f state)))
  (-swap!
   [env f a]
   (reset! env (f state a)))
  (-swap!
   [env f a b]
   (reset! env (f state a b)))
  (-swap!
   [env f a b etc]
   (reset! env (apply f state a b etc)))

  IReset
  (-reset!
   [env present]
   (let [validate (.-validator env)]
     (when-not (nil? validate)
       (assert (validate present) "Validator rejected reference state"))
     (let [past (.-state env)]
       (set! (.-state env) present)
       (update-env! present)
       (when-not (nil? (.-watches env))
         (-notify-watches env past present))
       present)))

  IEquiv
  (-equiv [env other] (identical? env other))

  IDeref
  (-deref [env]
          (let [present (json->edn *env*)]
            ;; If state has changed since it was last dereferenced
            ;; we reset state to trigger watchers that may expect
            ;; to be called. This is not ideal, but it is only option
            ;; cover changes done from JS side.
            (if (= state present)
              state
              (reset! env present))))

  IMeta
  (-meta [_] metadata)

  IPrintWithWriter
  (-pr-writer [env writer opts]
              (-write writer "#<ENV: ")
              (pr-writer @env writer opts)
              (-write writer ">"))

  IWatchable
  (-notify-watches [this oldval newval]
                   (doseq [[key f] watches]
                     (f key this oldval newval)))
  (-add-watch [this key f]
              (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
                 (set! (.-watches this) (dissoc watches key))))

(defn- string|number?
  [x]
  (or (string? x)
      (number? x)))

(defn- keyword->string-map?
  [x]
  (and (map? x)
       (every? keyword? (keys x))
       (every? string|number? (vals x))))

(def env (Environment. (json->edn *env*) {} keyword->string-map? nil))

(add-watch env nil (fn [_ _ _ state] (update-env! state)))
