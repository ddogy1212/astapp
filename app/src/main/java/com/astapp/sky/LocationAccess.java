package com.astapp.sky;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.CancellationSignal;

/** Foreground-only location. Never requests background tracking. */
public final class LocationAccess {
    private LocationAccess(){}
    public static final int REQ=4612;
    public interface Callback{void result(Location location,String error);}
    public static boolean permitted(Context ctx){
        return ctx.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED ||
               ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;
    }
    public static void request(Activity a){
        a.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ);
    }
    public static void current(Activity a,Callback callback){
        if(!permitted(a)){callback.result(null,"위치 권한이 필요합니다");return;}
        LocationManager manager=(LocationManager)a.getSystemService(Context.LOCATION_SERVICE);
        if(manager==null){callback.result(null,"위치 서비스가 없습니다");return;}
        String provider=null;
        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER))provider=LocationManager.GPS_PROVIDER;
        else if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))provider=LocationManager.NETWORK_PROVIDER;
        if(provider==null){callback.result(null,"휴대전화 위치 기능이 꺼져 있습니다");return;}
        try{
            if(Build.VERSION.SDK_INT>=30){
                manager.getCurrentLocation(provider,new CancellationSignal(),a.getMainExecutor(),loc->{
                    if(loc!=null)callback.result(loc,null);
                    else callback.result(null,"현재 위치를 확인할 수 없습니다. 다시 시도하거나 직접 입력하세요.");
                });
            }else{
                Location recent=manager.getLastKnownLocation(provider);
                if(recent!=null && System.currentTimeMillis()-recent.getTime()<180000){
                    callback.result(recent,null);return;
                }
                manager.requestSingleUpdate(provider,new android.location.LocationListener(){
                    @Override public void onLocationChanged(Location l){callback.result(l,null);}
                    @Override public void onProviderDisabled(String p){callback.result(null,"위치 서비스가 꺼져 있습니다");}
                },null);
            }
        }catch(SecurityException e){callback.result(null,"위치 권한을 확인하세요");}
        catch(Exception e){callback.result(null,"위치를 불러오지 못했습니다");}
    }
}
