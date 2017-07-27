package com.example.mohamedabdelaziz.chatapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

import data.message;

/**
 * Created by Mohamed Abd ELaziz on 7/21/2017.
 */

public class message_adapter extends BaseAdapter {
    ArrayList<message>list_message ;
    Context context ;
    public static String last_message_statues = "send" ;

    public message_adapter(ArrayList<message> list_message, Context context) {
        this.list_message = list_message;
        this.context = context;

    }

    @Override
    public int getCount() {
        return list_message.size();
    }
    @Override
    public Object getItem(int position) {
        return list_message.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = null;

        if(list_message.get(position).getBody().contains("fileuploadingtoyourfiendthismessagenotify"))
        {
            view = layoutInflater.inflate(R.layout.file_uploaded_item, null);
            TextView textView= (TextView) view.findViewById(R.id.file_upload);
            if( !list_message.get(position).getSender().equals(context.getSharedPreferences("logged", context.MODE_PRIVATE).getString("phone", "-1")))
            {
                textView.setText("your friend is uploading file ");
              //  button.setEnabled(false);
            }else if( list_message.get(position).getSender().equalsIgnoreCase(context.getSharedPreferences("logged", context.MODE_PRIVATE).getString("phone", "-1")))
            {
                textView.setText("uploading file ");
               // button.setEnabled(false);
            }

        }else if(list_message.get(position).getBody().contains("fileuploadedtoyourfiendthismessagenotify"))
        {
            view = layoutInflater.inflate(R.layout.file_uploaded_item, null);
            TextView textView= (TextView) view.findViewById(R.id.file_upload);
            textView.setEnabled(true);
                textView.setText("download ");
        }
        else{
             if (list_message.get(position).getSender().equals(context.getSharedPreferences("logged", context.MODE_PRIVATE).getString("phone", "-1")))
                 view = layoutInflater.inflate(R.layout.message_send, null);
             else
                 view = layoutInflater.inflate(R.layout.message_recieve, null);
                 Log.d("message is ",list_message.get(position).getBody()) ;
        TextView message_body = (TextView) view.findViewById(R.id.message_body);
        message_body.setText(list_message.get(position).getBody());
        ImageView imageView = (ImageView) view.findViewById(R.id.status);

        if (position == list_message.size() - 1 && list_message.get(position).getSender().equalsIgnoreCase(context.getSharedPreferences("logged", context.MODE_PRIVATE).getString("phone", "-1")))
        { if (last_message_statues.equalsIgnoreCase("send"))
                imageView.setImageResource(R.drawable.send);
            else if (last_message_statues.equalsIgnoreCase("rec"))
                imageView.setImageResource(R.drawable.rec);
            else if (last_message_statues.equalsIgnoreCase("seen"))
                imageView.setImageResource(R.drawable.seen);
            }
   }

        return view;
    }
}