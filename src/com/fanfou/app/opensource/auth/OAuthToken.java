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
import java.io.Serializable;

public class OAuthToken implements Serializable {

    private static final long serialVersionUID = 3891133932519746686L;

    public static OAuthToken from(final String response) throws IOException {
        return OAuthToken.parse(response);
    }

    private static OAuthToken parse(final String response) {
        OAuthToken token = null;
        try {
            final String[] strs = response.split("&");
            token = new OAuthToken();
            for (final String str : strs) {
                if (str.startsWith("oauth_token=")) {
                    token.setToken(str.split("=")[1].trim());
                } else if (str.startsWith("oauth_token_secret=")) {
                    token.setTokenSecret(str.split("=")[1].trim());
                }
            }
        } catch (final Exception e) {
        }
        return token;
    }

    private String token;

    private String tokenSecret;

    public OAuthToken() {
    }

    public OAuthToken(final String token, final String tokenSecret) {
        this.token = token;
        this.tokenSecret = tokenSecret;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OAuthToken)) {
            return false;
        }

        final OAuthToken that = (OAuthToken) o;

        if (!this.token.equals(that.token)) {
            return false;
        }
        if (!this.tokenSecret.equals(that.tokenSecret)) {
            return false;
        }

        return true;
    }

    public String getToken() {
        return this.token;
    }

    public String getTokenSecret() {
        return this.tokenSecret;
    }

    @Override
    public int hashCode() {
        int result = this.token.hashCode();
        result = (31 * result) + this.tokenSecret.hashCode();
        return result;
    }

    public boolean isNull() {
        return (this.token == null) || (this.tokenSecret == null);
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public void setTokenSecret(final String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    @Override
    public String toString() {
        return "OAuthToken{" + "token='" + this.token + '\''
                + ", tokenSecret='" + this.tokenSecret + '}';
    }
}
