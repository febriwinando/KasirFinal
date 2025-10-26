package tech.id.kasir.utility.btt;

import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;

public class CashDrawerHelper {
    private static final String TAG = "CashDrawerHelper";

    public static void open(OutputStream output) {
        try {
            byte[] command = new byte[]{27, 112, 0, 50, (byte)250};
            output.write(command);
        } catch (IOException e) {
            Log.e(TAG, "Error opening cash drawer", e);
        }
    }

    private byte[] intArrayToByteArray(int[] Iarr) {
        byte[] bytes = new byte[Iarr.length];
        for (int i = 0; i < Iarr.length; i++) {
            bytes[i] = (byte) (Iarr[i] & 0xFF);
        }
        return bytes;
    }

}
