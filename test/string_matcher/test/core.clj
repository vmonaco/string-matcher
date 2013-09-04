(ns string-matcher.test.core
  (:use [string-matcher.core])
  (:use [clojure.test]))

(defn string-match-fixture [f]
  ;; setup the string matcher
  (def dictionary ["vinnie" "vinnie monaco" "v" "v m" "vm"])
  (def matcher (new-string-matcher dictionary))
  (f)
  
  )

(use-fixtures :each string-match-fixture)

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
  (is (= "vm" (match-string matcher "v vm")))
  )
  

  