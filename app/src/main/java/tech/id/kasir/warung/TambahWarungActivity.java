package tech.id.kasir.warung;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import tech.id.kasir.R;

public class TambahWarungActivity extends AppCompatActivity {

    TextInputEditText tietNamaToko, tietOwnerToko, tietKontakToko, tietAlamatToko, tietKelurahanToko, tietKecamatanToko, tietKotaToko, tietProvinsiToko, tietKodePosToko;
    ImageButton ibSaveInfoWarung;
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



    }
}