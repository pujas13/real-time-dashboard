var port = 8080;

var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

var kafka = require('kafka-node');
var topic = 'product-data-visualization';
var client = new kafka.Client('localhost:2181', "consumer-" + Math.floor(Math.random() * 10000));
var topicSet = [{ topic: topic }];
var consumer = new kafka.Consumer(client, topicSet);


app.get('/', function(req, res){
    res.sendFile('web/index.html', {root: __dirname});
});

io = io.on('connection', function(socket){
    console.log('a user connected');
    socket.on('disconnect', function(){
        console.log('a user disconnected');
    });
});

//collect data from Kafka via product-data-visualization topic and emit them to clients
consumer = consumer.on('message', function(data){
    console.log(data.value);
    io.emit("aggregator-message", data.value);
});


http.listen(port, function(){
    console.log("Running on port " + port)
});