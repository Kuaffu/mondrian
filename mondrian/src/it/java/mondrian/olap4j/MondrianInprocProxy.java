/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package mondrian.olap4j;

import mondrian.olap.Util;
import mondrian.tui.XmlaSupport;

import org.apache.commons.collections.map.ReferenceMap;

import org.olap4j.driver.xmla.XmlaOlap4jServerInfos;
import org.olap4j.driver.xmla.proxy.XmlaOlap4jProxy;

import java.util.*;
import java.util.concurrent.*;

/**
 * Proxy which implements XMLA requests by talking to mondrian
 * in-process. This is more convenient to debug than an inter-process
 * request using HTTP.
 *
 * @author jhyde
 */
public class MondrianInprocProxy
    implements XmlaOlap4jProxy
{
    private final ExecutorService executor =
        Util.getExecutorService(
            1, 1, 1, "MondrianInprocProxy$executor",
            new ThreadPoolExecutor.CallerRunsPolicy());
    private final Map<String, String> catalogNameUrls;
    private final String urlString;
    private final Map servletCache =
        new ReferenceMap(ReferenceMap.HARD, ReferenceMap.WEAK);

    /**
     * Creates and initializes a MondrianInprocProxy.
     *
     * @param catalogNameUrls Collection of catalog names and the URL where
     * their catalog is to be found. For testing purposes, this should contain
     * a catalog called "FoodMart".
     *
     * @param urlString JDBC connect string; must begin with "jdbc:mondrian:"
     */
    public MondrianInprocProxy(
        Map<String, String> catalogNameUrls,
        String urlString)
    {
        this.catalogNameUrls = catalogNameUrls;
        if (!urlString.startsWith("jdbc:mondrian:")) {
            throw new IllegalArgumentException();
        }
        this.urlString = urlString.substring("jdbc:mondrian:".length());
    }

    public byte[] get(
        XmlaOlap4jServerInfos infos,
        String request)
    {
        try {
            return XmlaSupport.processSoapXmla(
                request, urlString, catalogNameUrls, null, null, servletCache);
        } catch (Exception e) {
            throw new RuntimeException(
                "Error while reading '" + infos.getUrl() + "'", e);
        }
    }

    public Future<byte[]> submit(
        final XmlaOlap4jServerInfos infos,
        final String request)
    {
        return this.executor.submit(
            new Callable<byte[]>() {
                public byte[] call() throws Exception {
                    return get(infos, request);
                }
            });
    }

    public String getEncodingCharsetName() {
        return "UTF-8";
    }
}

// End MondrianInprocProxy.java
