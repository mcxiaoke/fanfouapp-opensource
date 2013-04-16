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
package com.fanfou.app.opensource.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.methods.HttpGet;

import android.text.TextUtils;
import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.http.SimpleRequestParam;
import com.fanfou.app.opensource.util.support.Base64;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.30
 * @version 2.0 2011.12.01
 * @version 2.1 2011.12.02
 * @version 2.2 2011.12.07
 * 
 */
final class OAuthHelper {
    public static final String OAUTH_VERSION1 = "1.0";
    public static final String HMAC_SHA1 = "HmacSHA1";
    public final static String KEY_SUFFIX = "FE0687E249EBF374";
    public static final SimpleRequestParam OAUTH_SIGNATURE_METHOD = new SimpleRequestParam(
            "oauth_signature_method", "HMAC-SHA1");

    private static final String TAG = OAuthHelper.class.getSimpleName();

    public final static Random RAND = new Random();

    private static String alignParams(final List<SimpleRequestParam> params) {
        Collections.sort(params);
        return OAuthHelper.encodeParameters(params);
    }

    static String buildOAuthHeader(final String method, final String url,
            List<SimpleRequestParam> params, final OAuthConfig provider,
            final OAuthToken otoken) {
        if (params == null) {
            params = new ArrayList<SimpleRequestParam>();
        }
        final long timestamp = System.currentTimeMillis() / 1000;
        final long nonce = timestamp + OAuthHelper.RAND.nextInt();
        final List<SimpleRequestParam> oauthHeaderParams = new ArrayList<SimpleRequestParam>();
        oauthHeaderParams.add(new SimpleRequestParam("oauth_consumer_key",
                provider.getConsumerKey()));
        oauthHeaderParams.add(OAuthHelper.OAUTH_SIGNATURE_METHOD);
        oauthHeaderParams.add(new SimpleRequestParam("oauth_timestamp",
                timestamp));
        oauthHeaderParams.add(new SimpleRequestParam("oauth_nonce", nonce));
        oauthHeaderParams.add(new SimpleRequestParam("oauth_version",
                OAuthHelper.OAUTH_VERSION1));
        if (null != otoken) {
            oauthHeaderParams.add(new SimpleRequestParam("oauth_token", otoken
                    .getToken()));
        }
        final List<SimpleRequestParam> signatureBaseParams = new ArrayList<SimpleRequestParam>(
                oauthHeaderParams.size() + params.size());
        signatureBaseParams.addAll(oauthHeaderParams);
        if ((method != HttpGet.METHOD_NAME) && (params != null)
                && !SimpleRequestParam.hasFile(params)) {
            signatureBaseParams.addAll(params);
        }
        OAuthHelper.parseGetParams(url, signatureBaseParams);

        final String encodedUrl = OAuthHelper.encode(OAuthHelper
                .constructRequestURL(url));

        final String encodedParams = OAuthHelper.encode(OAuthHelper
                .alignParams(signatureBaseParams));

        final StringBuffer base = new StringBuffer(method).append("&")
                .append(encodedUrl).append("&").append(encodedParams);
        final String oauthBaseString = base.toString();

        if (AppContext.DEBUG) {
            Log.d(OAuthHelper.TAG, "getOAuthHeader() url=" + url);
            Log.d(OAuthHelper.TAG, "getOAuthHeader() encodedUrl=" + encodedUrl);
            Log.d(OAuthHelper.TAG, "getOAuthHeader() encodedParams="
                    + encodedParams);
            Log.d(OAuthHelper.TAG, "getOAuthHeader() baseString="
                    + oauthBaseString);
        }
        final SecretKeySpec spec = OAuthHelper.getSecretKeySpec(provider,
                otoken);
        oauthHeaderParams.add(new SimpleRequestParam("oauth_signature",
                OAuthHelper.getSignature(oauthBaseString, spec)));
        return "OAuth "
                + OAuthHelper.encodeParameters(oauthHeaderParams, ",", true);
    }

    static String buildXAuthHeader(final String username,
            final String password, final String method, final String url,
            final OAuthConfig provider) {
        final long timestamp = System.currentTimeMillis() / 1000;
        final long nonce = System.nanoTime() + OAuthHelper.RAND.nextInt();
        final List<SimpleRequestParam> oauthHeaderParams = new ArrayList<SimpleRequestParam>();
        oauthHeaderParams.add(new SimpleRequestParam("oauth_consumer_key",
                provider.getConsumerKey()));
        oauthHeaderParams.add(new SimpleRequestParam("oauth_signature_method",
                "HMAC-SHA1"));
        oauthHeaderParams.add(new SimpleRequestParam("oauth_timestamp",
                timestamp));
        oauthHeaderParams.add(new SimpleRequestParam("oauth_nonce", nonce));
        oauthHeaderParams.add(new SimpleRequestParam("oauth_version", "1.0"));
        oauthHeaderParams.add(new SimpleRequestParam("x_auth_username",
                username));
        oauthHeaderParams.add(new SimpleRequestParam("x_auth_password",
                password));
        oauthHeaderParams.add(new SimpleRequestParam("x_auth_mode",
                "client_auth"));
        final StringBuffer base = new StringBuffer(method)
                .append("&")
                .append(OAuthHelper.encode(OAuthHelper.constructRequestURL(url)))
                .append("&");
        base.append(OAuthHelper.encode(OAuthHelper
                .alignParams(oauthHeaderParams)));
        final String oauthBaseString = base.toString();
        final SecretKeySpec spec = OAuthHelper.getSecretKeySpec(provider, null);
        final String signature = OAuthHelper
                .getSignature(oauthBaseString, spec);
        oauthHeaderParams.add(new SimpleRequestParam("oauth_signature",
                signature));
        return "OAuth "
                + OAuthHelper.encodeParameters(oauthHeaderParams, ",", true);
    }

