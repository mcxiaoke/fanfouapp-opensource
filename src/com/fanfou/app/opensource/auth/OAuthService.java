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

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;

import com.fanfou.app.opensource.http.SimpleRequest;

/**
 * @author mcxiaoke
 * @version 2.0 2011.11.03
 * @version 2.1 2011.11.24
 * @version 2.2 2011.11.28
 * @version 3.0 2011.11.30
 * @version 4.0 2011.12.01
 * @version 4.1 2011.12.07
 * 
 */
public class OAuthService {

    private final OAuthConfig mOAuthConfig;
    private OAuthToken mOAuthToken;

    public OAuthService(final OAuthConfig provider) {
        this.mOAuthConfig = provider;
    }

    public OAuthService(final OAuthConfig provider, final OAuthToken token) {
        this.mOAuthConfig = provider;
        this.mOAuthToken = token;
    }

    public void addOAuthSignature(final SimpleRequest sr) {
        if (sr != null) {
            final HttpUriRequest request = sr.getHttpRequest();
            if (request != null) {
                final String authorization = OAuthHelper.buildOAuthHeader(
                        request.getMethod(), request.getURI().toString(),
                        sr.getParams(), this.mOAuthConfig, this.mOAuthToken);
                // request.addHeader(new BasicHeader("Host", "api.fanfou.com"));
                request.addHeader(new BasicHeader("Authorization",
                        authorization));
            }
        }

    }

    public void setOAuthToken(final OAuthToken token) {
        this.mOAuthToken = token;
    }

}
