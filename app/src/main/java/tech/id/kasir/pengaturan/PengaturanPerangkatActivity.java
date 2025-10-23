package tech.id.kasir.pengaturan;

import static tech.id.kasir.utility.btt.UtilBluetooth.CONNECTING_STATUS;
import static tech.id.kasir.utility.btt.UtilBluetooth.MESSAGE_READ;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import tech.id.kasir.R;
import tech.id.kasir.utility.btt.DaftarBluetoothAdapter;
import tech.id.kasir.utility.btt.ModelListBluetooth;
import tech.id.kasir.utility.btt.SintaksPOST;
import tech.id.kasir.utility.btt.UtilBluetooth;

public class PengaturanPerangkatActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SCAN = 101;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private Switch switchOnOffBluetooth;
    private TableRow cariPerangkat, ujiPrinter;
    private ProgressBar progressBarSearchBluetooth;
    private RecyclerView rvListBluetooth;
    private TextView pengaturan_bluetooth_status;
    private ImageView ivKembaliDariPengaturanPerangkat;
    private TextInputEditText tietJumlahMeja, tietTax;

    private ArrayList<ModelListBluetooth> listBluetooth = new ArrayList<>();
    private ArrayList<String> listNameBluetooth = new ArrayList<>();
    private ArrayList<String> listAddressBluetooth = new ArrayList<>();
    private DaftarBluetoothAdapter listBluetoothAdapter;

    private Handler mHandler;
    private SintaksPOST sintaksPOST = new SintaksPOST();
    private UtilBluetooth utilBluetooth = new UtilBluetooth();

    private String barcode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan_perangkat);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initViews();
        initHandler();
        initSwitch();
        initClickListeners();
    }

    private void initViews() {
        switchOnOffBluetooth = findViewById(R.id.switchOnOffBluetooth);
        cariPerangkat = findViewById(R.id.cariPerangkat);
        ujiPrinter = findViewById(R.id.ujiPrinter);
        progressBarSearchBluetooth = findViewById(R.id.progressBarSearchBluetooth);
        rvListBluetooth = findViewById(R.id.rvListBluetooth);
        pengaturan_bluetooth_status = findViewById(R.id.pengaturan_bluetooth_status);
        ivKembaliDariPengaturanPerangkat = findViewById(R.id.ivKembaliDariPengaturanPerangkat);
        tietJumlahMeja = findViewById(R.id.tietJumlahMeja);
        tietTax = findViewById(R.id.tietTax);
    }

    private void initHandler() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        pengaturan_bluetooth_status.setText((CharSequence) msg.obj);
                    } else {
                        pengaturan_bluetooth_status.setText("Bluetooth gagal terhubung");
                    }
                }
                if (msg.what == MESSAGE_READ) {
                    String readMessage = new String((byte[]) msg.obj);
                    // Bisa ditampilkan di TextView jika perlu
                }
            }
        };
    }

    private void initSwitch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_CODE_SCAN);
                return;
            }
        }

        switchOnOffBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!bluetoothAdapter.isEnabled()) enableBluetooth();
            } else {
                if (bluetoothAdapter.isEnabled()) bluetoothAdapter.disable();
            }
        });
    }

    private void initClickListeners() {
        ivKembaliDariPengaturanPerangkat.setOnClickListener(v -> finish());

        cariPerangkat.setOnClickListener(v -> {
            progressBarSearchBluetooth.setVisibility(android.view.View.VISIBLE);
            listPairedDevices();
        });

        ujiPrinter.setOnClickListener(v -> {
            if (isConnected()) {
                try {
                    outputStream = bluetoothSocket.getOutputStream();
                    sintaksPOST.bill(this, bluetoothSocket, outputStream);
                    openCashDrawer();
                } catch (IOException e) {
                    Toast.makeText(this, "OutputStream error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Belum Terhubung", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        return true;

    }

    private void requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    private void enableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_CODE_SCAN);
                return;
            }
        }
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void listPairedDevices() {
//        if (!hasBluetoothPermission()) {
//            requestBluetoothPermission();
//            return;
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_CODE_SCAN);
                return;
            }
        }

        listNameBluetooth.clear();
        listAddressBluetooth.clear();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                listNameBluetooth.add(device.getName());
                listAddressBluetooth.add(device.getAddress());
            }
        }

        listBluetooth.clear();
        for (int i = 0; i < listNameBluetooth.size(); i++) {
            ModelListBluetooth model = new ModelListBluetooth();
            model.setNama_bluetooth(listNameBluetooth.get(i));
            model.setAddress_bluetooth(listAddressBluetooth.get(i));
            listBluetooth.add(model);
        }

        showRecyclerList();
    }

    private void showRecyclerList() {
        rvListBluetooth.setLayoutManager(new LinearLayoutManager(this));
        listBluetoothAdapter = new DaftarBluetoothAdapter(listBluetooth);
        rvListBluetooth.setAdapter(listBluetoothAdapter);
        progressBarSearchBluetooth.setVisibility(android.view.View.GONE);

        listBluetoothAdapter.setOnItemClickCallback(data -> connectToDevice(data.getAddress_bluetooth(), data.getNama_bluetooth()));
    }

    private void connectToDevice(String address, String name) {
//        if (!hasBluetoothPermission()) {
//            requestBluetoothPermission();
//            return;
//        }

        new Thread(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.BLUETOOTH_SCAN},
                                REQUEST_CODE_SCAN);
                        return;
                    }
                }
                if (isConnected()) closeConnection();


                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(BT_MODULE_UUID);
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();

                runOnUiThread(() -> {
                    pengaturan_bluetooth_status.setText(name);
                    Toast.makeText(this, "Terhubung ke " + name, Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Gagal koneksi ke " + name, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void closeConnection() {
        try {
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException ignored) {}
    }

    public void openCashDrawer() {
        if (!isConnected()) return;
        try {
            byte[] cmd = new byte[]{27, 112, 0, 50, (byte) 250};
            outputStream.write(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_DOWN) {
            char pressedKey = (char) e.getUnicodeChar();
            barcode += pressedKey;
        }
        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            pengaturan_bluetooth_status.setText(barcode);
            barcode = "";
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            switchOnOffBluetooth.setChecked(bluetoothAdapter.isEnabled());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchOnOffBluetooth.setChecked(bluetoothAdapter.isEnabled());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SCAN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // izin scan diberikan, lanjutkan scan
                listPairedDevices(); // misal
            } else {
                Toast.makeText(this, "Izin scan Bluetooth dibutuhkan", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

