package com.codekiller.sharethoughts.UI;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UIUtils {
    Context context;
    ProgressDialog progressDialog;

    public UIUtils(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }
    public void showProgress(String title, String Messsage){
        progressDialog.setTitle(title);
        progressDialog.setMessage(Messsage);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }
    public void progressDismiss(){
        progressDialog.dismiss();
    }
    public void toast(String s){
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }
    public String getDate(){
        return new SimpleDateFormat("dd-mm-yyyy").format(new Date());
    }
    public String getTime(){
        return new SimpleDateFormat("hh:mm:ss").format(new Date());
    }
    public String showDatePicker(){
        Calendar cal = Calendar.getInstance();
        final String[] date = new String[1];
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date[0] = dayOfMonth+" - "+(month+1)+" - "+year;
            }
        },cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        return date[0];
    }
    public String getExtension(Uri uri){
        ContentResolver cr = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
    public String getFullFilePath(Uri data){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(data,filePathColumn,null,null,null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
    }
    public Uri getImageUri(String path){
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID};
        Cursor cursor = context.getContentResolver().query(uri,projection,MediaStore.Images.Media.DATA+"=?",new String[]{path},null);
        cursor.moveToFirst();
        return Uri.parse(uri.toString()+"/"+cursor.getString(cursor.getColumnIndex(projection[0])));
    }
    public Uri getVideoUri(String path){
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.Media._ID};
        Cursor cursor = context.getContentResolver().query(uri,projection,MediaStore.Video.Media.DATA+"=?",new String[]{path},null);
        cursor.moveToFirst();
        return Uri.parse(uri.toString()+"/"+cursor.getString(cursor.getColumnIndex(projection[0])));
    }
}
