package com.astapp.sky;

import java.util.*;

/** Approximate synodic lunar phase; not lunar ephemeris, eclipse predictor or moonrise engine. */
public final class MoonPhase {
    private MoonPhase(){}
    private static final double SYNODIC=29.530588853;
    private static final long KNOWN_NEW=947182440000L; // 2000-01-06 18:14 UTC
    public static double age(long utcMs){
        double days=(utcMs-KNOWN_NEW)/86400000.0;
        return ((days % SYNODIC)+SYNODIC)%SYNODIC;
    }
    public static double lit(long utcMs){return (1-Math.cos(2*Math.PI*age(utcMs)/SYNODIC))/2;}
    public static String description(long utcMs){
        double a=age(utcMs)/SYNODIC;
        if(a<.035||a>=.965)return "삭 · 그믐/초승";
        if(a<.215)return "초승달";
        if(a<.285)return "상현달";
        if(a<.465)return "차오르는 달";
        if(a<.535)return "보름달";
        if(a<.715)return "기우는 달";
        if(a<.785)return "하현달";
        return "그믐달";
    }
    public static double untilFull(long utcMs){
        double age=age(utcMs);
        double v=SYNODIC/2-age;
        return v>=0?v:v+SYNODIC;
    }
}
