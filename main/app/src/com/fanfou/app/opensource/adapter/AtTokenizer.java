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
package com.fanfou.app.opensource.adapter;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView.Tokenizer;

/**
 * AtTokenizer allows for auto-completion of user names starting with an @ sign.
 */
public class AtTokenizer implements Tokenizer {

    @Override
    public int findTokenEnd(final CharSequence text, final int cursor) {
        int i = cursor;
        final int len = text.length();
        while (i < len) {
            if (text.charAt(i) == '@') {
                return i;
            } else {
                i++;
            }
        }
        return len;
    }

    @Override
    public int findTokenStart(final CharSequence text, final int cursor) {
        int i = cursor;
        while ((i > 0) && (text.charAt(i - 1) != '@')) {
            i--;
        }
        while ((i < cursor) && (text.charAt(i) == ' ')) {
            i++;
        }
        return i;
    }

    @Override
    public CharSequence terminateToken(final CharSequence text) {
        int i = text.length();
        while ((i > 0) && (text.charAt(i - 1) == ' ')) {
            i--;
        }
        if ((i > 0) && (text.charAt(i - 1) == '@')) {
            return text;
        } else {
            if (text instanceof Spanned) {
                final SpannableString sp = new SpannableString(text);
                TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                        Object.class, sp, 0);
                return sp;
            } else {
                return text + " ";
            }
        }
    }
}
