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
package com.fanfou.app.opensource.task;

import java.io.Serializable;

/**
 * @author mcxiaoke
 * @version 1.0 20110830
 * 
 */
public final class AsyncTaskResult implements Serializable {
    public static final int CODE_ERROR = -1;
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAILED = 1;
    public static final int CODE_CANCELED = 2;;

    private static final long serialVersionUID = 4195237447592568873L;
    public final int code;
    public final String message;
    public final Object content;

    public AsyncTaskResult(final int code) {
        this(code, null, null);
    }

    public AsyncTaskResult(final int code, final String message) {
        this(code, message, null);
    }

    public AsyncTaskResult(final int code, final String message,
            final Object content) {
        this.code = code;
        this.message = message;
        this.content = content;
    }

}
