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
        new Object("타란툴라 성운","Tarantula Nebula","별 탄생 성운","대마젤란은하 속의 강력한 별 형성 지역.","Tarantula Nebula 30 Doradus",NASA),
        new Object("수성","Mercury","행성 · 암석형","태양에 가장 가까운 행성. 짧은 88일 공전 주기와 수많은 충돌구가 특징.","Mercury planet MESSENGER","https://science.nasa.gov/mercury/"),
        new Object("금성","Venus","행성 · 암석형","두꺼운 이산화탄소 대기와 강한 온실효과로 표면이 매우 뜨거워.","Venus planet NASA","https://science.nasa.gov/venus/"),
        new Object("지구","Earth","행성 · 암석형","액체 상태의 물과 생명체가 확인된 우리의 행성이야.","Earth blue marble NASA","https://science.nasa.gov/earth/"),
        new Object("화성","Mars","행성 · 암석형","붉은 산화철 먼지와 과거 액체 물의 흔적이 있어.","Mars planet NASA","https://science.nasa.gov/mars/"),
        new Object("목성","Jupiter","행성 · 가스 거대","태양계에서 가장 크며 대적점 같은 거대한 폭풍을 지녀.","Jupiter planet NASA","https://science.nasa.gov/jupiter/"),
        new Object("토성","Saturn","행성 · 가스 거대","얼음 입자와 암석 조각으로 구성된 광대한 고리가 특징이야.","Saturn rings Cassini NASA","https://science.nasa.gov/saturn/"),
        new Object("천왕성","Uranus","행성 · 얼음 거대","자전축이 크게 기울어진 푸른빛 얼음 거대행성이야.","Uranus Voyager 2 NASA","https://science.nasa.gov/uranus/"),
        new Object("해왕성","Neptune","행성 · 얼음 거대","강풍과 메탄이 특징적인 가장 바깥쪽의 공식 행성이야.","Neptune Voyager 2 NASA","https://science.nasa.gov/neptune/"),
        new Object("달","Moon","위성 · 지구","지구와 함께 진화해 온 자연위성. 충돌구와 달의 바다가 남아 있어.","Moon full disk NASA","https://science.nasa.gov/moon/"),
        new Object("명왕성","Pluto","왜행성","카이퍼 벨트의 왜행성. 뉴허라이즌스 탐사선이 2015년 근접 촬영했어.","Pluto New Horizons 2015","https://science.nasa.gov/dwarf-planets/pluto/"),
        new Object("유로파","Europa","위성 · 목성","목성의 얼음 위성으로, 얼음 껍질 아래 바다가 있을 것으로 연구돼.","Europa moon Galileo spacecraft","https://science.nasa.gov/jupiter/moons/europa/"),
        new Object("타이탄","Titan","위성 · 토성","두꺼운 대기와 액체 메탄·에탄 호수로 유명한 토성의 위성이야.","Titan moon Cassini Huygens","https://science.nasa.gov/saturn/moons/titan/"),
        new Object("엔셀라두스","Enceladus","위성 · 토성","남극 부근에서 얼음과 수증기 분출이 관측된 토성의 위성이야.","Enceladus moon Cassini geyser","https://science.nasa.gov/saturn/moons/enceladus/")
    };
    public static int daily(Context c,int widgetId) {
        long day=java.time.LocalDate.now().toEpochDay();
        int offset=c.getSharedPreferences("ast",0).getInt("discover_"+widgetId,0);
        return (int)Math.floorMod(day*17+offset*11,ALL.length);
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
