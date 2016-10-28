package com.persist.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.persist.bean.ImageFeature;
import com.persist.bean.SearchResult;
import com.persist.util.helper.FileLogger;
import com.persist.util.tool.INotifier;
import com.persist.util.tool.Search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by taozhiheng on 16-10-21.
 *
 * search similar images and notify invoker,
 * then submit result to RecordBolt for recording
 *
 */
public class SearchNotifyBolt extends BaseRichBolt {

    private final static String TAG = "SearchNotifyBolt";

    private OutputCollector mCollector;
    private INotifier<SearchResult> mNotifier;

    private Map<String, Boolean> mStreams;


    private FileLogger mLogger;
    private int id;
    private long count = 0;

    public SearchNotifyBolt(INotifier<SearchResult> notifier)
    {
        this.mNotifier = notifier;
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


    public void removeStream(String streamId)
    {
        if(mStreams != null && streamId != null)
        {
            mStreams.remove(streamId);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if(mNotifier != null)
        {
            mNotifier.cleanup();
        }
        mLogger.close();
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        mCollector = collector;
        if(mNotifier != null)
        {
            mNotifier.prepare();
        }
        id = context.getThisTaskId();
        mLogger = new FileLogger("search-notify@"+id);
        mLogger.log(TAG+"@"+id, "prepare");
    }

    @Override
    public void execute(Tuple input) {
        if(mStreams == null || !mStreams.containsKey(input.getSourceStreamId()))
        {
            //unknown source tuple
            mLogger.log(TAG+"@"+this.id, "unknown source tuple");
            return;
        }
        ImageFeature feature = (ImageFeature) input.getValue(0);
        //search result set from hbase or somewhere else
//        Object[] resultSet = Search.search(feature.feature);
        Object[] resultSet = new Object[]{"just", "for", "test"};
        String url = feature.info.url;
        String results = Arrays.toString(resultSet);
        boolean status = false;
        if(mNotifier != null)
        {
            status = mNotifier.notify(new String[]{url}, new SearchResult(feature, results));
        }
        mCollector.emit(new Values(feature, results, status));
        mLogger.log(TAG+"@"+this.id, "url="+url+", results="+results+", status="+status);
        mCollector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sample", "results", "status"));
    }
}
