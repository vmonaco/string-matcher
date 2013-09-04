(ns string-matcher.core
  (:require [string-matcher.bag :as bag]
            [clojure.contrib.string :as str]))

(defn alphanumeric-words-upper
  "Return a list of uppercase alphnumeric words in the string"
  [string]
  (->> string
       (str/split #"[^A-Za-z0-9]")
       (map str/upper-case)))

(defn letter-pairs 
  "Return a list of the adjacent letter pairs in a string"
  [string]
  (map (fn [a b] (str a b)) string (rest string)))

(defn letters
  "Return a list of characters from a list of words"
  [words]
  (vec (apply str words)))

(defn word-letter-pairs 
  "Return a list of all the letter pairs in the list of words"
  [words]
  (flatten (map letter-pairs words)))

(defn letter-pair-similarity
  "Compute the letter pair similarity between 2 strings.
With empty or 1-character strings, there are no letter pairs so
the similarity is 0. Identical strings will return 1.0"
  [string1 string2]
  (if (or (< (count string1) 2)
          (< (count string2) 2))
    0
    (let [bag1 (bag/bag-of (word-letter-pairs (alphanumeric-words-upper string1)))
          bag2 (bag/bag-of (word-letter-pairs (alphanumeric-words-upper string2)))]
      (double (/ (* 2 (count (bag/intersection bag1 bag2)))
                 (count (bag/union bag1 bag2)))))))

(defn letter-pair-character-similarity 
  "A combination of letter pair and character similarity.
Single characters are handled better than letter pair similarity.
Palindromes are scored higher. Identical strings will return 1.0"
  [string1 string2]
  (let [bag1 (-> string1 alphanumeric-words-upper word-letter-pairs bag/bag-of)
        bag2 (-> string2 alphanumeric-words-upper word-letter-pairs bag/bag-of)
        character-bag1 (-> string1 alphanumeric-words-upper letters bag/bag-of)
        character-bag2 (-> string2 alphanumeric-words-upper letters bag/bag-of)
        bag-union-count (count (bag/union bag1 bag2))
        letter-pair-score (/ (* 2 (count (bag/intersection bag1 bag2)))
                             (if (zero? bag-union-count) 1 bag-union-count))
        character-score (/ (* 2 (count (bag/intersection character-bag1 character-bag2)))
                           (count (bag/union character-bag1 character-bag2)))
        ]
    (if (zero? bag-union-count)
      (double character-score)
      (double (/ (+ letter-pair-score character-score) 2)))))


(defn closest-string
  "Find the closest string in the dictionary"
  [string dictionary]
  (let [score-string-pairs
        (map #(vector (letter-pair-character-similarity string %) %) dictionary)]
    (second (apply max-key first score-string-pairs))))

(defn new-string-matcher
  "Create a new string matcher agent with the given dictionary"
  [dictionary]
  (agent {:cache {}
          :dictionary (set dictionary)}))

(defn -add-to-dictionary
"Add new strings to the dictionary"
  [state new-strings]
  (assoc (dissoc state :dictionary :cache)
    :dictionary
    (into (:dictionary state) new-strings)
    :cache {}))

(defn add-to-dictionary
  "Adds new strings to the dictionary and clears the cache"
  [matcher new-strings]
  (send matcher -add-to-dictionary new-strings))

(defn cache-string
  "Cache the closest string found"
  [state key value]
  (let [old-cache (:cache state)]
    (assoc state
      :cache
      (assoc old-cache key value))))

(defn lookup-string
  "Lookup the closest string stored in cache"
  [matcher string]
  ((:cache @matcher) string))

(defn match-string 
  "Find the closest string in the matcher. Cache the result if it
hasn't already been"
  [matcher string]
    (let [lookup (lookup-string matcher string)]
      (if lookup
        ;; already in cache, return the result
        lookup
        ;; need to find the closest string and update the state
        (let [closest (closest-string string (:dictionary @matcher))]
          (send matcher cache-string string closest)
          closest))))