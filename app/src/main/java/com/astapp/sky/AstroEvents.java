package com.astapp.sky;

import java.util.*;

/** Hand-checked sample calendar: dates are UTC dates, not automatic visibility predictions.
 * Keep these entries updated when new IMO/NASA information is published. */
public final class AstroEvents {
    private AstroEvents(){}
    public static final class Event {
        public final String iso,title,note,source;
        public Event(String d,String t,String n,String s){iso=d;title=t;note=n;source=s;}
    }
    private static final String IMO="https://www.imo.net/calendar/";
    private static final String NASA="https://science.nasa.gov/eclipses/future-eclipses/";
    public static final Event[] EVENTS={
        new Event("2026-12-13","쌍둥이자리 유성우","12월 13~14일 무렵 극대. 실제 관측 시각은 IMO 일정 확인",IMO),
        new Event("2027-01-04","사분의자리 유성우","극대 시각은 매년 변화. 관측 조건은 지역과 달빛에 좌우됨",IMO),
        new Event("2027-02-06","금환일식","한국에서 관측 가능한 일식이 아님. 지역별 경로 확인",NASA),
        new Event("2027-02-20","반영월식","최대식 UTC 20일. 지역별 가시성은 NASA 자료 확인",NASA),
        new Event("2027-04-23","4월 거문고자리 유성우","극대 무렵 날짜. 현지 시간으로 바뀔 수 있음",IMO),
        new Event("2027-08-02","개기일식","한국에서 관측 가능한 일식이 아님. 지역별 경로 확인",NASA),
        new Event("2027-08-13","페르세우스자리 유성우","예상 활동 극대 무렵. 날짜는 UTC 기준",IMO),
        new Event("2027-12-14","쌍둥이자리 유성우","예상 활동 극대 무렵. 날짜는 UTC 기준",IMO)
    };
    public static List<Event> upcoming(){
        String today=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
        List<Event> future=new ArrayList<>();
        for(Event e:EVENTS)if(e.iso.compareTo(today)>=0)future.add(e);
        return future;
    }
}
