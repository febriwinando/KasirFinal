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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import tech.id.kasir.R;
import tech.id.kasir.utility.btt.CashDrawerHelper;
import tech.id.kasir.utility.btt.ConnectedThread;
import tech.id.kasir.utility.btt.DaftarBluetoothAdapter;
import tech.id.kasir.utility.btt.InputConnectThread;
import tech.id.kasir.utility.btt.ModelListBluetooth;
import tech.id.kasir.utility.btt.SintaksPOST;
import tech.id.kasir.utility.btt.UtilBluetooth;

public class PengaturanPerangkatActivity extends AppCompatActivity {

    private static ArrayList<ModelListBluetooth> listBluetooth = new ArrayList<>();
    static ArrayList<String> listNameBluetooth = new ArrayList<String>();
    static ArrayList<String> listAddressBluetooth = new ArrayList<String>();
    Switch switchOnOffBluetooth, switchCashDrawer, switchBarcode, switchTipeStore;
    private static final int REQUEST_ENABLE_BT = 1;
    TableRow cariPerangkat, ujiPrinter;
    ProgressBar progressBarSearchBluetooth;
    RecyclerView rvListBluetooth;
    TextView pengaturan_bluetooth_status;
    InputConnectThread inputConnectThread;
    ImageView ivKembaliDariPengaturanPerangkat;
    private ArrayAdapter<String> mBTArrayAdapter;
    public  static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> devices;
    public static Handler mHandler;
    private Set<BluetoothDevice> mPairedDevices;
    UtilBluetooth utilBluetooth = new UtilBluetooth();
    public static BluetoothSocket bluetoothSocket = null;
    public static OutputStream outputStreamer;
    SintaksPOST sintaksPOST = new SintaksPOST();
    private InputStream mInputStream;
    DaftarBluetoothAdapter listBluetoothAdapter;
    public static BluetoothDevice bluetoothDevice;
    public static ConnectedThread mConnectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        hideSystemUI();
        setContentView(R.layout.activity_pengaturan_perangkat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(PengaturanPerangkatActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(PengaturanPerangkatActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            }
        }


        listNameBluetooth.clear();
        listAddressBluetooth.clear();
        listBluetooth.clear();


        cariPerangkat = findViewById(R.id.cariPerangkat);
        progressBarSearchBluetooth = findViewById(R.id.progressBarSearchBluetooth);
        rvListBluetooth= findViewById(R.id.rvListBluetooth);
        pengaturan_bluetooth_status = findViewById(R.id.pengaturan_bluetooth_status);
        ujiPrinter = findViewById(R.id.ujiPrinter);
        switchOnOffBluetooth = findViewById(R.id.switchOnOffBluetooth);
        ivKembaliDariPengaturanPerangkat = findViewById(R.id.ivKembaliDariPengaturanPerangkat);

        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        devices = bluetoothAdapter.getBondedDevices();

        cariPerangkat.setOnClickListener(v -> {
                    progressBarSearchBluetooth.setVisibility(View.VISIBLE);
                    listPairedDevices();
                }
        );


        if (!bluetoothAdapter.isEnabled()) {
            switchOnOffBluetooth.setChecked(false);
        } else {
            switchOnOffBluetooth.setChecked(true);
        }


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
                        Toast.makeText(PengaturanPerangkatActivity.this, (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        pengaturan_bluetooth_status.setText("Gagal terhubung");
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
                        Toast.makeText(PengaturanPerangkatActivity.this, "Bluetooth Sudah dinyalakan", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    switchOnOffBluetooth.setChecked(true);
                }
            }
        });

        if (bluetoothAdapter == null) {
            // Perangkat tidak mendukung Bluetooth
        }

        mPairedDevices = bluetoothAdapter.getBondedDevices();

        ivKembaliDariPengaturanPerangkat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ujiPrinter.setOnClickListener(v -> {
            if (utilBluetooth.isConnected(bluetoothSocket)){
                try {
                    outputStreamer = bluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                sintaksPOST.bill(PengaturanPerangkatActivity.this, bluetoothSocket, outputStreamer);
                CashDrawerHelper.open(outputStreamer);
                Toast.makeText(this, ""+bluetoothSocket, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Belum Terhubung", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, ""+bluetoothSocket, Toast.LENGTH_SHORT).show();

        });

    }

    private void hideSystemUI() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_LAYOUT_FLAGS
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
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

//            Toast.makeText(getApplicationContext(), getString(R.string.show_paired_devices), Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Bluetooth tidak menyala", Toast.LENGTH_SHORT).show();
        }

        listBluetooth.clear();
        listBluetooth.addAll(getListBluetooth());

        showRecyclerList();

        inputConnectThread = new InputConnectThread(mInputStream);
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
                ActivityCompat.requestPermissions(PengaturanPerangkatActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            }
        }

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
}