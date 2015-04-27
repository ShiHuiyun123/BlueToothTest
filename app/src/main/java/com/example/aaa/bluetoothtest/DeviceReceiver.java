package com.example.aaa.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * Created by aaa on 15-4-22.
 */
public class DeviceReceiver extends BroadcastReceiver {
    private Handler handler;

    public DeviceReceiver(Handler handler) {
        this.handler = handler;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        //显示意图信息
        Log.d("DeviceReceiver",intent.toString());
        //获取额外信息   蓝牙设备
        BluetoothDevice extra=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.d("DeviceReceiver",extra.getName()+"");
        Log.d("DeviceReceiver",extra.getAddress());
        //通过handler发送消息
        Message message=handler.obtainMessage(0);
         message.setData(intent.getExtras());
        message.sendToTarget();
    }
}
