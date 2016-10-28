package com.persist.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.persist.bean.ComputeInfo;
import com.persist.bean.ImageInfo;
import com.persist.util.helper.BufferedImageHelper;
import com.persist.util.helper.FileHelper;
import com.persist.util.helper.FileLogger;
import com.persist.util.helper.HDFSHelper;
import com.persist.util.tool.Compute;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by taozhiheng on 16-10-20.
 *
 * download face images from network or hdfs, put images data into compute buffer area,
 * and send signal to ComputeBolt at appropriate time
 *
 */
public class SearchDownloadBolt extends BaseRichBolt {

    private final static String TAG = "SearchDownloadBolt";

    private OutputCollector mCollector;
    private Gson mGson;
    private HDFSHelper mHelper;

    private int mWidth = 227;
    private int mHeight = 227;

    private FileLogger mLogger;
    private int id;
    private long count = 0;

    public SearchDownloadBolt(int width, int height)
    {
        this.mWidth = width;
        this.mHeight = height;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        mHelper.close();
        mLogger.close();
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        mCollector = collector;
        mGson = new Gson();
        mHelper = new HDFSHelper(null);
        id = context.getThisTaskId();
        mLogger = new FileLogger("search-download@"+id);
//        System.setProperty("java.awt.headless", "true");
        mLogger.log(TAG+"@"+id, "prepare");
    }


    @Override
    public void execute(Tuple input) {
        String data = input.getString(0);
        try
        {
            ImageInfo info = mGson.fromJson(data, ImageInfo.class);
            //valid input, try to download the image
            if(info != null && info.url != null && info.url.length() > 0)
            {
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    //download image
                    boolean ok = false;

                    if(info.inner)
                    {
                        ok = mHelper.download(os, info.url);
                        mLogger.log(TAG + "@" + id, "download inner image from " + info.url+", status="+ok);
                    }
                    else
                    {
                        ok = FileHelper.download(os, info.url);
                        mLogger.log(TAG + "@" + id, "download outer image from " + info.url+", status="+ok);
                    }
                    if(ok)
                    {
                        InputStream in = new ByteArrayInputStream(os.toByteArray());
                        BufferedImage image = ImageIO.read(in);
                        //the image is null, just ignore
                        if(image == null)
                        {
                            mLogger.log(TAG+"@"+this.id, "fail downloading image from "+info.url);
                            os.close();
                            mCollector.ack(input);
                            return;
                        }
                        mLogger.log(TAG + "@" + id, "succeed downloading image from " + info.url);
                        //check and resize image
                        if (image.getWidth() != mWidth || image.getHeight() != mHeight) {
//                                image = ImageHelper.resize(image, mWidth, mHeight);
                            image = BufferedImageHelper.resize(image, mWidth, mHeight);

                        }
                        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer())
                                .getData();
                        //put image to compute buffer
                        boolean ready = Compute.append(info, new ComputeInfo(info.url, pixels, mWidth, mHeight));
                        count++;
                        boolean force = (info.type == ImageInfo.TYPE_SAMPLE);
                        mLogger.log(TAG + "@" + this.id, "append " + info.url + " ok, total=" + count
                                +", ready="+ready+", force="+force);

                        if(ready || force)
                        {
                            //trigger prediction
                            mCollector.emit(new Values(force));
                        }
                    }
                    else
                    {
                        mLogger.log(TAG+"@"+this.id, "fail downloading image from "+info.url);
                    }
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace(mLogger.getPrintWriter());
                    mLogger.getPrintWriter().flush();
                }
            }
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace(mLogger.getPrintWriter());
            mLogger.getPrintWriter().flush();
        }
        finally {
            mCollector.ack(input);
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("signal"));
    }
}
