package com.botata.wiplugme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.botata.wiplug.WiplugCmd;
import com.botata.wiplug.WiplugConnector;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class WiplugMeActivity extends Activity {

    protected android.net.wifi.WifiManager.MulticastLock lock;
    private Handler mupdatedevicehandler = new Handler();
    private Handler mwiplugcmdhandler = null;
	
    protected WiplugMDnsService mwiplugmdnsservice = null;
    //protected Spinner mspinnerdevice = null;
    protected LinearLayout mrootlayout = null;
    protected TextView mtextselecteddevice = null;
    protected Runnable mupdaterunnable = null;
    protected TextView mtextstus = null;
    protected TextView mmediaduration = null;
    protected TextView mmediaplaypos = null;
    protected ImageView mimgdevice = null;
    protected ImageView mimgpicavailable = null;
    protected ImageView mimgaudioavailable = null;
    protected ImageView mimgvideoavailable = null;
    protected ImageView mimgplayctrl = null;

    protected TextView mtextfloat = null;
    protected ArrayAdapter<String> mdevicelistadapter = null;

    protected HashMap<String, WiplugMdnsDevice>  mcurrentdevices;
    
	WiplugConnector mconnector = null;
	ActivityThread mactivethread = null;
    
	GestureDetector mgesturedector = null;
	WiplugGesture mwipluggesture = null;
	DisplayMetrics mdm = null;
	
	protected String mvideostus = "";
	protected String maudiostus = "";
	protected String mvideoduration = "";
	protected String mvideoposition = "";
    	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mwiplugmdnsservice = new WiplugMDnsService(this.getApplicationContext(), "_wiplug._tcp.local.");
        //mwiplugmdnsservice.startBrowse();
        mwiplugmdnsservice.start();
        
        //mspinnerdevice = (Spinner) this.findViewById(R.id.spinnerDevice);
        mrootlayout = (LinearLayout) this.findViewById(R.id.rootLayout);
        mtextselecteddevice = (TextView) this.findViewById(R.id.textViewSelectedDevice);
        mtextstus = (TextView) this.findViewById(R.id.textStus);
        mmediaduration = (TextView) this.findViewById(R.id.textMediaDuration);
        mmediaplaypos = (TextView) this.findViewById(R.id.textMediaPos);
        mimgdevice = (ImageView) this.findViewById(R.id.imageViewDevice);
        mimgpicavailable = (ImageView) this.findViewById(R.id.imagePicInd);
        mimgaudioavailable = (ImageView) this.findViewById(R.id.imageAudioInd);
        mimgvideoavailable = (ImageView) this.findViewById(R.id.imageVideoInd);
        mimgplayctrl = (ImageView)this.findViewById(R.id.imageController);
        
        mwiplugcmdhandler = new Handler(){
        	public void handleMessage(Message msg){
        		WiplugCmd cmd = (WiplugCmd) msg.obj;
        		handleWiplugCmd(cmd);
        	}
        };
        
        //initDeviceSpinner();
		mactivethread = new ActivityThread();
		mactivethread.start();
                
        mupdaterunnable = new Runnable(){
			public void run() {
        		try{        			
        			updateDeviceInfo();
        		}catch(Exception e){
        			e.printStackTrace();
        			//TODO. when switch network, it may throw exception which unexpected.
        			//Can not find where exception out now.
        			//Should use one Exception Window, and show debug message on it.
        			//just like flash.
        		}
        		mupdatedevicehandler.postDelayed(mupdaterunnable, 1000);
			}
        };
        mupdatedevicehandler.postDelayed(mupdaterunnable, 1000);
        
//        //---
//        mimgplayctrl.setOnTouchListener(new OnTouchListener(){
//
//			@Override
//			public boolean onTouch(View arg0, MotionEvent ev) {
//				return MainActivity.this.onTouchEvent(ev);
//			}
//        	
//        });
        
        mimgplayctrl.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
			
				String curmedia = "";
				String curaction = "";
				if(mvideostus.equals("playing")){				
					curmedia = "video";
					curaction = "/pause";
				}else if(mvideostus.equals("pause")){
					curmedia = "video";
					curaction = "/resume";
				}
				
				if(!curmedia.isEmpty()){
					WiplugCmd cmd = new WiplugCmd();
					cmd.maction = curaction;
					cmd.mparas.put("X-WiPlug-Type", curmedia);
					cmd.mparas.put("X-WiPlug-Adapter", "wiplug");
					
					if(mconnector != null){
						mconnector.putSendCmd(cmd);
					}
				}
				mtextstus.setText("playctrl clicked");
			}});
        
        //test
        
        mwipluggesture = new WiplugGesture();
        mgesturedector = new GestureDetector(mwipluggesture);
        mtextfloat = (TextView) this.findViewById(R.id.textViewfloat);
        //mrootlayout.setOnTouchListener(new WiplugGesture());
