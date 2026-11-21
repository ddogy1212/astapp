package com.astapp.sky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Transparent relative observing suitability, NOT a guarantee of visibility.
 * Uses calculated altitude, sun altitude, cloud forecast, rough equipment
 * accessibility, and a simplified moonlight penalty for faint deep-sky targets.
 * No claims about exact visual magnitude, terrain or light pollution.
 */
public final class ObservingPlanner {
    private ObservingPlanner(){}
    public static final class Candidate {
        public final String name,kind,source;
        public final Observation.Coord coords;
        public final int score;
        public final boolean accessible;
        public final String why,equipment;
        Candidate(String n,String k,String url,Observation.Coord pos,int rating,
                  boolean visible,String reason,String gear){
            name=n;kind=k;source=url;coords=pos;score=rating;
            accessible=visible;why=reason;equipment=gear;
        }
    }
    private static final String NASA="https://science.nasa.gov/";
    // Approximate reference J2000 coordinates for selected bright Messier objects.
    private static final String[] DSO_NAMES={"플레이아데스 M45","오리온 성운 M42","안드로메다 M31","헤라클레스 성단 M13","고리 성운 M57"};
    private static final double[][] DSO_COORD={
        {56.87,24.12},{83.82,-5.45},{10.69,41.27},{250.42,36.46},{283.40,33.03}
    };
    private static final int[] EASE={15,12,12,6,2};
    private static final String[] EQUIPMENT={
        "맨눈·쌍안경","맨눈(어두운 곳)·쌍안경","쌍안경·망원경","쌍안경·망원경","망원경"
    };
    private static final String[] MESSIER_URL={
        NASA+"mission/hubble/science/explore-the-night-sky/hubble-messier-catalog/messier-45/",
        NASA+"mission/hubble/science/explore-the-night-sky/hubble-messier-catalog/messier-42/",
        NASA+"mission/hubble/science/explore-the-night-sky/hubble-messier-catalog/messier-31/",
        NASA+"mission/hubble/science/explore-the-night-sky/hubble-messier-catalog/messier-13/",
        NASA+"mission/hubble/science/explore-the-night-sky/hubble-messier-catalog/messier-57/"
    };
    private static final int[] PLANET_EASE={6,14,0,12,15,11,2,1};
    private static final String[] PLANET_GEAR={
        "맨눈(태양 근처 저고도)","맨눈","—","맨눈·망원경","맨눈·쌍안경","맨눈·망원경","쌍안경·망원경","망원경"
    };
    private static double limit(double value,double a,double b){
        return Math.max(a,Math.min(b,value));
    }
    private static Candidate evaluate(String name,String kind,String url,Observation.Coord point,
                                     int ease,boolean faint,String gear,long date,
                                     Integer cloud,double sunAltitude,double illumination){
        double alt=point.altitude;
        boolean above=alt>=10;
        double altitudePoints=45*limit((alt-8)/55,0,1);
        double cloudPoints=cloud==null?12.5:25*(1-limit(cloud,0,100)/100);
        double nightPoints=sunAltitude<=-12?15:sunAltitude>=0?0:
                           15*limit((-sunAltitude)/12,0,1);
        double moonPenalty=faint?Math.round(9*illumination):0;
        double total=altitudePoints+cloudPoints+nightPoints+ease-moonPenalty;
        if(alt<0)total=0;
        else if(alt<10)total=Math.min(20,total);
        String altitude=alt<0?"지평선 아래":alt<15?"매우 낮은 고도":alt<30?"중간 고도":"비교적 높은 고도";
        String sky=sunAltitude>-6?" · 하늘이 밝은 시간":sunAltitude>-12?" · 박명 영향":"";
        String weather=cloud==null?" · 구름 예보 미확인":
            cloud>=75?" · 구름이 많을 전망":cloud>=40?" · 구름 영향 가능":" · 비교적 적은 구름 예보";
        String moon=faint && illumination>.65?" · 밝은 달빛 영향 주의":"";
        return new Candidate(name,kind,url,point,
            (int)Math.round(limit(total,0,100)),above,
            altitude+sky+weather+moon,gear);
    }
    public static List<Candidate> rank(long date,double lat,double lon,Integer cloud){
        ArrayList<Candidate> output=new ArrayList<>();
        double sun=Observation.sun(date,lat,lon).altitude,illum=MoonPhase.lit(date);
        for(int i=0;i<8;i++){
            if(i==2)continue;
            Observation.Coord pos=Observation.planet(i,date,lat,lon);
            output.add(evaluate(Ephemeris.NAME[i],"행성",PlanetFacts.URL[i],pos,
                PLANET_EASE[i],i>=6,PLANET_GEAR[i],date,cloud,sun,illum));
        }
        for(int j=0;j<DSO_NAMES.length;j++){
            Observation.Coord pos=Observation.forRaDec(DSO_COORD[j][0],
                DSO_COORD[j][1],date,lat,lon);
            output.add(evaluate(DSO_NAMES[j],"심우주 천체",MESSIER_URL[j],pos,
                EASE[j],j>1,EQUIPMENT[j],date,cloud,sun,illum));
        }
        Collections.sort(output,(a,b)->Integer.compare(b.score,a.score));
        return output;
    }
    public static String formula(){
        return "상대 점수 = 고도(45) + 구름량(25) + 하늘 어두움(15) + 일반적 관측 난이도(15) − 밝은 달빛 패널티(최대 9). "+
            "지평선 아래 천체는 0점. 구름 예보가 없으면 중립 12.5점을 적용. "+
            "이 점수는 공식 관측 확률이나 맨눈 밝기 예측이 아닙니다.";
    }
}
