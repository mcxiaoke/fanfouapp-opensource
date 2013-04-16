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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fanfou.app.opensource.AppContext;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.19
 * @version 1.1 2011.11.22
 * @version 1.2 2011.12.02
 * 
 */
public class StringHelper {

    // Regex that matches characters that have special meaning in HTML. '<',
    // '>', '&' and
    // multiple continuous spaces.
    private static final Pattern PLAIN_TEXT_TO_ESCAPE = Pattern
            .compile("[<>&]| {2,}|\r?\n");

    /**
     * converts given byte array to a hex string
     * 
     * @param bytes
     * @return
     */
    public static String byteArrayToHexString(final byte[] bytes) {
        final StringBuffer buffer = new StringBuffer();
        for (final byte b : bytes) {
            if ((b & 0xff) < 0x10) {
                buffer.append("0");
            }
            buffer.append(Long.toString(b & 0xff, 16));
        }
        return buffer.toString();
    }

    public static String bytesToHexString(final byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        final StringBuffer sb = new StringBuffer();
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * @param s
     *            原始消息字符串
     * @return 自动截断超过140个字符的消息，取前面133个字符，并添加...，预留转发的字符位置
     */
    public static String cut(final String s) {
        final String str = s.trim();
        final StringBuilder sb = new StringBuilder();
        if (str.length() > 140) {
            return sb.append(str.substring(0, 135)).append("...").toString();
        } else {
            return str;
        }
    }

    public static String encode(final String value) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (final UnsupportedEncodingException ignore) {
        }
        final StringBuffer buf = new StringBuffer(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if ((focus == '%') && ((i + 1) < encoded.length())
                    && (encoded.charAt(i + 1) == '7')
                    && (encoded.charAt(i + 2) == 'E')) {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }

    /**
     * Escape some special character as HTML escape sequence.
     * 
     * @param text
     *            Text to be displayed using WebView.
     * @return Text correctly escaped.
     */
    public static String escapeCharacterToDisplay(String text) {
        final Pattern pattern = StringHelper.PLAIN_TEXT_TO_ESCAPE;
        final Matcher match = pattern.matcher(text);

        if (match.find()) {
            final StringBuilder out = new StringBuilder();
            int end = 0;
            do {
                final int start = match.start();
                out.append(text.substring(end, start));
                end = match.end();
                final int c = text.codePointAt(start);
                if (c == ' ') {
                    // Escape successive spaces into series of "&nbsp;".
                    for (int i = 1, n = end - start; i < n; ++i) {
                        out.append("&nbsp;");
                    }
                    out.append(' ');
                } else if ((c == '\r') || (c == '\n')) {
                    out.append("<br>");
                } else if (c == '<') {
                    out.append("&lt;");
                } else if (c == '>') {
                    out.append("&gt;");
                } else if (c == '&') {
                    out.append("&amp;");
                }
            } while (match.find());
            out.append(text.substring(end));
            text = out.toString();
        }
        return text;
    }

    public static String getStackMessageString(final Throwable e) {
        final StringBuffer message = new StringBuffer();
        final StackTraceElement[] stack = e.getStackTrace();
        final StackTraceElement stackLine = stack[stack.length - 1];
        message.append(stackLine.getFileName());
        message.append(":");
        message.append(stackLine.getLineNumber());
        message.append(":");
        message.append(stackLine.getMethodName());
        message.append(" ");
        message.append(e.getMessage());
        return message.toString();
    }

    public static String getStackTraceString(final Throwable tr) {
        if (tr == null) {
            return "";
        }
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * converts given hex string to a byte array (ex: "0D0A" => {0x0D, 0x0A,})
     * 
     * @param str
     * @return
     */
    public static final byte[] hexStringToByteArray(final String str) {
        int i = 0;
        final byte[] results = new byte[str.length() / 2];
        for (int k = 0; k < str.length();) {
            results[i] = (byte) (Character.digit(str.charAt(k++), 16) << 4);
            results[i] += (byte) (Character.digit(str.charAt(k++), 16));
            i++;
        }
        return results;
    }

    /**
     * @param s
     *            原始字符串
     * @return 判断字符串是否为空
     */
    public static boolean isEmpty(final String s) {
        return (s == null) || s.trim().equals("");
    }

    public static String join(final Collection<?> items, final String delimiter) {
        if ((items == null) || items.isEmpty()) {
            return "";
        }

        final Iterator<?> iter = items.iterator();
        final StringBuilder buffer = new StringBuilder(iter.next().toString());

        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }

        return buffer.toString();
    }

    public static String join(final String separator, final Integer[] integers) {
        if (integers == null) {
            return null;
        }
        if (integers.length == 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(integers[0].toString());
        for (int i = 1, length = integers.length; i < length; i++) {
            builder.append(separator);
            builder.append(integers[i]);
        }
        return builder.toString();
    }

    public static String join(final String separator, final String[] strings) {
        if (strings == null) {
            return null;
        }
        if (strings.length == 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(strings[0]);
        for (int i = 1, length = strings.length; i < length; i++) {
            builder.append(separator);
            builder.append(strings[i]);
        }
        return builder.toString();
    }

    public static String md5(final String s) {
        final StringBuffer result = new StringBuffer();
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            final byte digest[] = md.digest();
            for (final byte element : digest) {
                result.append(Integer.toHexString(0xFF & element));
            }
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return (result.toString());
    }

    /**
     * MD5加密函数
     * 
     * @param str
     *            要加密的字符串
     * @return 加密后的字符串
     */
    public static String md5old(final String str) {
        if ((str == null) || ("".equals(str.trim()))) {
            return str;
        } else {
            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance("MD5");
                messageDigest.reset();
                messageDigest.update(str.getBytes("UTF-8"));
            } catch (final NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            final byte[] byteArray = messageDigest.digest();

            final StringBuffer md5StrBuff = new StringBuffer();

            for (final byte element : byteArray) {
                if (Integer.toHexString(0xFF & element).length() == 1) {
                    md5StrBuff.append("0").append(
                            Integer.toHexString(0xFF & element));
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF & element));
                }
            }
            return md5StrBuff.toString();
        }
    }

    /**
     * 
     * @param original
     * @return null if fails
     */
    public static String md5sum(final byte[] original) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(original, 0, original.length);
            final StringBuffer md5sum = new StringBuffer(new BigInteger(1,
                    md.digest()).toString(16));
            while (md5sum.length() < 32) {
                md5sum.insert(0, "0");
            }
            return md5sum.toString();
        } catch (final NoSuchAlgorithmException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 
     * @param original
     * @return null if fails
     */
    public static String md5sum(final String original) {
        return StringHelper.md5sum(original.getBytes());
    }

    /**
     * 字符串转化为数字
     * 
     * @param s
     *            字符串参数
     * @return 字符串代表的数字，如果无法转换，返回0
     */
    public static int toInt(final String s) {
        try {
            return Integer.parseInt(s);
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    public static String toString(final List<String> array) {
        if ((array == null) || (array.size() == 0)) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < array.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(array.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    public static String toString(final String[] array) {

        if ((array == null) || (array.length == 0)) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(array[i]);
        }
        sb.append(")");
        return sb.toString();

    }

    /**
     * 
     * @param encoded
     * @return null if fails
     */
    public static String urldecode(final String encoded) {
        try {
            return URLDecoder.decode(encoded, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 
     * @param original
     * @return null if fails
     */
    public static String urlencode(final String original) {
        try {
            // return URLEncoder.encode(original, "utf-8");
            // fixed: to comply with RFC-3986
            return URLEncoder.encode(original, "utf-8").replace("+", "%20")
                    .replace("*", "%2A").replace("%7E", "~");
        } catch (final UnsupportedEncodingException e) {
            if (AppContext.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
