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
package com.fanfou.app.opensource.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Html;
import android.text.Spannable;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.fanfou.app.opensource.AppContext;
import com.fanfou.app.opensource.api.bean.Status;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.01
 * @version 1.5 2011.10.26
 * @version 1.6 2011.11.17
 * @version 1.7 2011.11.21
 * 
 */
public class PatternsHelper {
    private static final String TAG = "StatusHelper";

    private static HashMap<String, String> userNameIdMap = new HashMap<String, String>();

    private static final Pattern PATTERN_USER = Pattern.compile("@.+?\\s");
    private static final LinkifyCompat.MatchFilter MATCH_FILTER_USER = new LinkifyCompat.MatchFilter() {
        @Override
        public final boolean acceptMatch(final CharSequence s, final int start,
                final int end) {
            final String name = s.subSequence(start + 1, end).toString().trim();
            return PatternsHelper.userNameIdMap.containsKey(name);
        }
    };

    private static final LinkifyCompat.TransformFilter TRANSFORM_USER = new LinkifyCompat.TransformFilter() {

        @Override
        public String transformUrl(final Matcher match, final String url) {
            final String name = url.subSequence(1, url.length()).toString()
                    .trim();
            return PatternsHelper.userNameIdMap.get(name);
        }
    };

    private static final String SCHEME_USER = "fanfou://user/";

    private static final Pattern PATTERN_SEARCH = Pattern.compile("#\\w+#");

    private static final LinkifyCompat.TransformFilter TRANSFORM_SEARCH = new LinkifyCompat.TransformFilter() {
        @Override
        public final String transformUrl(final Matcher match, final String url) {
            final String result = url.substring(1, url.length() - 1);
            return result;
        }
    };

    private static final String SCHEME_SEARCH = "fanfou://search/";

    private static Pattern PATTERN_USERLINK = Pattern
            .compile("@<a href=\"http:\\/\\/fanfou\\.com\\/(.*?)\" class=\"former\">(.*?)<\\/a>");

    private static final Pattern namePattern = Pattern.compile("@(.*?)\\s");

    private static final int MAX_NAME_LENGTH = 12;

    public static ArrayList<String> getMentions(final Status status) {
        final String text = status.simpleText;
        final ArrayList<String> names = new ArrayList<String>();
        names.add(status.userScreenName);
        final Matcher m = PatternsHelper.namePattern.matcher(text);
        while (m.find()) {
            final String name = m.group(1);
            if (!names.contains(name)
                    && (name.length() <= (PatternsHelper.MAX_NAME_LENGTH + 1))) {
                names.add(m.group(1));
            }
        }
        final String name = AppContext.getUserName();
        names.remove(name);
        return names;
    }

    public static String getSimpifiedText(final String text) {
        return Html.fromHtml(text).toString();
    }

    private static String handleText(final String text) {
        final Matcher m = PatternsHelper.PATTERN_USERLINK.matcher(text);
        while (m.find()) {
            PatternsHelper.userNameIdMap.put(m.group(2), m.group(1));
            if (AppContext.DEBUG) {
                Log.d(PatternsHelper.TAG,
                        "preprocessText() screenName=" + m.group(2)
                                + " userId=" + m.group(1));
            }
        }
        return Html.fromHtml(text).toString();
    }

    public static void linkifyTags(final TextView view) {
        LinkifyCompat.addLinks(view, PatternsHelper.PATTERN_SEARCH,
                PatternsHelper.SCHEME_SEARCH, null,
                PatternsHelper.TRANSFORM_SEARCH);
    }

    public static void linkifyUsers(final TextView view) {
        LinkifyCompat.addLinks(view, PatternsHelper.PATTERN_USER,
                PatternsHelper.SCHEME_USER, PatternsHelper.MATCH_FILTER_USER,
                PatternsHelper.TRANSFORM_USER);
    }

    public static void removeUnderlines(final TextView textView) {
        final Spannable s = (Spannable) textView.getText();
        final URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            final int start = s.getSpanStart(span);
            final int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new LinkifyCompat.URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    public static void setStatus(final TextView textView, final String text) {
        final String processedText = PatternsHelper.handleText(text);
        textView.setText(Html.fromHtml(processedText), BufferType.SPANNABLE);
        LinkifyCompat.addLinks(textView, LinkifyCompat.WEB_URLS);
        PatternsHelper.linkifyUsers(textView);
        PatternsHelper.linkifyTags(textView);
        PatternsHelper.userNameIdMap.clear();
    }

}
