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
import java.util.List;

import org.apache.http.NameValuePair;

import com.fanfou.app.opensource.util.CommonHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.02
 * @version 1.1 2011.05.03
 * @version 1.2 2011.05.04
 * @version 2.0 2011.11.03
 * 
 */
public final class SimpleRequestParam implements NameValuePair,
        Comparable<SimpleRequestParam> {
    public static boolean hasFile(final List<SimpleRequestParam> params) {
        if (CommonHelper.isEmpty(params)) {
            return false;
        }
        boolean containsFile = false;
        for (final SimpleRequestParam param : params) {
            if (param.isFile()) {
                containsFile = true;
                break;
            }
        }
        return containsFile;
    }

    private String name = null;
    private String value = null;

    private File file = null;

    public SimpleRequestParam(final NameValuePair pair) {
        this.name = pair.getName();
        this.value = pair.getValue();
    }

    public SimpleRequestParam(final String name, final boolean value) {
        this.name = name;
        this.value = String.valueOf(value);
    }

    public SimpleRequestParam(final String name, final double value) {
        this.name = name;
        this.value = String.valueOf(value);
    }

    public SimpleRequestParam(final String name, final File file) {
        assert (file != null);
        this.name = name;
        this.file = file;
        this.value = file.getName();
    }

    public SimpleRequestParam(final String name, final int value) {
        this.name = name;
        this.value = String.valueOf(value);
    }

    public SimpleRequestParam(final String name, final long value) {
        this.name = name;
        this.value = String.valueOf(value);
    }

    public SimpleRequestParam(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int compareTo(final SimpleRequestParam that) {
        int compared;
        compared = this.name.compareTo(that.getName());
        if (0 == compared) {
            compared = this.value.compareTo(that.getValue());
        }
        return compared;
    }

    public File getFile() {
        return this.file;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = (31 * result)
                + (this.value != null ? this.value.hashCode() : 0);
        result = (31 * result) + (this.file != null ? this.file.hashCode() : 0);
        return result;
    }

    public boolean isFile() {
        return this.file != null;
    }

    @Override
    public String toString() {
        return "[" + this.name + ":" + this.value + "]";
    }
}
