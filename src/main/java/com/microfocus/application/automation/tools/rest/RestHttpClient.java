/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.microfocus.application.automation.tools.rest;

import com.microfocus.application.automation.tools.common.SSEException;
import com.microfocus.application.automation.tools.sse.sdk.Client;
import com.microfocus.application.automation.tools.sse.sdk.HttpClientResponse;
import com.microfocus.application.automation.tools.sse.sdk.HttpRequestDecorator;
import com.microfocus.application.automation.tools.sse.sdk.Logger;
import com.microfocus.application.automation.tools.sse.sdk.ResourceAccessLevel;
import com.microfocus.application.automation.tools.sse.sdk.Response;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class RestHttpClient implements Client {

    private final String serverUrl;
    private final String restPrefix;
    private final String webuiPrefix;
    private final String username;
    private final Logger logger;

    private CloseableHttpClient httpCient;

    public RestHttpClient(String url, String domain, String project, String username, Logger logger) {
        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(
                ProxySelector.getDefault());
        httpCient = HttpClients.custom()
                .setRoutePlanner(routePlanner)
                .build();

        if (!url.endsWith("/")) {
            url = String.format("%s/", url);
        }
        serverUrl = url;
        this.username = username;
        restPrefix =
                getPrefixUrl(
                        "rest",
                        String.format("domains/%s", domain),
                        String.format("projects/%s", project));
        webuiPrefix = getPrefixUrl("webui/alm", domain, project);

        this.logger = logger;
    }

    @Override
    public Response httpGet(String url, String queryString, Map<String, String> headers, ResourceAccessLevel resourceAccessLevel) {
        if ((queryString != null) && !queryString.isEmpty()) {
            url += "?" + URLEncoder.encode(queryString);
        }
        try {
            headers = buildHeader(headers, resourceAccessLevel);
            HttpGet httpGet = new HttpGet(url);
            return dohttp(httpCient, httpGet, headers);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new SSEException(e);
        }
    }

    @Override
    public Response httpPost(String url, String content, Map<String, String> headers, ResourceAccessLevel resourceAccessLevel) {
        try {
            headers = buildHeader(headers, resourceAccessLevel);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(content));
            return dohttp(httpCient, httpPost, headers);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new SSEException(e);
        }
    }

    @Override
    public Response httpPut(String url, String content, Map<String, String> headers, ResourceAccessLevel resourceAccessLevel) {
        try {
            headers = buildHeader(headers, resourceAccessLevel);
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(new StringEntity(content));
            return dohttp(httpCient, httpPut, headers);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new SSEException(e);
        }
    }

    @Override
    public String build(String suffix) {
        return String.format("%1$s%2$s", serverUrl, suffix);
    }

    @Override
    public String buildRestRequest(String suffix) {
        return String.format("%1$s/%2$s", restPrefix, suffix);
    }

    @Override
    public String buildWebUIRequest(String suffix) {
        return String.format("%1$s/%2$s", webuiPrefix, suffix);
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getUsername() {
        return username;
    }

    private String getPrefixUrl(String protocol, String domain, String project) {
        return String.format("%s%s/%s/%s", serverUrl, protocol, domain, project);
    }

    private Response dohttp(HttpClient httpCient, HttpRequestBase httpRequestBase, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequestBase.addHeader(key, headers.get(key));
        }
        HttpResponse httpresponse = null;
        try {
            httpresponse = httpCient.execute(httpRequestBase);
            return new HttpClientResponse(httpresponse);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new SSEException(e);
        }
    }

    private Map<String, String> buildHeader(Map<String, String> headers, ResourceAccessLevel resourceAccessLevel) {
        Map<String, String> decoratedHeaders = new HashMap<String, String>();
        if (headers != null) {
            decoratedHeaders.putAll(headers);
        } else {
            headers = new HashMap<>();
        }
        HttpRequestDecorator.decorateHeaderWithUserInfo(
                decoratedHeaders,
                getUsername(),
                resourceAccessLevel);
        headers.putAll(decoratedHeaders);
        return headers;
    }
}
