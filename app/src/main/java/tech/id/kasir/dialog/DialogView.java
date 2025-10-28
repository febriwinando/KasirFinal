package tech.id.kasir.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tech.id.kasir.R;
import tech.id.kasir.database.DBHelper;

public class DialogView {
    private Context context;
    private DBHelper dbHelper;
    private OnItemDeletedListener onItemDeletedListener;

    public DialogView(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
    }

    public interface OnItemDeletedListener {
        void onItemDeleted(int deletedItemId);
    }

    // Setter listener
    public void setOnItemDeletedListener(OnItemDeletedListener listener) {
        this.onItemDeletedListener = listener;
    }
    public void viewHapusItemOrder(int id) {
        Dialog dialoghapus = new Dialog(context, R.style.DialogStyle);
        dialoghapus.setContentView(R.layout.view_hapus_item_order);
        dialoghapus.setCancelable(true);

        ImageView tvTutupDialog = dialoghapus.findViewById(R.id.tvTutupDialog);
        TextView tvHapusItemOrder = dialoghapus.findViewById(R.id.tvHapusItemOrder);

        tvHapusItemOrder.setOnClickListener(v -> {
            dbHelper.deleteOrderItemById(id);

            // panggil callback
            if (onItemDeletedListener != null) {
                onItemDeletedListener.onItemDeleted(id);
            }

            dialoghapus.dismiss();
        });

        tvTutupDialog.setOnClickListener(v -> dialoghapus.dismiss());
        dialoghapus.show();
    }
//    public void viewHapusItemOrder(int id){
//        Dialog dialoghapus = new Dialog(context, R.style.DialogStyle);
//        dialoghapus.setContentView(R.layout.view_hapus_item_order);
//        dialoghapus.setCancelable(true);
//        ImageView tvTutupDialog = dialoghapus.findViewById(R.id.tvTutupDialog);
//        TextView tvHapusItemOrder = dialoghapus.findViewById(R.id.tvHapusItemOrder);
//
//
//        tvHapusItemOrder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DBHelper dbHelper = new DBHelper(context);
//                dbHelper.deleteOrderItemById(id);
//                dialoghapus.dismiss();
//            }
//        });
//        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialoghapus.dismiss();
//            }
//        });
//
//        dialoghapus.show();
//
//    }

}
