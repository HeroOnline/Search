package com.persist.util.tool;

import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.util.Properties;

/**
 * Created by taozhiheng on 16-10-25.
 *
 * search similar face images
 *
 */
public class Search {


    private static PyString quorum;
    private static PyInteger port;
    private static PyString table;
    private static PyArray columns;

    private static PythonInterpreter interpreter;
    private static PyFunction initFunction;
    private static PyFunction searchFunction;

    public static void init(String quorum, int port, String table, String family, String[] columns,
                            String pythonScript, String init, String search)
    {
        Search.quorum = new PyString(quorum);
        Search.port = new PyInteger(port);
        Search.table = new PyString(table);
        //columns[0] is family
        Search.columns = new PyArray(String.class, columns.length+1);
        int i = 0;
        Search.columns.set(i, new PyString(family));
        i++;
        for(String column : columns) {
//            Search.columns.append(new PyString(column));
            Search.columns.set(i, new PyString(column));
            i++;
        }
        //load python script

        Search.interpreter = new PythonInterpreter();
        Search.interpreter.execfile(pythonScript);
        Search.initFunction =  Search.interpreter.get(init, PyFunction.class);
        Search.searchFunction = Search.interpreter.get(search, PyFunction.class);

        //init hbase config
        initFunction.__call__(Search.quorum, Search.port, Search.table, Search.columns);
    }

    /**
     * @param feature the image feature value
     * @return the urls of the similar images
     * */
    public static Object[] search(float[] feature)
    {
        if(feature == null)
            return null;
        //invoke python interface to search\
        PyArray array = new PyArray(float.class, feature.length);
        int i = 0;
        for(float v : feature) {
            array.set(i, new PyFloat(v));
            i++;
        }
        PyObject obj = Search.searchFunction.__call__(array);
        PyList ret = (PyList)obj;
        Object[] results = (Object[])ret.__tojava__(Object[].class);
        return results;
    }

}
