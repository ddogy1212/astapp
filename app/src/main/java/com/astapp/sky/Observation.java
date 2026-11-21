package com.astapp.sky;

import java.util.*;

/** Simple geometric alt/az planner. No atmospheric refraction, weather or terrain. */
public final class Observation {
    private Observation(){}
    public static final class Coord {
        public final double altitude,azimuth;
        Coord(double a,double az){altitude=a;azimuth=az;}
    }
    public static Coord horizontal(double[] geocentric,long time,double lat,double lon){
        double e=Math.toRadians(23.43928);
        double x=geocentric[0],y=geocentric[1]*Math.cos(e)-geocentric[2]*Math.sin(e);
        double z=geocentric[1]*Math.sin(e)+geocentric[2]*Math.cos(e);
        double ra=Math.atan2(y,x),dec=Math.atan2(z,Math.hypot(x,y));
        double jd=Ephemeris.julian(time),t=(jd-2451545.0)/36525;
        double gmst=280.46061837+360.98564736629*(jd-2451545.0)+0.000387933*t*t-t*t*t/38710000;
        double hour=Math.toRadians(gmst+lon)-ra;
        double latitude=Math.toRadians(lat);
        double alt=Math.asin(Math.sin(latitude)*Math.sin(dec)+Math.cos(latitude)*Math.cos(dec)*Math.cos(hour));
        double az=Math.atan2(-Math.sin(hour),Math.tan(dec)*Math.cos(latitude)-Math.sin(latitude)*Math.cos(hour));
        return new Coord(Math.toDegrees(alt),(Math.toDegrees(az)+360)%360);
    }
    /** Right ascension and declination in J2000 degrees (precession not applied). */
    public static Coord forRaDec(double raDegrees,double decDegrees,long time,double lat,double lon){
        double jd=Ephemeris.julian(time),t=(jd-2451545.0)/36525.0;
        double gmst=280.46061837+360.98564736629*(jd-2451545.0)+
            0.000387933*t*t-t*t*t/38710000.0;
        double hour=Math.toRadians(gmst+lon-raDegrees);
        double phi=Math.toRadians(lat),dec=Math.toRadians(decDegrees);
        double elevation=Math.asin(Math.sin(phi)*Math.sin(dec)+
            Math.cos(phi)*Math.cos(dec)*Math.cos(hour));
        double bearing=Math.atan2(-Math.sin(hour),
            Math.tan(dec)*Math.cos(phi)-Math.sin(phi)*Math.cos(hour));
        return new Coord(Math.toDegrees(elevation),(Math.toDegrees(bearing)+360)%360);
    }
    public static Coord planet(int i,long date,double lat,double lon) {
        double[] target=Ephemeris.position(i,date),earth=Ephemeris.position(2,date);
        return horizontal(new double[]{target[0]-earth[0],target[1]-earth[1],target[2]-earth[2]},date,lat,lon);
    }
    public static Coord sun(long date,double lat,double lon) {
        double[] earth=Ephemeris.position(2,date);
        return horizontal(new double[]{-earth[0],-earth[1],-earth[2]},date,lat,lon);
    }
    public static long nextNightAtNine() {
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,21);
        calendar.set(Calendar.MINUTE,0);calendar.set(Calendar.SECOND,0);calendar.set(Calendar.MILLISECOND,0);
        if(calendar.getTimeInMillis()<System.currentTimeMillis()-3600000L)calendar.add(Calendar.DAY_OF_MONTH,1);
        return calendar.getTimeInMillis();
    }
    public static final class Target {
        public final int index;
        public final Coord coord;
        Target(int i,Coord c){index=i;coord=c;}
    }
    public static List<Target> sorted(long date,double lat,double lon){
        ArrayList<Target> out=new ArrayList<>();
        for(int i=0;i<8;i++)if(i!=2)out.add(new Target(i,planet(i,date,lat,lon)));
        out.sort((a,b)->Double.compare(b.coord.altitude,a.coord.altitude));
        return out;
    }
    public static String azimuthText(double a){
        String[] dirs={"북","북동","동","남동","남","남서","서","북서"};
        return dirs[((int)Math.round(a/45.0))%8];
    }
}
