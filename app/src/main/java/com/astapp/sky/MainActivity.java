package com.astapp.sky;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Native-first ASTAPP alpha. Main app provides the detailed controls that
 * Android home widgets cannot animate continuously.
 */
public final class MainActivity extends Activity {
    private static final int BG=0xff07101F, SURFACE=0xff142239, SURFACE2=0xff243759;
    private static final int TEXT=0xffEFF6FF, MUTED=0xffA7B9D4, BLUE=0xff9AC5FF;
    private LinearLayout page,nav;
    private int activeTab=0;
    private long solarTime=System.currentTimeMillis();
    private boolean compressed=false;
    private PlanetView planetView;
    private int selectedCatalog=-1;
    private double observerLat=37.5665,observerLon=126.9780;
    private String site="서울 기준점(직접 변경 가능)";
    private static final String[] TABS={"🪐 태양계","🔭 관측","🌙 달","☄ 이벤트","✦ 우주 도감"};

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        android.content.SharedPreferences pref=getSharedPreferences("ast",0);
        observerLat=Double.longBitsToDouble(pref.getLong("lat",Double.doubleToLongBits(observerLat)));
        observerLon=Double.longBitsToDouble(pref.getLong("lon",Double.doubleToLongBits(observerLon)));
        site=pref.getString("site",site);
        compressed=pref.getBoolean("scale_compressed",false);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        setContentView(root);
        TextView heading=text("ASTAPP   ✦   우주를 홈 화면에",21,TEXT,true);
        heading.setPadding(dp(20),dp(26),dp(18),dp(14));root.addView(heading);
        HorizontalScrollView tabs=new HorizontalScrollView(this);
        tabs.setHorizontalScrollBarEnabled(false);
        nav=new LinearLayout(this);nav.setPadding(dp(8),0,dp(8),dp(8));
        tabs.addView(nav);tabs.setBackgroundColor(0xff0B1A2E);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14),dp(8),dp(14),dp(32));
        scroll.addView(page);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));root.addView(tabs);
        int requested=getIntent().getIntExtra("tab",0);
        showTab(Math.max(0,Math.min(TABS.length-1,requested)));
        for(int i=0;i<8;i++){
            final int index=i;
            PlanetPhotos.load(this,index,bmp->{if(bmp!=null){SkyWidgets.refreshSolar(this);if(planetView!=null)planetView.invalidate();}});
        }
        MoonPhoto.load(this,bmp->{});
    }
    @Override public void onNewIntent(Intent intent){
        super.onNewIntent(intent);setIntent(intent);
        showTab(Math.max(0,Math.min(4,intent.getIntExtra("tab",0))));
    }
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable background(int color,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),0xff344962);return d;
    }
    private TextView text(String s,int size,int color,boolean bold){
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);
        if(bold)v.setTypeface(android.graphics.Typeface.DEFAULT,1);
        v.setLineSpacing(dp(3),1.05f);
        return v;
    }
    private LinearLayout card(){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(15),dp(16),dp(17));
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff172A47,0xff0E1C34});
        bg.setCornerRadius(dp(22));bg.setStroke(dp(1),0xff2A405E);c.setBackground(bg);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(12));
        page.addView(c,p);return c;
    }
    private void line(LinearLayout c,String title,String value){
        c.addView(text(title,13,MUTED,false));
        TextView v=text(value,16,TEXT,true);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(3),0,dp(10));c.addView(v,lp);
    }
    private void paragraph(LinearLayout c,String s){TextView t=text(s,13,MUTED,false);t.setPadding(0,dp(4),0,dp(10));c.addView(t);}
    private TextView header(String s){
        TextView v=text(s,19,TEXT,true);v.setPadding(dp(2),dp(7),0,dp(12));page.addView(v);return v;
    }
    private LinearLayout row(LinearLayout parent){
        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.setMargins(0,dp(4),0,dp(6));
        parent.addView(r,params);return r;
    }
    private TextView button(LinearLayout where,String label,Runnable click){
        TextView b=text(label,12,TEXT,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(9),dp(12),dp(9),dp(12));
        b.setBackground(background(SURFACE2,16));b.setElevation(dp(1));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(3),0,dp(3),0);
        where.addView(b,lp);b.setOnClickListener(v->click.run());return b;
    }
    private void sourceButton(LinearLayout c,String label,String url){
        LinearLayout row=row(c);button(row,label+" ↗",()->{
            try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){
                Toast.makeText(this,"페이지를 열 수 없습니다",Toast.LENGTH_SHORT).show();}
        });
    }
    private void showTab(int tab){
        activeTab=tab;
        nav.removeAllViews();
        for(int i=0;i<TABS.length;i++){
            final int t=i;
            TextView b=text(TABS[i],13,i==tab?0xff051426:MUTED,true);
            b.setPadding(dp(15),dp(13),dp(15),dp(13));
            b.setBackground(background(i==tab?BLUE:SURFACE,14));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(dp(3),0,dp(3),0);
            nav.addView(b,p);b.setOnClickListener(v->showTab(t));
        }
        page.removeAllViews();
        switch(tab){
            case 0:showSolar();break;
            case 1:showObservation();break;
            case 2:showMoon();break;
            case 3:showEvents();break;
            default:showCatalog();break;
        }
    }
    private void shiftMonth(int month){
        Calendar c=Calendar.getInstance();c.setTimeInMillis(solarTime);c.add(Calendar.MONTH,month);
        if(!Ephemeris.inRange(c.getTimeInMillis())){
            Toast.makeText(this,"JPL 근사식의 유효 범위는 1800~2050년입니다",Toast.LENGTH_SHORT).show();return;
        }
        solarTime=c.getTimeInMillis();showTab(0);
    }
    private void showSolar(){
        header("실제 위치 기반 태양계");
        LinearLayout main=card();
        paragraph(main,"태양 중심 · J2000 황도 좌표 · JPL 1800~2050 근사식. 행성은 실제 계산 좌표를 사용하고 크기만 알아보기 쉽게 키웠어.");
        TextView date=text(new SimpleDateFormat("yyyy-MM-dd HH:mm z",Locale.KOREA).format(new Date(solarTime)),16,TEXT,true);
        main.addView(date);
        planetView=new PlanetView(this);
        main.addView(planetView,new LinearLayout.LayoutParams(-1,dp(360)));
        paragraph(main,"드래그로 시점 회전 · 두 손가락으로 확대/축소. 실제 내행성 위치를 자세히 보려면 아래 ‘내행성 확대’를 눌러봐.");
        LinearLayout focus=row(main);
        button(focus,"☀ 전체 태양계",()->{planetView.zoom=1;planetView.invalidate();});
        button(focus,"🔎 내행성 확대",()->{planetView.zoom=14;planetView.invalidate();});
        LinearLayout controls=row(main);
        button(controls,"◀ 한 달",()->shiftMonth(-1));
        button(controls,"현재 시각",()->{solarTime=System.currentTimeMillis();showTab(0);});
        button(controls,"한 달 ▶",()->shiftMonth(1));
        LinearLayout scale=row(main);
        button(scale,compressed?"축척: 궤도 압축":"축척: 실제 거리",()->{
            compressed=!compressed;getSharedPreferences("ast",0).edit().putBoolean("scale_compressed",compressed).apply();
            SkyWidgets.refreshAll(this);showTab(0);
        });
        LinearLayout planets=card();line(planets,"행성 위치","아래 행성을 누르면 태양 중심 거리와 지구에서 본 방향을 보여줍니다.");
        for(int group=0;group<2;group++){
            LinearLayout r=row(planets);
            for(int j=0;j<4;j++){
                int idx=group*4+j;
                button(r,Ephemeris.NAME[idx],()->planetDialog(idx));
            }
        }
        paragraph(planets,"정밀 우주항법용 자료가 아닙니다. 지구 좌표는 지구–달 질량중심 근사입니다. 행성 표면 텍스처와 자세는 실시간 관측 영상이 아닙니다.");
        sourceButton(planets,"JPL 원본 궤도요소","https://ssd.jpl.nasa.gov/planets/approx_pos.html");
        LinearLayout widgetCard=card();
        line(widgetCard,"홈 화면 위젯 3종","2×2 / 4×2 / 4×4 · 내행성/외행성 실제 축척 분리");
        paragraph(widgetCard,"갤럭시 홈 화면을 길게 누르기 → 위젯 → ASTAPP → 원하는 크기 추가. 위젯에서 ◀ / 지금 / ▶ 버튼으로 시각 변경.");
    }

    private void planetDialog(int i){
        double[] p=Ephemeris.position(i,solarTime);
        double[] earth=Ephemeris.position(2,solarTime);
        double[] difference={p[0]-earth[0],p[1]-earth[1],p[2]-earth[2]};
        ScrollView scroll=new ScrollView(this);
        LinearLayout wrap=new LinearLayout(this);wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(22),dp(10),dp(22),dp(12));scroll.addView(wrap);
        ImageView picture=new ImageView(this);
        picture.setBackground(background(0xff14273D,16));
        picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
        wrap.addView(picture,new LinearLayout.LayoutParams(-1,dp(190)));
        Bitmap cached=PlanetPhotos.cached(this,i);
        if(cached!=null)picture.setImageBitmap(cached);
        else PlanetPhotos.load(this,i,photo->{if(photo!=null)picture.setImageBitmap(photo);});
        wrap.addView(text(Ephemeris.NAME[i]+"  ·  "+Ephemeris.ENGLISH[i],22,TEXT,true));
        TextView credit=text("NASA 과거 관측 사진 · "+PlanetFacts.PHOTO_NOTE[i]+" · 현재 모습 아님",11,MUTED,false);
        wrap.addView(credit);
        TextView description=text(PlanetFacts.SUMMARY[i],14,TEXT,false);
        description.setPadding(0,dp(14),0,dp(16));wrap.addView(description);
        wrap.addView(text("태양까지 거리: "+Ephemeris.au(p),15,BLUE,true));
        wrap.addView(text("지구까지 거리(근사): "+Ephemeris.au(difference),15,BLUE,true));
        TextView xyz=text("J2000 황도 좌표 (AU):  X "+fmt(p[0])+"  Y "+fmt(p[1])+"  Z "+fmt(p[2]),12,MUTED,false);
        xyz.setPadding(0,dp(10),0,dp(10));wrap.addView(xyz);
        if(i!=2){
            Observation.Coord obs=Observation.planet(i,solarTime,observerLat,observerLon);
            wrap.addView(text(site+" 기준 고도 "+fmt(obs.altitude)+"° · 방위각 "+fmt(obs.azimuth)+"°",13,TEXT,false));
        }
        new AlertDialog.Builder(this).setView(scroll)
            .setNeutralButton("NASA 천체 정보",(d,which)->openWebsite(PlanetFacts.URL[i]))
            .setPositiveButton("닫기",null).show();
    }
    private void openWebsite(String url){
        try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
        catch(Exception e){Toast.makeText(this,"웹페이지를 열 수 없습니다",Toast.LENGTH_LONG).show();}
    }
    private String fmt(double d){return String.format(Locale.KOREA,"%.2f",d);}
    private void useGps(){
        if(!LocationAccess.permitted(this)){LocationAccess.request(this);return;}
        Toast.makeText(this,"현재 위치 확인 중…",Toast.LENGTH_SHORT).show();
        LocationAccess.current(this,(location,error)->{
            if(location==null){
                Toast.makeText(this,error==null?"위치를 찾지 못했어요":error,Toast.LENGTH_LONG).show();
                return;
            }
            observerLat=location.getLatitude();observerLon=location.getLongitude();
            site="내 현재 위치 (기기 위치 센서)";
            getSharedPreferences("ast",0).edit()
                .putLong("lat",Double.doubleToRawLongBits(observerLat))
                .putLong("lon",Double.doubleToRawLongBits(observerLon))
                .putString("site",site).apply();
            showTab(1);
        });
    }
    @Override public void onRequestPermissionsResult(int code,String[] perms,int[] grants){
        super.onRequestPermissionsResult(code,perms,grants);
        if(code==LocationAccess.REQ){
            if(LocationAccess.permitted(this))useGps();
            else Toast.makeText(this,"위치 권한 없이도 지역을 직접 설정할 수 있어요",Toast.LENGTH_LONG).show();
        }
    }

    private void selectSite(){
        LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);layout.setPadding(dp(24),dp(8),dp(24),dp(8));
        EditText name=new EditText(this);name.setSingleLine(true);name.setText(site);name.setHint("장소 표시 이름");
        EditText lat=new EditText(this);lat.setSingleLine(true);lat.setInputType(8194);lat.setText(String.valueOf(observerLat));lat.setHint("위도 °");
        EditText lon=new EditText(this);lon.setSingleLine(true);lon.setInputType(8194);lon.setText(String.valueOf(observerLon));lon.setHint("경도 °");
        layout.addView(name);layout.addView(lat);layout.addView(lon);
        new AlertDialog.Builder(this).setTitle("관측 위치 수동 설정 (GPS 사용 안 함)").setView(layout)
            .setPositiveButton("저장",(d,v)->{
                try{
                    double a=Double.parseDouble(lat.getText().toString()),b=Double.parseDouble(lon.getText().toString());
                    if(!Double.isFinite(a)||!Double.isFinite(b)||Math.abs(a)>90||Math.abs(b)>180)throw new Exception();
                    observerLat=a;observerLon=b;site=name.getText().toString().trim();
                    if(site.isEmpty())site="사용자 지정 위치";
                    getSharedPreferences("ast",0).edit().putLong("lat",Double.doubleToRawLongBits(a))
                        .putLong("lon",Double.doubleToRawLongBits(b)).putString("site",site).apply();
                    showTab(1);
                }catch(Exception ex){Toast.makeText(this,"위도 -90~90, 경도 -180~180을 입력하세요",Toast.LENGTH_LONG).show();}
            }).setNegativeButton("취소",null).show();
    }

    private void showObservation(){
        header("🔭 오늘 밤, 내 위치에서");
        long night=Observation.nextNightAtNine();
        Observation.Coord sun=Observation.sun(night,observerLat,observerLon);
        LinearLayout controls=card();
        line(controls,"관측 위치",site);
        paragraph(controls,"위도 "+fmt(observerLat)+"° · 경도 "+fmt(observerLon)+"°  |  위치 정보는 기기에 저장");
        line(controls,"예측 시각",new SimpleDateFormat("yyyy-MM-dd HH:mm z",Locale.KOREA).format(new Date(night)));
        LinearLayout r=row(controls);
        button(r,"📍 실제 GPS 위치",this::useGps);
        button(r,"✎ 직접 입력",this::selectSite);
        paragraph(controls,"GPS는 버튼을 눌렀을 때만 요청하고 백그라운드 위치 추적을 하지 않아. 아래 구름량을 확인할 때만 선택한 좌표가 Open-Meteo로 전달돼.");
        TextView clouds=text("☁ 내 지역의 실제 구름 예보 확인 중…",14,BLUE,true);
        clouds.setPadding(0,dp(8),0,dp(12));controls.addView(clouds);
        final double lat=observerLat,lon=observerLon;
        WeatherForecast.load(lat,lon,night,(coverage,timestamp,error)->{
            if(activeTab!=1||lat!=observerLat||lon!=observerLon)return;
            if(coverage==null)clouds.setText("☁ 날씨 자료 없음 · "+error);
            else clouds.setText("☁ "+timestamp+" 현지 구름량 "+coverage+"%  · Open-Meteo 예보");
        });
        if(sun.altitude>-12)paragraph(controls,"⚠ 이 시각 태양 고도 "+fmt(sun.altitude)+"°. 충분히 어둡지 않을 수 있어.");
        List<Observation.Target> list=Observation.sorted(night,observerLat,observerLon);
        int count=0;
        for(Observation.Target t:list){
            LinearLayout c=card();
            line(c,"0"+(++count)+"  "+Ephemeris.NAME[t.index],fmt(t.coord.altitude)+"° · "+Observation.azimuthText(t.coord.azimuth)+"  ("+fmt(t.coord.azimuth)+"°)");
            paragraph(c,t.coord.altitude<0?"지평선 아래 · 이 시각에는 보이지 않음":
                    t.coord.altitude<15?"고도가 낮아 건물·지형·대기의 영향을 많이 받음":
                    "지평선 위 · 구름·기상·밝기·광공해를 추가 확인해");
            LinearLayout pr=row(c);
            final int idx=t.index;
            button(pr,"행성 사진·자세히 보기",()->planetDialog(idx));
            if(count>=3)break;
        }
        LinearLayout source=card();
        paragraph(source,"현재 1~3위는 밝기 순위가 아닌 천체의 겉보기 고도 순위야. 실제 관측 가능 여부는 구름, 광공해, 지형, 밝기, 달빛 등의 영향을 받아.");
        sourceButton(source,"관측 좌표 기준","https://ssd.jpl.nasa.gov/planets/approx_pos.html");
        sourceButton(source,"날씨 정보 제공처","https://open-meteo.com/");
    }


    private void showMoon(){
        header("🌙 달 아카이브 & 위상");
        long now=System.currentTimeMillis();
        LinearLayout moon=card();
        ImageView lunar=new ImageView(this);
        lunar.setBackground(background(0xff06101E,16));
        lunar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        moon.addView(lunar,new LinearLayout.LayoutParams(-1,dp(260)));
        TextView loading=text("NASA 실제 달 사진을 불러오는 중…",12,MUTED,false);
        moon.addView(loading);
        Bitmap saved=MoonPhoto.cached(this);
        if(saved!=null){
            lunar.setImageBitmap(MoonPhoto.phaseComposited(saved,now,440));
            loading.setText("NASA 실제 사진 · 현재 위상은 계산한 명암을 입힌 합성 표현");
        }else{
            MoonPhoto.load(this,photo->{
                if(photo!=null && activeTab==2){
                    lunar.setImageBitmap(MoonPhoto.phaseComposited(photo,now,440));
                    loading.setText("NASA 실제 사진 · 현재 위상은 계산한 명암을 입힌 합성 표현");
                }else if(photo==null)loading.setText("사진을 불러오지 못했어 · 인터넷 연결을 확인해 줘");
            });
        }
        line(moon,MoonPhase.description(now),"태양에 비친 면의 비율 · 약 "+fmt(MoonPhase.lit(now)*100)+"%");
        line(moon,"삭 이후 경과","약 "+fmt(MoonPhase.age(now))+"일");
        line(moon,"다음 보름까지","약 "+fmt(MoonPhase.untilFull(now))+"일");
        paragraph(moon,"달 표면은 NASA의 실물 관측 사진이지만 촬영 당시의 기록이야. 어두운 부분은 현재 위상을 평균 삭망월로 단순화해 적용한 모형이며 실제 달의 방향·칭동을 정밀하게 재현한 실시간 영상이 아니야.");
        sourceButton(moon,"NASA 달 실제 사진 원문",MoonPhoto.SOURCE);
        sourceButton(moon,"NASA 달 관측 자료","https://science.nasa.gov/moon/image-galleries/");
    }

    private void showEvents(){
        header("☄ 주요 천문 이벤트");
        LinearLayout intro=card();
        paragraph(intro,"NASA와 국제유성기구(IMO) 일정을 참고한 오프라인 선별 달력이야. 실시간 갱신이나 한국 지역별 가시성 판정은 아직 없어. 날짜는 자료의 UTC 기준일이야.");
        java.util.List<AstroEvents.Event> events=AstroEvents.upcoming();
        if(events.isEmpty()){paragraph(intro,"현재 등록된 미래 이벤트가 없어. 데이터 업데이트가 필요해.");}
        for(AstroEvents.Event e:events){
            LinearLayout c=card();
            line(c,e.iso,e.title);
            paragraph(c,e.note);
            sourceButton(c,"공식 출처",e.source);
        }
    }
    private void showCatalog(){
        header("🌌 COSMIC DISCOVERY");
        if(selectedCatalog<0)selectedCatalog=CosmicCatalog.daily(this,-1);
        CosmicCatalog.Object obj=CosmicCatalog.ALL[selectedCatalog];
        LinearLayout c=card();
        line(c,"우주 도감 "+(selectedCatalog+1)+" / "+CosmicCatalog.ALL.length,obj.name);
        paragraph(c,obj.english+" · "+obj.kind);
        ImageView image=new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundColor(0xff182E4C);
        c.addView(image,new LinearLayout.LayoutParams(-1,dp(240)));
        Bitmap cached=NasaPhotos.cached(this,obj);
        if(cached!=null)image.setImageBitmap(cached);
        else {
            image.setImageDrawable(null);
            TextView loading=text("NASA Images 사진 검색 중… (없으면 텍스트만 표시)",11,MUTED,false);
            c.addView(loading);
            NasaPhotos.load(this,obj,(bitmap,credit)->{
                if(bitmap!=null && activeTab==4 && selectedCatalog>=0 && CosmicCatalog.ALL[selectedCatalog]==obj){
                    image.setImageBitmap(bitmap);loading.setText(credit);
                }else if(bitmap==null)loading.setText("NASA 사진 미확인 · 자료 링크에서 직접 확인 가능");
            });
        }
        CosmicDetails.Info details=CosmicDetails.get(selectedCatalog);
        line(c,"천체 유형",obj.kind+" · "+obj.english);
        line(c,"지구에서의 거리",details.distance);
        line(c,"하늘에서 찾는 영역",details.region);
        line(c,"무엇이 특별할까?",obj.fact);
        paragraph(c,details.science);
        line(c,"🔭 실제 관측 팁",details.observation);
        line(c,"💡 알아두면 좋은 사실",details.question);
        paragraph(c,"사진은 NASA Image Library의 과거 관측 기록이야. 관측 파장·합성 여부·원본 크레딧은 링크에서 확인해 줘. 확인되지 않은 천체 사진은 임의로 만들어 표시하지 않아.");
        sourceButton(c,"근거가 되는 NASA 연구·설명",details.reference);
        LinearLayout buttons=row(c);
        button(buttons,"↻ 다른 천체",()->{
            CosmicCatalog.next(this,-1);
            selectedCatalog=CosmicCatalog.daily(this,-1);
            showTab(4);SkyWidgets.refreshAll(this);
        });
        button(buttons,CosmicCatalog.favorite(this,obj.english)?"★ 저장됨":"☆ 즐겨찾기",()->{
            CosmicCatalog.toggleFavorite(this,obj.english);showTab(4);
        });
        sourceButton(c,"NASA 원문/도감",obj.url);
        sourceButton(c,"NASA 이미지 검색","https://images.nasa.gov/search?q="+Uri.encode(obj.query));
        LinearLayout info=card();
        line(info,"홈 화면에 발견 위젯 추가","2×1 / 2×2 / 4×2 / 4×4");
        paragraph(info,"위젯의 ↻ 버튼을 누르면 다른 천체를 보여줘. 사진은 인터넷이 있을 때 NASA에서 검색하고 휴대전화에 캐시해.");
        line(info,"이 버전의 데이터","검증된 천체 "+CosmicCatalog.ALL.length+"개 · 서버 계정 불필요");
    }

    private final class PlanetView extends View {
        double yaw=-.38,tilt=.42,zoom=1;float lastX,lastY;
        private final ScaleGestureDetector detector;
        PlanetView(Context context){
            super(context);setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            detector=new ScaleGestureDetector(context,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
                @Override public boolean onScale(ScaleGestureDetector d){
                    zoom=Math.max(.3,Math.min(24,zoom*d.getScaleFactor()));invalidate();return true;
                }
            });
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            SolarPainter.draw(MainActivity.this,canvas,getWidth(),getHeight(),solarTime,compressed,true,yaw,tilt,zoom);
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            detector.onTouchEvent(e);
            if(e.getActionMasked()==MotionEvent.ACTION_DOWN){lastX=e.getX();lastY=e.getY();return true;}
            if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&e.getPointerCount()==1&&!detector.isInProgress()){
                yaw+=(e.getX()-lastX)*.008;
                tilt=Math.max(-1.3,Math.min(1.3,tilt+(e.getY()-lastY)*.007));
                invalidate();
            }
            lastX=e.getX();lastY=e.getY();
            return true;
        }
    }
    private final class MoonView extends View {
        final long time;
        MoonView(Context c,long t){super(c);time=t;}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            int radius=Math.min(getWidth(),getHeight())/2-dp(13);
            int size=radius*2;
            Bitmap b=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);
            double phase=MoonPhase.age(time)/29.530588853*2*Math.PI;
            double sx=Math.sin(phase),sz=-Math.cos(phase);
            int[] pixels=new int[size*size];
            for(int y=0;y<size;y++)for(int x=0;x<size;x++){
                double u=(x-radius)/(double)radius,v=(y-radius)/(double)radius,r2=u*u+v*v;
                if(r2>1)continue;
                double z=Math.sqrt(1-r2),light=Math.max(0,u*sx+z*sz);
                double craters=.86+.09*Math.sin(x*.13+y*.06)+.05*Math.cos(x*.04-y*.17);
                int val=(int)Math.min(255,Math.max(13,(32+220*light)*craters));
                pixels[y*size+x]=Color.rgb(val,val,(int)Math.min(255,val*1.06));
            }
            b.setPixels(pixels,0,size,0,0,size,size);
            canvas.drawColor(0xff091A2D);
            Paint p=new Paint(3);canvas.drawBitmap(b,(getWidth()-size)/2f,(getHeight()-size)/2f,p);
            b.recycle();
        }
    }
}
