package tech.id.kasir.utility.btt;

import static tech.id.kasir.utility.btt.FormatWaktu.SIMPLE_FORMAT_TANGGAL_INVOICE;
import static tech.id.kasir.utility.btt.FormatWaktu.formatRupiah;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import tech.id.kasir.R;
import tech.id.kasir.response_api.OrderItemRequest;

public class SintaksPOST {
    ArrayList<OrderItemRequest> modelProdukFinalTransaksis = new ArrayList<>();

    // variabel dinamis untuk lebar karakter per baris
    private int lineChars = 42; // default aman untuk 58mm

    /**
     * Deteksi lebar karakter printer dengan mencoba mencetak baris uji
     */
    private void detectPrinterWidth(OutputStream outputStream) {
        try {
            outputStream.write(new byte[]{0x1B, 0x4D, 0x00}); // set font A monospace
            String testLine = "1234567890123456789012345678901234567890123456789";
            outputStream.write(testLine.getBytes());
            outputStream.write(PrinterCommands.LF);
            // Secara umum, printer 58mm = 32/42 chars, 80mm = 48/64 chars
            // Jadi kita bisa coba patokkan manual sesuai panjang
            if (testLine.length() >= 48) {
                lineChars = 48; // kemungkinan printer 80mm
            } else {
                lineChars = 42; // printer 58mm
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineChars = 42;
        }
    }

    public void droping(Activity context,
                        ArrayList<OrderItemRequest> modelProdukFinalTransaksis,
                        BluetoothSocket mBTSocket, OutputStream outputStream,
                        String pengunjung, String invoice, String tanggal, String costumer,
                        String tax, String order, String uangditerima, String uangkembali,
                        String totalTansaksi, String status) {

        try {
            Thread.sleep(1000);
            outputStream = mBTSocket.getOutputStream();

            // Deteksi lebar printer
            detectPrinterWidth(outputStream);

            // Gunakan font monospace konsisten
            outputStream.write(new byte[]{0x1B, 0x4D, 0x00}); // Font A
            outputStream.write(new byte[]{0x1B, 0x21, 0x00}); // normal text

            printCustom("KRIPSANG LUMER MAK EL", 1, 1, outputStream);
            printCustom("Jl. Danau Laut Tawar No.7, Kec.Padang Hulu", 0, 1, outputStream);
            printCustom("Hot Line: 0857-6218-3348", 0, 1, outputStream);

            printText(leftRightAlign("Tanggal", SIMPLE_FORMAT_TANGGAL_INVOICE.format(new Date())), outputStream);
            printText(leftRightAlign("Invoice", invoice), outputStream);
            printText(leftRightAlign("Pelanggan", costumer), outputStream);
            printCustom(new String(new char[lineChars]).replace("\0", "-"), 0, 1, outputStream);

            for (OrderItemRequest modelDroping : modelProdukFinalTransaksis) {
                float jumlahDroping = Float.parseFloat(modelDroping.getHarga_satuan())
                        * Float.parseFloat(String.valueOf(modelDroping.getJumlah()));
                String left = modelDroping.getJumlah() + " " + modelDroping.getNama_menu().toUpperCase();
                String right = formatRupiah.format(jumlahDroping);
                printText(leftRightAlign(left, right), outputStream);
            }

            printCustom(new String(new char[lineChars]).replace("\0", "."), 0, 1, outputStream);
            printText(leftRightAlign("Jumlah Pesanan", order), outputStream);
            printText(leftRightAlign("PPN", "(10%) " + tax), outputStream);
            printText(leftRightAlign("Total Belanja", totalTansaksi), outputStream);

            if (status.equals("1")) {
                printText(leftRightAlign("Uang Diterima", uangditerima), outputStream);
                printText(leftRightAlign("Uang Kembali", uangkembali), outputStream);
                printNewLine(outputStream);
                printCustom("LUNAS", 1, 1, outputStream);
            } else {
                printNewLine(outputStream);
                printNewLine(outputStream);
                printCustom("HUTANG", 1, 1, outputStream);
            }

            printNewLine(outputStream);
            printCustom("Thank you for coming & we look", 0, 1, outputStream);
            printCustom("forward to serve you again\n\n\n", 0, 1, outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //=====================================================
    // Utility Printing Functions
    //=====================================================

    public void printCustom(String msg, int size, int align, OutputStream outputStream) {
        byte[] normal = new byte[]{0x1B, 0x21, 0x00};
        byte[] bold = new byte[]{0x1B, 0x21, 0x08};
        byte[] large = new byte[]{0x1B, 0x21, 0x20};
        byte[] xlarge = new byte[]{0x1B, 0x21, 0x30};

        try {
            switch (size) {
                case 0: outputStream.write(normal); break;
                case 1: outputStream.write(bold); break;
                case 2: outputStream.write(large); break;
                case 3: outputStream.write(xlarge); break;
            }

            switch (align) {
                case 0: outputStream.write(PrinterCommands.ESC_ALIGN_LEFT); break;
                case 1: outputStream.write(PrinterCommands.ESC_ALIGN_CENTER); break;
                case 2: outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT); break;
            }

            outputStream.write(msg.getBytes("UTF-8"));
            outputStream.write(PrinterCommands.LF);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bill(Activity context, BluetoothSocket mBTSocket, OutputStream outputStream) {
        try {
            Thread.sleep(1000);
            outputStream = mBTSocket.getOutputStream();

            // Gunakan deteksi lebar printer yang sama dengan droping()
            detectPrinterWidth(outputStream);

            // Pastikan semua printer pakai font monospace (Font A)
            outputStream.write(new byte[]{0x1B, 0x4D, 0x00}); // Font A
            outputStream.write(new byte[]{0x1B, 0x21, 0x00}); // Normal style

            // Header
            printCustom("Febri Winando Purba", 2, 1, outputStream);
            printCustom("PT. KEGAGALAN Ltd.", 0, 1, outputStream);
            printCustom("Hot Line: 0821 6655 1658", 0, 1, outputStream);
            printCustom("Vat Reg : 0000000000, Mushak : 11", 0, 1, outputStream);

            // Date & time
            String[] dateTime = getDateTime();
            printText(leftRightAlign(dateTime[0], dateTime[1]), outputStream);
            printCustom(new String(new char[lineChars]).replace("\0", "-"), 0, 1, outputStream);

            // Table header
            printText(leftRightAlign("Qty: Name", "Price"), outputStream);
            printCustom(new String(new char[lineChars]).replace("\0", "."), 0, 1, outputStream);

            // Sample data (sesuaikan jika ada loop item)
            printText(leftRightAlign("2 Burger", "30,000"), outputStream);
            printText(leftRightAlign("1 Cola", "10,000"), outputStream);

            printCustom(new String(new char[lineChars]).replace("\0", "-"), 0, 1, outputStream);
            printText(leftRightAlign("Total", "40,000"), outputStream);

            printNewLine(outputStream);
            printCustom("Thank you for coming & we look", 0, 1, outputStream);
            printCustom("forward to serve you again", 1, 1, outputStream);
            printNewLine(outputStream);

            // Unicode & logo optional
            printUnicode(outputStream);
            printPhoto(context, R.drawable.img, outputStream);

            // Feed out paper
            printNewLine(outputStream);
            printNewLine(outputStream);
            printNewLine(outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public String[] getDateTime() {
        final Calendar c = Calendar.getInstance();
        String dateTime [] = new String[2];
        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) +"/"+ c.get(Calendar.MONTH) +"/"+ c.get(Calendar.YEAR);
        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) +":"+ c.get(Calendar.MINUTE);
        return dateTime;
    }
        //print unicode
    public void printUnicode(OutputStream outputStream){
        try {
            outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
            printText(Arrays.toString(Utils.UNICODE_TEXT), outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void printText(String msg, OutputStream outputStream) {
        try {
            outputStream.write(msg.getBytes("UTF-8"));
            outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printNewLine(OutputStream outputStream) {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String leftRightAlign(String left, String right) {
        // pastikan tidak melewati panjang maksimum
        if (left.length() + right.length() > lineChars) {
            return left + "\n" + right;
        }
        int spaces = lineChars - (left.length() + right.length());
        String padding = new String(new char[Math.max(1, spaces)]).replace("\0", " ");
        return left + padding + right;
    }

    public void printPhoto(Activity context, int img, OutputStream outputStream) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), img);
            if (bmp != null) {
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                outputStream.write(command);
                outputStream.flush();  // Pastikan untuk memanggil flush setelah menulis ke outputStream
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "Error printing photo");
        }
    }
}

//package tech.id.kasir.utility.btt;
//
//
//import static tech.id.kasir.utility.btt.FormatWaktu.SIMPLE_FORMAT_TANGGAL_INVOICE;
//import static tech.id.kasir.utility.btt.FormatWaktu.formatRupiah;
//
//import android.app.Activity;
//import android.bluetooth.BluetoothSocket;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.util.Log;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//
//import tech.id.kasir.R;
//import tech.id.kasir.response_api.OrderItemRequest;
//
//public class SintaksPOST {
//    ArrayList<OrderItemRequest> modelProdukFinalTransaksis = new ArrayList<>();
//
//    public void droping(Activity context,
//                        ArrayList<OrderItemRequest> modelProdukFinalTransaksis,
//                        BluetoothSocket mBTSocket, OutputStream outputStream,
//                        String pengunjung, String invoice, String tanggal, String costumer,
//                        String tax, String order, String uangditerima, String uangkembali, String totalTansaksi, String status) {
//        mBTSocket.getRemoteDevice();
//        mBTSocket.isConnected();
//        //print command
//        try {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            outputStream = mBTSocket.getOutputStream();
//
//            byte[] printformat = new byte[]{0x1B,0x21,0x03};
//            outputStream.write(printformat);
//
//            printCustom("KRIPSANG LUMER MAK EL",1,1, outputStream);
//            printCustom("Jl. Danau Laut Tawar No.7, Kec.Padang Hulu",0,1, outputStream);
//            printCustom("Hot Line: 0857-6218-3348",0,1, outputStream);
//
//            printText(leftRightAlign("Tanggal", SIMPLE_FORMAT_TANGGAL_INVOICE.format(new Date())), outputStream);
//            printText(leftRightAlign("Invoice", invoice), outputStream);
//            printText(leftRightAlign("Pelanggan" , costumer), outputStream);
//
//
//            for (OrderItemRequest modelDroping: modelProdukFinalTransaksis){
//                float jumlahDroping = Float.parseFloat(modelDroping.getHarga_satuan()) * Float.parseFloat(String.valueOf(modelDroping.getJumlah()));
//                printText(leftRightAlign(modelDroping.getJumlah()+"   "+modelDroping.getNama_menu().toUpperCase(), formatRupiah.format(jumlahDroping)), outputStream);
////                total += jumlahDroping;
////                printNewLine(outputStream);
//            }
//            printCustom(new String(new char[42]).replace("\0", "."),0,1, outputStream);
//            printText(leftRightAlign("Jumlah Pesanan" , order), outputStream);
//            printText(leftRightAlign("PPN" , "(10%) "+tax), outputStream);
//            printText(leftRightAlign("Total Belanja" , totalTansaksi), outputStream);
//            if (status.equals("1")){
//
//                printText(leftRightAlign("Uang Diterima" , uangditerima), outputStream);
//                printText(leftRightAlign("Uang Kembali" , uangkembali), outputStream);
//                printNewLine(outputStream);
//
//            }else{
//                printNewLine(outputStream);
//                printNewLine(outputStream);
//                printCustom("\n\n\n",0,1, outputStream);
//
//            }
//
//            if (status.equals("1")){
//                printCustom("LUNAS",1,1, outputStream);
//                printNewLine(outputStream);
//                printCustom("Thank you for coming & we look",0,1, outputStream);
//                printCustom("forward to serve you again\n\n\n",0,1, outputStream);
//            }else if (status.equals("2")){
//                printCustom("HUTANG",1,1, outputStream);
//                printNewLine(outputStream);
//                printCustom("Thank you for coming & we look",0,1, outputStream);
//                printCustom("forward to serve you again\n\n\n",0,1, outputStream);
//            }
//
////            printPhoto(context, R.drawable.imgtest, outputStream);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//
//    }
//    byte FONT_TYPE;
//    //    private static OutputStream outputStream;
//    public void bill(Activity context, BluetoothSocket mBTSocket, OutputStream outputStream) {
//        mBTSocket.getRemoteDevice();
//        mBTSocket.isConnected();
//        //print command
//        try {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            outputStream = mBTSocket.getOutputStream();
//
////            byte[] printformat = new byte[]{0x1B,0x21,0x03};
//            byte[] printformat = { 0x1B, 0*21, FONT_TYPE };
//            outputStream.write(printformat);
//
//            printCustom("Febri Winando Purba",2,1, outputStream);
//            printCustom("PT. KEGAGALAN Ltd.",0,1, outputStream);
////                printPhoto(R.drawable.ic_icon_pos);
//            printCustom("Hot Line: 0821 6655 1658",0,1, outputStream);
//            printCustom("Vat Reg : 0000000000, Mushak : 11",0,1, outputStream);
//            String[] dateTime = getDateTime();
//            printText(leftRightAlign(dateTime[0], dateTime[1]), outputStream);
//            printText(leftRightAlign("Qty: Name" , "Price "), outputStream);
//            printCustom(new String(new char[32]).replace("\0", "."),0,1, outputStream);
//            printText(leftRightAlign("Total" , "5,000,000/="), outputStream);
//            printNewLine(outputStream);
//            printCustom("Thank you for coming & we look",0,1, outputStream);
//            printCustom("forward to serve you again",3,1, outputStream);
//            printNewLine(outputStream);
//            printNewLine(outputStream);
//            printUnicode(outputStream);
//            printPhoto(context, R.drawable.img, outputStream);
//
//            printNewLine(outputStream);
//            printNewLine(outputStream);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
////    public void printPhoto(Activity context, int img, OutputStream outputStream) {
////        try {
////            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), img);
////            if (bmp != null) {
////                // konversi dari 8-bit PNG ke RGB tanpa alpha/transparansi
////                Bitmap newBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.RGB_565);
////                Canvas canvas = new Canvas(newBmp);
////                canvas.drawColor(Color.WHITE); // background putih
////                canvas.drawBitmap(bmp, 0, 0, null);
////                bmp = newBmp;
////
////                // batasi lebar printer thermal (58mm)
////                if (bmp.getWidth() > 384) {
////                    bmp = Bitmap.createScaledBitmap(bmp, 384, (bmp.getHeight() * 384) / bmp.getWidth(), false);
////                }
////
////                byte[] command = Utils.decodeBitmap(bmp);
////
////                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
////                outputStream.write(command);
////                outputStream.flush();
////
////                Thread.sleep(500);
////            } else {
////                Log.e("Print Photo", "Bitmap null or failed to decode");
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////            Log.e("PrintTools", "Print photo failed: " + e.getMessage());
////        }
////    }
//
//    //print photo
//
//
//    public void printPhoto(Activity context, int img, OutputStream outputStream) {
//        try {
//            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), img);
//            if (bmp != null) {
//                byte[] command = Utils.decodeBitmap(bmp);
//                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
//                outputStream.write(command);
//                outputStream.flush();  // Pastikan untuk memanggil flush setelah menulis ke outputStream
//            } else {
//                Log.e("Print Photo error", "the file isn't exists");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("PrintTools", "Error printing photo");
//        }
//    }
//
//    //print unicode
//    public void printUnicode(OutputStream outputStream){
//        try {
//            outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
//            printText(Utils.UNICODE_TEXT, outputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void printCustom(String msg, int size, int align, OutputStream outputStream) {
//
//        //Print config "mode"
//        byte[] cc = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
//        byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
//        byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
//        byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
//        byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
//        try {
//            switch (size){
//                case 0:
//                    outputStream.write(cc);
//                    break;
//                case 1:
//                    outputStream.write(bb);
//                    break;
//                case 2:
//                    outputStream.write(bb2);
//                    break;
//                case 3:
//                    outputStream.write(bb3);
//                    break;
//            }
//
//            switch (align){
//                case 0:
//                    //left align
//                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
//                    break;
//                case 1:
//                    //center align
//                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
//                    break;
//                case 2:
//                    //right align
//                    outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
//                    break;
//            }
//
//            outputStream.write(msg.getBytes());
//            outputStream.write(PrinterCommands.LF);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    //print text
//    public void printText(String msg, OutputStream outputStream) {
//        try {
//            // Print normal text
//            outputStream.write(msg.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    //print byte[]
//    private void printText(byte[] msg, OutputStream outputStream) {
//        try {
//            // Print normal text
//            outputStream.write(msg);
//            printNewLine(outputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    //print new line
//    public void printNewLine(OutputStream outputStream) {
//        try {
//            outputStream.write(PrinterCommands.FEED_LINE);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public String leftRightAlign(String str1, String str2) {
//        String ans = str1+""+str2;
//        if(ans.length() <= 42){
//            int n = (42 - (str1.length() + str2.length()));
//            ans = str1 +""+ new String(new char[n]).replace("\0", " ")+""+ str2;
//        }
//        return ans;
//    }
//
////    public String leftRightAlign(String str1, String str2) {
////        String ans = str1+""+ str2;
////        if(ans.length() <= 42){
////            int n = (42 - (str1.length() + str2.length()));
////            ans = ans.length()+" "+str1 +" "+ String.valueOf(n)+ str2;
////        }
////
////        return ans;
////    }
//    public String[] getDateTime() {
//        final Calendar c = Calendar.getInstance();
//        String dateTime [] = new String[2];
//        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) +"/"+ c.get(Calendar.MONTH) +"/"+ c.get(Calendar.YEAR);
//        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) +":"+ c.get(Calendar.MINUTE);
//        return dateTime;
//    }
//
//    public void openCashDrawer(OutputStream outputStream) {
//        try {
//            byte[] bytes = intArrayToByteArray(new int[]{27, 112, 0, 50, 250});
//            outputStream.write(bytes);
////            return true;
//        } catch (IOException e) {
//
////            return false;
//        }
//    }
//
//    private byte[] intArrayToByteArray(int[] Iarr) {
//        byte[] bytes = new byte[Iarr.length];
//        for (int i = 0; i < Iarr.length; i++) {
//            bytes[i] = (byte) (Iarr[i] & 0xFF);
//        }
//        return bytes;
//    }
//
//
//
//}
