package com.codekiller.sharethoughts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.codekiller.sharethoughts.UI.ImageProcess;
import com.codekiller.sharethoughts.UI.UIUtils;
import com.codekiller.sharethoughts.firebase.FBDatabase;
import com.codekiller.sharethoughts.firebase.FBStorage;
import com.codekiller.sharethoughts.firebase.FBUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class RegistrationActivity extends AppCompatActivity {
    public static final int IMAGE_REQUEST = 1;
    public static final int READ_STORAGE_REQUEST = 100;
    public static final String TAG = "REGISTRATION ACTIVITY";
    Uri imageUri;
    String gender, date, downloadImageUri;
    HashMap<String,String> map;

    StorageReference reference;
    DatabaseReference databaseReference;

    Toolbar toolbar;
    EditText userName, mailId, pass, cpass, dob;
    RadioGroup radioGroup;
    Button registerBtn, cancelBtn;
    CircleImageView userImage;
    ImageView chooseImage, editImage;

    UIUtils uiUtils;
    FBUtils fbUtils;
    FBDatabase fbDatabase;
    FBStorage fbStorage;
    ImageProcess imageProcess;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        userName = findViewById(R.id.username_text);
        mailId = findViewById(R.id.mailid_text);
        pass = findViewById(R.id.password_text);
        cpass = findViewById(R.id.cpassword_text);
        dob = findViewById(R.id.dob_text);
        radioGroup = findViewById(R.id.radio_group);
        registerBtn = findViewById(R.id.register_btn);
        cancelBtn = findViewById(R.id.cancel_btn);
        userImage = findViewById(R.id.user_image);
        chooseImage = findViewById(R.id.choose_image);
        editImage = findViewById(R.id.edit_image);

        fbDatabase = new FBDatabase();
        fbStorage = new FBStorage();
        fbUtils = new FBUtils();
        uiUtils = new UIUtils(this);
        imageProcess = new ImageProcess(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) +
                        ContextCompat.checkSelfPermission(RegistrationActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(RegistrationActivity.this,new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },READ_STORAGE_REQUEST);
                }else{
                    startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),IMAGE_REQUEST);
                }
            }
        });
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity(imageUri)
                        .setMultiTouchEnabled(true)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(RegistrationActivity.this);
            }
        });
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(RegistrationActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date = dayOfMonth+"-"+month+"-"+year;
                        dob.setText(date);
                    }
                },cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = userName.getText().toString(), mail = mailId.getText().toString(), password = pass.getText().toString();
                Log.d(TAG, "onClick: datas - "+name+"-"+mail+"-"+dob.getText().toString()+"-"+gender+"-"+password);
                if( name != null && mail != null && dob.getText().toString() != null && gender != null && password != null ){
                    if( password.length() >= 8 ){
                        if( password.equals(cpass.getText().toString()) ){
                            register(name, mail, password);
                        }else{
                            uiUtils.toast("password mismatch");
                        }
                    }else{
                        uiUtils.toast("password must be above 8 characters");
                    }
                }else{
                    uiUtils.toast("fill all the fields");
                }
            }
        });
    }

    //name, mail, date, gender, imageurl, userId
    private void register(String name, String mail, String password) {
        uiUtils.showProgress("Registering","Please Wait !");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(mail,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if( task.isSuccessful() ){
                            if( imageUri != null ){
                                //profile image uploading
                                reference = fbStorage.getStorage("UsersProfileImages")
                                        .child("IMG_"+System.currentTimeMillis()+"."+uiUtils.getExtension(imageUri));
                                reference.putFile(imageUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                reference.getDownloadUrl()//image download url
                                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                downloadImageUri = uri+"";
                                                                Log.d(TAG, "onSuccess: download uri - "+uri);
                                                                map = new HashMap<>();
                                                                map.put("username",name);
                                                                map.put("dob",date);
                                                                map.put("mailid",mail);
                                                                map.put("userid",fbUtils.getUID());
                                                                map.put("gender",gender);
                                                                map.put("imageurl",downloadImageUri);
                                                                Log.d(TAG, "onComplete: datas - "+name+"-"+mail+"-"+date+"-"+gender+"-"+fbUtils.getUID()+"-"+downloadImageUri);
                                                                databaseReference = fbDatabase.getDB("Users");
                                                                databaseReference.child(fbUtils.getUID())
                                                                        .child("profile")
                                                                        .setValue(map)
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                uiUtils.progressDismiss();
                                                                                uiUtils.toast("db error");
                                                                            }
                                                                        })
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                uiUtils.progressDismiss();
                                                                                uiUtils.toast("account created !");
                                                                                startActivity(new Intent(RegistrationActivity.this,MainActivity.class));
                                                                                finish();
                                                                            }
                                                                        });
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                uiUtils.toast("url generate error");
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                uiUtils.toast("image couldn't upload");
                                                uiUtils.progressDismiss();
                                            }
                                        });
                            }else{
                                //downloadImageUri = "default";
                                //user datas uploading
                                map = new HashMap<>();
                                map.put("username",name);
                                map.put("dob",date);
                                map.put("mailid",mail);
                                map.put("userid",fbUtils.getUID());
                                map.put("gender",gender);
                                map.put("imageurl","default");
                                Log.d(TAG, "onComplete: datas - "+name+"-"+mail+"-"+date+"-"+gender+"-"+fbUtils.getUID()+"-"+downloadImageUri);
                                databaseReference = fbDatabase.getDB("Users");
                                databaseReference.child(fbUtils.getUID())
                                        .child("profile")
                                        .setValue(map)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                uiUtils.progressDismiss();
                                                uiUtils.toast("db error");
                                            }
                                        })
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                uiUtils.progressDismiss();
                                                uiUtils.toast("account created !");
                                                startActivity(new Intent(RegistrationActivity.this,MainActivity.class));
                                                finish();
                                            }
                                        });
                            }

                        }else{
                            uiUtils.toast("check your internet connection");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uiUtils.toast("sign in error");
                        uiUtils.progressDismiss();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == IMAGE_REQUEST && data != null && resultCode == RESULT_OK ){
            imageUri = imageProcess.processImage(data.getData(),"User Profile Images");
            Glide.with(RegistrationActivity.this)
                    .load(imageUri)
                    .into(userImage);
            chooseImage.setVisibility(View.GONE);
            editImage.setVisibility(View.VISIBLE);
        }
        if( requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK ){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            Glide.with(RegistrationActivity.this)
                    .load(imageUri)
                    .into(userImage);
        }
    }

    public void genderSelection(View view) {
        switch ( view.getId() ){
            case R.id.id_male:
                gender = "Male";
                break;
            case R.id.id_female:
                gender = "Female";
                break;
            case R.id.id_others:
                gender = "Others";
                break;
        }
        /*int id = radioGroup.getCheckedRadioButtonId();
        if( id == R.id.id_male ){
            gender = "Male";
        }else if( id == R.id.id_female ){
            gender = "Female";
        }else if( id == R.id.id_others ){
            gender = "Others";
        }*/
    }
}