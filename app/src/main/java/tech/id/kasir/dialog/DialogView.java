package tech.id.kasir.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import tech.id.kasir.R;

public class DialogView {
    Context context;

    public DialogView(Context context) {
        this.context = context;
    }

    public int viewTambahCatatan(){
        Dialog dialogSukes = new Dialog(context, R.style.DialogStyle);
        dialogSukes.setContentView(R.layout.dialog_item_notes);
        dialogSukes.setCancelable(true);
//        ImageView tvTutupDialog = dialogSukes.findViewById(R.id.tvTutupDialog);

        final int[] tutup = {0};

//        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tutup[0] = 1;
//                dialogSukes.dismiss();
//            }
//        });

        dialogSukes.show();
        return tutup[0];

    }
    public int viewHapusItemOrder(){
        Dialog dialoghapus = new Dialog(context, R.style.DialogStyle);
        dialoghapus.setContentView(R.layout.view_hapus_item_order);
        dialoghapus.setCancelable(true);
        ImageView tvTutupDialog = dialoghapus.findViewById(R.id.tvTutupDialog);

        final int[] tutup = {0};

        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tutup[0] = 1;
                dialoghapus.dismiss();
            }
        });

        dialoghapus.show();
        return tutup[0];

    }

}
