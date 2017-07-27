package com.example.mohamedabdelaziz.chatapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import data.message ;
/**
 * Created by Mohamed Abd ELaziz on 7/22/2017.
 */

public class project_services extends Service {

    public static boolean service_started =false ;
    static boolean main_activity_started =false ;
    static boolean message_activity_started =false ;
    public static Context context ;
    public static message last_message=new message("non","non");
    static int seconds =-1,minute =-1 ,hour=-1 ;
    static Realtime_database realtime_database ;
    @Override
    public void onCreate() {
        super.onCreate();
    service_started=true ;
        Log.d("","service_started"+service_started) ;
    }

    public static void set_context(Context context1)
    {
     context=context1;
        realtime_database =new Realtime_database(context) ;

    }
    public static void Monotor_request()
    {
            realtime_database.get_messages_requests();
    }
    public static void Send_request(String name ,String phone,String statues ,String birth)
    {
        realtime_database.send_message_request(name,phone,statues,birth);
        Log.d("","send_request"+phone) ;
    }
    public static void  read_message(String phone)
    {
        realtime_database.read_message(phone,"");
    }
    public static void writeMessage(String text, String phone,String birth)
    {
        realtime_database.writeMessage(text,phone,birth);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    service_started=false ;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
