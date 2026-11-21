package com.astapp.sky;

import java.util.Locale;

/**
 * NASA/JPL Approximate Positions of the Planets, 1800-2050.
 * Heliocentric ecliptic rectangular J2000 coordinates in AU.
 * The Earth entry is Earth-Moon barycenter approximation, NOT the exact geocenter.
 * Not a full JPL Horizons solution; errors are published at the JPL source.
 */
public final class Ephemeris {
    private Ephemeris() {}
    public static final String[] NAME = {"수성","금성","지구","화성","목성","토성","천왕성","해왕성"};
    public static final String[] ENGLISH = {"Mercury","Venus","Earth","Mars","Jupiter","Saturn","Uranus","Neptune"};
    public static final int[] COLOR = {0xffA2AAA9,0xffE4C79D,0xff5DA9EF,0xffD78369,0xffD6AE87,0xffD6C18D,0xff8DDDE5,0xff4E7DDE};
    // a,e,i,L,longitude(perihelion),longitude(ascending node)
    private static final double[][] BASE = {
        {0.38709927,0.20563593,7.00497902,252.25032350,77.45779628,48.33076593},
        {0.72333566,0.00677672,3.39467605,181.97909950,131.60246718,76.67984255},
        {1.00000261,0.01671123,-0.00001531,100.46457166,102.93768193,0},
        {1.52371034,0.09339410,1.84969142,-4.55343205,-23.94362959,49.55953891},
        {5.20288700,0.04838624,1.30439695,34.39644051,14.72847983,100.47390909},
        {9.53667594,0.05386179,2.48599187,49.95424423,92.59887831,113.66242448},
        {19.18916464,0.04725744,0.77263783,313.23810451,170.95427630,74.01692503},
        {30.06992276,0.00859048,1.77004347,-55.12002969,44.96476227,131.78422574}
    };
    private static final double[][] RATE = {
        {0.00000037,0.00001906,-0.00594749,149472.67411175,0.16047689,-0.12534081},
        {0.00000390,-0.00004107,-0.00078890,58517.81538729,0.00268329,-0.27769418},
        {0.00000562,-0.00004392,-0.01294668,35999.37244981,0.32327364,0},
        {0.00001847,0.00007882,-0.00813131,19140.30268499,0.44441088,-0.29257343},
        {-0.00011607,-0.00013253,-0.00183714,3034.74612775,0.21252668,0.20469106},
        {-0.00125060,-0.00050991,0.00193609,1222.49362201,-0.41897216,-0.28867794},
        {-0.00196176,-0.00004397,-0.00242939,428.48202785,0.40805281,0.04240589},
        {0.00026291,0.00005105,0.00035372,218.45945325,-0.32241464,-0.00508664}
    };
    public static double julian(long timeMillis) { return 2440587.5 + timeMillis / 86400000.0; }
    public static boolean inRange(long timeMillis) {
        double jd=julian(timeMillis);
        return jd >= 2378496.5 && jd <= 2469807.5; // about 1800..2050
    }
    private static double[] elements(int index,double jd) {
        double t=(jd-2451545.0)/36525.0;
        double[] el=new double[6];
        for(int i=0;i<6;i++)el[i]=BASE[index][i]+RATE[index][i]*t;
        return el;
    }
    public static double[] position(int planet,long timeMillis) {
        double[] e=elements(planet,julian(timeMillis));
        double anomaly=Math.toRadians(e[3]-e[4]);
        anomaly=Math.atan2(Math.sin(anomaly),Math.cos(anomaly));
        double E=anomaly;
        for(int j=0;j<16;j++) {
            double next=(E-e[1]*Math.sin(E)-anomaly)/(1-e[1]*Math.cos(E));
            E-=next;
            if(Math.abs(next)<1e-12)break;
        }
        return withAnomaly(e,E);
    }
    public static double[] orbitPoint(int planet,long timeMillis,double eccentricAngle) {
        return withAnomaly(elements(planet,julian(timeMillis)),eccentricAngle);
    }
    private static double[] withAnomaly(double[] e,double E) {
        double a=e[0],ecc=e[1],incl=Math.toRadians(e[2]);
        double omega=Math.toRadians(e[4]-e[5]),node=Math.toRadians(e[5]);
        double x=a*(Math.cos(E)-ecc),y=a*Math.sqrt(1-ecc*ecc)*Math.sin(E);
        double cw=Math.cos(omega),sw=Math.sin(omega),cn=Math.cos(node),sn=Math.sin(node),ci=Math.cos(incl),si=Math.sin(incl);
        return new double[]{
            (cw*cn-sw*sn*ci)*x+(-sw*cn-cw*sn*ci)*y,
            (cw*sn+sw*cn*ci)*x+(-sw*sn+cw*cn*ci)*y,
            sw*si*x+cw*si*y
        };
    }
    public static double norm(double[] v){return Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);}
    public static String au(double[] vector) { return String.format(Locale.KOREA,"%.3f AU",norm(vector)); }
}
