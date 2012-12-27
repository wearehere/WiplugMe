package com.botata.wiplug;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import com.botata.util.FifoByteBuffer;

public class WiplugConnector extends Thread{
	public String localserviceip = "10.0.20.59";
	public int localserviceport = 10011;
		
	protected ArrayList<WiplugCmd> recvcmds = new ArrayList<WiplugCmd>();
	protected ArrayList<WiplugCmd> sendcmds = new ArrayList<WiplugCmd>();
	
	protected FifoByteBuffer mcacherecvbyts = new FifoByteBuffer();
	
	
	public void setBaseServiceAddress(String ip, int port){
		localserviceip = ip;
		localserviceport = port;
	}
	
	//--------------------------------------------------------------

	//----------------------------------------------
	
	public void recvData(ByteBuffer recvbuf){
		//check if finish one full message
		//putRecvCmd(cmd);
		mcacherecvbyts.pushBytes(recvbuf.array(), 0, recvbuf.position());
		while(true){
			WiplugCmd cmd = new WiplugCmd();
			int ret = cmd.fromBuffer(mcacherecvbyts.mcachebytes, mcacherecvbyts.mreadoffset,
					mcacherecvbyts.mwriteoffset - mcacherecvbyts.mreadoffset);
			if(ret > 0){
				mcacherecvbyts.readBytes(null, 0, ret);
				System.out.println("WiplugConnector Recv Command:"+cmd.maction);
				putRecvCmd(cmd);
			}
			else{
				break;
			}
		}
	}
	
	public void putSendCmd(WiplugCmd cmd){
		synchronized(sendcmds){
			sendcmds.add(cmd);
		}
	}
	
	public WiplugCmd getSendCmd(){
		if(sendcmds.isEmpty())
			return null;
		synchronized(sendcmds){
			WiplugCmd cmd = sendcmds.get(0);
			sendcmds.remove(0);
			return cmd;
		}
	}
	
	public void putRecvCmd(WiplugCmd cmd){
		synchronized(recvcmds){
			recvcmds.add(cmd);
		}
	}
	
	public WiplugCmd getRecvCmd(){
		if(recvcmds.isEmpty())
			return null;
		synchronized(recvcmds){
			WiplugCmd cmd = recvcmds.get(0);
			recvcmds.remove(0);
			return cmd;
		}
	}	
	
	SocketChannel connect(SocketAddress address){
		SocketChannel sc = null;
		try	{
			System.out.println("try connect to:"+address.toString());
			sc = SocketChannel.open();
			boolean connected = sc.connect(address);
			//wait for connect finish
			if(!connected){
				sc.close();
				System.out.println("connect fail");
				return null;
			}
			//System.out.println("connect ok");
			sc.configureBlocking(false);
			return sc;
		}catch(IOException x) {
			//System.out.println("connect exception");
			x.printStackTrace();
			if(sc != null){
				try{
					sc.close();
				}catch(IOException xx){}
			}
			return null;
		}
	}
	
	public void run(){
		while(true){
			if(this.isInterrupted()){
				System.out.println("Connector be stopped");
				return;							
			}

			InetSocketAddress addr = new InetSocketAddress(localserviceip, localserviceport);
			SocketChannel soc = null;
			try{
				soc = connect(addr);
				if(soc == null){
					sleep(1000);
					continue;
				}
			}catch(Exception e){
				try {
					sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
			System.out.println("WiplugConnector Connected...");
			Selector sel;
			try {
					sel = Selector.open();
					soc.register(sel, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
					while(true){
						if(this.isInterrupted()){
							System.out.println("Connector be stopped");
							return;							
						}
						
						try {
							sleep(50);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}

						int n = sel.select();
						if(n <= 0){
							try {
								sleep(1);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}							
							continue;
						}
						SelectionKey sk = sel.selectedKeys().iterator().next();
						//for(Iterator<SelectionKey> i = sel.selectedKeys().iterator(); i.hasNext();){
							//SelectionKey sk = (SelectionKey)i.next();
							//i.remove();
						SocketChannel sic = (SocketChannel)sk.channel();
						if(sk.isReadable()){
							//read data from socket
							ByteBuffer bb = ByteBuffer.allocate(2048);
							int ret = sic.read(bb);
							if(ret < 0){
								System.out.println("socket read error");
								sic.close();
								break;
							}
							else if(ret == 0){
								System.out.println("socket closed");
								sic.close();
								break;
							}
							//read some data
							System.out.println("socket read:"+String.valueOf(ret));
							if(bb.position() > 0){
								recvData(bb);
							}
						}
						
						if(sk.isWritable()){
							WiplugCmd cmd;
							cmd = getSendCmd();
							if(cmd != null){
								System.out.println("WiplugConnector Send Command:"+cmd.maction);
								byte[] writebuf = cmd.toBuffer();
								ByteBuffer buf = ByteBuffer.allocate(writebuf.length);
								buf.put(writebuf);
								buf.position(0);
								int ret = sic.write(buf);
								if(ret < 0){
									System.out.println("socket write error");
									break;
								}
								//System.out.println("socket write:"+ret);
							}
						} 
						sel.selectedKeys().clear();
						//} 
					}
				} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					soc.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}				
		}
	} //end run		

	public static void main(String args[]) {
		WiplugConnector connector = new WiplugConnector();
		connector.setBaseServiceAddress("127.0.0.1", 22333);
		connector.start();
		while(true){
			try{
				sleep(1000);
			}catch(Exception e){
				continue;
			}
		}
	}
}	