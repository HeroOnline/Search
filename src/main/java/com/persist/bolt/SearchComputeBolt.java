package com.persist.bolt;

import backtype.storm.Config;
import backtype.storm.Constants;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.persist.bean.ImageFeature;
import com.persist.bean.ImageInfo;
import com.persist.util.helper.FileLogger;
import com.persist.util.tool.Compute;

import java.util.List;
import java.util.Map;

/**
 * Created by taozhiheng on 16-10-21.
 *
 * compute feature values of face images,
 * then submit results to NotifierBolt(TYPE_SAMPLE) or RecordBolt(TYPE_NORMAL)
 *
 */
public class SearchComputeBolt extends BaseRichBolt{


    public final static String NORMAL_STREAM_ID = "compute-normal";
    public final static String SAMPLE_STREAM_ID = "compute-sample";

    private final static String TAG = "SearchComputeBolt";

    private final static int DEFAULT_BUFFER_SIZE = 1000;
    private final static long DEFAULT_DURATION = 3000;
    private final static int DEFALUT_TICK = 10;

    private OutputCollector mCollector;
    private int mTick;

    private FileLogger mLogger;
    private int id;
    private long count = 0;


    private String so;
    private int bufferSize;
    private long duration;


    public SearchComputeBolt(String so)
    {
        this(so, DEFAULT_BUFFER_SIZE, DEFAULT_DURATION, DEFALUT_TICK);
    }

    public SearchComputeBolt(String so, int bufferSize)
    {
        this(so, bufferSize, DEFAULT_DURATION, DEFALUT_TICK);
    }

    public SearchComputeBolt(String so, int bufferSize, long duration, int tick)
    {
        this.so = so;
        this.bufferSize = bufferSize;
        this.duration = duration;
        this.mTick = tick;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        mLogger.close();
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        mCollector = collector;
        Compute.init(so);
        Compute.setBufferSize(bufferSize);
        Compute.setDuration(duration);
        id = context.getThisTaskId();
        mLogger = new FileLogger("search-compute@"+id);
        mLogger.log(TAG+"@"+id, "prepare");
        Compute.setLogger(mLogger);
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        //set a tick tuple sender to flush compute buffer
        Config config = new Config();
        config.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, mTick);
        return config;
    }

    @Override
    public void execute(Tuple input) {
        boolean force = false;
        //receive tick tuple, flush compute buffer
        if(input.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
                && input.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID))
        {
            force = true;
            mLogger.log(TAG, "receive tick tuple, force to trigger compute");
        }
        //receive standard tuple
        else
        {
            force = input.getBoolean(0);
        }
        mLogger.log(TAG+"@"+id, "trigger compute, force="+force);
        List<ImageFeature> features = Compute.triggerCompute(force);
        if(features != null && features.size() > 0)
        {
            for(ImageFeature feature : features)
            {
                if(feature.info.type == ImageInfo.TYPE_NORMAL)
                    mCollector.emit(NORMAL_STREAM_ID, new Values(feature));
                else
                    mCollector.emit(SAMPLE_STREAM_ID, new Values(feature));
                mLogger.log(TAG+"@"+this.id, "emit feature: url="+feature.info.url+", type="+feature.info.type);
            }
            count += features.size();
            mLogger.log(TAG+"@"+id, "size="+features.size()+", total="+count);
        }
        mCollector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(NORMAL_STREAM_ID, new Fields("feature"));
        declarer.declareStream(SAMPLE_STREAM_ID, new Fields("feature"));
    }
}
