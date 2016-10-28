package com.persist.util.tool;

import com.persist.bean.SearchResult;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Created by taozhiheng on 16-10-21.
 *
 *
 *
 */
public class SearchNotifier implements INotifier<SearchResult>{


    private final static String TAG = "SearchNotifier";

    private Jedis mJedis;
    private String host;
    private int port;
    private String password;

    public SearchNotifier(String host, int port, String password)
    {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    private void initJedis()
    {
        if(mJedis != null)
            return;
        JedisPool pool = null;
        try {
            // JedisPool依赖于apache-commons-pools1
            JedisPoolConfig config = new JedisPoolConfig();
            pool = new JedisPool(config, host, port, 6000, password);
            mJedis = pool.getResource();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }


    @Override
    public void prepare() {
        initJedis();
    }

    @Override
    public boolean notify(String[] channels, SearchResult message) {
        boolean ok = false;
        if(mJedis == null) {
            initJedis();
        }
        if(mJedis != null && message.feature != null
                && message.feature.info != null && message.feature.info.url != null)
        {
            String content = message.result;
            try
            {
                if(channels != null && channels.length > 0)
                {
                    for(String channel : channels)
                    {
                        mJedis.publish(channel, content);
                    }
                }
                mJedis.publish(message.feature.info.url, content);
                ok = true;
            }
            catch (JedisConnectionException e)
            {
                cleanup();
            }
        }
        return ok;
    }

    @Override
    public void cleanup() {
        if(mJedis != null)
        {
            mJedis.close();
            mJedis = null;
        }
    }
}
