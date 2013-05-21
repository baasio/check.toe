/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.baas.checktoe.utils;

import com.kth.baasio.entity.BaasioBaseEntity;
import com.kth.baasio.entity.entity.BaasioEntity;

import org.codehaus.jackson.JsonNode;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;

import io.baas.checktoe.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * An assortment of UI helpers.
 */
public class EtcUtils {
    public static final String GOOGLE_PLUS_PACKAGE_NAME = "com.google.android.apps.plus";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setActivatedCompat(View view, boolean activated) {
        if (hasHoneycomb()) {
            view.setActivated(activated);
        }
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed
        // behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return hasHoneycomb() && isTablet(context);
    }

    public static String getStringFromEntity(BaasioBaseEntity entity, String key) {
        JsonNode node = entity.getProperties().get(key);
        if (node != null) {
            String value = node.getTextValue();
            if (value != null && value.length() > 0) {
                return value.replace("<br>", "\n").replace("<BR>", "\n").trim();
            } else {
                return null;
            }
        }

        return null;
    }

    public static long getLongFromEntity(BaasioEntity entity, String key, long value) {
        JsonNode node = entity.getProperties().get(key);
        if (node != null) {
            // String result = node.toString();
            // if(result != null && result.length() > 0) {
            // return Long.valueOf(result.replace("\"", "").trim());
            // }

            return node.getLongValue();
        }

        return value;
    }

    public static int getIntFromEntity(BaasioEntity entity, String key, int value) {
        JsonNode node = entity.getProperties().get(key);
        if (node != null) {
            return node.getIntValue();
        }

        return value;
    }

    public static boolean getBooleanFromEntity(BaasioEntity entity, String key, boolean value) {
        JsonNode node = entity.getProperties().get(key);
        if (node != null) {
            return node.getBooleanValue();
        }

        return value;
    }

    public static String getDateString(long millis) {
        String time = null;

        if (Locale.getDefault().equals(Locale.KOREA) || Locale.getDefault().equals(Locale.KOREAN)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd a hh:mm",
                    Locale.getDefault());
            time = formatter.format(new Date(millis));
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd a hh:mm",
                    Locale.getDefault());
            time = formatter.format(new Date(millis));
        }

        return time;
    }

    public static String getSimpleDateString(long millis) {
        String time = null;

        if (Locale.getDefault().equals(Locale.KOREA) || Locale.getDefault().equals(Locale.KOREAN)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy년 M월 d일 a h시 mm분",
                    Locale.getDefault());
            time = formatter.format(new Date(millis));
        } else {
            time = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT,
                    Locale.getDefault()).format(new Date(millis));
        }

        return time;
    }

    /**
     * 게시물이 만들어진 유닉스 타임을 현재 유닉스 타임을 반영하여 계산한다.
     * 
     * @param createdtime 게시물이 만들어진 유닉스 타임.
     * @return
     */
    public static String getAgoTimeString(Context ctx, long createdtime) {
        long currentTime = System.currentTimeMillis();
        long result = (currentTime - createdtime) / 1000;
        /*
         * Log.d("test", "CurrentTime :" + a + " " + currentTime); Log.d("test",
         * "Createdtime :" + b + " " + Createdtime); Log.d("test", "result : " +
         * c + " " + result);
         */
        String r = null;

        // 클라이언트 처리
        if (result < 10) {
            r = ctx.getString(R.string.time_now);
            // 1초
        } else if (result < 60) {
            r = result + ctx.getString(R.string.time_sec);
            // 1분
        } else if (result < 2 * 60) { // 2분
            r = ctx.getString(R.string.time_one_min);
            // 분
        } else if (result < 3600) { // 1시간
            r = result / 60 + ctx.getString(R.string.time_min);
            // 1시간
        } else if (result < 2 * 3600) { // 2시간
            r = ctx.getString(R.string.time_one_hour);
            // 시간
        } else if (result < 24 * 3600) { // 1일
            r = result / 3600 + 1 + ctx.getString(R.string.time_hour);
            // 어제
        } else if (result < 2 * 24 * 3600) { // 2일
            r = ctx.getString(R.string.time_one_day);
            // 일
        } else if (result < 7 * 24 * 3600) { // 7일
            r = result / (24 * 3600) + ctx.getString(R.string.time_day);
            // 주
        } else if (result < 30 * 24 * 3600) { // 한달
            long weeks = result / (7 * 24 * 3600);
            if (weeks <= 1) {
                r = ctx.getString(R.string.time_one_weeks);
            } else {
                r = weeks + ctx.getString(R.string.time_week);
            }
            // 달
        } else if (result < 12 * 30 * 24 * 3600) { // 1년
            long months = result / (30 * 24 * 3600);
            if (months <= 1) {
                r = ctx.getString(R.string.time_one_month);
            } else {
                r = months + ctx.getString(R.string.time_month);
            }
            // 년
        } else {
            long years = result / (12 * 30 * 24 * 3600);
            if (years <= 1) {
                r = ctx.getString(R.string.time_one_year);
            } else {
                r = years + ctx.getString(R.string.time_year);
            }
        }
        return r;
    }
}
