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

### AWS Lambda service
The service itself is written in Clojure. 
Special remarks:
* once compiled into a jar-file with class-files, the JVM sees a module.class "rest_in_lease_api_aws.core" with a static method "handler" which is be called by AWS Gateway (see below)
* the Clojure service does not make use of Clojure's ring-framework. There was just no need at the end. The job could be done with closure.data.json only.
* the current implemented version processes a GET request that ontains the inputs in the query string. The version that processes POST requests with JSON as input is commented out. 
* the request passed to "handler" is a Clojure map with the query-string key-value-pairs as strings. 
* the service creates a Clojure map and returns it to the calling API Gateway. The conversion from the JSON message from API Gateway into a Clojure map and visa versa  is done magically automatically. When using the POST method (code is commented out at the moment), the "body" in the input JSON from API Gateway is a string, containing the actual JSON with the leasing input parameters. This string needs to be converted into a Clojure map explicitely by json/read-string.   

Build:
```
project-folder$ lein uberjar
```
Deploy:
Logon to: 
https://aws.amazon.com/de/
Go to: MyAccount / AWS Management Console / Lambda
Delete the AWS Lambda service
Then deploy the uberjar to AWS Lambda:
```
project-folder$ aws lambda create-function   --function-name rest-in-lease-api-aws   --handler rest_in_lease_api_aws.core::handler   --runtime java8   --memory 512   --timeout 10    --zip-file fileb://./target/uberjar/rest-in-lease-api-aws-0.1.0-SNAPSHOT-standalone.jar  --role arn:aws:iam::545854326725:role/service-role/aws-lambda-03-recv-ret-json-2-log-role-cbh95yyk
```


