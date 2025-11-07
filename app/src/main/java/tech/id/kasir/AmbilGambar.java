package tech.id.kasir;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AmbilGambar {

    Context context;

    public AmbilGambar(Context context) {
        this.context = context;
    }


    public static Matrix exifInterface(String currentPhotoPath, int deteksi){

        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(currentPhotoPath);
        }catch (IOException e){
            e.printStackTrace();
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Log.d("ChekOrientasi", String.valueOf(orientation));
        Matrix matrix = new Matrix();
        switch (orientation){
            case ExifInterface.WHITEBALANCE_AUTO:
                if (deteksi ==1){
                    matrix.setRotate(270);
                }else{
                    matrix.setRotate(0);
                }
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
        }

        return matrix;
    }



    public Bitmap fileBitmap(File file){
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;


        if (getFileExt(file.getName()).equals("png") || getFileExt(file.getName()).equals("PNG")) {
            if (Build.VERSION.SDK_INT > 21){
                o.inSampleSize = 2;
            }else{
                o.inSampleSize = 2;
            }
        } else {
            if (Build.VERSION.SDK_INT > 27){
                o.inSampleSize = 2;
            }else{
                o.inSampleSize = 2;
            }
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BitmapFactory.decodeStream(inputStream, null, o);
        try {
            assert inputStream != null;
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // The new size we want to scale to
        final int REQUIRED_SIZE = 110;

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        if (Build.VERSION.SDK_INT > 27){
            while (o.outWidth / scale / 4 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 4 >= REQUIRED_SIZE) {
                scale *= 2;
            }
        }else{
            while (o.outWidth / scale / 4 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 4 >= REQUIRED_SIZE) {
                scale *= 2;
            }
        }


        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(inputStream, null, o2);
    }
    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

}
