package com.example.mohamedabdelaziz.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import data.User;


public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView name;
    private EditText phone ,birthday;
    private DatabaseReference mDatabase;
    private SharedPreferences sharedPreferences;
    String mPhoneNumber = null;
    private static final int REQUEST_PICK_FILE = 1;
    private File selectedFile;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        name = (AutoCompleteTextView) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);
        birthday= (EditText) findViewById(R.id.birthday);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        storage = FirebaseStorage.getInstance();
        sharedPreferences = getSharedPreferences("logged", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("logged", false) || !sharedPreferences.getString("phone", "-1").equals("-1"))
        {
            phone.setText(sharedPreferences.getString("phone","-1"));
            name.setText(sharedPreferences.getString("username","-1"));
            phone.setEnabled(false);
            mEmailSignInButton.setText("save change");
            viewprofile() ;
        }
        else {
            try {
                TelephonyManager tMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                mPhoneNumber = tMgr.getLine1Number();
                phone.setText(mPhoneNumber);
            } catch (Exception e) {
            }
        }
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getdata();
            }
        });
    }

    private void viewprofile() {
        StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.firebase_storage_url));
        StorageReference islandRef = storageRef.child("profile"+sharedPreferences.getString("phone", "-1"));

        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ( (ImageView)findViewById(R.id.image_profile)).setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(LoginActivity.this, "network probelm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void getdata() {
        Toast.makeText(this, "getdata", Toast.LENGTH_SHORT).show();
        if (!(name.getText().toString().isEmpty() || phone.getText().toString().isEmpty() || name.getText().toString().equalsIgnoreCase("") || phone.getText().toString().equalsIgnoreCase("") || phone.getText().length() > 11 || phone.getText().length() < 11
              || birthday.getText().toString().isEmpty() )) {
            try {
                start_upload(phone.getText().toString().replace("+2","").trim());
                findViewById(R.id.progress).setVisibility(View.VISIBLE);
                findViewById(R.id.email_login_form).setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                Log.d("error data", "");
            }
        } else
            Toast.makeText(this, "Error check your inputs", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void update_profile(View view) {
        Intent intent = new Intent(this, FilePicker.class);
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case REQUEST_PICK_FILE:

                    if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {

                        selectedFile = new File
                                (data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                       // start_upload(selectedFile.getPath());
                        ((ImageButton)findViewById(R.id.image_profile)).setImageURI(Uri.parse(selectedFile.getPath()));
                    }
                    break;
            }
        }
    }

    private void start_upload(String mPhoneNumber)
    {

        Realtime_database realtime_database =new Realtime_database(getApplicationContext());
        Uri file = Uri.fromFile(new File(selectedFile.getPath()));
        StorageReference strageRef = storage.getReferenceFromUrl("gs://chatapp-fba27.appspot.com");
        StorageReference riversRef = strageRef.child("profile"+mPhoneNumber);
        UploadTask uploadTask = riversRef.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Fialed to upload", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("phone", phone.getText().toString().replace("+2", "").replace("\\s", "").trim());
                editor.putString("username", name.getText().toString());
                editor.commit();
                Integer.parseInt(phone.getText().toString());
                Realtime_database realtime_database = new Realtime_database(getApplicationContext());
                realtime_database.new_user_register(name.getText().toString(), phone.getText().toString(),birthday.getText().toString());
                finish();
            }
        });

    }
}

