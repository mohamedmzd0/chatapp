package com.example.mohamedabdelaziz.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import data.message;
public class Message extends AppCompatActivity {
    private DatabaseReference mDatabase;
    EditText messag;
    Button send;
    database db;
    String next_phone = null, myphone , username , birthday;
    SharedPreferences sharedPreferences;
    public static ListView listView;
    public static ArrayList<message> list_message;
    private static final int REQUEST_PICK_FILE = 1;
    private File selectedFile;
    FirebaseStorage storage ;
   // StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        username =getIntent().getStringExtra("name") ;
        toolbar.setTitle(username);
        birthday=getIntent().getStringExtra("birth");
        setSupportActionBar(toolbar);
        list_message = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        messag = (EditText) findViewById(R.id.message);
        send = (Button) findViewById(R.id.send);
        sharedPreferences = getSharedPreferences("logged", MODE_PRIVATE);
        listView = (ListView) findViewById(R.id.list_message);
        next_phone = getIntent().getStringExtra("phone");
        myphone = sharedPreferences.getString("phone", "-1");
        next_phone = next_phone.replace("+2", "");
        next_phone = next_phone.replaceAll("\\s", "");
         storage = FirebaseStorage.getInstance();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messag.getText().toString().isEmpty() || messag.getText().equals(" ")) {
                    project_services.writeMessage(messag.getText().toString(), next_phone,sharedPreferences.getString("birth","---"));
                    messag.setText("");
                }
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(list_message.get(position).getBody().contains("fileuploadedtoyourfiendthismessagenotify")) {
                    download_file(list_message.get(position).getBody().replace("fileuploadedtoyourfiendthismessagenotify", "").trim().toString());
                    Toast.makeText(Message.this, "downloading file", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        project_services.message_activity_started = true;
        project_services.read_message(next_phone);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        project_services.message_activity_started = false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
//        menu.findItem(R.id.video).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        menu.findItem(R.id.call).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.profileactivity)
        {
            Intent intent=new Intent(getApplicationContext(),friend_details.class);
            intent.putExtra("phone",next_phone) ;
            intent.putExtra("name",username) ;
            intent.putExtra("birth",birthday) ;
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    public void upload_files(View view) {
        Intent intent = new Intent(this, FilePicker.class);
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    private void start_upload(String path)
    {
        Log.d("path  ",path) ;
        Realtime_database realtime_database =new Realtime_database(getApplicationContext());
        final Uri file = Uri.fromFile(new File(path));
        StorageReference strageRef = storage.getReferenceFromUrl("gs://chatapp-fba27.appspot.com");
        StorageReference riversRef = strageRef.child(realtime_database.detemined_node(next_phone)+"/"+file.getLastPathSegment().trim());
        UploadTask uploadTask = riversRef.putFile(file);
        project_services.writeMessage("fileuploadingtoyourfiendthismessagenotify"+file.getLastPathSegment().trim(),next_phone ,sharedPreferences.getString("birth","---").toString());
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(Message.this, "Fialed to upload", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                project_services.writeMessage("fileuploadedtoyourfiendthismessagenotify"+file.getLastPathSegment().trim(),next_phone,sharedPreferences.getString("birth","---"));
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }

    private void download_file(String file) {
        Realtime_database realtime_database =new Realtime_database(getApplicationContext());
        StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.firebase_storage_url));
        storageRef.child(realtime_database.detemined_node(next_phone)+"/"+file.trim())
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Toast.makeText(Message.this, "Downloaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(Message.this, "can't download file", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case REQUEST_PICK_FILE:

                    if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {

                        selectedFile = new File
                                (data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                      start_upload(selectedFile.getPath());
                    }
                    break;
            }
        }
    }

    public void add_emoji(View view) {
        messag.setText(messag.getText()+((TextView)view).getText().toString());
    }


}