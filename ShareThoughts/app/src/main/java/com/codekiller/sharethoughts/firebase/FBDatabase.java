package com.codekiller.sharethoughts.firebase;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FBDatabase {
    Context context;
    //FBUtils fbUtils;
    FirebaseDatabase firebaseDatabase;
    public FBDatabase() {
        //this.context = context;
        //fbUtils = new FBUtils();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }
    public DatabaseReference getDB(String name){
        return firebaseDatabase.getReference(name);
    }

}