//        mtextfloat = new FloatText(this);        
//        mtextfloat.setText("00:00");
//        mtextfloat.setTextSize(24);
//        //mtextfloat.setBackgroundResource(android.R.color.transparent);
//        mtextfloat.setVisibility(View.INVISIBLE);
//        
//        WindowManager wm = (WindowManager)getApplicationContext().getSystemService("window");
//        WindowManager.LayoutParams param = new WindowManager.LayoutParams();
//        param.alpha = 0;
//        param.type = WindowManager.LayoutParams.TYPE_PHONE;
//        param.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        param.width = 120;
//        param.height = 40;
//        param.gravity = Gravity.LEFT|Gravity.TOP;
//        wm.addView(mtextfloat, param);        
        
        mdm = new DisplayMetrics();   
        getWindowManager().getDefaultDisplay().getMetrics(mdm);          
    }
    

    @Override
    public boolean onTouchEvent(MotionEvent ev){
    	//check mvideostus
    	if(this.mvideoduration.isEmpty() || this.mvideoposition.isEmpty())
    		return false;
    	mwipluggesture.mediaduration = Integer.valueOf(this.mvideoduration);
    	mwipluggesture.mediaplaypos = Integer.valueOf(this.mvideoposition);
    	
//    	mwipluggesture.mediaduration = 1100000;
//    	mwipluggesture.mediaplaypos = 220000;
    	if(this.mgesturedector.onTouchEvent(ev))
    		return true;
    	if(ev.getAction() == MotionEvent.ACTION_UP){
    		if(this.mwipluggesture.mseektotime != -1){
    			this.mtextstus.setText("Seek to:"+formatTime(this.mwipluggesture.mseektotime/1000));
    			
    			String curmedia = "";
    			if(!mvideostus.isEmpty())
    				curmedia = "video";
    			else if(!maudiostus.isEmpty())
    				curmedia = "audio";
				WiplugCmd cmd = new WiplugCmd();
				cmd.maction = "/seek";
				cmd.mparas.put("X-WiPlug-Type", curmedia);
				cmd.mparas.put("X-WiPlug-Position", String.valueOf(mwipluggesture.mseektotime));
				cmd.mparas.put("X-WiPlug-Adapter", "wiplug");
				
				if(mconnector != null){
					mconnector.putSendCmd(cmd);
				}
    			
    			this.mwipluggesture.mseektotime = -1;    			
    			mtextfloat.setVisibility(View.INVISIBLE);
    		}
    	}
    	return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
//    protected void initDeviceSpinner(){
//        mspinnerdevice.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){ 
//            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) { 
//                // TODO Auto-generated method stub 
//                /* 将所选mySpinner 的值带入myTextView 中*/ 
//            	mtextstus.setText("您选择的是："+ mdevicelistadapter.getItem(arg2)); 
//                /* 将mySpinner 显示*/ 
//                arg0.setVisibility(View.VISIBLE); 
//            } 
//            public void onNothingSelected(AdapterView<?> arg0) { 
//                // TODO Auto-generated method stub 
//            	mtextstus.setText("NONE"); 
//                arg0.setVisibility(View.VISIBLE); 
//            } 
//        }); 
//        /*下拉菜单弹出的内容选项触屏事件处理*/ 
////        mspinnerdevice.setOnTouchListener(new Spinner.OnTouchListener(){ 
////            public boolean onTouch(View v, MotionEvent event) { 
////                // TODO Auto-generated method stub 
////                /* 将mySpinner 隐藏，不隐藏也可以，看自己爱好*/ 
////                v.setVisibility(View.INVISIBLE); 
////                return false; 
////            } 
////        }); 
//        /*下拉菜单弹出的内容选项焦点改变事件处理*/ 
//        mspinnerdevice.setOnFocusChangeListener(new Spinner.OnFocusChangeListener(){        
//            public void onFocusChange(View v, boolean hasFocus) { 
//            // TODO Auto-generated method stub 
//                v.setVisibility(View.VISIBLE); 
//            } 
//            });          	
//    	
//    }
    
	
    protected void handleWiplugCmd(WiplugCmd command){
    	if(command.maction.equals("/status")){
    		Set<String> keys = command.mparas.keySet();
    		if(keys.contains("X-WiPlug-PictureUrl"))
    			this.mimgpicavailable.setImageResource(R.drawable.pic);
    		else
    			this.mimgpicavailable.setImageResource(R.drawable.pic_1);
    		if(keys.contains("X-WiPlug-AudioStatus"))
    			this.mimgaudioavailable.setImageResource(R.drawable.music);
    		else
    			this.mimgaudioavailable.setImageResource(R.drawable.music_1);
    		if(keys.contains("X-WiPlug-VideoStatus")){
    			String vstus = command.mparas.get("X-WiPlug-VideoStatus");
    			if(vstus != null){
    				mvideostus = vstus;
    				if(vstus.equals("playing")){
    					this.mimgplayctrl.setImageResource(R.drawable.pause);
    					this.mimgplayctrl.setEnabled(true);
    				}else if(vstus.equals("pause")){
    					this.mimgplayctrl.setImageResource(R.drawable.play);
    					this.mimgplayctrl.setEnabled(true);    					
    				}else{
    					this.mimgplayctrl.setImageResource(R.drawable.playgray);
    					this.mimgplayctrl.setEnabled(false);
    				}
    			}else{
    				mvideostus = "";
    			}
    			
    			this.mimgvideoavailable.setImageResource(R.drawable.video);
    			String duration = command.mparas.get("X-WiPlug-Duration");
    			if(duration != null){
    				mvideoduration = duration;
    				int idur = Integer.parseInt(duration);
    				this.mmediaduration.setText(formatTime(idur/1000));
    			}
    			String playpos = command.mparas.get("X-WiPlug-Position");
    			if(playpos != null){
    				mvideoposition = playpos;
    				int ipos = Integer.parseInt(playpos);
    				this.mmediaplaypos.setText(formatTime(ipos/1000));
    			}
    		}
    		else
    			this.mimgvideoavailable.setImageResource(R.drawable.video_1);
    	}
    }
        
    protected void updateDeviceInfo(){
    	mcurrentdevices = (HashMap<String, WiplugMdnsDevice>) mwiplugmdnsservice.malldevices.clone();
    	if(mcurrentdevices.size() == 0){
    		mimgdevice.setImageResource(R.drawable.listgray);
    		mimgdevice.setEnabled(false);
    		return;
    	}
    	mimgdevice.setImageResource(R.drawable.list);
    	mimgdevice.setEnabled(true);
    	
    	List<String> devicelist = new ArrayList<String>();
    	Set<String> keys = mcurrentdevices.keySet();
    	for(String key:keys){
    		devicelist.add(key);
    	}
    	mdevicelistadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devicelist);
    	mdevicelistadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	//mspinnerdevice.setAdapter(mdevicelistadapter);
    	
    	
        mimgdevice.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				new AlertDialog.Builder(WiplugMeActivity.this).setTitle("Wiplug Device")
		        .setSingleChoiceItems(mdevicelistadapter, 1,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getApplicationContext(),mdevicelistadapter.getItem(which),Toast.LENGTH_SHORT).show();
                		String selectnm = mdevicelistadapter.getItem(which);
                		WiplugMdnsDevice device = mcurrentdevices.get(selectnm);
                		if(device != null){
                			connectDevice(device);
                		}
                        dialog.dismiss();
                    }
                }).show();				
			}});    	
    }
    
    protected void connectDevice(WiplugMdnsDevice device){
    	this.mtextselecteddevice.setText(device.devicenm);
    	if(device.deviceips.length > 0){
    		String serviceaddr = device.deviceips[0]+":"+String.valueOf(device.deviceport);
    		connectWiplugDevice(serviceaddr);
    	}
    }
    
    private void connectWiplugDevice(String serviceaddr){
    	if(mconnector != null){
    		mconnector.interrupt();
    		mconnector = null;
    	}
    	String[] ips = serviceaddr.split(":");
		mconnector = new WiplugConnector();
		mconnector.setBaseServiceAddress(ips[0], Integer.valueOf(ips[1]));
		Log.e("WiplugService","onStart address "+ips[0]+":"+ips[1]);
		mconnector.start();    	
    }    

	class ActivityThread extends Thread{
		
		public void run(){
			while(true){				
				if(this.isInterrupted())
					break;
				
				if (mconnector == null) {
					try {
						sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				
				WiplugCmd cmd = null;
				cmd = mconnector.getRecvCmd();
				if(cmd == null){
					try {
						sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				Message msg= new Message();
				msg.obj = cmd;
				mwiplugcmdhandler.sendMessage(msg);
			}
		}
	}        
	
	
	//---------------------------------
	public class WiplugGesture implements OnGestureListener{

		final int FLING_MIN_DISTANCE = 10;
		final int FLING_MIN_VELOCITY = 10;
		
		public int mediaduration = 0;
		public int mediaplaypos = 0;
		
		public int mseektotime = -1;
				
		@Override
		public boolean onDown(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// 参数解释：
			// e1：第1个ACTION_DOWN MotionEvent
			// e2：最后一个ACTION_MOVE MotionEvent
			// velocityX：X轴上的移动速度，像素/秒
			// velocityY：Y轴上的移动速度，像素/秒
			// 触发条件 ：
			// X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒			
			
//			float percent = calcPercent((int)e1.getX(), (int)e2.getX(), 20, mdm.widthPixels-20);
//			String showtime = calcuShowMediaTime(percent);			
			//Toast.makeText(MainActivity.this, "Fling:"+showtime, Toast.LENGTH_SHORT).show();
			//Toast.makeText(MainActivity.this, "Fling:"+String.valueOf(e2.getX()), Toast.LENGTH_SHORT).show();
//			if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
//				       && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
//				    // Fling left
//				Toast.makeText(MainActivity.this, "Fling Left:"+String.valueOf(e2.getX()), Toast.LENGTH_SHORT).show();
//			} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
//			        && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
//			    // Fling right
//				Toast.makeText(MainActivity.this, "Fling Right:"+String.valueOf(e2.getX()), Toast.LENGTH_SHORT).show();
//			}
//			mtextfloat.setVisibility(View.INVISIBLE);
//			mtextstus.setText("seek to:"+showtime);
			
			return false;
		}

		@Override
		public void onLongPress(MotionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		protected float calcPercent(int start, int end, int min, int max){
			
			if(start <= min || start >= max)
				return (float)0;
			if(end == start)
				return (float)0;
			int total = 0;
			if(start < end)
				total = max -start;
			else
				total = start - min;
			int distance = end - start;
			float value = (float) (Math.round(100*(distance/(float)total))/100.0);
			if(value < -1)
				value = -1;
			else if(value > 1)
				value = 1;
			return value;
		}
		
		
		protected int calcuShowMediaTime(float percent){
			int showtime = mediaplaypos;
			if(percent < 0){
				showtime = (int) ((1+percent)*mediaplaypos);
			}else if(percent > 0){
				showtime = (int) (mediaplaypos + (mediaduration-mediaplaypos)*percent);
			}
			return showtime;
		}
		

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
			if(e1.getX() < 20 || e1.getX() > mdm.widthPixels-20)
				return false;
			
			if(! WiplugMeActivity.this.mvideostus.equals("playing"))
				return false;
			
			mtextstus.setText("scroll:"+String.valueOf(e2.getX())+":"+String.valueOf(e2.getY()));
			
			mtextfloat.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(120,40);
			
			if(e2.getX() > mdm.widthPixels - 120)
				param.leftMargin = mdm.widthPixels - 120;
			else
				param.leftMargin = (int)(e2.getX());
			if(e2.getY() > 100)
				param.topMargin = (int)(e2.getY()) - 100;
			else
				param.topMargin = 0;
			
			//mtextfloat.setWidth(120);
			//mtextfloat.setHeight(90);
			mtextfloat.setTextColor(Color.CYAN);
			mtextfloat.setTextSize(36);			
			float percent = calcPercent((int)e1.getX(), (int)e2.getX(), 20, mdm.widthPixels-20);
			mseektotime = calcuShowMediaTime(percent);
			String showtime = formatTime(mseektotime/1000);
			mtextfloat.setText(showtime);
			mtextfloat.setLayoutParams(param);
			
//	        WindowManager wm = (WindowManager)getApplicationContext().getSystemService("window");
//	        WindowManager.LayoutParams param = new WindowManager.LayoutParams();
//	        param.type = WindowManager.LayoutParams.TYPE_PHONE;
//	        param.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//	        param.width = 120;
//	        param.height = 40;
//	        param.gravity = Gravity.LEFT|Gravity.TOP;
//	        param.x = (int)(e2.getX());
//	        //if((int)(e2.getY()) > 30)
//	        param.y = (int)(e2.getY()) - 100;
//	        //else
//	        //	param.y = (int)(e2.getY()) + 30;
//	        mtextfloat.setVisibility(View.VISIBLE);
//	        mtextfloat.setText(String.valueOf(e2.getX()));
//	        wm.updateViewLayout(mtextfloat, param);
			return false;
		}

		@Override
		public void onShowPress(MotionEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}		
	}
	
	//----------util--------------------
	protected String formatTime(long duration){
		String tm = "__:__";
		if(duration == -1)
			return tm;
		
		int hour = (int) (duration/3600);
		int minute = (int) ((duration%3600)/60);
		int second = (int) (duration%60);
		if(hour!= 0x00)
			tm = String.format("%02d:%02d:%02d", hour, minute, second);
		else
			tm = String.format("%02d:%02d", minute, second);
		return tm;			
	}		
}
