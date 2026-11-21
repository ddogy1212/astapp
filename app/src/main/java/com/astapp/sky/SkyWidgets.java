package com.astapp.sky;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/** Seven launchable widget templates, each supporting independent controls. */
public final class SkyWidgets {
    private SkyWidgets(){}
    private static final String SOLAR_STEP="com.astapp.sky.SOLAR_STEP";
    private static final String SOLAR_NOW="com.astapp.sky.SOLAR_NOW";
    private static final String DISC_NEXT="com.astapp.sky.DISC_NEXT";
    private static final String DISC_FAV="com.astapp.sky.DISC_FAV";
    private static final String PREF="ast";
    private static int dp(int x){return x;}
    private static PendingIntent broadcast(Context ctx,Class<?> cls,String action,int id,int salt){
        Intent it=new Intent(ctx,cls).setAction(action);
        it.setData(Uri.parse("astapp://widget/"+cls.getSimpleName()+"/"+id+"/"+salt));
        it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
        return PendingIntent.getBroadcast(ctx,id*11+salt,it,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    private static PendingIntent open(Context c,int id,int tab){
        Intent it=new Intent(c,MainActivity.class).putExtra("tab",tab);
        if(tab==4)it.putExtra("catalog_index",CosmicCatalog.daily(c,id));
        it.setData(Uri.parse("astapp://open/"+tab+"/"+id));
        return PendingIntent.getActivity(c,id*13+tab,it,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    private static long shifted(Context c,int id){
        int m=c.getSharedPreferences(PREF,0).getInt("solar_month_"+id,0);
        Calendar cal=Calendar.getInstance();cal.add(Calendar.MONTH,m);
        return cal.getTimeInMillis();
    }
    private static void solar(Context ctx,int id,int size,Class<?> klass){
        AppWidgetManager mgr=AppWidgetManager.getInstance(ctx);
        int w=size==0?330:640, h=size==2?440:230;
        long date=shifted(ctx,id);
        boolean compressed=ctx.getSharedPreferences(PREF,0).getBoolean("scale_compressed",false);
        RemoteViews rv=new RemoteViews(ctx.getPackageName(),R.layout.widget_solar);
        rv.setTextViewText(R.id.solar_title,"☀ AST · 실제 궤도");
        rv.setTextViewText(R.id.solar_date,new SimpleDateFormat("yyyy.MM.dd HH:mm",Locale.KOREA).format(date));
        rv.setTextViewText(R.id.solar_note,compressed?"궤도 간격 압축 / 위치 각도 유지":"실거리 / 행성 크기 확대");
        rv.setImageViewBitmap(R.id.solar_image,SolarPainter.bitmap(ctx,w,h,date,compressed,false,0,0,1));
        rv.setOnClickPendingIntent(R.id.solar_prev,broadcast(ctx,klass,SOLAR_STEP,id,1));
        rv.setOnClickPendingIntent(R.id.solar_now,broadcast(ctx,klass,SOLAR_NOW,id,2));
        rv.setOnClickPendingIntent(R.id.solar_next,broadcast(ctx,klass,SOLAR_STEP,id,3));
        rv.setOnClickPendingIntent(R.id.solar_image,open(ctx,id,0));
        mgr.updateAppWidget(id,rv);
    }
    public static abstract class SolarBase extends AppWidgetProvider {
        abstract int size();
        @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids) {
            for(int id:ids)solar(c,id,size(),getClass());
            for(int i=0;i<8;i++){
                final int planet=i;
                if(PlanetPhotos.cached(c,planet)==null){
                    PlanetPhotos.load(c,planet,photo->{if(photo!=null)refreshSolar(c);});
                }
            }
        }
        @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,android.os.Bundle opts) {
            solar(c,id,size(),getClass());
        }
        @Override public void onReceive(Context c,Intent i){
            super.onReceive(c,i);
            String a=i.getAction();
            if(!SOLAR_STEP.equals(a)&&!SOLAR_NOW.equals(a))return;
            int id=i.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
            if(id==AppWidgetManager.INVALID_APPWIDGET_ID)return;
            SharedPreferences pref=c.getSharedPreferences(PREF,0);
            int months=pref.getInt("solar_month_"+id,0);
            if(SOLAR_NOW.equals(a))months=0;
            else {
                String uri=String.valueOf(i.getData());
                int step=uri.endsWith("/1")?-1:1;
                Calendar cal=Calendar.getInstance();cal.add(Calendar.MONTH,months+step);
                if(Ephemeris.inRange(cal.getTimeInMillis()))months+=step;
            }
            pref.edit().putInt("solar_month_"+id,months).apply();
            solar(c,id,size(),getClass());
        }
    }
    public static final class SolarSmall extends SolarBase{int size(){return 0;}}
    public static final class SolarWide extends SolarBase{int size(){return 1;}}
    public static final class SolarLarge extends SolarBase{int size(){return 2;}}

    private static Bitmap fallback(int w,int h,CosmicCatalog.Object obj){
        Bitmap out=Bitmap.createBitmap(w,h,Bitmap.Config.RGB_565);
        Canvas canvas=new Canvas(out);
        Paint p=new Paint(3);
        p.setShader(new LinearGradient(0,0,w,h,0xff1A3665,0xff071428,Shader.TileMode.CLAMP));
        canvas.drawColor(0xff101D35);canvas.drawRect(0,0,w,h,p);p.setShader(null);
        Random rand=new Random(obj.name.hashCode());
        for(int i=0;i<180;i++){
            p.setColor(i%7==0?0xffC4C4C4:0xff6A6A6A);
            canvas.drawCircle(rand.nextInt(w),rand.nextInt(h),rand.nextInt(7)==0?1.4f:.7f,p);
        }
        p.setColor(0xffF3F3F3);p.setTypeface(Typeface.create("sans-serif-medium",0));
        p.setTextSize(Math.max(18,w/22f));
        canvas.drawText("✦ "+obj.english,18,h*.51f,p);
        p.setTextSize(Math.max(12,w/43f));p.setColor(0xffAAAAAA);
        canvas.drawText("관측 사진 확인 중",18,h*.63f,p);
        return out;
    }
    private static void discover(Context c,int id,int size,Class<?> klass,boolean fetch){
        AppWidgetManager mgr=AppWidgetManager.getInstance(c);
        int index=CosmicCatalog.daily(c,id);
        CosmicCatalog.Object o=CosmicCatalog.ALL[index];
        boolean small=size==0;
        RemoteViews rv=new RemoteViews(c.getPackageName(),small?R.layout.widget_discovery_small:R.layout.widget_discovery);
        rv.setTextViewText(R.id.discover_title,"AST / DISCOVERY");
        rv.setTextViewText(R.id.discover_name,o.name+" · "+o.english);
        rv.setTextViewText(R.id.discover_kind,o.kind);
        rv.setOnClickPendingIntent(R.id.discover_next,broadcast(c,klass,DISC_NEXT,id,4));
        rv.setOnClickPendingIntent(R.id.discover_root,open(c,id,4));
        if(!small){
            int w=size==3?650:size==2?640:330,h=size==3?440:190;
            Bitmap cached=SolarBodyFacts.isPlanet(index)?
                PlanetPhotos.cached(c,SolarBodyFacts.planetIndex(index)):
                SolarBodyFacts.isMoon(index)?MoonPhoto.cached(c):NasaPhotos.cached(c,o);
            if(cached==null)cached=NasaPhotos.cached(c,o);
            rv.setImageViewBitmap(R.id.discover_image,cached==null?fallback(w,h,o):cached);
            rv.setOnClickPendingIntent(R.id.discover_image,open(c,id,4));
            rv.setTextViewText(R.id.discover_fact,o.fact);
            rv.setTextViewText(R.id.discover_attribution,cached==null?
                "NASA 관측 이미지 확인 중":"NASA 아카이브 · 촬영 당시 사진");
            rv.setTextViewText(R.id.discover_fav,CosmicCatalog.favorite(c,o.english)?"★":"☆");
            rv.setOnClickPendingIntent(R.id.discover_fav,broadcast(c,klass,DISC_FAV,id,5));
            if(fetch&&cached==null){
                if(SolarBodyFacts.isPlanet(index)){
                    PlanetPhotos.load(c,SolarBodyFacts.planetIndex(index),photo->{
                        if(CosmicCatalog.daily(c,id)!=index)return;
                        if(photo!=null)discover(c,id,size,klass,false);
                        else NasaPhotos.load(c,o,(b,credit)->{
                            if(b!=null&&CosmicCatalog.daily(c,id)==index)
                                discover(c,id,size,klass,false);
                        });
                    });
                }else if(SolarBodyFacts.isMoon(index)){
                    MoonPhoto.load(c,photo->{
                        if(CosmicCatalog.daily(c,id)!=index)return;
                        if(photo!=null)discover(c,id,size,klass,false);
                        else NasaPhotos.load(c,o,(b,credit)->{
                            if(b!=null&&CosmicCatalog.daily(c,id)==index)
                                discover(c,id,size,klass,false);
                        });
                    });
                }else NasaPhotos.load(c,o,(b,credit)->{
                    if(b!=null&&CosmicCatalog.daily(c,id)==index)
                        discover(c,id,size,klass,false);
                });
            }
        }
        mgr.updateAppWidget(id,rv);
    }
    public static abstract class DiscoveryBase extends AppWidgetProvider{
        abstract int size();
        @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids) {
            for(int id:ids)discover(c,id,size(),getClass(),true);
        }
        @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,android.os.Bundle opts) {
            discover(c,id,size(),getClass(),true);
        }
        @Override public void onReceive(Context c,Intent i){
            super.onReceive(c,i);
            String action=i.getAction();
            if(!DISC_NEXT.equals(action)&&!DISC_FAV.equals(action))return;
            int id=i.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
            if(id==AppWidgetManager.INVALID_APPWIDGET_ID)return;
            if(DISC_NEXT.equals(action))CosmicCatalog.next(c,id);
            else CosmicCatalog.toggleFavorite(c,CosmicCatalog.current(c,id).english);
            discover(c,id,size(),getClass(),true);
        }
    }
    public static final class DiscoverySlim extends DiscoveryBase{int size(){return 0;}}
    public static final class DiscoverySquare extends DiscoveryBase{int size(){return 1;}}
    public static final class DiscoveryWide extends DiscoveryBase{int size(){return 2;}}
    public static final class DiscoveryLarge extends DiscoveryBase{int size(){return 3;}}

    /** Redraw planet photos and orbit graphics without triggering further downloads. */
    public static void refreshSolar(Context c){
        AppWidgetManager mgr=AppWidgetManager.getInstance(c);
        Class<?>[] classes={SolarSmall.class,SolarWide.class,SolarLarge.class};
        for(int i=0;i<classes.length;i++){
            for(int id:mgr.getAppWidgetIds(new ComponentName(c,classes[i]))){
                solar(c,id,i,classes[i]);
            }
        }
    }
    /** Called when settings are changed. */
    public static void refreshAll(Context c){
        AppWidgetManager mgr=AppWidgetManager.getInstance(c);
        Class<?>[] solar={SolarSmall.class,SolarWide.class,SolarLarge.class};
        for(int i=0;i<solar.length;i++)
            for(int id:mgr.getAppWidgetIds(new ComponentName(c,solar[i])))solar(c,id,i,solar[i]);
        Class<?>[] discovery={DiscoverySlim.class,DiscoverySquare.class,DiscoveryWide.class,DiscoveryLarge.class};
        for(int i=0;i<discovery.length;i++)
            for(int id:mgr.getAppWidgetIds(new ComponentName(c,discovery[i])))discover(c,id,i,discovery[i],true);
    }
}
