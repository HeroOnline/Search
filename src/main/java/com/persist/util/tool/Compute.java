package com.persist.util.tool;


import com.persist.bean.ComputeInfo;
import com.persist.bean.ImageFeature;
import com.persist.bean.ImageInfo;
import com.persist.util.helper.FileLogger;

import java.util.*;

/**
 * Created by taozhiheng on 16-10-21.
 *
 * compute feature values of face images
 *
 */
public class Compute {

    private final static String TAG = "Predict";
    private static boolean hasLoad = false;
    private static String so = "/home/hadoop/lib/libcaffe.so";

    private static long duration = 3000;
    private static long putTime;

    private static int bufferSize = 1000;
    //images info buffer
    private static Map<String, ImageInfo> infoBuffer = new HashMap<String, ImageInfo>();
    //images data buffer
    private static List<ComputeInfo> buffer = new ArrayList<ComputeInfo>();

    private static FileLogger mLogger;

    public static void init(String lib)
    {
        so = lib;
    }

    public static void setBufferSize(int size)
    {
        bufferSize = size;
    }

    public static void setDuration(long d)
    {
        duration = d;
    }

    public static void setLogger(FileLogger logger)
    {
        mLogger = logger;
    }

    /**
     * put image to buffer area
     * @param imageInfo the image description
     * @param computeInfo the image identity and entity data
     * @return whether the buffer area is full
     * */
    public synchronized static boolean append(ImageInfo imageInfo, ComputeInfo computeInfo)
    {
        if(imageInfo != null && computeInfo != null)
        {
            infoBuffer.put(imageInfo.url, imageInfo);
            buffer.add(computeInfo);
            int size = buffer.size();
            if(size == 1)
            {
                putTime = System.currentTimeMillis();
                return size >= bufferSize;
            }
            else if(size > 1)
            {
                long d = System.currentTimeMillis() - putTime;
                return size >= bufferSize || d >= duration;
            }
        }
        return false;
    }

    /**
     * trigger gpu to compute feature values of face images
     * @param force force gpu to compute if it is true
     * @return the result or null
     * */
    public synchronized static List<ImageFeature> triggerCompute(boolean force)
    {
        boolean sizeReady = buffer.size() >= bufferSize;
        long d = System.currentTimeMillis() - putTime;
        boolean timeReady = d >= duration;
        if(!force && !sizeReady && !timeReady)
        {
            return null;
        }
        Map<String, float[]> map = computeProxy(buffer);
        List<ImageFeature> features = null;
        ImageInfo info;
        float[] feature;
        if(map != null)
        {
            features = new ArrayList<>(map.size());
            for(Map.Entry<String, float[]> entry : map.entrySet())
            {
                info = infoBuffer.get(entry.getKey());
                feature = entry.getValue();
                if(info == null)
                {
                    info = new ImageInfo(entry.getKey(), "unknown", "unknown", true, ImageInfo.TYPE_NORMAL);
                }
                features.add(new ImageFeature(info, feature));
            }
        }
        buffer.clear();
        infoBuffer.clear();
        return features;
    }

    /**
     * compute feature values by invoking native method
     * */
    public static Map<String, float[]> computeProxy(List<ComputeInfo> images)
    {
//        if(!hasLoad)
//        {
//            if(so.contains(".so"))
//                System.load(so);
//            else
//                System.loadLibrary(so);
//            hasLoad = true;
//        }
        if(images == null || images.size() <= 0)
            return null;
        //just for test
        Map<String, float[]> map = new LinkedHashMap<>();
        float[] testValue = new float[]{0, 6, 0, 7};
        for(ComputeInfo info : images)
        {
            map.put(info.key, testValue);
        }
        return map;
//        return compute(images);
    }

    /**
     * real compute algorithm which is implemented by cpp, and invoked by XXX.so in linux
     *
     * how to build XXX.so:
     * No.1:    javah com.neptune.api.Compute
     * No.2:    g++ -I$JAVA_HOME/include -I$JAVA_HOME/include/linux
     *          -fPIC -shared XXX.cpp -o libXXX.so
     * */
    private static native Map<String, float[]> compute(List<ComputeInfo> images);

}
