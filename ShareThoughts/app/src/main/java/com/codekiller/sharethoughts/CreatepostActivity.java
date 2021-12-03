package com.codekiller.sharethoughts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.codekiller.sharethoughts.UI.ImageProcess;
import com.codekiller.sharethoughts.UI.UIUtils;
import com.codekiller.sharethoughts.UI.VideoProcess;
import com.codekiller.sharethoughts.firebase.FBDatabase;
import com.codekiller.sharethoughts.firebase.FBStorage;
import com.codekiller.sharethoughts.firebase.FBUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatepostActivity extends AppCompatActivity {
    public static final int READ_REQUEST_CODE = 100;
    public static final int IMAGE_REQUEST_CODE = 1;
    public static final int VIDEO_REQUEST_CODE = 2;
    public static final String TAG = "CREATE POST ACTIVITY";

    Toolbar toolbar;
    CircleImageView userImage;
    TextView userName;
    Button photoBtn, videoBtn, postBtn;
    ImageView imageView;
    VideoView videoView;
    EditText descriptionText;
    CardView cardView;

    FBDatabase fbDatabase;
    FBStorage fbStorage;
    FBUtils fbUtils;
    UIUtils uiUtils;
    ImageProcess imageProcess;
    VideoProcess videoProcess;

    Uri uploadUri;
    HashMap<String,String> postMap;
    HashMap<String,String> profileMap;
    String pushKey = "";

    DatabaseReference profileReference;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createpost);
        toolbar = findViewById(R.id.toolbar);
        userImage = findViewById(R.id.user_image);
        userName = findViewById(R.id.user_name);
        photoBtn = findViewById(R.id.photo_btn);
        videoBtn = findViewById(R.id.video_btn);
        postBtn = findViewById(R.id.post_btn);
        imageView = findViewById(R.id.upload_image);
        videoView = findViewById(R.id.upload_video);
        descriptionText = findViewById(R.id.description_view);
        cardView = findViewById(R.id.cardview1);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        uiUtils = new UIUtils(this);
        imageProcess = new ImageProcess(this);
        videoProcess = new VideoProcess(CreatepostActivity.this);
        fbDatabase = new FBDatabase();
        fbStorage = new FBStorage();
        fbUtils = new FBUtils();

        profileReference = fbDatabase.getDB("Users").child(fbUtils.getUID()).child("profile");
        profileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                profileMap = (HashMap<String, String>) snapshot.getValue();
                userName.setText(profileMap.get("username"));
                Glide.with(CreatepostActivity.this)
                        .load(profileMap.get("imageurl"))
                        .into(userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                if(ContextCompat.checkSelfPermission(CreatepostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(CreatepostActivity.this,new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },READ_REQUEST_CODE);
                }else{
                    startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),IMAGE_REQUEST_CODE);
                }
            }
        });

        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                ((RelativeLayout.LayoutParams) cardView.getLayoutParams()).addRule(RelativeLayout.BELOW,R.id.upload_video);
                if(ContextCompat.checkSelfPermission(CreatepostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(CreatepostActivity.this,new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },READ_REQUEST_CODE);
                }else{
                    startActivityForResult(new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
                            VIDEO_REQUEST_CODE);
                }
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: upload uri - "+(uploadUri));
                uploadPost(uploadUri);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void uploadPost(Uri uploaduri) {
        uiUtils.showProgress("Uploading","please wait");
        postMap = new HashMap<>();
        StorageReference reference = fbStorage.getStorage("Posts")
                .child("IMG_"+System.currentTimeMillis()+"."+uiUtils.getExtension(uploaduri));
                reference.putFile(uploaduri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "onSuccess: download uri - "+uri);
                                        pushKey = fbDatabase.getDB("Posts").push().getKey();//post's data upload into db
                                        postMap.put("authorname",profileMap.get("username"));
                                        postMap.put("authordpurl",profileMap.get("imageurl"));
                                        postMap.put("author_uid",profileMap.get("userid"));
                                        postMap.put("timedate",uiUtils.getDate()+" : "+uiUtils.getTime());
                                        postMap.put("post_imageurl",uri.toString());
                                        postMap.put("no_of_likes","0");
                                        postMap.put("no_of_comments","0");
                                        postMap.put("description_text",descriptionText.getText().toString());
                                        postMap.put("push_key",pushKey);
                                        fbDatabase.getDB("Posts")
                                                .child(pushKey)
                                                .setValue(postMap)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        uiUtils.toast("db error");
                                                        uiUtils.progressDismiss();
                                                    }
                                                });
                                        postMap.clear();
                                        postMap.put("push_key",pushKey);
                                        postMap.put("islike","false");
                                        fbDatabase.getDB("Users")
                                                .child(fbUtils.getUID())
                                                .child("myposts")
                                                .setValue(postMap)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        uiUtils.toast("my post db error");
                                                        uiUtils.progressDismiss();
                                                    }
                                                });
                                        fbDatabase.getDB("Users")
                                                .child(fbUtils.getUID())
                                                .child("friendspost")
                                                .setValue(postMap)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        uiUtils.toast("friends post db error");
                                                        uiUtils.progressDismiss();
                                                    }
                                                })
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if( task.isSuccessful() ){
                                                            uiUtils.progressDismiss();
                                                            finish();
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        uiUtils.toast("url failed");
                                        uiUtils.progressDismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uiUtils.toast("storage failed");
                        uiUtils.progressDismiss();
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( grantResults[0] == PackageManager.PERMISSION_GRANTED ){
            if( requestCode == IMAGE_REQUEST_CODE ){
                startActivityForResult(new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI),IMAGE_REQUEST_CODE);
            }else if( requestCode == VIDEO_REQUEST_CODE ){
                startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_PICK)
                                .setAction(Intent.ACTION_GET_CONTENT)
                                .setType("video/*"),"select video"),VIDEO_REQUEST_CODE);
            }
        }else{
            uiUtils.toast("permission denied");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( data != null && resultCode == RESULT_OK ){
            if( requestCode == IMAGE_REQUEST_CODE ){
                Log.d(TAG, "onActivityResult: result uri - "+data.getData());
                //uploadUri = imageProcess.processImage(data.getData(),"Posts");
                File dir = new File(new File(Environment.getExternalStorageDirectory().getAbsolutePath()),"ShareThoughts/Posts");
                new imageCompress().execute("false",data.getData().toString(),dir.getPath());
                Log.d(TAG, "onActivityResult: processed uri - "+uploadUri);
            }else if( VIDEO_REQUEST_CODE == requestCode ){
                //videoUri = videoProcess.processVideo(data.getData());
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                File dir = new File(file,"ShareThoughts/Posts/");
                if( !dir.exists() ){
                    dir.mkdirs();
                }
                new videoCompress().execute("false",data.getData().toString(),dir.getPath());
                /*videoView.setVideoURI(data.getData());
                videoView.start();*/
            }
        }

    }

    private class videoCompress extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoUri = null;
            try {
                videoUri = SiliCompressor.with(CreatepostActivity.this)
                        .compressVideo(Uri.parse(strings[1]),strings[2]);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "doInBackground: videouri - "+videoUri);
            return videoUri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uiUtils.showProgress("Loading","Please wait");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            uiUtils.progressDismiss();
            videoView.setVideoURI(Uri.fromFile(new File(s)));
            uploadUri = uiUtils.getVideoUri(s);
            videoView.start();
            Log.d(TAG, "onPostExecute: file path - "+s);
            Log.d(TAG, "onPostExecute: uri - "+uploadUri);
            Log.d(TAG, "onPostExecute: size - "+new File(s).length());
        }
    }

    private class imageCompress extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            String output = SiliCompressor.with(CreatepostActivity.this)
                    .compress(strings[1],new File(strings[2]));
            return output;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Glide.with(CreatepostActivity.this)
                    .load(s)
                    .into(imageView);
            Log.d(TAG, "onPostExecute: uri - "+s);
            uploadUri = uiUtils.getImageUri(s);
            Log.d(TAG, "onPostExecute: uri file - "+uploadUri);
        }
    }
}