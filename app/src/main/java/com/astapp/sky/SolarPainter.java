package com.astapp.sky;

import android.content.Context;
import android.graphics.*;
import java.util.Random;

/**
 * One astronomical reference frame (heliocentric J2000 ecliptic) and two
 * INDEPENDENT LINEAR-AU scale windows. No hand-arranged planet angles.
 * Actual planet marker sizes are intentionally exaggerated for readability.
 */
public final class SolarPainter {
    private SolarPainter(){}
    private static final int NAVY=0xff080808,INK=0xffEEEEEE,SOFT=0xffB0B0B0;
    private static final Paint P=new Paint(3);
    public static Bitmap bitmap(Context context,int w,int h,long date,boolean compressed,
                                boolean threeD,double yaw,double tilt,double zoom){
        Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        draw(context,c,w,h,date,compressed,threeD,yaw,tilt,zoom);
        return b;
    }
    public static void draw(Context context,Canvas c,int w,int h,long date,boolean compressed,
                            boolean threeD,double yaw,double tilt,double zoom){
        P.reset();P.setAntiAlias(true);
        P.setShader(new LinearGradient(0,0,w,h,0xff161616,NAVY,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,P);P.setShader(null);
        Random stars=new Random(28711);
        int count=Math.min(220,Math.max(35,w*h/1600));
        for(int i=0;i<count;i++){
            float x=stars.nextFloat()*w,y=stars.nextFloat()*h;
            P.setColor(i%7==0?0x8CB1D0FC:0x56749BCB);
            c.drawCircle(x,y,i%12==0?1.1f:.6f,P);
        }
        if(threeD){
            viewport(context,c,0,0,w,h,date,0,7,compressed?31.5:31.5,compressed,
                     yaw,tilt,zoom,false);
            return;
        }
        // Both panels are true scale inside their own windows:
        // 0-2.0 AU for the rocky planets, 0-34.5 AU for gas/ice giants.
        boolean sideBySide = w>h*1.2f;
        int gap=6,margin=7;
        if(sideBySide){
            float pw=(w-2*margin-gap)/2f,ph=h-2*margin;
            viewport(context,c,margin,margin,pw,ph,date,0,3,2.15,false,0,0,1,true);
            viewport(context,c,margin+pw+gap,margin,pw,ph,date,4,7,34.5,false,0,0,1,true);
        }else{
            float ph=(h-2*margin-gap)/2f,pw=w-2*margin;
            viewport(context,c,margin,margin,pw,ph,date,0,3,2.15,false,0,0,1,true);
            viewport(context,c,margin,margin+ph+gap,pw,ph,date,4,7,34.5,false,0,0,1,true);
        }
    }
    private static void viewport(Context context,Canvas c,float left,float top,float w,float h,
                                 long date,int first,int last,double maxAU,boolean compressed,
                                 double yaw,double tilt,double zoom,boolean boxed){
        P.setStyle(Paint.Style.FILL);
        if(boxed){
            P.setColor(0xE81B1B1B);
            c.drawRoundRect(new RectF(left,top,left+w,top+h),12,12,P);
        }
        c.save();
        c.clipRect(left+1,top+1,left+w-1,top+h-1);
        float titleH=boxed?22:8;
        float cx=left+w*.5f,cy=top+titleH+(h-titleH)*(boxed?.53f:.51f);
        float scale=(Math.min(w,h-titleH)*.415f)*(float)zoom;
        if(boxed){
            P.setColor(0xffF1F1F1);P.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
            P.setTextSize(Math.max(11,Math.min(17,w/17f)));
            c.drawText(first==0?"INNER · 0–2.15 AU":"OUTER · 0–34.5 AU",left+9,top+15,P);
            P.setTypeface(Typeface.DEFAULT);
        }
        // Draw 1 AU or 10 AU scale tick in each pane.
        if(boxed){
            double tick=first==0?1:10;
            P.setColor(0x558D8D8D);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(1);
            float rr=(float)(scale*tick/maxAU);
            c.drawCircle(cx,cy,rr,P);P.setStyle(Paint.Style.FILL);
            P.setColor(0xffB5B5B5);P.setTextSize(Math.max(9,Math.min(12,w/23f)));
            c.drawText(first==0?"1 AU":"10 AU",cx+rr+3,cy,P);
        }
        float sunRadius=Math.max(3.5f,Math.min(7,Math.min(w,h)*.026f));
        P.setShader(new RadialGradient(cx,cy,sunRadius*3.7f,new int[]{0x99FFD891,0x22FFB654,0x00FFBD7A},null,Shader.TileMode.CLAMP));
        c.drawCircle(cx,cy,sunRadius*3.7f,P);P.setShader(null);
        P.setColor(0xffFFC66A);c.drawCircle(cx,cy,sunRadius,P);
        P.setTypeface(Typeface.create("sans-serif",0));
        for(int n=last;n>=first;n--){
            Path orbit=new Path();
            for(int k=0;k<=160;k++){
                double[] v=Ephemeris.orbitPoint(n,date,Math.PI*2*k/160);
                float[] pt=point(v,cx,cy,scale,maxAU,compressed,yaw,tilt);
                if(k==0)orbit.moveTo(pt[0],pt[1]);else orbit.lineTo(pt[0],pt[1]);
            }
            P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(boxed?.85f:1.1f);
            P.setColor(boxed?0x668A8A8A:0x66888888);
            c.drawPath(orbit,P);P.setStyle(Paint.Style.FILL);
        }
        for(int n=last;n>=first;n--){
            float[] pt=point(Ephemeris.position(n,date),cx,cy,scale,maxAU,compressed,yaw,tilt);
            float radius=boxed?Math.max(4.6f,Math.min(10,Math.min(w,h)*.047f)):
                    Math.max(4.5f,Math.min(13,Math.min(w,h)*.028f));
            drawPlanet(context,c,n,pt[0],pt[1],radius);
            // Labels mark true computed positions; no approximate hand-placement.
            if(w>130 && h>105){
                P.setShader(null);P.setColor(INK);P.setTextSize(Math.max(9,Math.min(13,w/21f)));
                float dx=pt[0]<cx?-37:radius+3;
                float tx=Math.min(left+w-40,Math.max(left+4,pt[0]+dx));
                float ty=Math.max(top+titleH+8,Math.min(top+h-4,pt[1]-(n%2==0?radius:-(radius+7))));
                c.drawText(Ephemeris.NAME[n],tx,ty,P);
            }
        }
        if(!boxed){
            P.setShader(null);P.setTypeface(Typeface.create("sans-serif-medium",0));
            P.setTextSize(12);P.setColor(SOFT);
            c.drawText(compressed?"반경 압축 · 실제 방향":"JPL 실제 거리 · 행성 크기는 확대",left+10,top+h-12,P);
        }
        c.restore();
    }
    private static float[] point(double[] v,float cx,float cy,float scale,double maxAU,
                                 boolean compressed,double yaw,double tilt){
        double x=v[0],y=v[1],z=v[2];
        if(compressed){
            double r=Math.sqrt(x*x+y*y+z*z);
            if(r>0){double mapped=(Math.pow(1+4*r,.64)-1)/(Math.pow(1+4*maxAU,.64)-1)*maxAU;
                x*=mapped/r;y*=mapped/r;z*=mapped/r;
            }
        }
        double rx=x*Math.cos(yaw)-y*Math.sin(yaw);
        double ry=x*Math.sin(yaw)+y*Math.cos(yaw);
        double zy=ry*Math.cos(tilt)-z*Math.sin(tilt);
        return new float[]{(float)(cx+rx*scale/maxAU),(float)(cy-zy*scale/maxAU)};
    }
    private static void drawPlanet(Context context,Canvas c,int index,float x,float y,float r){
        Bitmap photo=context==null?null:PlanetPhotos.cached(context,index);
        P.setShader(null);
        P.setColor(0x44777777);
        c.drawCircle(x,y,r+2.5f,P);
        if(photo!=null && !photo.isRecycled()){
            Path clip=new Path();clip.addCircle(x,y,r,Path.Direction.CW);
            c.save();c.clipPath(clip);
            float targetDiameter=2*r;
            float crop=Math.min(photo.getWidth(),photo.getHeight());
            RectF dest=new RectF(x-r,y-r,x+r,y+r);
            float sX=(photo.getWidth()-crop)/2,sY=(photo.getHeight()-crop)/2;
            P.setColor(Color.WHITE);
            c.drawBitmap(photo,new Rect((int)sX,(int)sY,(int)(sX+crop),(int)(sY+crop)),dest,P);
            c.restore();
        }else{
            P.setShader(new RadialGradient(x-r*.3f,y-r*.4f,r*1.7f,
                new int[]{0xffF4F8FF,Ephemeris.COLOR[index],0xff0E1C38},
                new float[]{0,.35f,1},Shader.TileMode.CLAMP));
            c.drawCircle(x,y,r,P);P.setShader(null);
        }
        if(index==5){
            P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(Math.max(1,r*.22f));
            P.setColor(0xffD9BF91);
            c.drawOval(new RectF(x-r*1.6f,y-r*.63f,x+r*1.6f,y+r*.63f),P);
            P.setStyle(Paint.Style.FILL);
        }
    }
}
