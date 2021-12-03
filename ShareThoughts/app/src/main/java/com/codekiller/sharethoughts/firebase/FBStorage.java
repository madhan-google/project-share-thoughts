package com.codekiller.sharethoughts.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBStorage {
    FirebaseStorage firebaseStorage;

    public FBStorage() {
        firebaseStorage = FirebaseStorage.getInstance();
    }
    public StorageReference getStorage(String name){
        return firebaseStorage.getReference(name);
    }
}
