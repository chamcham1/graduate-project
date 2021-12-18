package com.example.aafw.util;

public class Constants {

    /* SharedPreferences 관련 상수 */
    public static class SharedPreferencesName {
        public static final String ALARM_NO = "alarm_no";   // 알람번호
    }

    /* 네이버 뉴스 api 관련 상수 */
    public static class NaverNewsApi {
        public static final String ADDRESS = "https://openapi.naver.com/v1/search/news.json";
        public static final String CLIENT_ID = "sCMHVg9qKjRcRQcs2MDk";
        public static final String CLIENT_SECRET = "2FO093ACO0";
    }

    /* 로컬 DB 테이블 이름 */
    public static class DataBaseTableName {
        public static final String ALARM = "Alarm";         // 알람
    }

    /* 로딩 딜레이 */
    public static class LoadingDelay {
        public static final int SHORT = 500;
        public static final int LONG = 1000;
    }
}
