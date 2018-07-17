NPM Packages that I installed in order to create the realtime dashboard :-

	npm install --save express
	npm install --save socket.io
	npm install --save mongo-oplog-watch
	npm install --save nodemon

In case mongodb is used, will have to create these two databases :-

	mongod --port 27017 --dbpath C:\Temp\data\rs1 --replSet rs

	mongod --port 27018 --dbpath C:\Temp\data\rs2 --replSet rs	

	mongo localhost:27017

STEPS TO RUN THIS PROJECT:

1. Start the zookeeper server 

Goto C:\kafka\0.10.1.1\bin\windows path and execute the following command on cmd prompt:
zookeeper-server-start.bat C:\kafka\0.10.1.1\config\zookeeper.properties

2. Start the kafka server

Goto C:\kafka\0.10.1.1\bin\windows path and execute the following:
kafka-server-start.bat C:\kafka\0.10.1.1\config\server.properties

3. Create Kafka topics (DON'T NEED TO CREATE NOW AS IT HAS ALREADY BEEN CREATED)

Goto C:\kafka\0.10.1.1\bin\windows path and execute the following:
kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic product-data-visualization


4. Run(Submit) the Streaming consumer jar

Goto C:\spark\1.6.3\bin and execute:
spark-submit --class com.senpuja.spark.kafka.demo.ProductStreamingConsumer C:\Users\UC230235\IdeaProjects\ProductDataProducer\dist\product-streaming-consumer-1.0.jar

5. Run the DataProducer jar with the arguments like host, topic, time-interval, and the record range

goto C:\Users\UC230235\IdeaProjects\ProductDataProducer\dist and run:
java -cp product-data-producer-1.0.jar com.senpuja.kafka.demo.ProductDataProducer localhost:9092 prod-data 0 30 50 1200

6. run the index.js file

Goto C:\Projects\nodejs\realtime-dashboard and run:
nodemon index.js

7. Open localhost:8080 in browser
