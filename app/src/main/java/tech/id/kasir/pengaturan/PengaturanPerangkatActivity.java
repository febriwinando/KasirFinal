//package tech.id.kasir.pengaturan;
//
//import static tech.id.kasir.utility.btt.UtilBluetooth.BT_MODULE_UUID;
//import static tech.id.kasir.utility.btt.UtilBluetooth.CONNECTING_STATUS;
//import static tech.id.kasir.utility.btt.UtilBluetooth.MESSAGE_READ;
//
//import android.Manifest;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.View;
//import android.widget.CompoundButton;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.Switch;
//import android.widget.TableRow;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.window.OnBackInvokedDispatcher;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//import androidx.core.graphics.Insets;
//import androidx.core.os.BuildCompat;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.textfield.TextInputEditText;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Set;
//import java.util.UUID;
//
//import tech.id.kasir.R;
//import tech.id.kasir.utility.btt.BluetoothHelper;
//import tech.id.kasir.utility.btt.BluetoothPermissionManager;
//import tech.id.kasir.utility.btt.CashDrawerHelper;
//import tech.id.kasir.utility.btt.ConnectedThread;
//import tech.id.kasir.utility.btt.DaftarBluetoothAdapter;
//import tech.id.kasir.utility.btt.ModelListBluetooth;
//import tech.id.kasir.utility.btt.SintaksPOST;
//import tech.id.kasir.utility.btt.UtilBluetooth;
//
//public class PengaturanPerangkatActivity extends AppCompatActivity {
//
//    // --- UI Components ---
//    private TableRow cariPerangkat, ujiPrinter;
//    private ImageView ivKembaliDariPengaturanPerangkat;
//    private TextInputEditText tietJumlahMeja, tietTax;
//    private ProgressBar progressBarSearchBluetooth;
//    private RecyclerView rvListBluetooth;
//    private Switch switchOnOffBluetooth;
//    private TextView pengaturan_bluetooth_status;
//
//    // --- Bluetooth Components ---
//    private ArrayList<ModelListBluetooth> listBluetooth = new ArrayList<>();
//    private DaftarBluetoothAdapter listBluetoothAdapter;
//    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    public static BluetoothSocket bluetoothSocket;
//    public static OutputStream outputStreamer;
//    private static final int REQUEST_ENABLE_BT = 1;
//
//    public static ConnectedThread mConnectedThread;
//    public static Handler mHandler;
//
//    private SintaksPOST sintaksPOST = new SintaksPOST();
//    private String barcode = "";
//    UtilBluetooth utilBluetooth = new UtilBluetooth();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_pengaturan_perangkat);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        initViews();
//
//        // Pastikan izin Bluetooth diberikan
//        BluetoothPermissionManager.ensurePermissions(this, 100);
//
////        setupHandler();
//        setupSwitch();
//        setupListeners();
//
//        // Jika pernah terhubung sebelumnya → auto reconnect
////        boolean reconnected = BluetoothHelper.autoConnectIfAvailable(
////                this, bluetoothAdapter, mHandler, BT_MODULE_UUID);
////        if (reconnected) {
////            pengaturan_bluetooth_status.setText("Tersambung otomatis ke perangkat terakhir");
////        }
//
//
//        ivKembaliDariPengaturanPerangkat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//
//        if (BuildCompat.isAtLeastT()) {
//            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
//                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
//                    this::finish
//            );
//        }
//
//    }
//
//
//    private void initViews() {
//        cariPerangkat = findViewById(R.id.cariPerangkat);
//        ujiPrinter = findViewById(R.id.ujiPrinter);
//        progressBarSearchBluetooth = findViewById(R.id.progressBarSearchBluetooth);
//        rvListBluetooth = findViewById(R.id.rvListBluetooth);
//        pengaturan_bluetooth_status = findViewById(R.id.pengaturan_bluetooth_status);
//        ivKembaliDariPengaturanPerangkat = findViewById(R.id.ivKembaliDariPengaturanPerangkat);
//        tietJumlahMeja = findViewById(R.id.tietJumlahMeja);
//        tietTax = findViewById(R.id.tietTax);
//        switchOnOffBluetooth = findViewById(R.id.switchOnOffBluetooth);
//    }
//
////    private void setupHandler() {
////        mHandler = new Handler(Looper.getMainLooper()) {
////            @Override
////            public void handleMessage(Message msg) {
////                if (msg.what == MESSAGE_READ) {
////                    String readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
////                    Log.d("Bluetooth", "Pesan diterima: " + readMessage);
////                }
////                if (msg.what == CONNECTING_STATUS) {
////                    if (msg.arg1 == 1) {
////                        pengaturan_bluetooth_status.setText((CharSequence) msg.obj);
////                    } else {
////                        pengaturan_bluetooth_status.setText("Gagal Menghubungkan Bluetooth");
////                    }
////                }
////            }
////        };
////    }
//
//    private void setupSwitch() {
//        switchOnOffBluetooth.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
//            if (isChecked) {
//                if (!bluetoothAdapter.isEnabled()) {
//                    nyalakanBluetooth();
//                } else {
//                    Toast.makeText(this, "Bluetooth Telah Aktif", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                switchOnOffBluetooth.setChecked(true); // tidak bisa dimatikan dari app
//            }
//        });
//    }
//
//    private void setupListeners() {
//        cariPerangkat.setOnClickListener(v -> {
//            progressBarSearchBluetooth.setVisibility(View.VISIBLE);
//            listPairedDevices();
//        });
//
//        ujiPrinter.setOnClickListener(v -> {
//            if (utilBluetooth.isConnected(bluetoothSocket)) {
//                try {
//                    outputStreamer = bluetoothSocket.getOutputStream();
//                    sintaksPOST.bill(this, bluetoothSocket, outputStreamer);
//                    CashDrawerHelper.open(outputStreamer);
//                    Toast.makeText(this, "Uji printer berhasil", Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    Toast.makeText(this, "Gagal mengirim ke printer", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(this, "Belum Terhubung ke perangkat", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void listPairedDevices() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                == PackageManager.PERMISSION_DENIED) {
//            BluetoothPermissionManager.ensurePermissions(this, 200);
//            return;
//        }
//
//        listBluetooth.clear();
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//
//        if (bluetoothAdapter.isEnabled()) {
//            for (BluetoothDevice device : pairedDevices) {
//                ModelListBluetooth model = new ModelListBluetooth();
//                model.setNama_bluetooth(device.getName());
//                model.setAddress_bluetooth(device.getAddress());
//                listBluetooth.add(model);
//            }
//            showRecyclerList();
//        } else {
//            Toast.makeText(this, "Bluetooth Tidak Menyala", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void showRecyclerList() {
//        rvListBluetooth.setLayoutManager(new LinearLayoutManager(this));
//        listBluetoothAdapter = new DaftarBluetoothAdapter(listBluetooth);
//        rvListBluetooth.setAdapter(listBluetoothAdapter);
//
//        progressBarSearchBluetooth.setVisibility(android.view.View.GONE);
//
//        listBluetoothAdapter.setOnItemClickCallback(data -> {
//            BluetoothPermissionManager.ensurePermissions(this, 201);
//            bluetoothSocket = BluetoothHelper.connectDevice(
//                    this, bluetoothAdapter,
//                    data.getAddress_bluetooth(),
//                    data.getNama_bluetooth(),
//                    mHandler, BT_MODULE_UUID
//            );
//            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
//                Toast.makeText(this, "Terhubung ke " + data.getNama_bluetooth(), Toast.LENGTH_SHORT).show();
//                try {
//                    outputStreamer = bluetoothSocket.getOutputStream();
//                } catch (IOException e) {
//                    Log.e("BT", "Gagal mengambil OutputStream", e);
//                }
//            } else {
//                Toast.makeText(this, "Gagal menghubungkan perangkat", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//    }
//
//    private void nyalakanBluetooth() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
//                == PackageManager.PERMISSION_DENIED) {
//            BluetoothPermissionManager.ensurePermissions(this, 202);
//        }
//
//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_ENABLE_BT) {
//            if (resultCode == RESULT_OK) {
//                switchOnOffBluetooth.setChecked(bluetoothAdapter.isEnabled());
//            } else {
//                nyalakanBluetooth();
//            }
//        }
//    }
//
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent e) {
//        if (e.getAction() == KeyEvent.ACTION_DOWN) {
//            char pressedKey = (char) e.getUnicodeChar();
//            barcode += pressedKey;
//        }
//        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
//            pengaturan_bluetooth_status.setText(barcode);
//            barcode = "";
//        }
//        return super.dispatchKeyEvent(e);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        BluetoothPermissionManager.ensurePermissions(this, 203);
//
//        if (!bluetoothAdapter.isEnabled()) {
//            switchOnOffBluetooth.setChecked(false);
//            nyalakanBluetooth();
//        } else {
//            switchOnOffBluetooth.setChecked(true);
//        }
//    }
//}

package tech.id.kasir.pengaturan;

import static android.content.ContentValues.TAG;
import static tech.id.kasir.utility.btt.UtilBluetooth.CONNECTING_STATUS;
import static tech.id.kasir.utility.btt.UtilBluetooth.MESSAGE_READ;

import android.Manifest;
import android.app.Activity;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import tech.id.kasir.R;
import tech.id.kasir.utility.btt.ConnectedThread;
import tech.id.kasir.utility.btt.DaftarBluetoothAdapter;
import tech.id.kasir.utility.btt.InputConnectThread;
import tech.id.kasir.utility.btt.ModelListBluetooth;
import tech.id.kasir.utility.btt.SintaksPOST;
import tech.id.kasir.utility.btt.UtilBluetooth;

public class PengaturanPerangkatActivity extends AppCompatActivity {

    private TableRow cariPerangkat, ujiPrinter;
    private ImageView ivKembaliDariPengaturanPerangkat;
    private TextInputEditText tietJumlahMeja, tietTax;

    private String barcode = "";
    private ArrayAdapter<String> mBTArrayAdapter;
    UUID sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static Handler mHandler;
    public static BluetoothDevice bluetoothDevice;
    private static final int REQUEST_ENABLE_BT = 1;
    Switch switchOnOffBluetooth;
    public  static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static ConnectedThread mConnectedThread;
    DaftarBluetoothAdapter listBluetoothAdapter;
    UtilBluetooth utilBluetooth = new UtilBluetooth();
    SintaksPOST sintaksPOST = new SintaksPOST();
    ProgressBar progressBarSearchBluetooth;
    private static ArrayList<ModelListBluetooth> listBluetooth = new ArrayList<>();
    static ArrayList<String> listNameBluetooth = new ArrayList<String>();
    static ArrayList<String> listAddressBluetooth = new ArrayList<String>();
    RecyclerView rvListBluetooth;
    public static BluetoothSocket bluetoothSocket = null;
    public static OutputStream outputStreamer;
    TextView pengaturan_bluetooth_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pengaturan_perangkat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        if (ContextCompat.checkSelfPermission(PengaturanPerangkatActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(PengaturanPerangkatActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            }
        }


        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
//                    mReadBuffer.setText(readMessage);
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1){
                        pengaturan_bluetooth_status.setText((CharSequence) msg.obj);
                    }
                    else{
                        pengaturan_bluetooth_status.setText("Gagal Menghubungkan Bluetooth");
                    }
                }
            }
        };

        switchOnOffBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchOnOffBluetooth.isChecked()){
                    if (!bluetoothAdapter.isEnabled()){
                        nyalakanBluetooh();
                    }else{
                        Toast.makeText(PengaturanPerangkatActivity.this, "Bluetooth Telah Aktif", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    switchOnOffBluetooth.setChecked(true);
                }
            }
        });

        cariPerangkat.setOnClickListener(v -> {
                    progressBarSearchBluetooth.setVisibility(View.VISIBLE);
                    listPairedDevices();
                }
        );

        ujiPrinter.setOnClickListener(v -> {
            if (utilBluetooth.isConnected(bluetoothSocket)){
                try {
                    outputStreamer = bluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                sintaksPOST.bill(PengaturanPerangkatActivity.this, bluetoothSocket, outputStreamer);
                openCashDrawer();
                Toast.makeText(this, ""+bluetoothSocket, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Belum Terhubung", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, ""+bluetoothSocket, Toast.LENGTH_SHORT).show();

        });

    }

    private void initViews() {
        cariPerangkat = findViewById(R.id.cariPerangkat);
        ujiPrinter = findViewById(R.id.ujiPrinter);
        progressBarSearchBluetooth = findViewById(R.id.progressBarSearchBluetooth);
        rvListBluetooth = findViewById(R.id.rvListBluetooth);
        pengaturan_bluetooth_status = findViewById(R.id.pengaturan_bluetooth_status);
        ivKembaliDariPengaturanPerangkat = findViewById(R.id.ivKembaliDariPengaturanPerangkat);
        tietJumlahMeja = findViewById(R.id.tietJumlahMeja);
        tietTax = findViewById(R.id.tietTax);
        switchOnOffBluetooth = findViewById(R.id.switchOnOffBluetooth);

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

    private void listPairedDevices(){
        listNameBluetooth.clear();
        listAddressBluetooth.clear();
        if (ContextCompat.checkSelfPermission(PengaturanPerangkatActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(PengaturanPerangkatActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            }
        }

        mBTArrayAdapter.clear();
        Set<BluetoothDevice> mPairedDevices = bluetoothAdapter.getBondedDevices();
        if(bluetoothAdapter.isEnabled()) {
            // put it's one to the adapter

            if (mPairedDevices.size() > 0 ){
//                tvInfoDaftarBluetoothPerangkat.setVisibility(View.VISIBLE);
            }
            for (BluetoothDevice device : mPairedDevices){

                listNameBluetooth.add(device.getName());
                listAddressBluetooth.add(device.getAddress());
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }

        }
        else{
            Toast.makeText(getApplicationContext(), "Bluetooth Tidak Menyala", Toast.LENGTH_SHORT).show();
        }

        listBluetooth.clear();
        listBluetooth.addAll(getListBluetooth());

        showRecyclerList();

    }

    public void openCashDrawer() {
        try {
            byte[] bytes = intArrayToByteArray(new int[]{27, 112, 0, 50, 250});
            outputStreamer.write(bytes);
            //            return true;
        } catch (IOException e) {
            Log.e(TAG, "Open drawer error", e);
            //            return false;
        }

    }

    private byte[] intArrayToByteArray(int[] Iarr) {
        byte[] bytes = new byte[Iarr.length];
        for (int i = 0; i < Iarr.length; i++) {
            bytes[i] = (byte) (Iarr[i] & 0xFF);
        }
        return bytes;
    }

    private void showRecyclerList(){

        rvListBluetooth.setLayoutManager(new LinearLayoutManager(this));
        listBluetoothAdapter = new DaftarBluetoothAdapter(listBluetooth);
        rvListBluetooth.setAdapter(listBluetoothAdapter);
        progressBarSearchBluetooth.setIndeterminate(false);
        progressBarSearchBluetooth.setVisibility(View.GONE);

        listBluetoothAdapter.setOnItemClickCallback(new DaftarBluetoothAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(ModelListBluetooth data) {

                if (!bluetoothAdapter.isEnabled()){
                    nyalakanBluetooh();
                }

                if (!utilBluetooth.isConnected(bluetoothSocket)){
                    utilBluetooth.finishConnected(bluetoothSocket, outputStreamer);
                }

                menghubungkanPerangkatBLuetooth(data.getAddress_bluetooth(), data.getNama_bluetooth(), PengaturanPerangkatActivity.this);

            }
        });
    }

    public static final boolean menghubungkanPerangkatBLuetooth(String addresBLuetoothPerangkat, String namaPerangkat, Activity context){
        final boolean[] fail = {false};

        new Thread()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {

                bluetoothDevice = bluetoothAdapter.getRemoteDevice(addresBLuetoothPerangkat);

                try {
                    bluetoothSocket = createBluetoothSocket(bluetoothDevice, context);
                } catch (IOException e) {
                    fail[0] = true;
//                    Toast.makeText(getBaseContext(), getString(R.string.ErrSockCrea), Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        {
                            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        }
                    }
                    bluetoothSocket.connect();

                    if (bluetoothSocket.isConnected()){
//                        Toast.makeText(PengaturanPerangkatActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
//                        bluetooth_status.setText("Berhasil");
                    }
//                    bluetoothSocket.getMaxTransmitPacketSize();
                    bluetoothSocket.getMaxTransmitPacketSize();
                } catch (IOException e) {
                    try {
                        fail[0] = true;
                        bluetoothSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
//                        Toast.makeText(context, getString(R.string.ErrSockCrea), Toast.LENGTH_SHORT).show();
                    }
                }

                if(!fail[0]) {
                    mConnectedThread = new ConnectedThread(bluetoothSocket, mHandler);
                    mConnectedThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, namaPerangkat)
                            .sendToTarget();
                }
            }
        }.start();

        return !fail[0];
    }

    private static BluetoothSocket createBluetoothSocket(BluetoothDevice device, Activity context) throws IOException {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            }
        }
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, UtilBluetooth.BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(UtilBluetooth.BT_MODULE_UUID);
    }

    static ArrayList<ModelListBluetooth> getListBluetooth() {
        ArrayList<ModelListBluetooth> modelListBluetooths = new ArrayList<>();
        modelListBluetooths.clear();
        for (int position = 0; position < listNameBluetooth.size(); position++) {
            ModelListBluetooth modelListBluetooth = new ModelListBluetooth();
            modelListBluetooth.setNama_bluetooth(listNameBluetooth.get(position));
            modelListBluetooth.setAddress_bluetooth(listAddressBluetooth.get(position));
            modelListBluetooths.add(modelListBluetooth);
        }
        return modelListBluetooths;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                if (bluetoothAdapter.isEnabled()) {
                    switchOnOffBluetooth.setChecked(true);
                }else{
                    switchOnOffBluetooth.setChecked(false);
                }
                // Izin akses Bluetooth diberikan, lanjutkan dengan operasi Bluetooth yang diinginkan
            } else {

                nyalakanBluetooh();
            }
        }
    }

    public void nyalakanBluetooh(){
        if (ContextCompat.checkSelfPermission(PengaturanPerangkatActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(PengaturanPerangkatActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
            }
        }

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(PengaturanPerangkatActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(PengaturanPerangkatActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            }


        }else{
            if (!bluetoothAdapter.isEnabled()){
                switchOnOffBluetooth.setChecked(false);

                nyalakanBluetooh();
            }else{
                switchOnOffBluetooth.setChecked(true);
            }
        }
    }
}
