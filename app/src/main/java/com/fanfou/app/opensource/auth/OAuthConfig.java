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

import com.fanfou.app.opensource.AppContext;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.30
 * @version 1.1 2013.03.20
 * 
 */
public final class OAuthConfig {
    private static final String REQUEST_TOKEN_URL = "http://fanfou.com/oauth/request_token";
    private static final String AUTHORIZE_URL = "http://fanfou.com/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "http://fanfou.com/oauth/access_token";

    public String getAccessTokenURL() {
        return OAuthConfig.ACCESS_TOKEN_URL;
    }

    public String getAuthorizeURL() {
        return OAuthConfig.AUTHORIZE_URL;
    }

    public String getConsumerKey() {
        return AppContext.getConsumerKey();
    }

    public String getConsumerSercret() {
        return AppContext.getConsumerSecret();
    }

    public String getRequestTokenURL() {
        return OAuthConfig.REQUEST_TOKEN_URL;
    }

}
