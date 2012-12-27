package com.botata.wiplugme;

import java.io.IOException;
import java.util.HashMap;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.impl.JmDNSImpl;

import android.content.Context;

public class WiplugMDnsService extends Thread{
	
    private String mtype = "_wiplug._tcp.local.";
    private JmDNS jmdns = null;
    private javax.jmdns.ServiceListener listener = null;
    private android.net.wifi.WifiManager.MulticastLock lock;
    private Context mcontext = null;
    
    public HashMap<String, WiplugMdnsDevice>  malldevices;
    
    public WiplugMDnsService(Context ctx, final String servicetype){
    	mcontext = ctx;
    	mtype = servicetype;
    	malldevices = new HashMap<String, WiplugMdnsDevice>();
    }
    
    //ServiceInfo.create("_test._tcp.local.", "AndroidTest", 0, "plain test service from android");
    public void registerService(String name, String text){
    	ServiceInfo serviceInfo = ServiceInfo.create(mtype, name, 0, text);
    	try{
    		jmdns.registerService(serviceInfo);
    	}catch(Exception e){
    		
    	}
    }
    
    public void run(){
//    	while(true){
//    		if(this.isInterrupted())
//    			break;
//        	startBrowse();
//        	for(int i =0; i< 5; i++){
//        		try {
//					sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//        			break;
//				}
//        	}
//        	stopBrowse();
//        	try {
//				sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				break;
//			}
//    	}
    	this.startBrowse();
    	while(true){
	    	for(int i =0; i< 5; i++){
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}	
	    	}
	    	JmDNSImpl impl = (JmDNSImpl)jmdns;
	    	impl.startProber();
    	}
    }

	public void startBrowse(){
        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager)mcontext.getSystemService(android.content.Context.WIFI_SERVICE);
        lock = wifi.createMulticastLock("mylockthereturn");
        lock.setReferenceCounted(true);
        lock.acquire();
        try {
            jmdns = JmDNS.create();
            jmdns.addServiceListener(mtype, listener = new javax.jmdns.ServiceListener() {

                public void serviceResolved(ServiceEvent ev) {
                    String additions = "";
                    if (ev.getInfo().getInetAddresses() != null && ev.getInfo().getInetAddresses().length > 0) {
                        additions = ev.getInfo().getInetAddresses()[0].getHostAddress();
                    }
                    ServiceInfo sinfo = ev.getInfo();
                    String fullnm = sinfo.getQualifiedName();
                    String name = fullnm.split("\\.")[0];
                    
                    WiplugMdnsDevice adevice = null;
                    if(malldevices.keySet().contains(name)){
                    	adevice = malldevices.get(name);
                    }else{
                    	adevice = new WiplugMdnsDevice();
                        malldevices.put(name, adevice);
                    }
                    
                    adevice.devicefullnm = fullnm;
                    adevice.deviceport = sinfo.getPort();
                    adevice.deviceips = sinfo.getHostAddresses();
                    adevice.devicenm = name;
                }

                public void serviceRemoved(ServiceEvent ev) {
                    ServiceInfo sinfo = ev.getInfo();
                    String fullnm = sinfo.getQualifiedName();
                    String name = fullnm.split(".")[0];
                    //ev.getName();
                    if(malldevices.keySet().contains(name)){
                    	malldevices.remove(name);
                    }
                }

                public void serviceAdded(ServiceEvent event) {
                    // Required to force serviceResolved to be called again (after the first search)
                    jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }		
		
	}
	
	public void stopBrowse(){
    	if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(mtype, listener);
                listener = null;
            }
            //jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            jmdns = null;
    	}
    	//repo.stop();
        //s.stop();
        lock.release();		
	}
	
}
