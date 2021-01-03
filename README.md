# Rest-in-Lease
Sample REST-Service written in Clojure and implemented on AWS Lamda and AWS Gateway. 

## What it does
The Lambda service is written in Clojure and it takes 6 parameters as inputs n the GET request, such as 
```
https://3xhwkqfrmd.execute-api.us-east-2.amazonaws.com/Test/calc-profit?numLeasePeriods=36&leaseRate=400&deprecRateYrly=0.15&purchasePrice=30000&refiInterestRateYrly=0.03&refiAnnuity=1000
```
It returns an JSON with the results, e.g.
```
{
    "remaining_value": 19075,
    "amortization_periods": 32,
    "contract_profit": 2251,
    "paid_amortization": 30000,
    "paid_interest": 1224
}
```
The service can be called via URL entered in a web browser, via Postmann or from bash: 
```
$ curl --location --request GET 'https://3xhwkqfrmd.execute-api.us-east-2.amazonaws.com/Test/calc-profit?numLeasePeriods=36&leaseRate=400&deprecRateYrly=0.15&purchasePrice=30000&refiInterestRateYrly=0.03&refiAnnuity=1000'
```

Also, there is a simple website index.html in the clojure project's src folder. When called from a local webserver (e.g. MiniWebserver etc.) inputs can be entered and results from the Rest-in-Lease Lambda service are displayed.

## How it is implememted
This little sample application consists of 3 elements: 
* the AWS Lambda service
* the AWS Gateway configuration
* (optional) the website to demo it 


