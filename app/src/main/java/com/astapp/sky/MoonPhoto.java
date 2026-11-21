package com.astapp.sky;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NASA full-Moon archival photograph, not live satellite imagery.
 * NASA gallery: https://www.nasa.gov/gallery/moon/
 */
public final class MoonPhoto {
    private MoonPhoto(){}
    public static final String SOURCE="https://www.nasa.gov/gallery/moon/";
    private static final String PHOTO="https://images-assets.nasa.gov/image/GSFC_20171208_Archive_e001515/GSFC_20171208_Archive_e001515~orig.jpg?w=700&h=700&fit=clip";
    private static final ExecutorService EXEC=Executors.newSingleThreadExecutor();
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static Bitmap memory;
    private static boolean fetching;
    private static final java.util.ArrayList<Callback> WAITING=new java.util.ArrayList<>();
    public interface Callback{void ready(Bitmap bitmap);}
    public static synchronized Bitmap cached(Context ctx){
        if(memory!=null && !memory.isRecycled())return memory;
        File f=new File(ctx.getCacheDir(),"nasa_lunar_full_disk.jpg");
        if(f.isFile())memory=BitmapFactory.decodeFile(f.getAbsolutePath());
        return memory;
    }
    public static void load(Context context,Callback callback){
        Context app=context.getApplicationContext();
        Bitmap found=cached(app);
        if(found!=null){if(callback!=null)MAIN.post(()->callback.ready(found));return;}
        synchronized(MoonPhoto.class){
            if(callback!=null)WAITING.add(callback);
            if(fetching)return;
            fetching=true;
        }
        EXEC.execute(()->{
            Bitmap bmp=null;
            try{
                HttpURLConnection con=(HttpURLConnection)new URL(PHOTO).openConnection();
                con.setConnectTimeout(9000);con.setReadTimeout(14000);
                try{
                    if(con.getResponseCode()==200){
                        BitmapFactory.Options opts=new BitmapFactory.Options();
                        opts.inSampleSize=2;opts.inPreferredConfig=Bitmap.Config.RGB_565;
                        try(InputStream in=con.getInputStream()){
                            bmp=BitmapFactory.decodeStream(in,null,opts);
                        }
                    }
                }finally{con.disconnect();}
                if(bmp!=null)try(FileOutputStream out=new FileOutputStream(new File(app.getCacheDir(),"nasa_lunar_full_disk.jpg"))){
                    bmp.compress(Bitmap.CompressFormat.JPEG,88,out);
                }
            }catch(Exception ignored){}
            java.util.List<Callback> callbacks;
            synchronized(MoonPhoto.class){
                memory=bmp;fetching=false;
                callbacks=new java.util.ArrayList<>(WAITING);WAITING.clear();
            }
            Bitmap result=bmp;
            for(Callback cb:callbacks)MAIN.post(()->cb.ready(result));
        });
    }
    public static Bitmap phaseComposited(Bitmap photo,long utcMs,int diameter){
        if(photo==null)return null;
        int s=Math.min(diameter,600);
        Bitmap scaled=Bitmap.createScaledBitmap(photo,s,s,true);
        int[] px=new int[s*s];
        scaled.getPixels(px,0,s,0,0,s,s);
        double phase=MoonPhase.age(utcMs)/29.530588853*2*Math.PI;
        // Phase direction is symbolic: this does not model libration or sky orientation.
        double sx=Math.sin(phase),sz=-Math.cos(phase),r=s*.5;
        for(int y=0;y<s;y++)for(int x=0;x<s;x++){
            double u=(x+.5-r)/r,v=(y+.5-r)/r,rr=u*u+v*v;
            if(rr>1){px[y*s+x]=0;continue;}
            double z=Math.sqrt(1-rr);
            double illuminated=u*sx+z*sz;
            double shade=0.12+0.88*Math.max(0,Math.min(1,(illuminated+.025)*18));
            int color=px[y*s+x],red=(int)(android.graphics.Color.red(color)*shade),
                green=(int)(android.graphics.Color.green(color)*shade),
                blue=(int)(android.graphics.Color.blue(color)*shade);
            px[y*s+x]=android.graphics.Color.argb(255,red,green,blue);
        }
        Bitmap result=Bitmap.createBitmap(s,s,Bitmap.Config.ARGB_8888);
        result.setPixels(px,0,s,0,0,s,s);
        if(scaled!=photo)scaled.recycle();
        return result;
    }
}
