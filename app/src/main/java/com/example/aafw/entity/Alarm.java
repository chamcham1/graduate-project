package com.example.aafw.entity;

import android.os.Parcel;
import android.os.Parcelable;

/*
Parcelable 타입의 객체만 Intent 을 통해 Activity 간 데이터를 넘길 수 있음
 */
public class Alarm implements Parcelable {

    public int alarmNo;             // 알람번호 key
    public long dateTime;           // 알람일시 (millisecond 로 표현)
    public String name;             // 알람이름

    public Alarm(int alarmNo, long dateTime, String name) {
        this.alarmNo = alarmNo;
        this.dateTime = dateTime;
        this.name = name;
    }

    public Alarm(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.alarmNo);
        dest.writeLong(this.dateTime);
        dest.writeString(this.name);
    }

    private void readFromParcel(Parcel in){
        this.alarmNo = in.readInt();
        this.dateTime = in.readLong();
        this.name = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };
}
