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

package com.microfocus.application.automation.tools.sse.sdk;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientResponse extends Response {

    private final String content;
    private Throwable failure;
    private int statusCode;
    private byte[] data;
    private Map<String, List<String>> headerMap;

    public HttpClientResponse(HttpResponse httpResponse) throws IOException {
        headerMap = new HashMap<String, List<String>>();
        Header[] headers = httpResponse.getAllHeaders();
        for (Header header : headers) {
            List<String> values = new ArrayList<String>();
            values.add(header.getValue());
            headerMap.put(header.getName().toLowerCase(), values);
        }

        content = EntityUtils.toString(httpResponse.getEntity());
        data = content.getBytes();
        statusCode = httpResponse.getStatusLine().getStatusCode();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headerMap;
    }

    @Override
    public void setHeaders(Map<String, List<String>> responseHeaders) {
        headerMap = responseHeaders;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public Throwable getFailure() {
        return failure;
    }

    @Override
    public void setFailure(Throwable cause) {
        this.failure = cause;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public boolean isOk() {
        return getFailure() == null
                && (getStatusCode() == HttpURLConnection.HTTP_OK
                || getStatusCode() == HttpURLConnection.HTTP_CREATED || getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
    }

    @Override
    public String toString() {
        return content;
    }
}
