
package com.example.android.wifidirect.discovery;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class ChatManager implements Runnable {
    private Context context;
    private Socket socket = null;
    private Handler handler;
    private  static ArrayList<Socket> socketList = new ArrayList<Socket>();
    private boolean isGroupOwner;
    public ChatManager(Socket socket, Handler handler,boolean isGroupOwner , Context context) {
        this.context = context;
        this.isGroupOwner = isGroupOwner;
        if(isGroupOwner)
        {
            this.socketList.add(socket);
            Log.i("Length of socketlist","" + socketList.size());
        }

            this.socket = socket;

        this.handler = handler;
    }
    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "ChatHandler";

    @Override
    public void run() {
        try {

            byte[] buffer = new byte[1024];

            if(isGroupOwner)
            {
                /////
                Log.i("i AM IN GROUP OWNER","......");
                Socket socket = this.socket;

                if(socket!= null)
                Log.i("i AM IN GROUP OWNER","......");

                try {
                    socket = this.socket;
                    //iStream = socket.getInputStream();
                    //oStream = socket.getOutputStream();

                    int bytes;
                    handler.obtainMessage(WiFiServiceDiscoveryActivity.MY_HANDLE, this)
                            .sendToTarget();
                    String filename  = receiveFile(socket,this.context);

                    for(int i = 0 ; i < socketList.size();i++)
                    {
                        Log.i("Length of socketlist","......" + i);

                        if(socketList.get(i)==socket)
                            continue;

                        oStream = socketList.get(i).getOutputStream();

                        sendFile(socketList.get(i),filename,oStream);
                        //   oStream.write(buffer);
                    }

                    /*while (true) {
                        try {
                            // Read from the InputStream
                            //bytes = iStream.read(buffer);
                            //if (bytes == -1) {
                              //  Log.i("i got breaked",":)");
                               // break;

                           // }
                            String filename  = receiveFile(socket,this.context);

                            for(int i = 0 ; i < socketList.size();i++)
                            {
                                Log.i("Length of socketlist","......" + i);

                                if(socketList.get(i)==socket)
                                    continue;

                                oStream = socketList.get(i).getOutputStream();

                                sendFile(socketList.get(i),filename,oStream);
                             //   oStream.write(buffer);

                            }
                            

                            // Send the obtained bytes to the UI Activity
                            Log.d(TAG, "Rec:" + String.valueOf(buffer));
                            //handler.obtainMessage(WiFiServiceDiscoveryActivity.MESSAGE_READ,
                              //      bytes, -1, buffer).sendToTarget();
                        }catch (IOException e) {
                            Log.e(TAG, "disconnected", e);
                        }
                    } */
                }
                catch(IOException e) {
                }
                ///-------
                }
            else
            {
                //iStream = socket.getInputStream();
                oStream = socket.getOutputStream();
                buffer = new byte[1024];
                int bytes;
                handler.obtainMessage(WiFiServiceDiscoveryActivity.MY_HANDLE, this)
                        .sendToTarget();
                receiveFile(socket,this.context);
                /* while (true) {
                    try {
                        // Read from the InputStream
                        bytes = iStream.read(buffer);
                        if (bytes == -1) {
                            break;
                        }

                        // Send the obtained bytes to the UI Activity
                        Log.d(TAG, "Rec:" + String.valueOf(buffer));
                        handler.obtainMessage(WiFiServiceDiscoveryActivity.MESSAGE_READ,
                                bytes, -1, buffer).sendToTarget();
                    } catch (IOException e) {
                        Log.e(TAG, "disconnected", e);
                    }
                }*/

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {

                if(socket== null)
                    Log.i("Socket is null","......");
                if(socket != null)
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] buffer) {

        String FileURL = new String(buffer);
        if(isGroupOwner) {

            for (int i = 0; i < socketList.size(); i++) {

                try {
                    Socket socket = socketList.get(i);

                    OutputStream oStream = socket.getOutputStream();
                    //oStream.flush();
                    sendFile(socket,FileURL,oStream);
                    //oStream.write(buffer);
                    //oStream.flush();

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                }

            }
        }
        else
        {
            /*
            try {
                //oStream.write(buffer);
                sendFile(this.socket,FileURL,oStream);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }*/
            sendFile(this.socket,FileURL,oStream);
        }
    }
    public String receiveFile(Socket socket, Context context)
    {
        String fileName = "";
        int bytesRead;
        int current = 0;
        try {


            InputStream in = socket.getInputStream();

            DataInputStream clientData = new DataInputStream(in);

            fileName = clientData.readUTF();
            OutputStream output = new FileOutputStream("" +Environment.getExternalStorageDirectory() + "/"+ fileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            // Closing the FileOutputStream handle
            in.close();
            clientData.close();
            output.close();
        }
        catch(IOException e) {
            Log.i("Receive file" , "receiveing error");
        }
        return fileName;
    }
//    FileURL = "" +Environment.getExternalStorageDirectory() + "/"
  //          + "Pictures/AayushiJain.pdf";

    void sendFile(Socket sock, String FileURL, OutputStream stream) {
        File myFile = new File("" +Environment.getExternalStorageDirectory() + "/"
                + "Pictures/AayushiJain.pdf");
        byte[] mybytearray = new byte[(int) myFile.length()];
        try {
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            //bis.read(mybytearray, 0, mybytearray.length);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            OutputStream os = sock.getOutputStream();

            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();

            //Sending file data to the server
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
        }
        catch(IOException e )
        {
            Log.i("Sending file","IOExceprion" );
        }

    }
}
