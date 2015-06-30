package com.example.jimmyklein.wifidirecttest2;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectionManager{

    private Socket server;
    private ServerSocket serverSocket;
    private static String TAG = "ConnectionManager";
    private ArrayList<Socket> clients;

    //We are the client
    public ConnectionManager(final InetAddress host){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    server = new Socket(host, 8988);
                    server.setReuseAddress(true);
                }catch(java.io.IOException e){
                    Log.d(TAG, e.toString());
                }
            }
        }).start();

    }

    //We are the host
    public ConnectionManager(){
	    new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(8988);
                    serverSocket.setReuseAddress(true);
                }catch(java.io.IOException e){
                    Log.d(TAG, e.toString());
                }
            }
        }).start();

    }

    public void serverConnect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(clients==null){
                        clients = new ArrayList<>();
                    }
                    clients.add(serverSocket.accept());
                    if(clients.size()>0) {
                        serverListen(clients.get(clients.size() - 1));
                    }
                }catch(java.io.IOException e){
                    Log.d(TAG, e.toString());
                }
            }
        }).start();
    }

    public void clientListen(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean notReceived = true;
                while (notReceived) {
                    try {
                        InputStream stream = server.getInputStream();
                        byte[] data = new byte[64];
                        int count = stream.read(data);
                        if(count==64){
                            notReceived = false;
                        }
                    } catch (java.io.IOException e) {
                        Log.d(TAG, e.toString());
                    }
                }
            }
        }).start();
    }

    public void serverListen(final Socket socket){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isListening = true;
                while(isListening) {
                    try {
                        InputStream stream = socket.getInputStream();
                        byte[] data = new byte[64];
                        int count = stream.read(data);
                        if(count==64){
                            if ((socket == clients.get(0)) && (clients.size() > 1)) {
                                serverSendPing(clients.get(1));
                                isListening=false;
                            } else if ((socket == clients.get(1)) && (clients.size() > 1)) {
                                serverSendPing(clients.get(0));
                                isListening=false;
                            }
                        }
                    } catch (java.io.IOException e) {
                        Log.d(TAG, e.toString());
                    }
                }
            }
        }).start();
    }

    public void closeServerConnection(){
        try {
            serverSocket.close();
        }catch(java.io.IOException e){
            Log.d(TAG, e.toString());
        }
    }

    public void clientSendPing(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data = getByteArray();
                    DataOutputStream dataOutputStream = new DataOutputStream(server.getOutputStream());
                    dataOutputStream.write(data,0,64);
                    dataOutputStream.close();
                }catch(java.io.IOException e){
                    Log.d(TAG, e.toString());
                }
            }
        }).start();
    }

    private void serverSendPing(final Socket otherClient){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    byte[] data = getByteArray();
                    DataOutputStream dataOutputStream = new DataOutputStream(otherClient.getOutputStream());
                    dataOutputStream.write(data,0,64);
                    dataOutputStream.close();
                }catch(java.io.IOException e){
                    Log.d(TAG, e.toString());
                }
            }
        }).start();
    }

    private byte[] getByteArray(){
	byte[] tmp = new byte[64];

	for(int i=0;i<64;i++){
	    tmp[i]=0xF;
	}
	return tmp;
    }
}
