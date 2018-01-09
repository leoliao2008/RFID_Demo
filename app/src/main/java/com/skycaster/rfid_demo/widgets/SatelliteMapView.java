package com.skycaster.rfid_demo.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.animation.RotateAnimation;

import com.skycaster.inertial_navi_lib.GPGSV.GPGSVBean;
import com.skycaster.inertial_navi_lib.GPGSV.PRNBean;
import com.skycaster.rfid_demo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by 廖华凯 on 2017/6/23.
 */

public class SatelliteMapView extends TextureView {

    private float innerRadius;
    private float outerRadius;
    private int canvasWidth;
    private int canvasHeight;
    private Paint mPaintDarkBlue;
    private Paint mPaintBlue;
    private Paint mPaintWhiteSolid;
    private Paint mPaintWhiteDot;
    private Paint mPaintSatellite;
    private TextPaint mTextPaintFontSmall;
    private TextPaint mTextPaintFontBig;
    private float centerX;
    private float centerY;
    private ArrayList<GpsSatellite> mSatellites=new ArrayList<>();
    private double mFontHeight;
    private SensorManager mSensorManager;
    private Sensor mAccelerationSensor;
    private Sensor mMagneticSensor;
    private float[] rotationMatrix =new float[9];
    private float[] accelerateMatrix =new float[3];
    private float[] magneticMatrix =new float[3];
    private float[] orientationMatrix=new float[3];
    private float mLastDegree;
    private SensorEventListener mSensorEventListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER:
                    System.arraycopy(event.values,0, accelerateMatrix,0,3);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    System.arraycopy(event.values,0, magneticMatrix,0,3);
                    break;
                default:
                    break;
            }
            SensorManager.getRotationMatrix(rotationMatrix,null,accelerateMatrix,magneticMatrix);
            SensorManager.getOrientation(rotationMatrix,orientationMatrix);
            double degree = -Math.toDegrees(orientationMatrix[0]);
            if(Math.abs(degree- mLastDegree)>1){
                RotateAnimation animation=new RotateAnimation(
                        mLastDegree,
                        (float) degree,
                        RotateAnimation.RELATIVE_TO_SELF,
                        0.5f,
                        RotateAnimation.RELATIVE_TO_SELF,
                        0.5f
                );
                animation.setFillAfter(true);
                animation.setInterpolator(getContext(),android.R.interpolator.bounce);
                animation.setDuration(50);
                startAnimation(animation);
                mLastDegree = (float) degree;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private TextPaint mTextPaintPrn;
    private Rect mPrnRect=new Rect();
    private Comparator<GpsSatellite> mComparator=new Comparator<GpsSatellite>() {
        @Override
        public int compare(GpsSatellite o1, GpsSatellite o2) {
            return (int) (o1.getSnr()-o2.getSnr());
        }
    };
    private Paint mPaintBackGround;



    public SatelliteMapView(Context context) {
        this(context,null);
    }

    public SatelliteMapView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SatelliteMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SatelliteMapView);
        outerRadius =typedArray.getDimension(R.styleable.SatelliteMapView_radius, 400);
        typedArray.recycle();

        mPaintBlue =new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBlue.setColor(Color.parseColor("#00003E"));

        mPaintDarkBlue=new Paint(mPaintBlue);
        mPaintDarkBlue.setColor(Color.parseColor("#01152E"));

        mPaintBackGround=new Paint(mPaintBlue);
        mPaintBackGround.setColor(getResources().getColor(R.color.darcula));


        mPaintWhiteSolid =new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintWhiteSolid.setColor(Color.parseColor("#B5B5B6"));
        mPaintWhiteSolid.setStyle(Paint.Style.STROKE);
        mPaintWhiteSolid.setStrokeWidth(1);

        mPaintWhiteDot=new Paint(mPaintWhiteSolid);
        mPaintWhiteDot.setPathEffect(new DashPathEffect(new float[]{9,9},0));

        mTextPaintFontSmall=new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaintFontSmall.setColor(Color.parseColor("#B5B5B6"));
        mTextPaintFontSmall.setTextSize(getResources().getDimension(R.dimen.text_size_for_satellite_view_small));
        mTextPaintFontSmall.setStrokeWidth(2);

        mTextPaintFontBig=new TextPaint(mTextPaintFontSmall);
        mTextPaintFontBig.setTextSize(getResources().getDimension(R.dimen.text_size_for_satellite_view_big));
        mTextPaintFontBig.setStrokeWidth(4);
        Paint.FontMetrics fontMetrics = mTextPaintFontBig.getFontMetrics();
        mFontHeight = Math.ceil(fontMetrics.descent - fontMetrics.ascent-fontMetrics.leading*2);
        innerRadius= (float) (outerRadius-mFontHeight);

        mPaintSatellite=new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaintPrn=new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaintPrn.setTextSize(getResources().getDimension(R.dimen.text_size_for_satellite_view_smaller));
        mTextPaintPrn.setColor(Color.WHITE);


        mSensorManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Canvas canvas = lockCanvas();
                drawCompass(canvas);
                unlockCanvasAndPost(canvas);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                enableCompassMode(false);
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        setLayerType(LAYER_TYPE_SOFTWARE,null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        canvasWidth=MeasureSpec.getSize(widthMeasureSpec);
        canvasHeight=MeasureSpec.getSize(heightMeasureSpec);
        centerX=canvasWidth/2;
        centerY=canvasHeight/2;
    }



    /**
     * 绘制罗盘
     * @param canvas
     */
    private synchronized void drawCompass(Canvas canvas) {
        //画灰色背景
        canvas.drawCircle(centerX,centerY, (float) Math.sqrt(Math.pow(canvasWidth,2)+Math.pow(canvasHeight,2)),mPaintBackGround);
        //画外部实心圆
        canvas.drawCircle(centerX, centerY, outerRadius, mPaintDarkBlue);
        //画内部实心圆
        canvas.drawCircle(centerX, centerY, innerRadius, mPaintBlue);
        //画最外层两个实线圈圈
        canvas.drawCircle(centerX, centerY, outerRadius, mPaintWhiteSolid);
        canvas.drawCircle(centerX, centerY, innerRadius, mPaintWhiteSolid);
        //画内部4个虚线圈圈
        for(int i=1;i<5;i++){
            canvas.drawCircle(centerX, centerY, innerRadius / 5 * i, mPaintWhiteDot);
        }
        //分别画24条标识角度的虚线，以圆心为终点
        for(int i=1;i<25;i++){
            int degree = 15 * i;
            double lineAngle=degree*Math.PI/180;
            float startX= (float) (centerX+Math.cos(lineAngle)*(innerRadius));
            float startY= (float) (centerY-Math.sin(lineAngle)*(innerRadius));
            float stopX=centerX;
            float stopY=centerY;
            canvas.drawLine(startX,startY,stopX,stopY,mPaintWhiteDot);
            //每条虚线起始位置绘制表示其度数的文字
            String info;
            TextPaint textPaint=mTextPaintFontSmall;
            switch (degree){
                case 90:
                    info="N";
                    textPaint=mTextPaintFontBig;
                    break;
                default:
                    int temp=90-degree;
                    if(temp<0){
                        temp+=360;
                    }
                    info=temp +"°";
                    break;
            }
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            double deviation= Math.ceil(fontMetrics.descent - fontMetrics.ascent-fontMetrics.leading*2)/2;
            if(degree!=90){
                startX= (float) (centerX+Math.cos(lineAngle)*(innerRadius + mFontHeight/2-deviation));
                startY= (float) (centerY-Math.sin(lineAngle)*(innerRadius + mFontHeight/2-deviation));
            }else {
                startX= (float) (centerX+Math.cos(lineAngle)*(innerRadius + getResources().getDimension(R.dimen.deviation_1)));
                startY= (float) (centerY-Math.sin(lineAngle)*(innerRadius + getResources().getDimension(R.dimen.deviation_1)));
            }

            //旋转文字使其方向与园外径相切
            canvas.save();
            canvas.rotate(90-degree,startX,startY);
            float textWidth = textPaint.measureText(info);
            //设置文字起始点
            float textStartX=startX-textWidth/2;
            float textStartY=startY;
            canvas.drawText(info,textStartX,textStartY,textPaint);
            //返回默认的旋转角度
//            canvas.rotate(degree-90,startX,startY);
            canvas.restore();
        }
    }

    public synchronized int updateSatellites(GPGSVBean bean){
        Canvas canvas = lockCanvas();
        drawSatellitesWithGPGSV(canvas,bean);
        unlockCanvasAndPost(canvas);
        return bean.getSatCount();
    }

    /**
     * 利用U-Box生产的NEO-M8N模块传过来的GPGSV数据绘制卫星图
     * @param canvas
     * @param bean
     */
    private void drawSatellitesWithGPGSV(Canvas canvas,GPGSVBean bean) {
        drawCompass(canvas);
        for( PRNBean prnBean:bean.getPrns()){
            float elevation = prnBean.getElevation();
            float r = innerRadius * ((90-elevation) / 90);
            double degree = (90 - prnBean.getAzimuth() + 360) * Math.PI / 180;
            float x= (float) (centerX+Math.cos(degree)*r);
            float y= (float) (centerY-Math.sin(degree)*r);
            float snr = prnBean.getSnr();
            if(snr<=10){
                mPaintSatellite.setColor(Color.parseColor("#FB041D"));
            }else if(snr<=20){
                mPaintSatellite.setColor(Color.parseColor("#FB9804"));
            }else if(snr<=30){
                mPaintSatellite.setColor(Color.parseColor("#FBFB04"));
            }else if(snr<=50){
                mPaintSatellite.setColor(Color.parseColor("#B0FB04"));
            }else {
                mPaintSatellite.setColor(Color.parseColor("#007029"));
            }
            String prn = prnBean.getPrnCode();
            mTextPaintPrn.getTextBounds(prn,0,prn.length(),mPrnRect);
            float dotR= (float) Math.sqrt(Math.pow((mPrnRect.top-mPrnRect.bottom),2)+Math.pow(mPrnRect.width(),2));
            mPaintSatellite.setShadowLayer(dotR,0,0,Color.WHITE);
            mTextPaintPrn.setShadowLayer(dotR,0,0,Color.BLACK);
            canvas.drawCircle(x,y,dotR,mPaintSatellite);
            canvas.rotate(-mLastDegree,x,y);
            canvas.drawText(prn,x-mPrnRect.width()/2,y+(mPrnRect.bottom-mPrnRect.top)/2,mTextPaintPrn);
            canvas.rotate(mLastDegree,x,y);
        }
    }

    public synchronized int updateSatellites(@NonNull ArrayList<GpsSatellite>list){
        showLog("updateSatellites......");
        if(list.size()>0){
            mSatellites.clear();
            mSatellites.addAll(list);
        }
        Collections.sort(mSatellites,mComparator);
        Canvas canvas =lockCanvas();
        int count= drawSatellitesWithGPS(canvas);
        unlockCanvasAndPost(canvas);
        return count;
    }

    /**
     * 利用安卓底层封装好的类GpsSatellite绘制卫星图
     * @param canvas
     * @return 卫星绘制的数量
     */
    private synchronized int drawSatellitesWithGPS(Canvas canvas){
        drawCompass(canvas);
        int count=0;
        for(GpsSatellite satellite:mSatellites){
            int satellitePrn = satellite.getPrn();
            float elevation = satellite.getElevation();
            if(satellitePrn>0&&satellitePrn<33){
                float r = innerRadius * ((90-elevation) / 90);
                double degree = (90 - satellite.getAzimuth() + 360) * Math.PI / 180;
                float x= (float) (centerX+Math.cos(degree)*r);
                float y= (float) (centerY-Math.sin(degree)*r);
                float snr = satellite.getSnr();
                if(snr<=10){
                    mPaintSatellite.setColor(Color.parseColor("#FB041D"));
                }else if(snr<=20){
                    mPaintSatellite.setColor(Color.parseColor("#FB9804"));
                }else if(snr<=30){
                    mPaintSatellite.setColor(Color.parseColor("#FBFB04"));
                }else if(snr<=50){
                    mPaintSatellite.setColor(Color.parseColor("#B0FB04"));
                }else {
                    mPaintSatellite.setColor(Color.parseColor("#007029"));
                }
                String prn = String.format("%02d", satellitePrn);
                mTextPaintPrn.getTextBounds(prn,0,prn.length(),mPrnRect);
                float dotR= (float) Math.sqrt(Math.pow((mPrnRect.top-mPrnRect.bottom),2)+Math.pow(mPrnRect.width(),2));
                mPaintSatellite.setShadowLayer(dotR,0,0,Color.WHITE);
                mTextPaintPrn.setShadowLayer(dotR,0,0,Color.BLACK);
                canvas.drawCircle(x,y,dotR,mPaintSatellite);
                canvas.rotate(-mLastDegree,x,y);
                canvas.drawText(prn,x-mPrnRect.width()/2,y+(mPrnRect.bottom-mPrnRect.top)/2,mTextPaintPrn);
                canvas.rotate(mLastDegree,x,y);
                ++count;
            }
        }
        return count;
    }


    public void enableCompassMode(boolean isToEnable){
        if(isToEnable){
            if(mAccelerationSensor!=null){
                mSensorManager.registerListener(mSensorEventListener,mAccelerationSensor,SensorManager.SENSOR_DELAY_NORMAL);
            }
            if(mMagneticSensor!=null){
                mSensorManager.registerListener(mSensorEventListener,mMagneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
            }
        }else {
            if(mAccelerationSensor!=null){
                mSensorManager.unregisterListener(mSensorEventListener,mAccelerationSensor);
            }
            if(mMagneticSensor!=null){
                mSensorManager.unregisterListener(mSensorEventListener,mMagneticSensor);
            }
        }
    }

    private void showLog(String msg){
        Log.e(getClass().getSimpleName(),msg);
    }
}
