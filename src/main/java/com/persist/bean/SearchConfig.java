package com.persist.bean;

/**
 * Created by taozhiheng on 16-10-21.
 *
 *
 */
public class SearchConfig {

    //the library to compute
    public String so = "CalculatorImpl";
    //the image width and height
    public int width = 227;
    public int height = 227;
    //the image buffer size and timeout duration
    //If there are more bufferSize images or the time from last prediction to now is more than duration,
    //the computation will be triggered
    public int bufferSize = 1000;
    public long duration = 3000;
    //the tick time to check and flush buffer (time: tick seconds)
    public int tick = 10;

    //the KafkaSpout parallelism
    public int urlSpoutParallel = 1;
    //the ImageDownloadBolt parallelism
    public int downloadBoltParallel = 5;
    //the NotifyBolt parallelism
    public int notifyBoltParallel = 3;
    //the RecordBolt parallelism
    public int recordBoltParallel = 3;

    //split multi zk with ','
    public String zks = "zk01:2181,zk02:2181,zk03:2181/kafka";
    //the brokers zk path
    public String zkPath = "/brokers";
    //the top name of the msg from kafka
    public String topic = "search";
    //the zk root dir to store zk data
    public String zkRoot = "/storm/search";
    //the kafka consumer id which seems useless
    public String id = "consumer-search";
    //the zk servers' hostname or ip
    public String[] zkServers;
    //the client port of zk servers
    public int zkPort = 2181;

    //the redis hostname or ip
    public String redisHost = "develop.finalshares.com";
    //the redis client port
    public int redisPort = 6379;
    //the redis password
    public String redisPassword = "redis.2016@develop.finalshares.com";

    //the hbase server hostname or ip
    public String hbaseQuorum = "192.168.0.189";
    //the client port of the zk in hbase server
    public int hbasePort = 2181;
    //the feature table
    public String hbaseFeatureTable = "features";
    public String hbaseFeatureFamily = "info";
    public String[] hbaseFeatureColumns;
    //the search result table
    public String hbaseResultTable = "results";
    public String hbaseResultFamily = "info";
    public String[] hbaseResultColumns;

    //the worker number (process number), suggest to set 1,
    //otherwise the gpu resources may be not enough, because each process uses dependent gpu resources
    public int workerNum= 1;

    public SearchConfig()
    {

    }

}
