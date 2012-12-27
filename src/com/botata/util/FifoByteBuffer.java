package com.botata.util;

public class FifoByteBuffer{
	public byte[] mcachebytes;
	public int mreadoffset;
	public int mwriteoffset;
	
	//mwriteoffset - mreadoffset is valid length
	
	public FifoByteBuffer(){
		mcachebytes = new byte[1024];
		mreadoffset = 0;
		mwriteoffset = 0;
	}	
	
	protected void checkMoreBufLen(int morelen){
		if(morelen +  mwriteoffset < mcachebytes.length)
			return;
		int newlen = (int)((morelen + mwriteoffset - mreadoffset)/2.0);
		newlen *= 4;
		if(newlen > mcachebytes.length){
			byte[] newbb = new byte[newlen];
			System.arraycopy(mcachebytes, mreadoffset, newbb, 0, mwriteoffset - mreadoffset);
			mwriteoffset = mwriteoffset - mreadoffset;
			mreadoffset = 0;
			mcachebytes = newbb;
		}else{
			System.arraycopy(mcachebytes, mreadoffset, mcachebytes, 0, mwriteoffset - mreadoffset);
			mwriteoffset = mwriteoffset - mreadoffset;
			mreadoffset = 0;			
		}
	}
	
	public void pushBytes(byte[] bb, int offset, int length){
		checkMoreBufLen(length);
		System.arraycopy(bb, offset, mcachebytes, mwriteoffset, length);
		mwriteoffset += length;
	}
	
	//if bb is null, just jump off length
	public int readBytes(byte[]bb, int offset, int length){
		int readlen = getReadLength();
		if (length < readlen){
			readlen = length;
		}
		if(readlen == 0)
			return 0;
		if(bb != null)
			System.arraycopy(mcachebytes, mreadoffset, bb, offset, readlen);
		this.mreadoffset += length;
		return readlen;
	}
	
	public int getReadLength(){
		return mwriteoffset - mreadoffset;
	}
	
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
		//test FifoByteBuffer
		FifoByteBuffer abuf = new FifoByteBuffer();
		System.out.println(""+abuf.mcachebytes.length+" "+abuf.mreadoffset+" "+abuf.mwriteoffset);
		byte[] examplebytes = new byte[10];
		for(int i=0; i< 10; i++){
			examplebytes[i] = (byte)(i&0xff);
		}
		abuf.pushBytes(examplebytes, 0, 5);
		printHexString(abuf.mcachebytes, abuf.mreadoffset, abuf.mwriteoffset - abuf.mreadoffset);
		System.out.println(""+abuf.mcachebytes.length+" "+abuf.mreadoffset+" "+abuf.mwriteoffset);
		byte[] outbytes = new byte[10];
		System.out.println(outbytes.length);
		int ret = abuf.readBytes(outbytes, 0, 3);
		System.out.println(""+abuf.mcachebytes.length+" "+abuf.mreadoffset+" "+abuf.mwriteoffset);
		printHexString(outbytes, 0, ret);
		printHexString(abuf.mcachebytes, abuf.mreadoffset, abuf.mwriteoffset - abuf.mreadoffset);
	}	
}
