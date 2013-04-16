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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;

import android.text.TextUtils;

import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.NetworkHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.03
 * @version 1.1 2011.11.04
 * @version 1.2 2011.11.18
 * @version 1.3 2011.11.22
 * @version 1.4 2011.11.23
 * @version 2.0 2011.12.01
 * @version 2.1 2011.12.02
 * @version 2.2 2011.12.05
 * 
 */
public final class SimpleRequest {
    public static class Builder {
        private boolean post;
        private final List<SimpleRequestParam> params;
        private final List<Header> headers;
        private String url;

        public Builder() {
            this.post = false;
            this.params = new ArrayList<SimpleRequestParam>();
            this.headers = new ArrayList<Header>();
        }

        public SimpleRequest build() {
            return new SimpleRequest(this);
        }

        public Builder count(final int count) {
            this.params.add(new SimpleRequestParam("count", count));
            return this;
        }

        public Builder format(final String format) {
            if (!TextUtils.isEmpty(format)) {
                this.params.add(new SimpleRequestParam("format", format));
            }
            return this;
        }

        public Builder header(final Header header) {
            this.headers.add(header);
            return this;
        }

        public Builder header(final String name, final String value) {
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                this.headers.add(new BasicHeader(name, value));
            }
            return this;
        }

        public Builder headers(final List<Header> headers) {
            this.headers.addAll(headers);
            return this;
        }

        public Builder id(final String id) {
            if (!TextUtils.isEmpty(id)) {
                this.params.add(new SimpleRequestParam("id", id));
            }
            return this;
        }

        public Builder location(final String location) {
            if (!TextUtils.isEmpty(location)) {
                this.params.add(new SimpleRequestParam("location", location));
            }
            return this;
        }

        public Builder maxId(final String maxId) {
            if (!TextUtils.isEmpty(maxId)) {
                this.params.add(new SimpleRequestParam("max_id", maxId));
            }
            return this;
        }

        public Builder mode(final String mode) {
            if (!TextUtils.isEmpty(mode)) {
                this.params.add(new SimpleRequestParam("mode", mode));
            }
            return this;
        }

        public Builder page(final int page) {
            if (page > 0) {
                this.params.add(new SimpleRequestParam("page", page));
            }
            return this;
        }

        public Builder param(final NameValuePair pair) {
            this.params.add(new SimpleRequestParam(pair));
            return this;
        }

        public Builder param(final SimpleRequestParam param) {
            this.params.add(param);
            return this;
        }

        public Builder param(final String name, final boolean value) {
            if (!TextUtils.isEmpty(name)) {
                this.params.add(new SimpleRequestParam(name, value));
            }
            return this;
        }

        public Builder param(final String name, final File value) {
            if (!TextUtils.isEmpty(name) && (value != null)) {
                this.params.add(new SimpleRequestParam(name, value));
            }
            return this;
        }

        public Builder param(final String name, final int value) {
            if (!TextUtils.isEmpty(name)) {
                this.params.add(new SimpleRequestParam(name, value));
            }
            return this;
        }

        public Builder param(final String name, final long value) {
            if (!TextUtils.isEmpty(name)) {
                this.params.add(new SimpleRequestParam(name, value));
            }
            return this;
        }

        public Builder param(final String name, final String value) {
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                this.params.add(new SimpleRequestParam(name, value));
            }
            return this;
        }

        public Builder params(final List<SimpleRequestParam> params) {
            this.params.addAll(params);
            return this;
        }

        public Builder post() {
            this.post = true;
            return this;
        }

        public Builder post(final boolean post) {
            this.post = post;
            return this;
        }

        public Builder sinceId(final String sinceId) {
            if (!TextUtils.isEmpty(sinceId)) {
                this.params.add(new SimpleRequestParam("since_id", sinceId));
            }
            return this;
        }

        public Builder status(final String status) {
            if (!TextUtils.isEmpty(status)) {
                this.params.add(new SimpleRequestParam("status", status));
            }
            return this;
        }

        public Builder url(final String url) {
            if (TextUtils.isEmpty(url)) {
                throw new IllegalArgumentException(
                        "Builder.url() request url must not be empty or null.");
            }
            this.url = url;
            return this;
        }

    }

    private static final String TAG = SimpleRequest.class.getSimpleName();

    public static Builder newBuilder() {
        return new Builder();
    }

    public final boolean post;
    private final List<SimpleRequestParam> params;
    private final List<Header> headers;
    public final HttpEntity entity;

    public final String url;

    private final HttpRequestBase httpRequest;

    private SimpleRequest(final Builder builder) {
        this.post = builder.post;
        this.headers = builder.headers;
        this.params = builder.params;
        if (this.post) {
            this.url = builder.url;
            this.httpRequest = new HttpPost(this.url);
            if (!CommonHelper.isEmpty(this.params)) {
                if (SimpleRequestParam.hasFile(this.params)) {
                    this.entity = NetworkHelper
                            .encodeMultipartParameters(this.params);
                } else {
                    this.entity = NetworkHelper
                            .encodePostParameters(this.params);
                }
                ((HttpPost) this.httpRequest).setEntity(this.entity);
            } else {
                this.entity = null;
            }
        } else {
            this.entity = null;
            if (CommonHelper.isEmpty(this.params)) {
                this.url = builder.url;
            } else {
                this.url = builder.url + "?"
                        + NetworkHelper.encodeQueryParameters(this.params);
                ;
            }
            this.httpRequest = new HttpGet(this.url);
        }

        if (this.headers != null) {
            for (final Header header : this.headers) {
                this.httpRequest.addHeader(header);
            }
        }
    }

    public void abort() {
        if (this.httpRequest != null) {
            this.httpRequest.abort();
        }
    }

    public List<Header> getHeaders() {
        return this.headers;
    }

    public HttpRequestBase getHttpRequest() {
        return this.httpRequest;
    }

    public List<SimpleRequestParam> getParams() {
        return this.params;
    }

}
