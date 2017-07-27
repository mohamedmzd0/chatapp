package com.example.mohamedabdelaziz.chatapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import data.User;

/**
 * Created by Mohamed Abd ELaziz on 7/21/2017.
 */

public class list_adapter extends BaseAdapter {
    Context context;
    ArrayList<User> users ;
    FirebaseStorage storage ;
    public list_adapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
        storage = FirebaseStorage.getInstance();
    }

    @Override

    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view =layoutInflater.inflate(R.layout.message_item,null);
        ImageView profile_image = (ImageView) view.findViewById(R.id.profile_image) ;
        TextView profile_name = (TextView) view.findViewById(R.id.profile_name);
        TextView last_message= (TextView) view.findViewById(R.id.last_message);
        profile_name.setText(MainActivity.users.get(position).getUsername());
        last_message.setText(MainActivity.users.get(position).getUsername());
        viewprofile(profile_image,users.get(position).getPhone());
        return view;
    }
    private void viewprofile(final ImageView imageView , String phone) {
        StorageReference storageRef = storage.getReferenceFromUrl("gs://chatapp-fba27.appspot.com");
        StorageReference islandRef = storageRef.child("profile"+phone);
        final long ONE_MEGABYTE = 256 * 256;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }
}