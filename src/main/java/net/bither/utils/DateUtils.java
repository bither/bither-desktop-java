/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package net.bither.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * <p>Utilities to provide the following to application:</p>
 * <ul>
 * <li>Provision of standard date and time handling</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public final class DateUtils {

    public static final long SECOND_IN_MILLIS = 1000;

    /**
     * Utility class should not have a public constructor
     */
    private DateUtils() {
    }

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String DEFAULT_TIMEZONE = "GMT+0";
    private static final String DATE_TIME_FORMAT_DCIM_FilENAME = "yyyyMMdd_HHmmss";

    public static DateTime nowUtc() {
        return new DateTime().withZone(DateTimeZone.UTC);
    }


    public static final String getDateTimeString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
        String result = df.format(date);
        return result;
    }

    public static final Date getDateTimeForTimeZone(String str)
            throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
        Long time = new Date(df.parse(str).getTime()).getTime();
        return getDateTimeForTimeZone(time);
    }

    public static final Date getDateTimeForTimeZone(Long time) {
        Long sourceRelativelyGMT = time
                - TimeZone.getTimeZone(DEFAULT_TIMEZONE).getRawOffset();
        Long targetTime = sourceRelativelyGMT
                + TimeZone.getDefault().getRawOffset();

        Date date = new Date(targetTime);
        return date;

    }

    public static final String getNameForFile(long time) {
        Date date = new Date(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                DATE_TIME_FORMAT_DCIM_FilENAME);
        return dateFormat.format(date);
    }

    public static String dateToRelativeTime(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date now = new Date();
        long between = (now.getTime() - date.getTime()) / 1000;
        long day = between / (24 * 3600);
        long hour = between % (24 * 3600) / 3600;
        long minute = between % 3600 / 60;
        long second = between;

        String timeStr;
        if (day > 0 && day <= 29) {
            timeStr = day + " day " + hour + ":" + minute;
        } else if (day > 29) {
            timeStr = df.format(date);
        } else {
            if (hour > 0) {
                timeStr = hour + " hour " + minute + " minutes";
            } else {
                if (minute > 0) {
                    timeStr = minute + " minutes";
                } else {
                    timeStr = second + " seconds";
                }
            }
        }
        return timeStr;
    }
}
