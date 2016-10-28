package com.persist.util.tool;


import com.persist.bean.SearchResult;
import com.persist.util.helper.HBaseHelper;

/**
 * Created by taozhiheng on 16-10-21.
 *
 *
 *
 */
public class SearchRecorder implements IRecorder<SearchResult> {

    private final static String TAG = "PictureRecorderImpl";

    private HBaseHelper mHelper;
    private String quorum;
    private int port;

    private String featureTable;
    private String featureFamily;
    private String[] featureColumns;

    private String resultTable;
    private String resultFamily;
    private String[] resultColumns;

    public SearchRecorder(String quorum, int port,
                          String featureTable, String featureFamily, String[] featureColumns,
                          String resultTable, String resultFamily, String[] resultColumns)
    {
        this.quorum = quorum;
        this.port = port;
        this.featureTable = featureTable;
        this.featureFamily = featureFamily;
        this.featureColumns = featureColumns;
        this.resultTable = resultTable;
        this.resultFamily = resultFamily;
        this.resultColumns = resultColumns;
    }

    private void initHBase()
    {
        if(mHelper != null)
            return;
        mHelper = new HBaseHelper(quorum, port);
    }

    @Override
    public void prepare() {
        initHBase();
    }

    @Override
    public boolean record(SearchResult data) {
        boolean ok = false;

        if(mHelper == null)
            initHBase();

        if(mHelper != null && data != null)
        {
            //record details
            ok = true;
        }
        return ok;
    }

    @Override
    public void cleanup() {
        if(mHelper != null)
        {
            mHelper.close();
            mHelper = null;
        }
    }
}
