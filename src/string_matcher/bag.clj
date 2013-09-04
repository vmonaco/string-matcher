(ns string-matcher.bag)

(defprotocol SetOps
  (disjoin* [this obj])
  (has* [this obj])
  (total [this obj])
  (counts [this]))

(defn disjoin
  ([s] s)
  ([s obj]
     (disjoin* s obj))
  ([s obj & more]
     (apply disjoin (disjoin s obj) more)))

(defn difference
  ([s] s)
  ([s other]
     (apply disjoin s other))
  ([s other & more]
     (apply difference (difference s other) more)))

(defn intersection
  ([s] s)
  ([s other]
     (difference s (difference s other)))
  ([s other & more]
     (apply intersection (intersection s other) more)))

(defn union
  ([s] s)
  ([s other]
     (if (empty? other) s (apply conj s other)))
  ([s other & more]
     (apply union (union s other) more)))

(defn has?
  ([s] true)
  ([s obj]
     (has* s obj))
  ([s obj & more]
     (and (has? s obj) (apply has? s more))))

(extend-type clojure.lang.IPersistentSet
  SetOps
  (disjoin* [this obj] (disj this obj))
  (has* [this obj] (contains? this obj))
  (total [this obj] (if (contains? this obj) 1 0))
  (counts [this] (zipmap this (repeat 1))))

(deftype Bag [m c]
  clojure.lang.IObj
  (withMeta [this, mtd]
    (Bag. (with-meta m mtd) c))
  (meta [this] (meta m))
  clojure.lang.IPersistentCollection
  (count [this] c)
  (empty [this] (Bag. {} 0))
  (equiv [this other] (= (seq this) other))
  (seq [this]
    (seq
     (mapcat
      (fn [[k v]]
        (repeat v k))
      m)))
  (cons [this obj]
    (Bag. (merge-with + m {obj 1}) (inc c)))
  SetOps
  (disjoin* [this obj]
    (if-let [n (m obj)]
      (if (= 1 n)
        (Bag. (dissoc m obj) (dec c))
        (Bag. (assoc m obj (dec n)) (dec c)))
      this))
  (has* [this obj] (contains? m obj))
  (total [this obj] (m obj 0))
  (counts [this] m)
  clojure.lang.Counted
  Object
  (toString [this]
    (apply str
           (interpose " "
                      (seq this)))))

(defn bag [& objects]
  (Bag. (frequencies objects) (count objects)))

(defn bag-of [coll]
  (apply bag coll))