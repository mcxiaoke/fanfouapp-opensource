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
package com.fanfou.app.opensource.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * @author mcxiaoke
 * @version 1.0 2011.09.29
 * @version 1.1 2011.12.02
 * 
 * @param <T>
 */
class AbstractCache<T> implements ICache<T> {

    final HashMap<String, SoftReference<T>> memoryCache;
    final boolean onlyMemoryCache;

    public AbstractCache() {
        this.memoryCache = new HashMap<String, SoftReference<T>>(64);
        this.onlyMemoryCache = true;
    }

    public AbstractCache(final boolean onlyMemoryCache) {
        this.memoryCache = new HashMap<String, SoftReference<T>>();
        this.onlyMemoryCache = onlyMemoryCache;
    }

    @Override
    public void clear() {
        this.memoryCache.clear();
    }

    @Override
    public boolean containsKey(final String key) {
        if ((key == null) || key.equals("")) {
            return false;
        }
        if (this.onlyMemoryCache) {
            return this.memoryCache.containsKey(key);
        } else {
            return get(key) != null;
        }
    }

    @Override
    public T get(final String key) {
        if ((key == null) || key.equals("")) {
            return null;
        }
        T result = null;
        final SoftReference<T> reference = this.memoryCache.get(key);

        if (reference != null) {
            result = reference.get();
        } else {
            if (!this.onlyMemoryCache) {
                result = read(key);
            }
        }
        return result;
    }

    @Override
    public int getCount() {
        return this.memoryCache.size();
    }

    @Override
    public boolean isEmpty() {
        return this.memoryCache.isEmpty();
    }

    @Override
    public boolean put(final String key, final T t) {
        if ((key == null) || key.equals("") || (t == null)) {
            return false;
        }
        boolean result = true;
        synchronized (this) {
            result = this.memoryCache.put(key, new SoftReference<T>(t)) != null;
        }
        if (!this.onlyMemoryCache) {
            result = write(key, t);
        }
        return result;
    };

    protected T read(final String key) {
        throw new NullPointerException(
                "file cache must override read() method.");
    }

    protected boolean write(final String key, final T t) {
        throw new NullPointerException(
                "file cache must override write() method.");
    };

}
