package com.cyberocw.habittodosecretary.keyword.vo;

import java.util.Date;

/**
 * Created by cyber on 2017-07-06.
 */

public class KeywordVO {
      private long id = -1;
      private String keyword ="";
      private int rank = -1;
      private String fromSite = "";
      private float point = 0;
      private Date regDate;
      private long simpleDate;
      private int typeCode;
    private String rankNAVER;
    private String rankDAUM;
    private String rankZUM;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getFromSite() {
        return fromSite;
    }

    public void setFromSite(String fromSite) {
        this.fromSite = fromSite;
    }

    public float getPoint() {
        return point;
    }

    public void setPoint(float point) {
        this.point = point;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public long getSimpleDate() {
        return simpleDate;
    }

    public void setSimpleDate(long simpleDate) {
        this.simpleDate = simpleDate;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    public String getRankNAVER() {
        return rankNAVER;
    }

    public void setRankNAVER(String rankNAVER) {
        this.rankNAVER = rankNAVER;
    }

    public String getRankDAUM() {
        return rankDAUM;
    }

    public void setRankDAUM(String rankDAUM) {
        this.rankDAUM = rankDAUM;
    }

    public String getRankZUM() {
        return rankZUM;
    }

    public void setRankZUM(String rankZUM) {
        this.rankZUM = rankZUM;
    }
}
