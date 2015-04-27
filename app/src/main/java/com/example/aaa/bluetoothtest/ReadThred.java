package com.example.aaa.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;


/**
 * Created by aaa on 15-4-22.
 */
public class ReadThred extends Thread {
    private BluetoothSocket socket;
    private Handler handler;
 public ReadThred(BluetoothSocket socket,Handler handler)
 {
     this.socket=socket;
     this.handler=handler;
 }




    @Override
    public void run() {
        super.run();
       BluetoothDevice device= socket.getRemoteDevice();
      String utf;


        DataInputStream stream= null;
        try {
            stream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while((utf=stream.readUTF())!=null)
            {
                String msg=utf+"";
                Log.d("sss",device.getName());
                handler.obtainMessage(1,msg).sendToTarget();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
