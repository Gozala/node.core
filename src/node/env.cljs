(ns node.env)


(def *env* js/process.env)

(defn env->dictionary
  [env]
  (persistent! (reduce (fn [dictionary key]
                         (assoc!
                          dictionary
                          (keyword key)
                          (aget env key)))
                       (transient {})
                       (js/Object.keys env))))

(def
  ^{:doc "Function that deletes given `field` on the given `target` "}
  delete!
  (js/Function "target" "field" "delete target[field]"))


(deftype Environment
  [state meta validator watches]
  Object
  ;IAtom

  ;ISwap
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

  ;IReset
  (-reset!
   [env present]
   (assert (map? present) "Validator rejected reference state")
   (doseq [[key value] present
           id (name key)]
     (if-not (= (aget *env* id) value)
       (aset *env* id value)))
   present)


  IEquiv
  (-equiv [env other] (identical? env other))

  IDeref
  (-deref [_] (env->dictionary *env*))

  IMeta
  (-meta [_] meta)

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

(def env (Environment. (env->dictionary *env*)
                       {}
                       map?
                       nil))

(add-watch env :update
           (fn [_ env past present]
             (doseq [[key value] present]
               (let [id (name key)]
                 (if-not (= (aget *env* id) value)
                   (if (nil? value)
                     (delete! *env* id)
                     (aset *env* id value)))))))



;(swap! env assoc :BAR "bar")
;(assert (= (:BAR env) "bar"))

;(swap! env dissoc :BAR)



;(:BAR @env)
;(.-BAR *env*)