    private static String constructRequestURL(String url) {
        final int index = url.indexOf("?");
        if (-1 != index) {
            url = url.substring(0, index);
        }
        final int slashIndex = url.indexOf("/", 8);
        final String baseURL = url.substring(0, slashIndex).toLowerCase();
        url = baseURL + url.substring(slashIndex);
        if (AppContext.DEBUG) {
            Log.d(OAuthHelper.TAG, "constructRequestURL result=" + url);
        }
        return url;
    }

    static long createNonce() {
        return (System.currentTimeMillis() / 1000) + OAuthHelper.RAND.nextInt();
    }

    static long createTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    static String encode(final String value) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (final UnsupportedEncodingException ignore) {
        }
        if (!TextUtils.isEmpty(encoded)) {
            final StringBuilder buf = new StringBuilder(encoded.length());
            char focus;
            for (int i = 0; i < encoded.length(); i++) {
                focus = encoded.charAt(i);
                if (focus == '*') {
                    buf.append("%2A");
                } else if (focus == '+') {
                    buf.append("%20");
                } else if ((focus == '%') && ((i + 1) < encoded.length())
                        && (encoded.charAt(i + 1) == '7')
                        && (encoded.charAt(i + 2) == 'E')) {
                    buf.append('~');
                    i += 2;
                } else {
                    buf.append(focus);
                }
            }
            return buf.toString();
        }
        return value;
    }

    private static String encodeParameters(
            final List<SimpleRequestParam> httpParams) {
        return OAuthHelper.encodeParameters(httpParams, "&", false);
    }

    private static String encodeParameters(
            final List<SimpleRequestParam> httpParams, final String splitter,
            final boolean quot) {
        final StringBuffer buf = new StringBuffer();
        for (final SimpleRequestParam param : httpParams) {
            if (!param.isFile()) {
                if (buf.length() != 0) {
                    if (quot) {
                        buf.append("\"");
                    }
                    buf.append(splitter);
                }
                buf.append(OAuthHelper.encode(param.getName())).append("=");
                if (quot) {
                    buf.append("\"");
                }
                buf.append(OAuthHelper.encode(param.getValue()));
            }
        }
        if (buf.length() != 0) {
            if (quot) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }

    static SecretKeySpec getSecretKeySpec(final OAuthConfig provider) {
        final String oauthSignature = OAuthHelper.encode(provider
                .getConsumerSercret()) + "&";
        return new SecretKeySpec(oauthSignature.getBytes(),
                OAuthHelper.HMAC_SHA1);
    }

    static SecretKeySpec getSecretKeySpec(final OAuthConfig provider,
            final OAuthToken token) {
        if (null == token) {
            final String oauthSignature = OAuthHelper.encode(provider
                    .getConsumerSercret()) + "&";
            return new SecretKeySpec(oauthSignature.getBytes(),
                    OAuthHelper.HMAC_SHA1);
        } else {
            final String oauthSignature = OAuthHelper.encode(provider
                    .getConsumerSercret())
                    + "&"
                    + OAuthHelper.encode(token.getTokenSecret());
            return new SecretKeySpec(oauthSignature.getBytes(),
                    OAuthHelper.HMAC_SHA1);
        }
    }

    private static String getSignature(final String data,
            final SecretKeySpec spec) {
        byte[] byteHMAC = null;
        try {
            final Mac mac = Mac.getInstance(OAuthHelper.HMAC_SHA1);
            mac.init(spec);
            byteHMAC = mac.doFinal(data.getBytes());
        } catch (final InvalidKeyException ike) {
            throw new AssertionError(ike);
        } catch (final NoSuchAlgorithmException nsae) {
            throw new AssertionError(nsae);
        }
        return Base64.encodeBytes(byteHMAC);
    }

    private static void parseGetParams(final String url,
            final List<SimpleRequestParam> signatureBaseParams) {
        final int queryStart = url.indexOf("?");
        if (-1 != queryStart) {
            final String[] queryStrs = url.substring(queryStart + 1).split("&");
            try {
                for (final String query : queryStrs) {
                    final String[] split = query.split("=");
                    if (split.length == 2) {
                        signatureBaseParams.add(new SimpleRequestParam(
                                URLDecoder.decode(split[0], "UTF-8"),
                                URLDecoder.decode(split[1], "UTF-8")));
                    } else {
                        signatureBaseParams.add(new SimpleRequestParam(
                                URLDecoder.decode(split[0], "UTF-8"), ""));
                    }
                }
            } catch (final UnsupportedEncodingException ignore) {
            }

        }

    }

}
