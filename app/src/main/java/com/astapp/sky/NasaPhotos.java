package com.astapp.sky;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Only downloads NASA Images API results on demand. No image scraping. */
public final class NasaPhotos {
    private static final ExecutorService IO=Executors.newSingleThreadExecutor();
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private NasaPhotos(){}
    public interface Callback {void onResult(Bitmap bitmap,String attribution);}
    public static Bitmap cached(Context c,CosmicCatalog.Object o) {
        File f=path(c,o);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inPreferredConfig=Bitmap.Config.RGB_565;
        if(f.exists()){
            Bitmap cached=BitmapFactory.decodeFile(f.getAbsolutePath(),options);
            if(cached!=null)return cached;
        }
        for(int i=0;i<CosmicCatalog.ALL.length;i++){
            if(CosmicCatalog.ALL[i].english.equals(o.english)){
                try(InputStream in=c.getAssets().open("nasa/cosmic_"+i+".jpg")){
                    return BitmapFactory.decodeStream(in,null,options);
                }catch(IOException ignored){
                    for(String extension:new String[]{"webp","png"}){
                        try(InputStream in=c.getAssets().open("nasa/cosmic_"+i+"."+extension)){
                            Bitmap result=BitmapFactory.decodeStream(in,null,options);
                            if(result!=null)return result;
                        }catch(IOException ignoredOther){}
                    }
                    return null;
                }
            }
        }
        return null;
    }
    private static File path(Context c,CosmicCatalog.Object o){
        return new File(c.getCacheDir(),"nasa_"+Integer.toHexString(o.query.hashCode())+".jpg");
    }
    public static void load(Context c,CosmicCatalog.Object o,Callback cb) {
        Bitmap cached=cached(c,o);
        if(cached!=null){MAIN.post(()->cb.onResult(cached,"NASA 이미지 검색 · 저장본"));return;}
        Context app=c.getApplicationContext();
        IO.execute(()->{
            Bitmap bitmap=null;
            String credit="NASA Images (개별 이미지 저작권·크레딧 확인 필요)";
            try {
                String q=URLEncoder.encode(o.query,"UTF-8");
                JSONObject result=jsonUrl("https://images-api.nasa.gov/search?q="+q+"&media_type=image&page_size=5");
                JSONArray items=result.getJSONObject("collection").getJSONArray("items");
                for(int i=0;i<items.length();i++){
                    JSONObject item=items.getJSONObject(i);
                    JSONArray data=item.optJSONArray("data");
                    JSONArray links=item.optJSONArray("links");
                    if(data==null||data.length()==0||links==null||links.length()==0)continue;
                    JSONObject targetMetadata=data.getJSONObject(0);
                    String title=targetMetadata.optString("title","").toLowerCase(java.util.Locale.ROOT);
                    String desc=targetMetadata.optString("description","").toLowerCase(java.util.Locale.ROOT);
                    String label=o.english.toLowerCase(java.util.Locale.ROOT);
                    String queryLabel=o.query.toLowerCase(java.util.Locale.ROOT);
                    String mainTerm=queryLabel.split(" ")[0];
                    String messier="";
                    if(label.startsWith("messier ")){
                        String number=label.substring("messier ".length()).trim();
                        messier="m"+number;
                    }
                    boolean match=title.contains(label)||
                        title.contains(mainTerm)||
                        (messier.length()>1&&(title.contains(messier)||desc.contains("messier "+label.substring(8))))||
                        desc.contains(label);
                    if(!match)continue;
                    if(title.matches(".*(artist|illustrat|concept|diagram|infographic|poster|animation|render|artwork).*"))continue;
                    String href=links.getJSONObject(0).optString("href","");
                    if(!href.startsWith("https://"))continue;
                    URL imageUrl=new URL(href);
                    String host=imageUrl.getHost();
                    if(!host.endsWith(".nasa.gov"))continue;
                    HttpURLConnection conn=(HttpURLConnection)imageUrl.openConnection();
                    conn.setConnectTimeout(9000);conn.setReadTimeout(12000);
                    if(conn.getResponseCode()!=200){conn.disconnect();continue;}
                    try(InputStream in=conn.getInputStream()){
                        BitmapFactory.Options opt=new BitmapFactory.Options();
                        opt.inPreferredConfig=Bitmap.Config.RGB_565;opt.inSampleSize=2;
                        Bitmap full=BitmapFactory.decodeStream(in,null,opt);
                        if(full==null)continue;
                        int w=full.getWidth(),h=full.getHeight();
                        double factor=Math.min(1,780.0/Math.max(w,h));
                        bitmap=(factor<1)?Bitmap.createScaledBitmap(full,Math.max(1,(int)(w*factor)),Math.max(1,(int)(h*factor)),true):full;
                        if(bitmap!=full)full.recycle();
                        try(FileOutputStream out=new FileOutputStream(path(app,o))){
                            bitmap.compress(Bitmap.CompressFormat.JPEG,82,out);
                        }
                        if(data.length()>0){
                            JSONObject meta=data.getJSONObject(0);
                            credit=meta.optString("secondary_creator",meta.optString("center","NASA Images"))+" · 원본 저작권 정보 확인";
                        }
                    }finally{conn.disconnect();}
                    break;
                }
            }catch(Exception ignored){}
            final Bitmap done=bitmap; final String caption=credit;
            MAIN.post(()->cb.onResult(done,caption));
        });
    }
    public static JSONObject jsonUrl(String url) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
        c.setConnectTimeout(10000);c.setReadTimeout(12000);
        c.setRequestProperty("Accept","application/json");
        try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[4096];int r;
            while((r=in.read(b))>=0 && out.size()<2000000)out.write(b,0,r);
            return new JSONObject(out.toString("UTF-8"));
        }finally{c.disconnect();}
    }
}
