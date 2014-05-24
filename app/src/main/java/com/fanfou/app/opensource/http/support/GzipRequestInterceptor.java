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
package com.fanfou.app.opensource.http.support;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.03
 * 
 */
public class GzipRequestInterceptor implements HttpRequestInterceptor {
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    @Override
    public void process(final HttpRequest request, final HttpContext context)
            throws HttpException, IOException {
        if (!request
                .containsHeader(GzipRequestInterceptor.HEADER_ACCEPT_ENCODING)) {
            request.addHeader(GzipRequestInterceptor.HEADER_ACCEPT_ENCODING,
                    GzipRequestInterceptor.ENCODING_GZIP);
        }
    }

}
