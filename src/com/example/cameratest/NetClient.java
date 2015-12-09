package com.example.cameratest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetClient {
	private InetAddress    DstAddress;
	private short		   DstPort;
	private DatagramSocket ds = null;  
    public NetClient(final String  dsthost,short port) throws SocketException, UnknownHostException{
    	 ds = new DatagramSocket();   
    	 DstAddress = InetAddress.getByName(dsthost);
    	 DstPort = port;
    }
    public void Send(final byte[] data,int size) throws IOException{
    	DatagramPacket dp = new DatagramPacket(data,size, DstAddress,DstPort);
    	ds.send(dp);
    }
}
