package com.astapp.sky;

import android.graphics.*;
import java.util.Random;

/** Render the actual computed heliocentric coordinates (planet radii are enlarged). */
public final class SolarPainter {
    private SolarPainter(){}
    private static final int BG=0xff071426;
    private static final Paint P = new Paint(3);

    public static Bitmap bitmap(int width,int height,long date,boolean compressed,boolean threeD,double yaw,double tilt,double zoom) {
        Bitmap b=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        draw(c,width,height,date,compressed,threeD,yaw,tilt,zoom);
        return b;
    }

    public static void draw(Canvas c,int w,int h,long date,boolean compressed,boolean threeD,double yaw,double tilt,double zoom) {
        P.reset(); P.setAntiAlias(true);
        P.setShader(new LinearGradient(0,0,w,h,0xff0D2340,BG,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,P);P.setShader(null);
        Random stars=new Random(4387);
        int dots=Math.min(105,Math.max(25,w*h/1100));
        for(int i=0;i<dots;i++){
            float x=stars.nextFloat()*w,y=stars.nextFloat()*h;
            P.setColor((i%5==0)?0x99A8CFFF:0x55738BA5);
            c.drawCircle(x,y,(i%9==0)?1.3f:0.65f,P);
        }
        double edge=Math.min(w,h)*0.42*zoom;
        double cx=w*.5,cy=h*.51;
        if(!threeD){yaw=0;tilt=0;}
        double sinYaw=Math.sin(yaw),cosYaw=Math.cos(yaw),sinTilt=Math.sin(tilt),cosTilt=Math.cos(tilt);
        // All orbital directions/positions use identical J2000 ecliptic coordinates.
        for(int n=7;n>=0;n--) {
            Path orbit=new Path();
            for(int k=0;k<=180;k++){
                double[] p=Ephemeris.orbitPoint(n,date,Math.PI*2*k/180);
                float[] q=project(p,cx,cy,edge,compressed,cosYaw,sinYaw,cosTilt,sinTilt);
                if(k==0)orbit.moveTo(q[0],q[1]);else orbit.lineTo(q[0],q[1]);
            }
            P.setStyle(Paint.Style.STROKE);
            P.setStrokeWidth(n<4?1.1f:.75f);
            P.setColor(n<4?0x6682A1CF:0x446981B1);
            c.drawPath(orbit,P);P.setStyle(Paint.Style.FILL);
        }
        // Sun at the origin.
        float sunR=Math.max(5f,Math.min(w,h)*.022f);
        P.setShader(new RadialGradient((float)cx,(float)cy,sunR*3,new int[]{0x88FFE59C,0x44EC902C,0x00FFBC4C},null,Shader.TileMode.CLAMP));
        c.drawCircle((float)cx,(float)cy,sunR*3,P);P.setShader(null);
        P.setColor(0xffF7B84E);c.drawCircle((float)cx,(float)cy,sunR,P);
        P.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        P.setTextSize(Math.max(10,Math.min(15,w/35f)));
        P.setColor(0xffE9DFBA);
        c.drawText("태양",(float)cx+sunR+3,(float)cy-sunR,P);
        for(int n=7;n>=0;n--){
            double[] v=Ephemeris.position(n,date);
            float[] q=project(v,cx,cy,edge,compressed,cosYaw,sinYaw,cosTilt,sinTilt);
            float r=(float)Math.max(3.8,Math.min(9.5,Math.min(w,h)*(.017+(n>=4?.005:0))));
            P.setShader(new RadialGradient(q[0]-r*.32f,q[1]-r*.38f,r*1.8f,
                new int[]{0xffffffff,Ephemeris.COLOR[n],0xff13213B},new float[]{0,.29f,1},Shader.TileMode.CLAMP));
            c.drawCircle(q[0],q[1],r,P);P.setShader(null);
            if(n==5){
                P.setStyle(Paint.Style.STROKE);P.setColor(0xffC5A57E);P.setStrokeWidth(Math.max(1,r/3));
                RectF ring=new RectF(q[0]-r*1.65f,q[1]-r*.66f,q[0]+r*1.65f,q[1]+r*.66f);
                c.drawOval(ring,P);P.setStyle(Paint.Style.FILL);
            }
            if(w>175 && h>120){
                P.setTextSize(Math.max(10,Math.min(15,w/45f)));
                P.setColor(0xffDBE8FA);
                float tx=q[0]+r+3,ty=q[1]-r-1;
                if(tx>w-39)tx=q[0]-r-34;
                ty=Math.max(12,Math.min(h-4,ty));
                c.drawText(Ephemeris.NAME[n],tx,ty,P);
            }
        }
        if(w>220 && h>130) {
            P.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
            P.setTextSize(11); P.setColor(0xff91ABC9);
            c.drawText(compressed?"궤도 간격 압축 · 위치 각도 보존":"실거리 궤도 · 행성 크기 확대",9,h-8,P);
        }
    }

    private static float[] project(double[] v,double cx,double cy,double edge,boolean compressed,
                                   double cosYaw,double sinYaw,double cosTilt,double sinTilt){
        double d=Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
        double radius=compressed?Math.pow(d/31.5,0.43):d/31.5;
        double norm=Math.max(1e-15,Math.sqrt(v[0]*v[0]+v[1]*v[1]));
        double x=v[0]/norm*radius,y=v[1]/norm*radius,z=v[2]/Math.max(1e-15,d)*radius;
        double rx=x*cosYaw-y*sinYaw,ry=x*sinYaw+y*cosYaw;
        double screenY=ry*cosTilt-z*sinTilt;
        return new float[]{(float)(cx+edge*rx),(float)(cy-edge*screenY)};
    }
}
