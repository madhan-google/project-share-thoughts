package com.codekiller.sharethoughts.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FBUtils {
    FirebaseAuth auth;
    public FBUtils() {
        auth = FirebaseAuth.getInstance();
    }
    public FirebaseAuth getAuth(){
        return auth;
    }
    public void signOut(){
        FirebaseAuth.getInstance().signOut();
    }
    public String getUID(){
        return auth.getCurrentUser().getUid();
    }
    public FirebaseUser getUser(){
        return auth.getCurrentUser();
    }
}
