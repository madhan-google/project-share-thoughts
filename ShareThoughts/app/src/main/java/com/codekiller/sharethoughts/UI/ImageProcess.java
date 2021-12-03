package com.codekiller.sharethoughts.UI;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageProcess {
    Context context;
    UIUtils uiUtils;
    String finalPath;

    public ImageProcess(Context context) {
        this.context = context;
        uiUtils = new UIUtils(context);
    }

    public Uri processImage(Uri data, String folder) {
        //Bitmap bitmap = BitmapFactory.decodeFile(uiUtils.getFullFilePath(data));
        /*Bitmap compressImage = null;
        try {
            compressImage = SiliCompressor.with(context)
                    .getCompressBitmap(uiUtils.getFullFilePath(data),false);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        /*double ratioSquare;
        int bitmapHeight, bitmapWidth;
        bitmapHeight = bitmap.getHeight();
        bitmapWidth = bitmap.getWidth();
        ratioSquare = (bitmapHeight * bitmapWidth) / 360000;
        if (ratioSquare <= 1)
            return data;
        double ratio = Math.sqrt(ratioSquare);
        int requireHeight = (int) Math.round(bitmapHeight / ratio);
        int requireWidth = (int) Math.round(bitmapWidth / ratio);
        Bitmap compressImage = Bitmap.createScaledBitmap(bitmap, requireWidth, requireHeight, true);
        FileOutputStream fileOutputStream = null;
        File f = Environment.getExternalStorageDirectory();
        File dir = new File(f.getAbsolutePath() + "/ShareThoughts/"+folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "IMG_" + System.currentTimeMillis() + "." + uiUtils.getExtension(data));
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        compressImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        try {
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        File dir = new File(file,"ShareThoughts/"+folder);
        //new imageCompress().execute("false",data.toString(),dir.getPath());
        return Uri.fromFile(new File(finalPath));
    }

    /*private class imageCompress extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            String imageUri = SiliCompressor.with(context)
                    .compress(strings[1],new File(strings[2]));
            return imageUri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            finalPath = s;
        }
    }*/
}
