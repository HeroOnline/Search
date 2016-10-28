package com.persist;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.persist.bean.SearchConfig;
import com.persist.bean.SearchResult;
import com.persist.bolt.SearchComputeBolt;
import com.persist.bolt.SearchDownloadBolt;
import com.persist.bolt.SearchNotifyBolt;
import com.persist.bolt.SearchRecordBolt;
import com.persist.util.helper.FileHelper;
import com.persist.util.tool.INotifier;
import com.persist.util.tool.IRecorder;
import com.persist.util.tool.SearchNotifier;
import com.persist.util.tool.SearchRecorder;
import storm.kafka.*;
import java.util.Arrays;

/**
 * Created by taozhiheng on 16-10-21.
 *
 *
 *
 */
public class SearchTopology {

    private static final String TAG = "grab-topology";
    public static final String URL_SPOUT = "kafka-spout";
    public static final String DOWNLOAD_BOLT = "download-bolt";
    public static final String COMPUTE_BOLT = "compute-bolt";
    public static final String NOTIFY_BOLT = "notify-bolt";
    public static final String RECORD_BOLT = "record-bolt";

    public static void main(String[] args) throws Exception
    {
        String configPath = "search_config.json";
        if (args.length > 0)
            configPath = args[0];

        //从json文件获取配置
        Gson gson = new Gson();
        SearchConfig baseConfig = new SearchConfig();
        System.out.println("config="+baseConfig);
        try {
            String text = FileHelper.readString(configPath);
            baseConfig = gson.fromJson(text, SearchConfig.class);
            System.out.println(gson.toJson(baseConfig));
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        //construct kafka spout config
        BrokerHosts brokerHosts = new ZkHosts(baseConfig.zks);
        SpoutConfig spoutConfig = new SpoutConfig(
                brokerHosts, baseConfig.topic, baseConfig.zkRoot, baseConfig.id);
        spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        spoutConfig.zkServers = Arrays.asList(baseConfig.zkServers);
        spoutConfig.zkPort = baseConfig.zkPort;
        spoutConfig.forceFromStart = false;

        //construct topology builder
        TopologyBuilder builder = new TopologyBuilder();
        //set KafkaSpuot
        builder.setSpout(URL_SPOUT, new KafkaSpout(spoutConfig), baseConfig.urlSpoutParallel);
        //set ImageDownloadBolt: shuffleGrouping from URL_SPOUT
        builder.setBolt(DOWNLOAD_BOLT, new SearchDownloadBolt(baseConfig.width, baseConfig.height), baseConfig.downloadBoltParallel)
                .shuffleGrouping(URL_SPOUT);
        //set ComputeBolt: shuffleGrouping from DOWNLOAD_BOLT
        builder.setBolt(COMPUTE_BOLT, new SearchComputeBolt(baseConfig.so, baseConfig.bufferSize, baseConfig.duration, baseConfig.tick), 1)
                .shuffleGrouping(DOWNLOAD_BOLT);
        //set NotifyBolt: shuffleGrouping from COMPUTE_BOLT(SAMPLE_STREAM_ID)
        INotifier<SearchResult> notifier = new SearchNotifier(baseConfig.redisHost, baseConfig.redisPort, baseConfig.redisPassword);
        SearchNotifyBolt notifyBolt = new SearchNotifyBolt(notifier);
        notifyBolt.appendStream(SearchComputeBolt.SAMPLE_STREAM_ID);
        builder.setBolt(NOTIFY_BOLT, notifyBolt, baseConfig.notifyBoltParallel)
                .shuffleGrouping(COMPUTE_BOLT, SearchComputeBolt.SAMPLE_STREAM_ID);
        //set RecordBolt: shuffleGrouping from COMPUTE_BOLT(NORMAL_STREAM_ID) , and shuffleGrouping from NOTIFY_BOLT
        IRecorder<SearchResult> recorder = new SearchRecorder(baseConfig.hbaseQuorum, baseConfig.hbasePort,
                baseConfig.hbaseFeatureTable, baseConfig.hbaseFeatureFamily, baseConfig.hbaseFeatureColumns,
                baseConfig.hbaseResultTable, baseConfig.hbaseResultFamily, baseConfig.hbaseResultColumns);
        SearchRecordBolt searchRecordBolt = new SearchRecordBolt(recorder);
        searchRecordBolt.appendStream(SearchComputeBolt.NORMAL_STREAM_ID);
        searchRecordBolt.appendComponent(NOTIFY_BOLT);
        builder.setBolt(RECORD_BOLT, searchRecordBolt, baseConfig.recordBoltParallel)
                .shuffleGrouping(COMPUTE_BOLT, SearchComputeBolt.NORMAL_STREAM_ID)
                .shuffleGrouping(NOTIFY_BOLT);

        //submit topology
        Config conf = new Config();
        if (args.length > 1) {
            conf.setNumWorkers(baseConfig.workerNum);
            conf.setDebug(false);
            StormSubmitter.submitTopology(args[1], conf, builder.createTopology());
        } else {
            conf.setDebug(true);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("SearchTopology", conf, builder.createTopology());
        }

    }

}
