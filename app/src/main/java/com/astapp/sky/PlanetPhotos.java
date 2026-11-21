package com.astapp.sky;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Real archived NASA spacecraft imagery, NOT a live picture of the planet.
 * Filters library search results to avoid unrelated mission art and diagrams.
 */
public final class PlanetPhotos {
    private PlanetPhotos(){}
    private static final ExecutorService IO=Executors.newFixedThreadPool(2);
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final Bitmap[] HOT=new Bitmap[8];
    private static final boolean[] BUSY=new boolean[8];
    private static final String[] TERMS={
        "MESSENGER Mercury global mosaic","Venus Pioneer orbiter global ultraviolet",
        "Earth blue marble full disk","Mars global Hubble full disk",
        "Jupiter Cassini full disk","Saturn Cassini planet rings",
        "Uranus Voyager 2 planet disk","Neptune Voyager 2 planet disk"
    };
    private static final String[] KEY={
        "mercury","venus","earth","mars","jupiter","saturn","uranus","neptune"
    };
    public interface Listener{void onPhoto(Bitmap photo);}
    private static File path(Context c,int i){return new File(c.getCacheDir(),"planet_nasa_"+i+".jpg");}
    public static synchronized Bitmap cached(Context c,int index){
        if(index<0||index>=8)return null;
        if(HOT[index]!=null && !HOT[index].isRecycled())return HOT[index];
        File f=path(c,index);
        if(f.isFile()){
            BitmapFactory.Options op=new BitmapFactory.Options();op.inSampleSize=1;
            HOT[index]=BitmapFactory.decodeFile(f.getAbsolutePath(),op);
        }
        return HOT[index];
    }
    public static void load(Context c,int index,Listener listener){
        if(index<0||index>=8)return;
        Bitmap prior=cached(c,index);
        if(prior!=null){if(listener!=null)MAIN.post(()->listener.onPhoto(prior));return;}
        synchronized(PlanetPhotos.class){if(BUSY[index])return;BUSY[index]=true;}
        Context app=c.getApplicationContext();
        IO.execute(()->{
            Bitmap bitmap=null;
            try {
                // Try a targeted NASA mission query, then a broader subject query.
                for(String term:new String[]{TERMS[index], Ephemeris.ENGLISH[index]+" planet"}){
                    String q=URLEncoder.encode(term,"UTF-8");
                    JSONObject obj=NasaPhotos.jsonUrl("https://images-api.nasa.gov/search?q="+q+"&media_type=image&page_size=40");
                    JSONArray items=obj.getJSONObject("collection").getJSONArray("items");
                    for(int k=0;k<items.length();k++){
                        JSONObject item=items.getJSONObject(k);
                        JSONArray data=item.optJSONArray("data"), links=item.optJSONArray("links");
                        if(data==null||data.length()==0||links==null||links.length()==0)continue;
                        String title=data.getJSONObject(0).optString("title","").toLowerCase(java.util.Locale.ROOT);
                        String desc=data.getJSONObject(0).optString("description","").toLowerCase(java.util.Locale.ROOT);
                        if(!title.contains(KEY[index]) && !desc.contains(KEY[index]))continue;
                        // We must not disguise a drawing, simulation or infographic as a photograph.
                        if(title.matches(".*(artist|illustrat|concept|poster|animation|diagram|infographic|render|model|painting|artwork).*"))continue;
                        String href=links.getJSONObject(0).optString("href","");
                        if(!href.startsWith("https://"))continue;
                        URL u=new URL(href);
                        if(!u.getHost().endsWith(".nasa.gov"))continue;
                        bitmap=download(u);
                        if(bitmap!=null)break;
                    }
                    if(bitmap!=null)break;
                }
                if(bitmap!=null){
                    try(FileOutputStream out=new FileOutputStream(path(app,index))){
                        bitmap.compress(Bitmap.CompressFormat.JPEG,87,out);
                    }
                }
            }catch(Exception ignored){}
            synchronized(PlanetPhotos.class){HOT[index]=bitmap;BUSY[index]=false;}
            final Bitmap result=bitmap;
            if(listener!=null)MAIN.post(()->listener.onPhoto(result));
        });
    }
    private static Bitmap download(URL url)throws Exception{
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(9000);conn.setReadTimeout(13000);
        try{
            if(conn.getResponseCode()!=200)return null;
            BitmapFactory.Options op=new BitmapFactory.Options();op.inSampleSize=2;op.inPreferredConfig=Bitmap.Config.RGB_565;
            try(InputStream in=conn.getInputStream()){
                Bitmap raw=BitmapFactory.decodeStream(in,null,op);
                if(raw==null)return null;
                if(Math.max(raw.getWidth(),raw.getHeight())<=600)return raw;
                double k=600.0/Math.max(raw.getWidth(),raw.getHeight());
                Bitmap small=Bitmap.createScaledBitmap(raw,Math.max(1,(int)(raw.getWidth()*k)),Math.max(1,(int)(raw.getHeight()*k)),true);
                raw.recycle();return small;
            }
        }finally{conn.disconnect();}
    }
    public static void warm(Context c,Runnable refresh){
        final int[] completed={0};
        for(int i=0;i<8;i++){
            load(c,i,b->{completed[0]++;if(completed[0]==8 && refresh!=null)refresh.run();});
        }
    }
}
