package com.astapp.sky;

import android.os.Handler;
import android.os.Looper;
import org.json.*;
import java.net.*;
import java.time.*;
import java.util.concurrent.*;

/** Live cloud forecast from open-meteo.com, explicitly labelled with forecast timestamp. */
public final class WeatherForecast {
    private WeatherForecast(){}
    private static final ExecutorService IO=Executors.newSingleThreadExecutor();
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    public interface Listener{void ready(Integer cloudPercent,String time,String error);}
    public static void load(double latitude,double longitude,long utcMillis,Listener listener){
        IO.execute(()->{
            Integer cloud=null;String when=null,error=null;
            try{
                String url="https://api.open-meteo.com/v1/forecast?latitude="+latitude+"&longitude="+longitude+
                    "&hourly=cloud_cover&forecast_days=3&timezone=auto";
                JSONObject o=NasaPhotos.jsonUrl(url);
                JSONArray t=o.getJSONObject("hourly").getJSONArray("time");
                JSONArray v=o.getJSONObject("hourly").getJSONArray("cloud_cover");
                int offset=o.optInt("utc_offset_seconds",0);
                String target=Instant.ofEpochMilli(utcMillis).plusSeconds(offset).toString().substring(0,13);
                for(int i=0;i<t.length();i++){
                    String d=t.optString(i);
                    if(d.startsWith(target)){
                        if(!v.isNull(i))cloud=v.getInt(i);
                        when=d;break;
                    }
                }
                if(cloud==null)error="해당 시각의 구름 예보가 없습니다";
            }catch(Exception e){error="날씨 서버에 연결하지 못했습니다";}
            final Integer fCloud=cloud;final String fWhen=when;final String fError=error;
            MAIN.post(()->listener.ready(fCloud,fWhen,fError));
        });
    }
}
