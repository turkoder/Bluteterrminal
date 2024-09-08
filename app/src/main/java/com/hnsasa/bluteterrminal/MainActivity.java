package com.hnsasa.bluteterrminal;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private TextView connectedDeviceName;
    private Button connectButton;
    private EditText messageText;

    // ActivityResultLauncher ile yeni API'yi kullanıyoruz
    private final ActivityResultLauncher<Intent> deviceListLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String deviceAddress = data.getStringExtra("deviceAddress");
                    String deviceName = data.getStringExtra("deviceName");

                    if (deviceAddress != null) {
                        try {
                            bluetoothSocket = ((MyApplication) getApplication()).getBluetoothSocket();
                            if (bluetoothSocket != null) {
                                outputStream = bluetoothSocket.getOutputStream();
                                Toast.makeText(MainActivity.this, "Cihaz Bağlandı: " + deviceName, Toast.LENGTH_SHORT).show();

                                // Bağlanılan cihazın adını göster
                                connectedDeviceName.setText(deviceName);
                                connectedDeviceName.setVisibility(View.VISIBLE);

                                // Bağlan butonunun rengini yeşil yap
                                connectButton.setText("Bağlı");
                                //connectButton.setBackgroundResource(R.drawable.button_green);
                                //connectButton.setBackgroundColor(Color.parseColor("#4CAF50"));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectedDeviceName = findViewById(R.id.connectedDeviceName);
        connectButton = findViewById(R.id.connectButton);
        Button sendButton = findViewById(R.id.sendButton);
        messageText = findViewById(R.id.messageText);

        // Başlangıçta bağlantı yok, buton rengi kırmızı
        //connectButton.setBackgroundResource(R.drawable.button_red);
        //connectButton.setBackgroundColor(Color.parseColor("#FF0000"));
        connectButton.setText("Bağlan");
        // Bağlan butonuna tıklayınca cihaz listesini aç
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                deviceListLauncher.launch(intent); // Yeni API'yi kullanıyoruz

            }
        });

        // Mesaj gönder butonuna tıklayınca
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageText.getText().toString();
                if (!message.isEmpty()) {
                    sendBluetoothSignal(message);
                } else {
                    Toast.makeText(MainActivity.this, "Lütfen bir mesaj yazın", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Bluetooth mesajı gönderme
    private void sendBluetoothSignal(String signal) {
        try {
            if (outputStream != null) {
                outputStream.write(signal.getBytes());
                Toast.makeText(this, "Mesaj gönderildi: " + signal, Toast.LENGTH_SHORT).show();
                messageText.setText("");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Mesaj gönderme hatası", Toast.LENGTH_SHORT).show();
        }
    }
}
