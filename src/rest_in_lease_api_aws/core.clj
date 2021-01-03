
;POST https://3xhwkqfrmd.execute-api.us-east-2.amazonaws.com/Test/calc-profit
;{
;    "numLeasePeriods": 36,
;    "leaseRate" : 550,
;    "deprecRateYrly": 0.15,
;    "purchasePrice": 30000,
;    "refiInterestRateYrly": 0.03,
;    "refiAnnuity": 1000
;}

;GET https://3xhwkqfrmd.execute-api.us-east-2.amazonaws.com/Test/calc-profit?numLeasePeriods=36&leaseRate=650&deprecRateYrly=0.15&purchasePrice=30000&refiInterestRateYrly=0.03&refiAnnuity=1000

(ns rest-in-lease-api-aws.core
  (:require
    [clojure.data.json :as json]
   )
  (:gen-class
   :methods [^:static [handler [Object] Object]]
  )
)

;; ---- helper functions ----
(defn sumUpList [l & {:keys [vecPos] :or {vecPos eval}}] (reduce + (map #(vecPos %) l)))

(defn PadListRight [listToBePadded elem num]
    (defn createExtension [elem num] (if (<= num 0) nil (list* elem (createExtension elem (dec num)))))
    (concat listToBePadded (createExtension elem num))        
)

;; ---- leasing core  functions ----
(defn DeprecCashFlowListForPeriods [deprecRate remLeasePeriods remValue]
    (if (zero? remLeasePeriods) 
        nil 
        (let [deprecOfPeriod (* remValue deprecRate)]
            (list* deprecOfPeriod (DeprecCashFlowListForPeriods deprecRate (dec remLeasePeriods) (- remValue deprecOfPeriod)))
        )
    )
)

(defn AmortAndInterestCashFlowList [intRate annuity remDebt] ; returns a list of tuples ([amort interest] ...)
    (let [interest (* remDebt intRate)
          amort (min remDebt (- annuity interest))
         ]
        (if (>= interest annuity) ; will never end: refactor to exception raising 
            nil ; abort 
            (if (> remDebt 0) 
                (list* [amort interest] (AmortAndInterestCashFlowList intRate annuity (- remDebt amort)))
                nil
            )
        )
     )
)
(defn NetCashFlowList [leaseRate period remValue a&iCfList]
    (if (= (count a&iCfList) 0)
        nil
        (list* 
              (- 
                  (cond
                    (pos? period)   leaseRate
                    (zero? period)  remValue ; sell the asset
                    :else           0
                  )
                  (first (first a&iCfList)) 
                  (last  (first a&iCfList))
              )
              (NetCashFlowList leaseRate (dec period) remValue (rest a&iCfList))
        )
    )
)

(defn contractProfit [numLeasePeriods leaseRate deprecRate purchasePrice refiInterestRate refiAnnuity]
    (def deprecCashFlowList (DeprecCashFlowListForPeriods deprecRate numLeasePeriods purchasePrice))
    (def remValue (- purchasePrice (sumUpList deprecCashFlowList)))
    (def amortAndInterestCashFlowList (AmortAndInterestCashFlowList refiInterestRate refiAnnuity purchasePrice))  ; => ([amort interest] ...)
    (def amortAndInterestCashFlowListExt (PadListRight amortAndInterestCashFlowList [0 0] (+ (- numLeasePeriods (count amortAndInterestCashFlowList)) 1)))

    (def netCashFlowList (NetCashFlowList leaseRate numLeasePeriods remValue amortAndInterestCashFlowListExt))    
    
    (hash-map   
                "paid_amortization"       (Math/round (sumUpList amortAndInterestCashFlowList :vecPos first))
                "paid_interest"           (Math/round (sumUpList amortAndInterestCashFlowList :vecPos last))
                "amortization_periods"    (count amortAndInterestCashFlowList)
                "remaining_value"         (Math/round remValue) 
                "contract_profit"         (Math/round (sumUpList netCashFlowList)) 
    )
)
  
;; ---- funtions that handle interaction with the API Gateway --- 

(defn create-response [input-map]
  (let [  numLeasePeriods      (:numLeasePeriods       input-map) 
          leaseRate            (:leaseRate             input-map)
          deprecRate        (/ (:deprecRateYrly        input-map) 12)
          purchasePrice        (:purchasePrice         input-map)
          refiInterestRate  (/ (:refiInterestRateYrly  input-map) 12)
          refiAnnuity          (:refiAnnuity           input-map)
          return-map           (contractProfit numLeasePeriods leaseRate deprecRate purchasePrice refiInterestRate refiAnnuity)
       ]
    (prn "Inside create-response: return-map=" return-map) 
    return-map
  )
)

;(use '[ring.middleware.json :only [wrap-json-body wrap-json-response]] 
;     '[ring.util.response :only [response]])

(defn -handler [request] ; request is a Clojure map with keys and values in string form; the key "body" contains a JSON document as string
; POST handling
;  (def request-body-string (get-in request ["body"]))
;  (def request-body-as-map (json/read-str request-body-string :key-fn keyword)) ; convert the body string nto a proper Coljure map
;  (prn "handler: request/body as map =" request-body-as-map)
;
;  (create-response request-body-as-map)
;  ;((wrap-json-response create-response) request-body-as-map)

; GET handling
  (def request-params-map-strings (get-in request ["queryStringParameters"])) ; => map with key value pairs, both strings
; (prn "-handler: request-params-map-strings= " request-params-map-strings)
  (def request-params-map-keyed (into {} (for [[k v] request-params-map-strings] [(keyword k) (read-string v) ])))
; (prn "-handler: request-params-map-keyed= " request-params-map-keyed)
  (create-response request-params-map-keyed)
)