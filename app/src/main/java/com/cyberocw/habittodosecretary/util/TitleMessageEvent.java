package com.cyberocw.habittodosecretary.util;

/**
 * Created by user on 2017-09-11.
 */

public class TitleMessageEvent {
    public final String title;
    public final boolean isShowHelp;

    public TitleMessageEvent(String message, boolean isShowHelp) {
        this.title = message;
        this.isShowHelp = isShowHelp;
    }
}
