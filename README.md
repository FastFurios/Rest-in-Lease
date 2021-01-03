# Rest-in-Lease
Sample REST-Service written in Clojure and implemented on AWS Lamda and AWS API Gateway. 

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

Also, there is a simple webpage index.html in the clojure project's src folder. When called from a local webserver (e.g. MiniWebserver etc.) inputs can be entered and results from the Rest-in-Lease Lambda service are displayed.

## How it is implememted
This little sample application consists of the following elements: 
* the AWS Lambda service
* the AWS API Gateway configuration
* (optional) AWS CloudWatch
* (optional) the webpage to demo it 

### AWS Lambda service
The service itself is written in Clojure. 
Special remarks:
* once compiled into a jar-file with class-files, the JVM sees a module.class "rest_in_lease_api_aws.core" with a static method "handler" which is be called by AWS Gateway (see below)
* the Clojure service does not make use of Clojure's popular "ring" http-handling-framework. There was just no need at the end. The job could be done with closure.data.json.
* the current implemented version processes a GET request that ontains the inputs in the query string. The version that processes POST requests with JSON as input is commented out. 
* the request passed to "handler" is a Clojure map with the query-string key-value-pairs as strings. 
* the service creates a Clojure map and returns it to the calling API Gateway. The conversion from the JSON message from API Gateway into a Clojure map and visa versa  is done magically automatically. When using the POST method (code is commented out at the moment), the "body" in the input JSON from API Gateway is a string, containing the actual JSON with the leasing input parameters. This string needs to be converted into a Clojure map explicitely by json/read-string.   

Build:
```
project-folder$ lein uberjar
```
Deploy:
* Logon to: https://aws.amazon.com/de/
* Go to: MyAccount / AWS Management Console / Lambda
* Delete the AWS Lambda service
* Then deploy the uberjar to AWS Lambda:
```
project-folder$ aws lambda create-function   --function-name rest-in-lease-api-aws   --handler rest_in_lease_api_aws.core::handler   --runtime java8   --memory 512   --timeout 10    --zip-file fileb://./target/uberjar/rest-in-lease-api-aws-0.1.0-SNAPSHOT-standalone.jar  --role arn:aws:iam::545854326725:role/service-role/aws-lambda-03-recv-ret-json-2-log-role-cbh95yyk
```
* Check if upload was successful by refreshing the Lambda functions list in the AWS Lambda console
* Then continue with the steps in AWS Gateway, see below.

### AWS API Gateway
* if none yet existent, create a new API Gateway: 
  * protocol = http
  * Deploy / Stages: create a "Test" stage with Automatic Deployment disabled
  * Develop / Routes: set REST end-point path = /calc-profit, add GET route with an in Integration to a Lambda service, select the Lambda service "rest-in-lease-api-aws", set Payload Format Version to 2.0 and Grant API Gateway permission to invoke your Lambda function.
  * CORS: Access-Control-Allow-Origin = * , Access-Control-Allow-Headers = * , Access-Control-Allow-Methods = *
  * Logging: to AWS CloudWatch can be set up, e.g. with log desination "arn:aws:logs:us-east-2:545854326725:log-group:rest-in-lease-aws-api" 
 * When new version of the Lambda function was deployed:
   * Integrations: Manage Integrations: edit / save (to bind the newly deployed Lambda function to this API route) and deploy to stage "Test".
 
 ### AWS CloudWatch
 * For Lambda functions AWS logs into CloudWatch by default, see also:  Lambda > Functions > rest-in-lease-api-aws > Edit monitoring tools
 * For API Gateways logging has to be activated explicitely, see above.
 * To check logs: look under Log Groups for the ones setup for the AWS API Gateway and the AWS Lambda function 

## The webpage
* Run a local webserver on your machine
* Call the index.html page (in src/rest-in-lease-aws-api) 
* Enter input values, press button "Deckungbeitrag Vetrag"
* the results should show up as the first row in the table below.
* Be patient at the first request to the API as AWS Lambda takes a while to start the service. Subsequent calls are responded quickly.
Remark:
* CORS is enabled in the GET request as the request to the AWS API goes to another domain then the the domain the webpage came from (=localhost). It is important that CORS is set in the API Gateway as described above.  



