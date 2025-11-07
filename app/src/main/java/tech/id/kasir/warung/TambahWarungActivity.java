package tech.id.kasir.warung;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.id.kasir.AmbilGambar;
import tech.id.kasir.R;
import tech.id.kasir.api.RetrofitClient;
import tech.id.kasir.response_api.ApiResponse;

public class TambahWarungActivity extends AppCompatActivity {

    static TextInputEditText tietNamaToko, tietOwnerToko, tietKontakToko, tietAlamatToko, tietKelurahanToko, tietKecamatanToko, tietKotaToko, tietProvinsiToko, tietKodePosToko;
    ImageButton ibSaveInfoWarung;
    AmbilGambar ambilGambar = new AmbilGambar(TambahWarungActivity.this);
    Bitmap rotationBitmapSurat;
    static String lampiran = "gambar";
    static String ekslampiran = "ekstensi";
    ActivityResultLauncher<Intent> resultLauncher;

    ShapeableImageView ivFotoWarung, iconWarung;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambah_warung);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        tietNamaToko = findViewById(R.id.tietNamaToko);
        tietOwnerToko = findViewById(R.id.tietOwnerToko);
        tietKontakToko = findViewById(R.id.tietKontakToko);
        tietAlamatToko = findViewById(R.id.tietAlamatToko);
        tietKelurahanToko = findViewById(R.id.tietKelurahanToko);
        tietKecamatanToko = findViewById(R.id.tietKecamatanToko);
        tietKotaToko = findViewById(R.id.tietKotaToko);
        tietProvinsiToko = findViewById(R.id.tietProvinsiToko);
        tietKodePosToko = findViewById(R.id.tietKodePosToko);
        ibSaveInfoWarung = findViewById(R.id.ibSaveInfoWarung);
        ivFotoWarung = findViewById(R.id.ivFotoWarung);
        iconWarung = findViewById(R.id.iconWarung);

        iconWarung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Pilih Gambar"), 33);

            }
        });

        ibSaveInfoWarung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataWarungToServer();
            }
        });



    }


    public static void sendDataWarungToServer() {

        String namaToko = Objects.requireNonNull(tietNamaToko.getText()).toString().trim();
        String owner = Objects.requireNonNull(tietOwnerToko.getText()).toString().trim();
        String kontak = Objects.requireNonNull(tietKontakToko.getText()).toString().trim();
        String alamat = Objects.requireNonNull(tietAlamatToko.getText()).toString().trim();
        String kelurahan = Objects.requireNonNull(tietKelurahanToko.getText()).toString().trim();
        String kecamatan = Objects.requireNonNull(tietKecamatanToko.getText()).toString().trim();
        String kota = Objects.requireNonNull(tietKotaToko.getText()).toString().trim();
        String provinsi = Objects.requireNonNull(tietProvinsiToko.getText()).toString().trim();
        String kodepos = Objects.requireNonNull(tietKodePosToko.getText()).toString().trim();


        Call<ApiResponse> call = RetrofitClient.getInstance().getApi().simpantoko(namaToko, owner, kontak, alamat, kelurahan, kecamatan, kota, provinsi, kodepos, lampiran, ekslampiran);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, Response<ApiResponse> response) {
                if (!response.isSuccessful()) {
                    Log.d("Toko", 1+" - "+response.message());
                    return;
                }
                if (response.isSuccessful()) {
                    Log.d("Toko", "Token berhasil dikirim ke server "+response.body().getMessage());
                } else {
                    Log.e("Toko", "Gagal kirim token: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, Throwable t) {
                Log.e("Toko", "Error: " + t.getMessage());

            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED){

            if (requestCode == 33 && resultCode == Activity.RESULT_OK && data != null){
                requestPermission();

                Uri selectedImageUri = data.getData();
                String FilePath2  = getDriveFilePath(selectedImageUri, TambahWarungActivity.this);

                File file1 = new File(FilePath2);
                Bitmap bitmap = ambilGambar.fileBitmap(file1);
                rotationBitmapSurat = Bitmap.createBitmap(bitmap, 0,0, bitmap.getWidth(), bitmap.getHeight(), AmbilGambar.exifInterface(FilePath2, 0), true);

                ivFotoWarung.setImageBitmap(rotationBitmapSurat);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,75, byteArrayOutputStream);
                byte[] imageInByte = byteArrayOutputStream.toByteArray();
                lampiran =  Base64.encodeToString(imageInByte,Base64.DEFAULT);
                ekslampiran = "jpg";
            }

        }
    }
    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},123);
        }
    }


    public static String getDriveFilePath(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();

        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }
}