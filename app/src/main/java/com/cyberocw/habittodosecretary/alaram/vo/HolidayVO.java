package com.cyberocw.habittodosecretary.alaram.vo;

/**
 * Created by cyberocw on 2016-12-12.
 */

public class HolidayVO {
    //Seq, Year, month, day, type, name
    private long seq = -1;
    private long id = -1;
    private int year = -1;
    private int month = -1;
    private int day = -1;
    private String type = "";
    private String name = "";
    private String fullDate = "";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullDate() {
        return fullDate;
    }

    public void setFullDate(String fullDate) {
        this.fullDate = fullDate;
    }

    @Override
    public String toString() {
        return "HolidayVO{" +
                "seq=" + seq +
                ", id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", fullDate='" + fullDate + '\'' +
                '}';
    }
}
