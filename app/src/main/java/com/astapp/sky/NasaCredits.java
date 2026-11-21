package com.astapp.sky;

import android.content.Context;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Reads provenance of NASA photography embedded in an APK at build time. */
public final class NasaCredits {
    private NasaCredits(){}
    public static final class Credit{
        public final String nasaId,title,center,attribution,url;
        Credit(String id,String title,String center,String credit,String url){
            nasaId=id;this.title=title;this.center=center;attribution=credit;this.url=url;
        }
        public String caption(){return nasaId.isEmpty()?"NASA / ESA · "+title:"NASA ID "+nasaId+" · "+title;}
    }
    private static JSONObject catalog;
    private static boolean checked;
    private static synchronized void init(Context c){
        if(checked)return;
        checked=true;
        try(InputStream in=c.getAssets().open("nasa/credits.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] bytes=new byte[4096];int read;
            while((read=in.read(bytes))!=-1)out.write(bytes,0,read);
            catalog=new JSONObject(out.toString("UTF-8"));
        }catch(Exception ignored){catalog=new JSONObject();}
    }
    public static Credit forCatalog(Context c,int index){
        String key;
        if(index>=22&&index<30)key="planet_"+(index-22);
        else if(index==30)key="moon";
        else key="cosmic_"+index;
        return byKey(c,key);
    }
    public static Credit byKey(Context c,String key){
        init(c);
        JSONObject v=catalog.optJSONObject(key);
        if(v==null)return null;
        return new Credit(v.optString("nasa_id",""),v.optString("title",""),
            v.optString("center",""),v.optString("credit",""),v.optString("details",""));
    }
}
