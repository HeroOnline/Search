package com.persist.test;

import com.persist.util.tool.Search;

import java.util.Arrays;

/**
 * Created by taozhiheng on 16-10-25.
 *
 * just for testing invoking python script in java
 *
 */
public class SearchTest {

    public static void main(String[] args)
    {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String script = args[2];
//        String host = "192.168.1.3";
//        int port = 9090;
//        String script = "search.py";
        Search.init(host, port, "search", "info", new String[]{"feature"},
                script, "init", "search");
        float[] floats = new float[]{0, 1, 2, 3, 4, 5};
        Object[] results = Search.search(floats);
        for(Object obj : results)
            System.out.println(obj);
//        System.out.println(Arrays.toString(results));
    }
}
