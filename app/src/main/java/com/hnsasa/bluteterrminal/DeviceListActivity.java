package com.hnsasa.bluteterrminal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.Manifest;

public class DeviceListActivity extends AppCompatActivity {
    private final String [] blucon = {Manifest.permission.BLUETOOTH_CONNECT};
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> deviceList = new ArrayList<>();
    private BluetoothDevice hc06Device;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        ListView listView = findViewById(R.id.deviceListView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listView.setAdapter(arrayAdapter);

        // Eşleşmiş cihazları listeleme
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            requestPermissions(blucon,1);
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device.getName() + "\n" + device.getAddress());
            }
            arrayAdapter.notifyDataSetChanged();
        }

        // Cihaz seçimi
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = deviceList.get(position);
                String deviceName = selectedItem.split("\n")[0];
                String deviceAddress = selectedItem.split("\n")[1];
                hc06Device = bluetoothAdapter.getRemoteDevice(deviceAddress);

                try {
                    if (ActivityCompat.checkSelfPermission(DeviceListActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        requestPermissions(blucon,1);
                        return;
                    }
                    BluetoothSocket bluetoothSocket = hc06Device.createRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothSocket.connect();
                    ((MyApplication) getApplication()).setBluetoothSocket(bluetoothSocket);

                    // Cihaz adı ve adresini geri gönder
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("deviceName", deviceName);
                    resultIntent.putExtra("deviceAddress", deviceAddress);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(DeviceListActivity.this, "Bağlantı başarısız!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
