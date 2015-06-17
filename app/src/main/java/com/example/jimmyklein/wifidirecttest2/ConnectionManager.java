package com.example.jimmyklein.wifidirecttest2;

import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionManager{

    private Socket socket;
    private ServerSocket sSocket;
    private static String TAG = "ConnectionManager";
    public ConnectionManager(){

    }
    
    public ConnectionManager(int socket){
	    try {
            sSocket = new ServerSocket(socket);
        }catch(java.io.IOException e){
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
