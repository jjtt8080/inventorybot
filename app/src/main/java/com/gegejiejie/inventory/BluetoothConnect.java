package com.gegejiejie.inventory;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;


public class BluetoothConnect extends AsyncTask<String, String, String> {
    public static final String MY_UUID =  "00001101-0000-1000-8000-00805f9b34fb";
    static public final int REQUEST_ENABLE_BT = 100;
    private static final String TAG = BluetoothConnect.class.getSimpleName();
    private String result = "";
    private String previousResult = "";
    private AsyncListener listener;

    BluetoothConnect(AsyncListener c) {
        listener = c;

    }
    @Override
    protected String doInBackground(String... params)
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
           return null;
        }

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        BluetoothDevice device = null;
        String address;
        for (BluetoothDevice p  : pairedDevices) {
            if (p.getName().contains("BarCode")) {
                device = p;
                address = p.getAddress();
                break;
            }
        }
        if (device == null) {
            return null;
        }
        try
        {

            ParcelUuid[] uuids = device.getUuids();


            for (ParcelUuid p : uuids) {
                String uuidString = p.toString();
                Log.e(TAG, uuidString);

            }
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            socket.connect();
            btAdapter.cancelDiscovery();

            InputStream stream = socket.getInputStream();
            int read = 0;
            byte[] buffer = new byte[128];
            String data = "";
            do
            {
                try
                {
                    read = stream.read(buffer);
                    data = new String(buffer, 0, read);
                    publishProgress(data);
                }
                catch(Exception ex)
                {
                    read = -1;
                }
            }
            while (read > 0);
            result = previousResult + data;
            Log.e(TAG,"result: " + result);
            return result;

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(String... values)
    {
        if (values.length == 0)
            return;
        if (values[0].equals("\r"))
        {
            result = previousResult + values[0];
            previousResult = "";

        }
        else {
            result = values[0];

        }
        super.onProgressUpdate(values);
        listener.processResult(result);
        return;
    }

    public void onPostExecute(String r) {
        listener.processResult(r);
    }
}
