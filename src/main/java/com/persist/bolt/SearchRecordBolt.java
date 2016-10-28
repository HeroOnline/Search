package com.persist.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.persist.bean.ImageFeature;
import com.persist.bean.SearchResult;
import com.persist.util.helper.FileLogger;
import com.persist.util.tool.IRecorder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by taozhiheng on 16-10-21.
 *
 * record features or search results into hbase
 *
 */
public class SearchRecordBolt extends BaseRichBolt{

    private final static String TAG = "SearchRecordBolt";

    private OutputCollector mCollector;
    private IRecorder<SearchResult> mRecorder;

    private Map<String, Boolean> mStreams;
    private Map<String, Boolean> mComponents;

    private FileLogger mLogger;
    private int id;
    private long count = 0;

    public SearchRecordBolt(IRecorder<SearchResult> recorder)
    {
        this.mRecorder = recorder;
    }

    public void appendStream(String streamId)
    {
        if(mStreams == null)
        {
            mStreams = new HashMap<>();
        }
        if(streamId != null)
        {
            mStreams.put(streamId, true);
        }
    }

    public void appendComponent(String componentId)
    {
        if(mComponents == null)
        {
            mComponents = new HashMap<>();
        }
        if(componentId != null)
        {
            mComponents.put(componentId, true);
        }
    }

    public void removeStream(String streamId)
    {
        if(mStreams != null && streamId != null)
        {
            mStreams.remove(streamId);
        }
    }

    public void removeComponent(String componentId)
    {
        if(mComponents != null && componentId != null)
        {
            mComponents.remove(componentId);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if(mRecorder != null)
        {
            mRecorder.cleanup();
        }
        mLogger.close();
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        mCollector = collector;
        if(mRecorder != null)
        {
            mRecorder.prepare();
        }
        id = context.getThisTaskId();
        mLogger = new FileLogger("search-record@"+id);
        mLogger.log(TAG+"@"+id, "prepare");
    }

    @Override
    public void execute(Tuple input) {

        ImageFeature feature;
        //sample tuple
        if(mComponents != null && mComponents.containsKey(input.getSourceComponent()))
        {

            feature = (ImageFeature) input.getValue(0);
            String result = (String)input.getValue(1);
            boolean status = mRecorder.record(new SearchResult(feature, result));
            mLogger.log(TAG+"@"+this.id, "filter=streamId, url="+feature.info.url+", result="+result+", status="+status);
            mCollector.ack(input);
        }
        //normal tuple
        else if(mStreams != null && mStreams.containsKey(input.getSourceStreamId()))
        {

            feature = (ImageFeature) input.getValue(0);
            boolean status = mRecorder.record(new SearchResult(feature, null));
            mLogger.log(TAG+"@"+this.id, "filter=componentId, url="+feature.info.url+ ", feature="+ Arrays.toString(feature.feature)+", status="+status);
            mCollector.ack(input);
        }
        else
        {
            //unknown source tuple
            mLogger.log(TAG+"@"+this.id, "unknown source tuple, streamId="+input.getSourceStreamId()
            +", componentId="+input.getSourceComponent());
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
