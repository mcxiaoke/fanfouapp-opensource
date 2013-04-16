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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.19
 * @version 1.5 2011.10.25
 * @version 1.6 2011.10.26
 * @version 1.7 2011.11.21
 * 
 */
public class DateTimeHelper {

    private static final String FANFOU_DATE_FORMAT_STRING = "EEE MMM dd HH:mm:ss Z yyyy";
    private static final String SIMPLE_DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm";
    private static final String FILENAME_DATE_FORMAT_STRING = "yyyy_MM_dd_HH_mm_ss";
    private static final String DATE_ONLY_FORMAT_STRING = "yyyy-MM-dd";
    private static final String TIME_ONLY_FORMAT_STRING = "HH:mm:ss";

    public static final SimpleDateFormat FANFOU_DATE_FORMAT = new SimpleDateFormat(
            DateTimeHelper.FANFOU_DATE_FORMAT_STRING, Locale.US);

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(
            DateTimeHelper.SIMPLE_DATE_FORMAT_STRING, Locale.US);

    private static final SimpleDateFormat FILENAME_DATE_FORMAT = new SimpleDateFormat(
            DateTimeHelper.FILENAME_DATE_FORMAT_STRING, Locale.US);

    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat(
            DateTimeHelper.DATE_ONLY_FORMAT_STRING, Locale.US);

    private static final SimpleDateFormat TIME_ONLY_FORMAT = new SimpleDateFormat(
            DateTimeHelper.TIME_ONLY_FORMAT_STRING, Locale.US);

    /** 以秒为单位计算时间间隔 */
    private static final long MIN = 60;
    private static final long HOUR = DateTimeHelper.MIN * 60;
    private static final long DAY = DateTimeHelper.HOUR * 24;
    private static final long WEEK = DateTimeHelper.DAY * 7;
    /**
     * @param s
     *            代表饭否日期和时间的字符串
     * @return 字符串解析为对应的Date对象
     */
    private static final ParsePosition mPosition = new ParsePosition(0);

    public static Date fanfouStringToDate(final String s) {
        // Fanfou Date String example --> "Mon Dec 13 03:10:21 +0000 2010"
        // final ParsePosition position = new ParsePosition(0);//
        // 这个如果放在方法外面的话，必须每次重置Index为0
        DateTimeHelper.mPosition.setIndex(0);
        return DateTimeHelper.FANFOU_DATE_FORMAT.parse(s,
                DateTimeHelper.mPosition);
    }

    /**
     * 将Date对象解析为饭否格式的字符串
     * 
     * @param date
     *            Date对象
     * @return 饭否格式日期字符串
     */
    public static String formatDate(final Date date) {
        return DateTimeHelper.formatDate(date,
                DateTimeHelper.SIMPLE_DATE_FORMAT);
    }

    public static String formatDate(final Date date,
            final SimpleDateFormat format) {
        if (date == null) {
            return "";
        }
        if (format == null) {
            return DateTimeHelper.SIMPLE_DATE_FORMAT.format(date);
        }
        return format.format(date);
    }

    public static String formatDateFileName(final Date date) {
        return DateTimeHelper.formatDate(date,
                DateTimeHelper.FILENAME_DATE_FORMAT);
    }

    public static String formatDateOnly(final Date date) {
        return DateTimeHelper.formatDate(date, DateTimeHelper.DATE_ONLY_FORMAT);
    }

    public static String formatTimeOnly(final Date date) {
        return DateTimeHelper.formatDate(date, DateTimeHelper.TIME_ONLY_FORMAT);
    }

    /**
     * 返回指定时间与当前时间的间隔
     * 
     * @param date
     *            指定的日期
     * @return 返回字符串类型的时间间隔
     */
    public static String getInterval(final Date date) {
        if (date == null) {
            return "";
        }
        final long seconds = (System.currentTimeMillis() - date.getTime()) / 1000;
        if (seconds < 3) {
            return "刚刚";
        } else if (seconds < DateTimeHelper.MIN) {
            return seconds + "秒钟前";
        } else if (seconds < DateTimeHelper.HOUR) {
            return (seconds / DateTimeHelper.MIN) + "分钟前";
        } else if (seconds < DateTimeHelper.DAY) {
            return (seconds / DateTimeHelper.HOUR) + "小时前";
        } else if (seconds < DateTimeHelper.WEEK) {
            return (seconds / DateTimeHelper.DAY) + "天前";
        } else {
            return DateTimeHelper.formatDate(date);
        }
    }

    /**
     * 返回指定时间与当前时间的间隔，单位为秒
     * 
     * @param date
     *            指定日期
     * @return 返回时间间隔，单位为秒
     */
    public static long interval(final Date date) {
        return (Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"))
                .getTimeInMillis() - date.getTime()) / 1000;
    }

}
