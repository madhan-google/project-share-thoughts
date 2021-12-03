package com.codekiller.sharethoughts.UI;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.net.URISyntaxException;

public class VideoProcess {
    public static final String TAG = "VIDEO PROCESS";

    Context context;
    UIUtils uiUtils;
    File file, dir;
    String outputUri;

    public VideoProcess(Context context) {
        this.context = context;
        uiUtils = new UIUtils(context);
    }

    public Uri processVideo(Uri uri){
        //uiUtils.showProgress("Loading","Please Wait");
        file = Environment.getExternalStorageDirectory();
        //dir = new File(file.getAbsolutePath(),"ShareThoughts/Posts");
        new videoCompress().execute(file.getPath(),uri.toString());
        /*try {
            outputUri = SiliCompressor.with(context)
                    .compressVideo(uri,file.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "processVideo: dir - "+dir);
        Log.d(TAG, "processVideo: output uri - "+outputUri);
        uiUtils.progressDismiss();*/
        return Uri.fromFile(new File(outputUri));
    }

    private class videoCompress extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uiUtils.showProgress("Loading..","Please Wait");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            uiUtils.progressDismiss();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                outputUri = SiliCompressor.with(context)
                        .compressVideo(Uri.parse(strings[1]), strings[0]);
                Log.d(TAG, "doInBackground: output uri - "+outputUri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
