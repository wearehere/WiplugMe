package com.botata.wiplug;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class WiplugCmd{
		
	public WiplugCmd(){
		mparas = new HashMap<String, String>();
		mcontent = null;
	}
	
	public String getPrintString(){
		String outstr = "";
		outstr += "Action:"+maction+"\n";
		for(Iterator<String> i = mparas.keySet().iterator(); i.hasNext();){
			String key = i.next();
			String value = mparas.get(key);
			outstr += key+ "=>" +value +"\n";
		}
		return outstr;
	}
	
	public byte[] toBuffer(){
		if(maction == "")
			return null;
		String outcmdstr = "GET "+maction+" wiplug/1.0\r\n";
		Set<String> keys = mparas.keySet();
		for(String key:keys){
			if(key.equals("Content-Length"))
				continue;
			outcmdstr += key+":"+mparas.get(key)+"\r\n";
			//System.out.println("[toBuffer]"+key+"=>"+mparas.get(key));
		}
		
		if(mcontent == null)
			outcmdstr += "Content-Length:0\r\n";
		else
			outcmdstr += "Content-Length:"+String.valueOf(mcontent.length)+"\r\n";
		outcmdstr += "\r\n";
		
		int length = outcmdstr.length();
		if(mcontent != null)
			length += mcontent.length;
			
		//System.out.println("toBuffer:"+length);
		//System.out.println("[toBuffer all]"+outcmdstr);
		byte[] rtnbuf = new byte[length];
		System.arraycopy(outcmdstr.getBytes(),0, rtnbuf, 0, outcmdstr.length());
		if(mcontent != null && mcontent.length > 0)
			System.arraycopy(mcontent, 0, rtnbuf, outcmdstr.length(), mcontent.length);
		return rtnbuf;
	}
	
	public int fromBuffer(byte[] ba, int offset, int length){
		String bstr =  new String(ba, offset, length);
		int endpos = bstr.indexOf("\r\n\r\n");
		if(endpos == -1)
			return -1;
		bstr = bstr.substring(0, endpos);
		int actionpos = bstr.indexOf("\r\n");
		String baction = bstr.substring(0, actionpos);
		String[] paras = baction.split(" ");
		if(paras.length <= 1){
			//Log.e("", "Message not correct:"+baction+"\n");
			System.out.println( "Message not correct:"+baction);
		}
		else
			maction = paras[1];
		bstr = bstr.substring(actionpos+2);
		paras = bstr.split("\r\n");
		mparas.clear();
		for(int i=0; i< paras.length; i++){
			int comapos = paras[i].indexOf(":");
			if(comapos == -1)
				continue;
			String key = paras[i].substring(0, comapos);
			String value = paras[i].substring(comapos+1);
			//System.out.println(key+"=>"+value);
			mparas.put(key, value);
		} 
		if(mparas.get("Content-Length") == null ||
			mparas.get("Content-Length").equals("0"))
		{
			//System.out.println(mparas.get("Content-Length") == "0");
			//System.out.println(mparas.get("Content-Length").equals("0"));
			return endpos+4;
		}
		int contentlen = Integer.parseInt(mparas.get("Content-Length"));
		if(length - endpos < contentlen)
			return 0;
		if(contentlen > 0)
			System.arraycopy(ba, offset+endpos, mcontent, 0, contentlen);
		return endpos+4+contentlen;
	}
	
	public String GetParam(String key){
		try{
			String vle = mparas.get(key);
			return vle;
		}catch(Exception e){
			return null;
		}		
	}
	
	public String getUrl(){
		return GetParam("X-WiPlug-Url");
	}
	
	public String getType(){
		return GetParam("X-WiPlug-Type");
	}
	
	public String getAudioType(){
		return GetParam("X-WiPlug-AudioType"); //mono stereo		
	}
	
	public String getAudioSampleRate(){
		return GetParam("X-WiPlug-AudioSampleRate"); //44100
	}
	
	public String getAudioFormat(){
		return GetParam("X-WiPlug-AudioFormat"); //8 16  which mean PCM_8_BIT or PCM_16_BIT
	}
	
	public String getPosition(){
		return GetParam("X-WiPlug-Position");
	}
	
	public String getValue(){
		return GetParam("X-WiPlug-Value");
	}
	
	public String getAdapter(){
		return GetParam("X-WiPlug-Adapter");
	}
	
	public String maction;
	public HashMap<String, String> mparas;
	public byte[] mcontent;
	
	public static void printHexString(byte[] b, int offset, int length){
		for (int i = offset; i < offset + length; i++) { 
			if(i >= b.length){
				break;
			}
			String hex = Integer.toHexString(b[i] & 0xFF); 
			if (hex.length() == 1) { 
				hex = '0' + hex; 
			} 
			System.out.print(hex.toUpperCase() +" "); 
		} 	
		System.out.print("\n"); 
	}
	
	public static void main(String args[]) {
		//test wiplugcmd
		WiplugCmd cmd = new WiplugCmd();
		int ret;
		String testcm = new String();
		testcm = "GET /play WiPlug/1.0\r\n";
		testcm += "Content-Length:0\r\n";
		testcm += "X-WiPlug-Type:picture\r\n";
		testcm += "X-WiPlug-Url:http://192.168.1.123/abc\r\n";
		testcm += "\r\n\r\n";

		ret = cmd.fromBuffer(testcm.getBytes(),0,20);
		System.out.println("action:[S:-1]:"+ret);
		String playurl = cmd.getUrl();
		if(playurl == null)
			System.out.println("playurl null");
		ret = cmd.fromBuffer(testcm.getBytes(), 0, testcm.length());
		System.out.println("return length:"+ret);
		System.out.println("action:[S:Action]"+cmd.maction);
		for(Iterator<String> i = cmd.mparas.keySet().iterator(); i.hasNext();){
			String key = i.next();
			String value = cmd.mparas.get(key);
			System.out.println(key+":"+value);
		}
		
		System.out.println("ToBUFFER-------------------------");
		byte[] outbuf = cmd.toBuffer();
		printHexString(outbuf, 0, outbuf.length);		
		String outstr = new String(outbuf);
		System.out.print(outstr);
		System.out.println("END TEST-------------------------");
	}
}

	