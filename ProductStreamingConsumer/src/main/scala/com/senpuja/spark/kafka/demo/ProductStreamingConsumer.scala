package com.senpuja.spark.kafka.demo

import java.text.SimpleDateFormat
import java.util.{Calendar, Properties, UUID}
import com.google.gson.Gson
import com.mongodb.spark.MongoSpark
import kafka.serializer.StringDecoder
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object ProductStreamingConsumer {
  case class ProductAggreration(product_name: String, total: Int, date_time: String)

  var brokers = ""
  var outTopic = ""

  def main(args: Array[String]): Unit = {
    //Kafka broker(node)
    brokers = util.Try(args(0)).getOrElse("localhost:9092")

    //Input the topic generated by the data producer
    val inTopic = util.Try(args(1)).getOrElse("prod-data")

    //The topic used to produce the data after streaming
    outTopic = util.Try(args(2)).getOrElse("product-data-visualization")

	//the duration in seconds in which you want to consume the data
    val batchDuration = util.Try(args(3)).getOrElse("60").toInt 


    //Create streaming context using Spark
    val streamCtx = new StreamingContext(SparkCommon.conf, Seconds(batchDuration))
    val sparkCtx = streamCtx.sparkContext
    val inTopicSet = Set(inTopic)
    val kafkaParams = Map[String, String](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
    )

    val msg = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      streamCtx,
      kafkaParams,
      inTopicSet
    )

    // Processing the data

    //get the value from the data received
    val value = msg.map(_._2)

    //name of the products
    val products = value.map(x => x.split(",")(1))

    //create a tuple with key-value pair
    val productsDStream = products.map(product => Tuple2(product, 1))
    val aggregatedProducts = productsDStream.reduceByKey(_+_)


    //print the processed data
    aggregatedProducts.print()

    //create dataframe for each RDD in the defined format
    aggregatedProducts.foreachRDD( rdd => {

      val today = Calendar.getInstance.getTime
      val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

      //Map the data
      val data = rdd.map(
        x => ProductAggreration(x._1, x._2, formatter.format(today))
      )

      data.foreachPartition(pushProductInfoInKafka)

    })

   //after formatting the data

    //Start the stream
    streamCtx.start()
    streamCtx.awaitTermination()
  }

  def pushProductInfoInKafka(items: Iterator[ProductAggreration]): Unit = {

    //Create the properties for kafka
    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("client.id", UUID.randomUUID().toString())
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")


    val producer = new KafkaProducer[String, String](props)

    items.foreach( obj => {
      val key = UUID.randomUUID().toString().split("-")(0)
      val gson = new Gson()
      val value = gson.toJson(obj)

      val data = new ProducerRecord[String, String](outTopic, key, value)

      println("--- topic: " + outTopic + " ---")
      println("key: " + data.key())
      println("value: " + data.value() + "\n")

      producer.send(data)

    })
    producer.close()
  }
}