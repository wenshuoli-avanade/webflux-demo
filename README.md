# weflux-demo
weflux-demo

I packaged the jars of demo application and wiremock, which are in the directory /webflux-demo/runable.

The application is configured using port 18091.

The wiremock upstream service is using port 18092.

If those two ports are conflict on your machine, please change it at your convenience. (in application.yml)

I integrate the wiremock in my unit test. So, there is no need to run standalone wiremock during running unit test.


Here is the command running application jar and wiremock jar

java -jar .\webflux-demo-0.0.1-SNAPSHOT.jar

java -jar .\wiremock-jre8-standalone-2.33.2.jar --port=18092

Endpoints:

Retrieve products
GET /v1/product/

Retrieve a single product
GET /v1/product/{id}

Request “book” a product with an inventory id
POST /v1/product/{inventoryId}
