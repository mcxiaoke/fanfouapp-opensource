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
package com.fanfou.app.opensource.api;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.02
 * @version 1.1 2011.05.05
 * @version 1.2 2011.05.06
 * @version 1.3 2011.05.18
 * 
 */
public class ApiException extends Exception {

    private static final long serialVersionUID = 6673077544941712048L;
    public final int statusCode;
    public final String errorMessage;

    public ApiException(final int statusCode) {
        super();
        this.statusCode = statusCode;
        this.errorMessage = "";
    }

    public ApiException(final int statusCode, final String detailMessage) {
        super(detailMessage);
        this.statusCode = statusCode;
        this.errorMessage = detailMessage;
    }

    public ApiException(final int statusCode, final String detailMessage,
            final Throwable throwable) {
        super(detailMessage, throwable);
        this.statusCode = statusCode;
        this.errorMessage = detailMessage;

    }

    public ApiException(final int statusCode, final Throwable throwable) {
        super(throwable);
        this.statusCode = statusCode;
        this.errorMessage = throwable.toString();

    }

    @Override
    public String toString() {
        return new StringBuilder().append("code:").append(this.statusCode)
                .append(" msg:").append(getMessage()).toString();
    }

}
