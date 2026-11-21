package com.astapp.sky;

import android.content.Context;
import android.graphics.*;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.*;
import android.widget.FrameLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Locale;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Interactive, hardware-rendered 3D model of JPL-based heliocentric orbits.
 * Planet display markers use archived NASA photographs as CAMERA-FACING sprites,
 * not as georeferenced globe textures. True orbital geometry, enlarged marker sizes.
 * Orthographic projection enables scientific comparison without perspective distortion.
 */
public final class SolarExplorer extends FrameLayout {
    public interface Listener{void onPlanetSelected(int planet);}
    private final UniverseSurface surface;
    private final Labels labels;
    private final Scene renderer;
    private final Listener listener;
    private float downX,downY,lastX,lastY;
    private boolean dragged=false;
    private final android.view.ScaleGestureDetector scale;
    public SolarExplorer(Context context,Listener listener){
        super(context);
        this.listener=listener;
        setBackgroundColor(0xff050505);
        renderer=new Scene(context.getApplicationContext());
        surface=new UniverseSurface(context);
        surface.setEGLContextClientVersion(2);
        surface.setPreserveEGLContextOnPause(true);
        surface.setRenderer(renderer);
        surface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        labels=new Labels(context);
        addView(surface,new LayoutParams(-1,-1));
        addView(labels,new LayoutParams(-1,-1));
        scale=new android.view.ScaleGestureDetector(context,new android.view.ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override public boolean onScale(android.view.ScaleGestureDetector detector){
                renderer.extent=Math.max(.11f,Math.min(38f,
                    renderer.extent/detector.getScaleFactor()));
                refresh();return true;
            }
        });
        labels.setOnTouchListener((v,e)->handleTouch(e));
        renderer.setTime(System.currentTimeMillis());
    }
    public void setTime(long millis){renderer.setTime(millis);refresh();}
    public void showWholeSystem(){renderer.focus=-1;renderer.extent=35;refresh();}
    public void showInnerSystem(){renderer.focus=-1;renderer.extent=2.3f;refresh();}
    public void showOuterSystem(){renderer.focus=-1;renderer.extent=35;refresh();}
    public void focus(int index){
        renderer.focus=index;
        renderer.extent=index<=3?.52f:1.6f;
        refresh();
    }
    public void refresh(){surface.requestRender();labels.invalidate();}
    public void pause(){surface.onPause();}
    public void resume(){surface.onResume();refresh();}
    public void updatePhotos(){renderer.requestTextureSync();refresh();}
    public double getExtent(){return renderer.extent;}
    private boolean handleTouch(MotionEvent event){
        scale.onTouchEvent(event);
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                downX=lastX=event.getX();downY=lastY=event.getY();dragged=false;return true;
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount()==1&&!scale.isInProgress()){
                    float dx=event.getX()-lastX,dy=event.getY()-lastY;
                    if(Math.abs(event.getX()-downX)+Math.abs(event.getY()-downY)>15)dragged=true;
                    if(dragged){
                        renderer.yaw+=dx*.006f;
                        renderer.tilt=Math.max(.07f,Math.min(1.5f,renderer.tilt+dy*.005f));
                        refresh();
                    }
                }else dragged=true;
                lastX=event.getX();lastY=event.getY();return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                dragged=true;return true;
            case MotionEvent.ACTION_UP:
                if(!dragged){
                    int selected=hit(event.getX(),event.getY());
                    if(selected>=0){
                        renderer.focus=selected;
                        listener.onPlanetSelected(selected);
                        refresh();
                    }
                }
                return true;
            default:return true;
        }
    }
    private int hit(float x,float y){
        float[][] coords=renderer.screen;
        if(coords==null)return -1;
        float best=Float.MAX_VALUE;int target=-1;
        float threshold=getResources().getDisplayMetrics().density*36;
        for(int i=0;i<coords.length;i++){
            float[] p=coords[i];
            if(p==null||p[2]<0||p[2]>1)continue;
            float dx=x-p[0]*getWidth(),dy=y-p[1]*getHeight();
            float d=dx*dx+dy*dy;
            if(d<best&&d<threshold*threshold){best=d;target=i;}
        }
        return target;
    }
    private final class Labels extends View {
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        Labels(Context context){super(context);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float[][] xy=renderer.screen;
            if(xy==null)return;
            float density=getResources().getDisplayMetrics().density;
            paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
            paint.setTextSize(12*density);
            for(int i=0;i<xy.length;i++){
                float[] p=xy[i];if(p==null||p[2]<0||p[2]>1)continue;
                float x=p[0]*getWidth(),y=p[1]*getHeight();
                if(x<0||x>getWidth()||y<32*density||y>getHeight()-27*density)continue;
                String label=Ephemeris.NAME[i];
                float width=paint.measureText(label);
                float tx=Math.max(6*density,Math.min(getWidth()-width-6*density,x+12*density));
                float ty=Math.max(25*density,Math.min(getHeight()-10*density,y-11*density));
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xb0000000);c.drawRoundRect(tx-4*density,ty-13*density,
                    tx+width+4*density,ty+4*density,4*density,4*density,paint);
                paint.setColor(0xfff0f0f0);c.drawText(label,tx,ty,paint);
            }
            paint.setColor(0xffb1b1b1);paint.setTextSize(10*density);
            c.drawText("3D ORBIT / NASA PHOTO MARKERS",12*density,getHeight()-12*density,paint);
        }
    }
    private static final class UniverseSurface extends GLSurfaceView{
        UniverseSurface(Context context){super(context);}
    }
    private static final class Scene implements GLSurfaceView.Renderer {
        final Context app;
        volatile float extent=35f,yaw=.46f,tilt=.42f;
        volatile long time=System.currentTimeMillis();
        volatile int focus=-1;
        volatile float[][] screen;
        private int program,uMatrix,uColor,uSize,uMode,uTexture,aPosition;
        private final int[] textures=new int[8];
        private final boolean[] tried=new boolean[8];
        private final float[] proj=new float[16],view=new float[16],vp=new float[16];
        private float aspect=1f;
        private int screenW=1,screenH=1;
        private volatile boolean reload=false;
        private static final String VERTEX=
            "uniform mat4 uMatrix; attribute vec3 aPosition; uniform float uSize;"+
            "void main(){gl_Position=uMatrix*vec4(aPosition,1.0);gl_PointSize=uSize;}";
        private static final String FRAGMENT=
            "precision mediump float; uniform vec4 uColor; uniform int uMode;"+
            "uniform sampler2D uTexture; void main(){"+
            "if(uMode==1){vec2 r=gl_PointCoord-vec2(.5);if(dot(r,r)>.25) discard;"+
            "gl_FragColor=texture2D(uTexture,gl_PointCoord);"+
            "}else if(uMode==2){vec2 r=gl_PointCoord-vec2(.5);if(dot(r,r)>.25)discard;"+
            "float v=max(0.2,1.0-dot(r,r)*2.0);gl_FragColor=vec4(uColor.rgb*v,1.0);"+
            "}else{gl_FragColor=uColor;}}";
        Scene(Context c){app=c;}
        void setTime(long date){time=date;}
        void requestTextureSync(){reload=true;}
        @Override public void onSurfaceCreated(GL10 gl,EGLConfig config){
            GLES20.glClearColor(.014f,.014f,.014f,1);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);
            program=buildProgram(VERTEX,FRAGMENT);
            GLES20.glUseProgram(program);
            aPosition=GLES20.glGetAttribLocation(program,"aPosition");
            uMatrix=GLES20.glGetUniformLocation(program,"uMatrix");
            uColor=GLES20.glGetUniformLocation(program,"uColor");
            uSize=GLES20.glGetUniformLocation(program,"uSize");
            uMode=GLES20.glGetUniformLocation(program,"uMode");
            uTexture=GLES20.glGetUniformLocation(program,"uTexture");
            for(int i=0;i<8;i++){textures[i]=0;tried[i]=false;}
        }
        @Override public void onSurfaceChanged(GL10 gl,int w,int h){
            screenW=Math.max(w,1);screenH=Math.max(h,1);
            aspect=screenW/(float)screenH;
            GLES20.glViewport(0,0,screenW,screenH);
        }
        private void syncTextures(){
            if(reload){for(int i=0;i<8;i++)if(textures[i]==0)tried[i]=false;reload=false;}
            for(int i=0;i<8;i++){
                if(tried[i])continue;
                Bitmap photo=PlanetPhotos.cached(app,i);
                if(photo==null)continue;
                tried[i]=true;
                Bitmap crop=bitmapForPoint(photo);
                int[] id=new int[1];GLES20.glGenTextures(1,id,0);
                if(id[0]==0){crop.recycle();continue;}
                textures[i]=id[0];
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,id[0]);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,crop,0);crop.recycle();
            }
        }
        private Bitmap bitmapForPoint(Bitmap b){
            int side=Math.min(b.getWidth(),b.getHeight());
            Bitmap square=Bitmap.createBitmap(b,(b.getWidth()-side)/2,(b.getHeight()-side)/2,side,side);
            Bitmap resized=Bitmap.createScaledBitmap(square,128,128,true);
            if(square!=b && square!=resized)square.recycle();
            return resized;
        }
        @Override public void onDrawFrame(GL10 gl){
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(program);
            double[] center=focus<0?new double[]{0,0,0}:Ephemeris.position(focus,time);
            // Camera is at a real 3D vantage point orbiting the selected focus.
            float az=yaw,pitch=tilt;
            float distance=Math.max(12,extent*3.8f);
            Matrix.setLookAtM(view,0,
                (float)center[0]+distance*(float)(Math.cos(az)*Math.sin(pitch)),
                (float)center[1]+distance*(float)(Math.sin(az)*Math.sin(pitch)),
                (float)center[2]+distance*(float)Math.cos(pitch),
                (float)center[0],(float)center[1],(float)center[2],
                0,1,0);
            Matrix.orthoM(proj,0,-extent*aspect,extent*aspect,-extent,extent,.1f,1000);
            Matrix.multiplyMM(vp,0,proj,0,view,0);
            GLES20.glUniformMatrix4fv(uMatrix,1,false,vp,0);
            // Actual orbital 3D eccentricity and inclination are retained.
            for(int i=7;i>=0;i--){
                float[] orbit=new float[(161)*3];
                for(int k=0;k<=160;k++){
                    double[] p=Ephemeris.orbitPoint(i,time,Math.PI*2*k/160);
                    orbit[k*3]=(float)p[0];
                    orbit[k*3+1]=(float)p[1];
                    orbit[k*3+2]=(float)p[2];
                }
                GLES20.glUniform4f(uColor,.39f,.39f,.39f,.60f);
                draw(GLES20.GL_LINE_STRIP,orbit,161,0,1);
            }
            // The Sun is the world origin, never artificially displaced.
            GLES20.glUniform4f(uColor,1,1,1,1);
            float sunSize=Math.max(9,Math.min(30,screenH*.045f));
            draw(GLES20.GL_POINTS,new float[]{0,0,0},1,2,sunSize);
            syncTextures();
            float[][] positions=new float[8][];
            for(int i=7;i>=0;i--){
                double[] p=Ephemeris.position(i,time);
                float[] xyz={(float)p[0],(float)p[1],(float)p[2]};
                float[] clip={0,0,0,0};
                Matrix.multiplyMV(clip,0,vp,0,new float[]{xyz[0],xyz[1],xyz[2],1},0);
                float w=Math.abs(clip[3])<.001f?1:clip[3];
                positions[i]=new float[]{(clip[0]/w+1)/2,(1-clip[1]/w)/2,(clip[2]/w+1)/2};
                float size=Math.max(18,Math.min(45,screenH*.064f));
                if(i==focus)size*=1.25f;
                if(textures[i]!=0){
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[i]);
                    GLES20.glUniform1i(uTexture,0);
                    GLES20.glUniform4f(uColor,1,1,1,1);
                    draw(GLES20.GL_POINTS,xyz,1,1,size);
                }else{
                    int col=Ephemeris.COLOR[i];
                    float red=((col>>16)&255)/255f,green=((col>>8)&255)/255f,blue=(col&255)/255f;
                    GLES20.glUniform4f(uColor,red,green,blue,1);
                    draw(GLES20.GL_POINTS,xyz,1,2,size);
                }
            }
            screen=positions;
        }
        private void draw(int mode,float[] coords,int count,int style,float size){
            FloatBuffer buffer=ByteBuffer.allocateDirect(coords.length*4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(coords).position(0);
            GLES20.glEnableVertexAttribArray(aPosition);
            GLES20.glVertexAttribPointer(aPosition,3,GLES20.GL_FLOAT,false,0,buffer);
            GLES20.glUniform1i(uMode,style);GLES20.glUniform1f(uSize,size);
            GLES20.glDrawArrays(mode,0,count);
            GLES20.glDisableVertexAttribArray(aPosition);
        }
        private int shader(int type,String source){
            int result=GLES20.glCreateShader(type);
            GLES20.glShaderSource(result,source);GLES20.glCompileShader(result);
            int[] ok=new int[1];GLES20.glGetShaderiv(result,GLES20.GL_COMPILE_STATUS,ok,0);
            if(ok[0]==0)throw new IllegalStateException("GL shader: "+GLES20.glGetShaderInfoLog(result));
            return result;
        }
        private int buildProgram(String vs,String fs){
            int v=shader(GLES20.GL_VERTEX_SHADER,vs),f=shader(GLES20.GL_FRAGMENT_SHADER,fs);
            int p=GLES20.glCreateProgram();GLES20.glAttachShader(p,v);GLES20.glAttachShader(p,f);
            GLES20.glLinkProgram(p);
            int[] ok=new int[1];GLES20.glGetProgramiv(p,GLES20.GL_LINK_STATUS,ok,0);
            if(ok[0]==0)throw new IllegalStateException("GL linker: "+GLES20.glGetProgramInfoLog(p));
            GLES20.glDeleteShader(v);GLES20.glDeleteShader(f);return p;
        }
    }
}
