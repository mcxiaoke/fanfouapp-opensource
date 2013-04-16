/*******************************************************************************
 * Copyright 2011, 2012, 2013 fanfou.com, Xiaoke, Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fanfou.app.opensource.http;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.util.NetworkHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.02
 * @version 1.1 2011.05.03
 * @version 1.2 2011.05.03
 * @version 1.3 2011.05.04
 * @version 1.4 2011.05.05
 * @version 1.5 2011.10.25
 * @version 2.0 2011.11.03
 * @version 2.1 2011.11.04
 * @version 3.0 2011.11.09
 * @version 3.1 2011.11.15
 * @version 3.2 2011.11.24
 * @version 3.3 2011.11.28
 * @version 3.4 2011.11.29
 * @version 4.0 2011.12.01
 * @version 4.1 2011.12.02
 * @version 4.2 2011.12.05
 * @version 4.3 2011.12.07
 * @version 5.0 2011.12.12
 * 
 */
public class SimpleClient {

    private static final String TAG = SimpleClient.class.getSimpleName();

    private final Context mAppContext;

    public SimpleClient(final Context context) {
        this.mAppContext = context.getApplicationContext();
    }

    public HttpResponse exec(final SimpleRequest cr) throws IOException {
        if (TextUtils.isEmpty(cr.url)) {
            throw new IOException("request url must not be empty or null.");
        }
        signRequest(cr);
        return executeImpl(cr.getHttpRequest());
    }

    private final HttpResponse executeImpl(final HttpRequestBase request)
            throws IOException {
        if (request == null) {
            throw new IOException("http request is null");
        }
        final HttpClient client = getHttpClient();
        if (AppContext.DEBUG) {
            Log.d(SimpleClient.TAG, "[Request] "
                    + request.getRequestLine().toString());
        }
        final HttpResponse response = client.execute(request);
        if (AppContext.DEBUG) {
            Log.d(SimpleClient.TAG, "[Response] "
                    + response.getStatusLine().toString());
        }
        return response;
    }

    public final HttpResponse get(final String url) throws IOException {
        return executeImpl(new HttpGet(url));
    }

    public final Bitmap getBitmap(final String url) throws IOException {
        final HttpResponse response = get(url);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (AppContext.DEBUG) {
            Log.d(SimpleClient.TAG, "getBitmap() statusCode=" + statusCode
                    + " [" + url + "]");
        }
        if (statusCode == 200) {
            return BitmapFactory
                    .decodeStream(response.getEntity().getContent());
        }
        return null;
    }

    protected HttpClient getHttpClient() {
        return NetworkHelper.createHttpClient(this.mAppContext);
    }

    public final HttpResponse post(final String url,
            final List<SimpleRequestParam> params) throws IOException {
        return executeImpl(SimpleRequest.newBuilder().url(url).params(params)
                .post().build().getHttpRequest());
    }

    protected void signRequest(final SimpleRequest cr) {
    }

}
