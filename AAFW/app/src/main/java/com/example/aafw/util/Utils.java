package com.example.aafw.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    /* 날자 구하기 */
    public static String getDate(String format, long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date(timeMillis);

        return dateFormat.format(date);
    }

    /* 웹브라우저 연결 */
    public static void linkURL(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        context.startActivity(intent);
    }

}
