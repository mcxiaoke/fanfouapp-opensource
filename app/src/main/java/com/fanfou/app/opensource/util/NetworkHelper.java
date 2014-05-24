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
package com.fanfou.app.opensource.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.http.SimpleRequestParam;
import com.fanfou.app.opensource.http.support.GzipRequestInterceptor;
import com.fanfou.app.opensource.http.support.GzipResponseInterceptor;
import com.fanfou.app.opensource.http.support.RequestRetryHandler;

/**
 * @author mcxiaoke
 * @version 1.0 2011.12.02
 * @version 1.1 2011.12.07
 * @version 1.2 2011.12.12
 * @version 1.3 2011.12.21
 * @version 1.4 2013.03.16
 * 
 */
public final class NetworkHelper {
    private static final String TAG = NetworkHelper.class.getSimpleName();
    public static final int SOCKET_BUFFER_SIZE = 8 * 1024;
    public static final int CONNECTION_TIMEOUT_MS = 20000;
    public static final int SOCKET_TIMEOUT_MS = 20000;
    public static final int MAX_TOTAL_CONNECTIONS = 20;
    public static final int MAX_RETRY_TIMES = 3;
    private static final String WIFI = "WIFI";
    private static final String MOBILE_CTWAP = "ctwap";
    private static final String MOBILE_CMWAP = "cmwap";
    private static final String MOBILE_3GWAP = "3gwap";
    private static final String MOBILE_UNIWAP = "uniwap";

    /**
     * 根据当前网络状态填充代理
     * 
     * @param context
     * @param httpParams
     */
    private static final void checkAndSetProxy(final Context context,
            final HttpParams httpParams) {
        boolean needCheckProxy = true;

        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if ((networkInfo == null)
                || NetworkHelper.WIFI.equalsIgnoreCase(networkInfo
                        .getTypeName()) || (networkInfo.getExtraInfo() == null)) {
            needCheckProxy = false;
        }
        if (needCheckProxy) {
            final String typeName = networkInfo.getExtraInfo();
            if (NetworkHelper.MOBILE_CTWAP.equalsIgnoreCase(typeName)) {
                final HttpHost proxy = new HttpHost("10.0.0.200", 80);
                httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            } else if (NetworkHelper.MOBILE_CMWAP.equalsIgnoreCase(typeName)
                    || NetworkHelper.MOBILE_UNIWAP.equalsIgnoreCase(typeName)
                    || NetworkHelper.MOBILE_3GWAP.equalsIgnoreCase(typeName)) {
                final HttpHost proxy = new HttpHost("10.0.0.172", 80);
                httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }
        }

        // String defaultProxyHost = android.net.Proxy.getDefaultHost();
        // int defaultProxyPort = android.net.Proxy.getDefaultPort();
        // if (defaultProxyHost != null && defaultProxyHost.length() > 0
        // && defaultProxyPort > 0) {
        // HttpHost proxy = new HttpHost(defaultProxyHost, defaultProxyPort);
        // httpParams.setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
        // }
    }

    public final static DefaultHttpClient createHttpClient(final Context context) {
        final HttpParams params = NetworkHelper.createHttpParams();
        final DefaultHttpClient client = new DefaultHttpClient(params);
        client.addRequestInterceptor(new GzipRequestInterceptor());
        client.addResponseInterceptor(new GzipResponseInterceptor());
        client.setHttpRequestRetryHandler(new RequestRetryHandler(
                NetworkHelper.MAX_RETRY_TIMES));
        NetworkHelper.checkAndSetProxy(context, params);
        return client;
    }

    private static final HttpParams createHttpParams() {
        final HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setUseExpectContinue(params, false);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        ConnManagerParams.setTimeout(params, NetworkHelper.SOCKET_TIMEOUT_MS);
        HttpConnectionParams.setConnectionTimeout(params,
                NetworkHelper.CONNECTION_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params,
                NetworkHelper.SOCKET_TIMEOUT_MS);

        ConnManagerParams.setMaxConnectionsPerRoute(params,
                new ConnPerRouteBean(NetworkHelper.MAX_TOTAL_CONNECTIONS));
        ConnManagerParams.setMaxTotalConnections(params,
                NetworkHelper.MAX_TOTAL_CONNECTIONS);

        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params,
                NetworkHelper.SOCKET_BUFFER_SIZE);
        HttpClientParams.setRedirecting(params, false);
        HttpProtocolParams.setUserAgent(params, "FanFou for Android/"
                + AppContext.appVersionName);
        return params;
    }

    private static String encode(final String input) {
        try {
            return URLEncoder.encode(input, HTTP.UTF_8);
        } catch (final UnsupportedEncodingException e) {
        }
        return input;
    }

    public static MultipartEntity encodeMultipartParameters(
            final List<SimpleRequestParam> params) {
        if (CommonHelper.isEmpty(params)) {
            return null;
        }
        final MultipartEntity entity = new MultipartEntity();
        try {
            for (final SimpleRequestParam param : params) {
                if (param.isFile()) {
                    entity.addPart(param.getName(),
                            new FileBody(param.getFile()));
                } else {
                    entity.addPart(
                            param.getName(),
                            new StringBody(param.getValue(), Charset
                                    .forName(HTTP.UTF_8)));
                }
            }
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return entity;
    }

    public static HttpEntity encodePostParameters(
            final List<SimpleRequestParam> params) {
        HttpEntity entity = null;
        if (!CommonHelper.isEmpty(params)) {
            try {
                entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            } catch (final UnsupportedEncodingException e) {
            }
        }
        return entity;
    }

    public static String encodeQueryParameters(
            final List<SimpleRequestParam> params) {
        if (CommonHelper.isEmpty(params)) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            final SimpleRequestParam p = params.get(i);
            if (p.isFile()) {
                throw new IllegalArgumentException("GET参数不能包含文件");
            }
            if (i > 0) {
                sb.append("&");
            }
            sb.append(NetworkHelper.encode(p.getName())).append("=")
                    .append(NetworkHelper.encode(p.getValue()));
        }

        return sb.toString();
    }

    public static final boolean isConnected(final Context context) {
        final ConnectivityManager connec = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = connec.getActiveNetworkInfo();
        return (info != null) && info.isConnectedOrConnecting();
    }

    public static final boolean isNotConnected(final Context context) {
        return !NetworkHelper.isConnected(context);
    }

    public static final boolean isWifi(final Context context) {
        final ConnectivityManager connec = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = connec.getActiveNetworkInfo();
        return (info != null)
                && (info.getType() == ConnectivityManager.TYPE_WIFI);
    }
}
