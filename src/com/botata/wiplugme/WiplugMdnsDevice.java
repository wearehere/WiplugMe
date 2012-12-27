package com.botata.wiplugme;

import java.util.HashMap;
import java.util.Set;

public class WiplugMdnsDevice {
	
	public int ifindex;
	public String devicenm;
	public String devicefullnm;
	public String devicehostnm;
	public String[] deviceips;
	public int deviceport;
	
	
	public boolean hasPassword(){
		if(mtextrecord.mparas.containsKey("passwd"))
			return false;
		return true;
	}
	
	public String getDeviceId(){
		if(!mtextrecord.mparas.containsKey("deviceid"))
			return "";
		return mtextrecord.mparas.get("deviceid");
	}
	
	public void setDeviceText(String text){
		mtextrecord.mparas.clear();
		mtextrecord.fromString(text);
	}
	
	protected MdnsTextRecord mtextrecord;
	
	public class MdnsTextRecord{
		public HashMap<String, String> mparas;
		
		public int fromString(String str){
			int offset = 0;
			while(true){
				if(offset >= str.length())
					break;
				int length = (int)str.substring(offset, offset+1).getBytes()[0];
				if(length + offset > str.length())
					break;
				String sunit = str.substring(offset+1, offset+1+length);
				offset += (length+1);

				String[] result = sunit.split("=");
				if(result.length == 1){
					mparas.put(result[0], "");
				}else if(result.length == 2){
					mparas.put(result[0], result[1]);
				}
			}
			return 0;
		}
		
		public String toString(){
			String finalstr = "";
			Set<String> keyset = mparas.keySet();
			for(String key:keyset){
				int length = key.length();
				if(!mparas.get(key).equals("")){
					length += (1+mparas.get(key).length());
				}
				byte[] vle = new byte[1];
				vle[0] = Integer.valueOf(length).byteValue();
				finalstr += new String(vle);
				finalstr += key;
				if(!mparas.get(key).equals("")){
					finalstr += "=mparas.get(key)";
				}
			}
			return finalstr;
		}
	}

}
