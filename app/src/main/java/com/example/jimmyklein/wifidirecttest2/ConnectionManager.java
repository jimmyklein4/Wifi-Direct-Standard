package com.example.jimmyklein.wifidirecttest2;

import android.util.Log;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionManager{

    private Socket client;
    private byte[] ping;
    private ServerSocket sSocket;
    private static String TAG = "ConnectionManager";

    public ConnectionManager(){ }

    
    public ConnectionManager(int port){
	    try {
            sSocket = new ServerSocket(port);
            sSocket.setReuseAddress(true);
        }catch(java.io.IOException e){
            Log.d(TAG, e.toString());
        }
    }

    public void setPort(int port){
        try {
            sSocket.close();
            sSocket = new ServerSocket(port);
            sSocket.setReuseAddress(true);
        }catch(java.io.IOException e){
            Log.d(TAG, e.toString());
        }
    }

    public InetAddress Listen(){
        try {
            client = sSocket.accept();
            ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
            try{
                Object object = objectInputStream.readObject();
                if(object.getClass().equals(byte.class)){
                    return client.getInetAddress();
                }
            }catch(ClassNotFoundException f){
                Log.d(TAG, f.toString());
            }
        }catch(java.io.IOException e){
            Log.d(TAG, e.toString());
        }
        return null;
    }

    public void CloseServerConnection(){
        try {
            sSocket.close();
        }catch(java.io.IOException e){
            Log.d(TAG, e.toString());
        }
    }

    public void SendPing(InetAddress addr, int port){
        Socket socket = new Socket();
        try {
            socket.connect((new InetSocketAddress(addr, port)), 5000);
            socket.setReuseAddress(true);
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(getByteArray());
            oos.close();
            os.close();
            socket.close();
        }catch(java.io.IOException e){
            Log.d(TAG, e.toString());
        }
    }

    public void SendObject(InetAddress addr, int port, Object obj){
        Socket socket = new Socket();
        try{
            socket.connect((new InetSocketAddress(addr, port)),5000);
            socket.setReuseAddress(true);
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(obj);
            oos.close();
            os.close();
            socket.close();
        } catch(java.io.IOException e){
            Log.d(TAG, e.toString());
        }
    }

    private byte[] getByteArray(){
	byte[] tmp = new byte[64];

	for(int i=0;i<64;i++){
	    tmp[i]=0xF;
	}
	return tmp;
    }
}
