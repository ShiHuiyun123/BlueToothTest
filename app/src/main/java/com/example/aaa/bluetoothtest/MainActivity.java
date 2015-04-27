package com.example.aaa.bluetoothtest;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{
private static final String uuid="4e3a500b-1ba9-4c3f-a5fe-76cb46608b5f";
    private BluetoothAdapter bluetoothAdapter;
     private DeviceAdapter adapter;
    private RecyclerView recycler;
    private DeviceReceiver receiver;
    private  BluetoothServerSocket server;
    private Map<BluetoothDevice,BluetoothSocket> socketMap=new HashMap<>();
private Handler handler=new Handler(){
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what)
        {
            case 0:
               BluetoothDevice device= msg.getData().getParcelable(BluetoothDevice.EXTRA_DEVICE);
                adapter.add(device);
                break;
            case 1:
              Toast.makeText(MainActivity.this,(String)msg.obj,Toast.LENGTH_SHORT).show();
                break;

        }
    }
};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获得蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        recycler= ((RecyclerView) findViewById(R.id.recycler));
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(this,new ArrayList<BluetoothDevice>());
        recycler.setAdapter(adapter);

        if(bluetoothAdapter==null)
        {
            Toast.makeText(this,"本设备不具备蓝牙模块",Toast.LENGTH_SHORT).show();
            finish();
        }
        //  检测蓝牙是否开启            isEnabled（）:开启状态
        if (!bluetoothAdapter.isEnabled())
        {
            //开启方式两种 1：意图 2.直接用代码开启
            //调用开启方法
           // bluetoothAdapter.enable();
            //通过意图开启
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
        }
 else
        {
            //getBondedDevices（）获得已配对的设备列表
            adapter.addAll(bluetoothAdapter.getBondedDevices());
            discovery();
        }
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private void discovery()
    {
        //开始扫描  通过广播找到蓝牙设备  扫描的时候广播，
        bluetoothAdapter.startDiscovery();
        receiver = new DeviceReceiver(handler);
        //找到蓝牙设备的广播  就通知一次广播
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);

       //耗时操作，不可再主线程工作
        try {
            server= bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("qq", UUID.fromString(uuid));
    new  Thread()
   {
    @Override
    public void run() {
        try {
            BluetoothSocket socket= server.accept();
       while((socket=server.accept())!=null)
       {
           //打印谁发送来的数据
          BluetoothDevice device= socket.getRemoteDevice();
         while((socket=server.accept())!=null)
         {
             BluetoothDevice device1=socket.getRemoteDevice();
             Bundle bundle=new Bundle();
             bundle.putParcelable(BluetoothDevice.EXTRA_DEVICE,device1);
            Message message=  handler.obtainMessage();
             message.setData(bundle);
             message.sendToTarget();
           socketMap.put(device,socket);
             //发起一个连接
             socket.connect();
             new ReadThred(socket,handler).start();
             Log.d("BlutoothSocket",device.getName());

             //打印数据
             DataInputStream stream= new DataInputStream(socket.getInputStream());
             Log.d("BlutoothSocket",stream.readUTF());
         }


       }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
      }.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver!=null)
        {
            unregisterReceiver(receiver);
        }
        if(server!=null)
        {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
      Set<Map.Entry<BluetoothDevice,BluetoothSocket>>set= socketMap.entrySet();
for(Map.Entry<BluetoothDevice,BluetoothSocket>ss:set)
{
    try {
        ss.getValue().close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK)
        {
            Toast.makeText(this,"开启成功",Toast.LENGTH_SHORT).show();
            //开启成功后扫描
            discovery();
        }
        else
        {
            Toast.makeText(this,"开启失败",Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public void onClick(View v) {
  int position=recycler.getChildPosition(v);
       final BluetoothDevice item=adapter.getItem(position);
     /*   ParcelUuid[]uuids=item.getUuids();
        for (ParcelUuid uuid:uuids)
        {
            Log.d("BluetoothDevice",uuid.toString());
        }*/
        new Thread()
        {
            @Override
            public void run() {
            BluetoothSocket socket;
                try {

                  BluetoothSocket socket1=socketMap.get(item);
                    if(socket1==null)
                    {
                        socket1 = item.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
                        socket1.connect();
                        socketMap.put(item,socket1);
                    }





                    DataOutputStream   stream = new DataOutputStream(socket1.getOutputStream());

                    stream.writeUTF("发送测试");

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}
