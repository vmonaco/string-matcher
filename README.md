# Clojure string matcher

The Clojure string matcher agent provides a simple way to lookup similar strings with a built in cache. The similarity between two strings is given by

    |adj(s1) ∩ adj(s2)|/(|adj(s1)| + |adj(s2)|)
    + |char(s1) ∩ char(s2)|/(|char(s1)| + |char(s2)|)

where adj(s1) is the bag of adjacent letter pairs in string s1, char(s1) is the bag of characters in string s1, and |·| is set cardinality. A bag is a set that may contain duplicate elements. Non-alphanumeric characters are ignored.

## Example

Create the string matcher with the supplied dictionary.

```clojure
(defn string-match-fixture [f]
  ;; setup the string matcher
  (def dictionary ["vinnie" "vinnie monaco" "v" "v m" "vm"])
  (def matcher (new-string-matcher dictionary))
  (f))
```

This test should pass.

```clojure
(deftest test-matcher
  ;; identical strings
  (is (= "vinnie" (match-string matcher "vinnie")))
  ;; case insensitive
  (is (= "vinnie" (match-string matcher "Vinnie")))
  ;; single letters
  (is (= "v" (match-string matcher "V")))
  ;; two single letters
  (is (= "v m" (match-string matcher "v M")))
  ;; one single letter
  (is (= "vm" (match-string matcher "v vm"))))
```

## Usage

To create an agent with mutable state:

```clojure
(defn new-string-matcher
"Create a new string matcher agent with the given dictionary"
  [dictionary]
  (agent {:cache {}
          :dictionary (set dictionary)}))
```

This creates a string-matcher agent with the given dictionary.

Agents are modified by sending an action, which can also take arguments. The `match-string` function does this:

```clojure
(defn match-string
"Find the closest string in the matcher. 
Cache the result if it hasn't already been"
[matcher string]
  (let [lookup (lookup-string matcher string)]
    (if lookup
      ;; already in cache, return the result
      lookup
      ;; need to find the closest string and update the state
      (let [closest (closest-string string (:dictionary @matcher))]
        (send matcher cache-string string closest)
        closest))))
```

The actions must return the new state of the agent. This is how caching is achieved:

```clojure
(defn cache-string
"Cache the closest string found"
[state key value]
  (let [old-cache (:cache state)]
    (assoc state
      :cache (assoc old-cache key value))))
```

The state of the agent can be accessed using the `@` operator.
