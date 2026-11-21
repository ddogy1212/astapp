package com.astapp.sky;

import android.content.Context;
import java.util.Calendar;

/** Curated, manually reviewed introductory objects. Distances intentionally omitted
 * when estimates depend strongly on model/method. The linked NASA source is primary. */
public final class CosmicCatalog {
    private CosmicCatalog(){}
    public static final class Object {
        public final String name,english,kind,fact,query,url;
        Object(String n,String e,String k,String f,String q,String u){
            name=n;english=e;kind=k;fact=f;query=q;url=u;
        }
    }
    private static final String M="https://science.nasa.gov/mission/hubble/science/explore-the-night-sky/hubble-messier-catalog/";
    private static final String NASA="https://images.nasa.gov/";
    public static final Object[] ALL={
        new Object("오리온 성운","Messier 42","발광 성운","오리온자리의 별 탄생 지역. 뜨거운 어린 별들이 주위 가스를 밝힌다.","Orion Nebula M42",M),
        new Object("안드로메다 은하","Messier 31","나선은하","우리은하와 같은 국부은하군의 거대 나선은하로 약 250만 광년 떨어져 있다.","Andromeda galaxy M31",M),
        new Object("플레이아데스 성단","Messier 45","산개성단","푸른색의 젊은 별들이 모인 황소자리 산개성단. 맨눈으로도 볼 수 있다.","Pleiades M45",M),
        new Object("게 성운","Messier 1","초신성 잔해","1054년에 관측된 초신성과 관련된 잔해. 중심에는 펄서가 있다.","Crab Nebula M1",M),
        new Object("독수리 성운","Messier 16","성운과 성단","유명한 '창조의 기둥'을 포함하는 별 탄생 지역.","Eagle Nebula M16 Pillars of Creation",M),
        new Object("고리 성운","Messier 57","행성상 성운","별이 생애 말기에 바깥층의 가스를 내보내 만든 행성상 성운.","Ring Nebula M57",M),
        new Object("헤라클레스 구상성단","Messier 13","구상성단","오래된 별들이 밀집된 거대한 구상성단.","M13 globular cluster",M),
        new Object("소용돌이 은하","Messier 51","상호작용 은하","동반은하와 상호작용하는 뚜렷한 나선팔로 유명하다.","Whirlpool galaxy M51",M),
        new Object("삼각형자리 은하","Messier 33","나선은하","국부은하군에 속하는 가까운 나선은하.","Triangulum galaxy M33",M),
        new Object("벌집 성단","Messier 44","산개성단","게자리에 있는 비교적 가까운 산개성단.","Beehive Cluster M44",M),
        new Object("석호 성운","Messier 8","성운","궁수자리 방향의 별 탄생 영역. 밝은 가스와 어두운 먼지 구조를 볼 수 있다.","Lagoon Nebula M8",M),
        new Object("삼엽 성운","Messier 20","성운","밝게 빛나는 가스와 먼지 띠가 독특한 무늬를 만든다.","Trifid Nebula M20",M),
        new Object("솜브레로 은하","Messier 104","은하","중앙 팽대부와 뚜렷한 먼지 띠가 모자처럼 보인다.","Sombrero galaxy M104",M),
        new Object("보데 은하","Messier 81","나선은하","큰곰자리에 있는 밝은 나선은하.","Bode Galaxy M81",M),
        new Object("히아데스 성단","Hyades","산개성단","황소자리의 별 무리로, 플레이아데스와는 다른 성단이다.","Hyades star cluster",NASA),
        new Object("용골자리 성운","Carina Nebula","발광 성운","용골자리 방향의 거대한 별 탄생 지역. 매우 무거운 별들이 존재한다.","Carina Nebula NGC 3372",NASA),
        new Object("나선 성운","Helix Nebula","행성상 성운","우리 태양과 비슷한 별의 진화 후기에 만들어진 가스 껍질.","Helix Nebula NGC 7293",NASA),
        new Object("말머리 성운","Horsehead Nebula","암흑 성운","먼지 구름이 뒤편의 밝은 가스를 가려 말 머리 같은 윤곽을 만든다.","Horsehead Nebula Barnard 33",NASA),
        new Object("장미 성운","Rosette Nebula","방출 성운","중심의 젊은 별들이 주변 가스를 밝히는 넓은 별 형성 영역.","Rosette Nebula NGC 2237",NASA),
        new Object("베텔게우스","Betelgeuse","적색초거성","오리온자리의 밝은 적색초거성. 밝기가 변하는 변광성이다.","Betelgeuse star",NASA),
        new Object("프록시마 센타우리","Proxima Centauri","적색왜성","태양에 가장 가까운 것으로 알려진 별. 센타우루스자리 방향에 있다.","Proxima Centauri star",NASA),
        new Object("타란툴라 성운","Tarantula Nebula","별 탄생 성운","대마젤란은하 속의 강력한 별 형성 지역.","Tarantula Nebula 30 Doradus",NASA)
    };
    public static int daily(Context c,int widgetId) {
        long day=System.currentTimeMillis()/86400000L;
        int offset=c.getSharedPreferences("ast",0).getInt("discover_"+widgetId,0);
        return (int)Math.floorMod(day*17+offset*7,ALL.length);
    }
    public static Object current(Context c,int widgetId){return ALL[daily(c,widgetId)];}
    public static void next(Context c,int widgetId) {
        android.content.SharedPreferences p=c.getSharedPreferences("ast",0);
        p.edit().putInt("discover_"+widgetId,p.getInt("discover_"+widgetId,0)+1).apply();
    }
    public static boolean favorite(Context c,String id) {
        return c.getSharedPreferences("ast",0).getBoolean("fav_"+id,false);
    }
    public static boolean toggleFavorite(Context c,String id) {
        boolean next=!favorite(c,id);
        c.getSharedPreferences("ast",0).edit().putBoolean("fav_"+id,next).apply();
        return next;
    }
}
