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

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.auth.exception.OAuthTokenException;
import com.fanfou.app.opensource.http.SimpleClient;
import com.fanfou.app.opensource.http.SimpleRequest;
import com.fanfou.app.opensource.http.SimpleResponse;

/**
 * @author mcxiaoke
 * @version 2.0 2011.11.03
 * @version 2.1 2011.11.24
 * @version 2.2 2011.11.28
 * @version 3.0 2011.11.30
 * @version 4.0 2011.12.01
 * @version 4.1 2011.12.02
 * @version 4.2 2011.12.05
 * @version 4.3 2011.12.07
 * @version 4.4 2011.12.12
 * 
 */
public class XAuthService {
    private static final String TAG = XAuthService.class.getSimpleName();

    private final OAuthConfig mOAuthProvider;

    public XAuthService(final OAuthConfig provider) {
        this.mOAuthProvider = provider;
    }

    private void log(final String message) {
        Log.d(XAuthService.TAG, message);
    }

    public OAuthToken requestOAuthAccessToken(final String username,
            final String password) throws OAuthTokenException, IOException {
        final String authorization = OAuthHelper.buildXAuthHeader(username,
                password, HttpGet.METHOD_NAME,
                this.mOAuthProvider.getAccessTokenURL(), this.mOAuthProvider);
        final SimpleRequest nr = SimpleRequest.newBuilder()
                .url(this.mOAuthProvider.getAccessTokenURL())
                .header("Authorization", authorization).build();
        final SimpleClient client = new SimpleClient(AppContext.getAppContext());
        final HttpResponse response = client.exec(nr);
        final SimpleResponse res = new SimpleResponse(response);
        final String content = res.getContent();
        if (AppContext.DEBUG) {
            log("requestOAuthAccessToken() code=" + res.statusCode
                    + " response=" + content);
        }
        if (res.statusCode == 200) {
            return OAuthToken.from(content);
        }
        throw new OAuthTokenException("登录失败，帐号或密码错误");
    }
}
