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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.10
 * @version 2.0 2011.09.05
 * @version 3.0 2011.11.10
 * @version 3.1 2011.12.05
 * 
 */
public class SimpleResponse implements ResponseCode {
    private static final String TAG = SimpleResponse.class.getSimpleName();

    private BufferedHttpEntity entity;
    public final StatusLine statusLine;
    public final int statusCode;

    public SimpleResponse(final HttpResponse response) {
        this.statusLine = response.getStatusLine();
        this.statusCode = this.statusLine.getStatusCode();

        final HttpEntity wrappedHttpEntity = response.getEntity();
        if (wrappedHttpEntity != null) {
            try {
                this.entity = new BufferedHttpEntity(wrappedHttpEntity);
            } catch (final IOException e) {
                this.entity = null;
            }
        }
    }

    public final String getContent() throws IOException {
        if (this.entity != null) {
            return EntityUtils.toString(this.entity, HTTP.UTF_8);
        }
        return null;
    }

    public final JSONArray getJSONArray() throws IOException {
        JSONArray json = null;
        try {
            final String content = getContent();
            if (content != null) {
                json = new JSONArray(content);
            }
        } catch (final JSONException e) {
        }
        return json;
    }

    public final JSONObject getJSONObject() throws IOException {
        JSONObject json = null;
        try {
            final String content = getContent();
            if (content != null) {
                json = new JSONObject(content);
            }
        } catch (final JSONException e) {
        }
        return json;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SimpleResponse [entity=");
        builder.append(this.entity);
        builder.append(", statusLine=");
        builder.append(this.statusLine);
        builder.append(", statusCode=");
        builder.append(this.statusCode);
        builder.append("]");
        return builder.toString();
    }
}
