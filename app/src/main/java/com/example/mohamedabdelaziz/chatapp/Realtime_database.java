package com.example.mohamedabdelaziz.chatapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import data.User;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Mohamed Abd ELaziz on 7/21/2017.
 */
public class Realtime_database {
    Context context ;
    final String messages_node ="messages" ;
    final String request_message_node ="requests" ;
    private DatabaseReference mDatabase;
    String myphone ;
    String users_node="users" ;
    public static database db ;
    public static boolean just_played =false ;
    int loop_index = 0;
    public Realtime_database(Context context)
    {
     this.context =context ;
        myphone =context.getSharedPreferences("logged",Context.MODE_PRIVATE).getString("phone","-1") ;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        db=new database(context);
        SQLiteDatabase sqLiteDatabase=db.getWritableDatabase();

    }
    public void read_message(final String nextphone, final String birth)
    {
        Log.d("size_before",Message.list_message.size()+"") ;
        mDatabase.child(messages_node).child(detemined_node(nextphone)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("", "added");
                if (dataSnapshot != null) {
                    Log.d("", "child");
                    Iterator i = dataSnapshot.getChildren().iterator();
                    while (i.hasNext()) {
                        Log.d("", "while");
                        try {
                            String body = (String) ((DataSnapshot) i.next()).getValue();
                            String sender = (String) ((DataSnapshot) i.next()).getValue();
                            String statues = (String) ((DataSnapshot) i.next()).getValue();
                            if(!sender.equals(myphone) && !statues.equals("seen") ) {
                                if(project_services.last_message.getBody()!=body && project_services.last_message.getSender()!=sender){
                                    project_services.last_message.setBody(body);
                                    project_services.last_message.setSender(sender);
                                db.insert_message(nextphone,body,sender,statues);
                                    if(project_services.message_activity_started) {
                                        mDatabase.child(messages_node).child(detemined_node(nextphone)).child(dataSnapshot.getKey()).child("statues").setValue("seen");
                                        send_message_request(context.getSharedPreferences("logged",Context.MODE_PRIVATE).getString("username",""),"0"+nextphone,"seen",birth);
                                    } else if(!project_services.message_activity_started) {
                                        mDatabase.child(messages_node).child(detemined_node(nextphone)).child(dataSnapshot.getKey()).child("statues").setValue("rec");
                                        send_message_request(context.getSharedPreferences("logged",Context.MODE_PRIVATE).getString("username",""),"0"+nextphone,"rec",birth);
                                    }
                                }
                            }

                        }catch (Exception e){}
                }
                }
                if(project_services.message_activity_started) {
                    Message.list_message.clear();
                    Message.list_message = db.restore_message(nextphone);
                    Message.listView.setAdapter(new message_adapter(Message.list_message, context));
                }
                else
                    push_notification("new message",nextphone);
            }
            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    Log.d("", "child");
                    final Iterator i = dataSnapshot.getChildren().iterator();

                    while (i.hasNext()) {
                        Log.d("", "while");
                        String body = "non", sender = myphone, statues = "send";
                        try {
                             body = (String) ((DataSnapshot) i.next()).getValue();
                             sender = (String) ((DataSnapshot) i.next()).getValue();
                             statues = (String) ((DataSnapshot) i.next()).getValue();
                           {
                                if (sender.equals(myphone) && statues.equals("seen")) {
                                    dataSnapshot.getRef().removeValue();
                                }
                                    message_adapter.last_message_statues=statues;
                               Message.listView.setAdapter(new message_adapter(Message.list_message, context));
                            }
                        }catch (Exception e)
                        {}
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        if(project_services.message_activity_started) {
            Message.list_message.clear();
            Message.list_message = db.restore_message(nextphone);
            Message.listView.setAdapter(new message_adapter(Message.list_message, context));
        }
        else
            push_notification("new message",nextphone);
    }
    public boolean new_user_register( String name, String phone ,String birthday) {
        User user = new User(name, phone,"reg",birthday);
        mDatabase.child("users").child(phone).setValue(user);
        SharedPreferences sharedPreferences =context.getSharedPreferences("logged",Context.MODE_APPEND) ;
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putBoolean("logged",true) ;
        editor.putString("phone",phone) ;
        editor.commit() ;
        return true;
    }
    public void writeMessage( String data,String nextphone,String birth) {
        String node =detemined_node(nextphone) ;
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            String temp_key = mDatabase.child(messages_node).child(node).push().getKey();
            mDatabase.child(messages_node).child(node).updateChildren(map);
            DatabaseReference message_root = mDatabase.child(messages_node).child(node).child(temp_key);
            Map<String, Object> map2 = new HashMap<String, Object>();
            map2.put("message", data);
            map2.put("sender",myphone);
            map2.put("statues", "send");
            message_root.updateChildren(map2);
            send_message_request(context.getSharedPreferences("logged",Context.MODE_PRIVATE).getString("username",""),"0"+nextphone,"send",birth);
            db.insert_message(nextphone, data, myphone, "send");
        }catch (Exception e)
        {}
    }
    public void get_my_contacts()
    {
        try{
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
            while (phones.moveToNext())
            {
                String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                MainActivity.users.add(new User(name,phoneNumber.replace("+2","").replace("\\s","") , "non","")) ;
            }
            phones.close();
        }catch (Exception e)
        {}
        MainActivity.listview.setAdapter(new list_adapter(context,MainActivity.users));
    }
    public void find(final ArrayList<User>userArrayList)
    {

        Log.d("","start method ");
        for ( loop_index =0 ; loop_index < userArrayList.size(); loop_index++) {
            mDatabase.child(users_node).child(userArrayList.get(loop_index).getPhone()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                              MainActivity.users.add(userArrayList.get(loop_index));
                                Log.d("","start if  ");
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("","method cancel ");
                        }

                    });
        }
        MainActivity.listview.setAdapter(new list_adapter(context,MainActivity.users));
    }
    public void send_message_request(String name ,String nextphone,String statues,String birth)
    {
        String temp =null ;
        if(nextphone.length()==11)
        nextphone="-"+nextphone ;
        else if(nextphone.length()==10)
            nextphone="-0"+nextphone ;
        if(myphone.length()==11)
            temp=myphone ;
        else if(myphone.length()==10)
            temp="0"+myphone ;
        Map<String, Object> map = new HashMap<String, Object>();
        mDatabase.child(request_message_node).child(nextphone).updateChildren(map);
        DatabaseReference message_root = mDatabase.child(request_message_node).child(nextphone).child(temp);
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("name", name);
        map2.put("phone", temp);
        map2.put("statues",statues) ;
        map2.put("birth",birth) ;
        message_root.updateChildren(map2);
    }
    public void get_messages_requests() {
        MainActivity.users.clear();
        String temp =null ;
        if(myphone.length()==11)
            temp=myphone ;
        else if(myphone.length()==10)
            temp="0"+myphone ;

        mDatabase.child(request_message_node).child("-"+temp).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("", "added");
                if (dataSnapshot != null) {
                    Iterator i = dataSnapshot.getChildren().iterator();
                    while (i.hasNext()) {
                        try {
                            String birth = (String) ((DataSnapshot) i.next()).getValue();
                            String name = (String) ((DataSnapshot) i.next()).getValue();
                            String sender = (String) ((DataSnapshot) i.next()).getValue();
                            String statues = (String) ((DataSnapshot) i.next()).getValue();
                            db.insert_friend(sender, name, statues,birth);
                        }catch (Exception e)
                        {}
                    }
                }

                if (project_services.main_activity_started) {
                    MainActivity.users = db.restore_friends();
                    MainActivity.listview.setAdapter(new list_adapter(context, MainActivity.users));
                }
                else
                    push_notification("new message request","");
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String st ="";
                Log.d("","change") ;
                if (dataSnapshot != null) {
                    Iterator i = dataSnapshot.getChildren().iterator();
                    while (i.hasNext()) {
                        try {
                            String birth = (String) ((DataSnapshot) i.next()).getValue();
                            String name = (String) ((DataSnapshot) i.next()).getValue();
                            String sender = (String) ((DataSnapshot) i.next()).getValue();
                            String statues = (String) ((DataSnapshot) i.next()).getValue();
                            db.insert_friend(sender, name, statues,birth);
                            st=statues ;
                            push_notification("accept new message request", "");
                        }catch (Exception e)
                        {}
                    }
                }
                if (project_services.main_activity_started) {
                    MainActivity.users = db.restore_friends();
                    MainActivity.listview.setAdapter(new list_adapter(context, MainActivity.users));
                }
                else {
                    if(st.equals("add"))
                    push_notification("accept new message request", "");

                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if (project_services.main_activity_started) {
            MainActivity.users = db.restore_friends();
            MainActivity.listview.setAdapter(new list_adapter(context, MainActivity.users));
        }
    }
    public void push_notification(String title ,String body)
    {
        if(!just_played) {
         just_played=true ;
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(context);
        builder.setAutoCancel(false);
        builder.setSmallIcon(R.drawable.message)
                .setTicker(body)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setSound(notification)
                .setContentText(body);
        Intent intent=new Intent(context,MainActivity.class);
        PendingIntent pendingIntent =PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent) ;
        NotificationManager nm =(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1000,builder.build());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    just_played=false ;
                }
            },100);
        }
    }
    public String detemined_node(String nextphone)
    {
        try{
            String temp=null ;
           if( Integer.parseInt(myphone.replace("+2","").trim()) > Integer.parseInt(nextphone.replace("+2","").trim()) ) {
               if(nextphone.length()==11)
                   nextphone="-"+nextphone ;
                else if(nextphone.length()==10)
                   nextphone="-0"+nextphone ;
               if(myphone.length()==11)
                   temp=myphone ;
               else if(myphone.length()==10)
               temp="0"+myphone ;
               return nextphone + temp;
           }
            else
           {
               if(myphone.length()==11)
                   temp="-"+myphone ;
               else if(myphone.length()==10)
                   temp="-0"+myphone ;
               if(nextphone.length()==11)
                   nextphone=nextphone ;
               else if(nextphone.length()==10)
                   nextphone="0"+nextphone ;
               return temp+nextphone ;}
        }catch (Exception e)    {return  null;}
    }
}