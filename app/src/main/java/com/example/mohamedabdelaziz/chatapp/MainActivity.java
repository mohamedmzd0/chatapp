package com.example.mohamedabdelaziz.chatapp;

;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import data.User;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences ;
    public static ListView listview ;
    public static  ArrayList<User> users = new ArrayList<>();
    Realtime_database realtime_database ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        listview = (ListView) findViewById(R.id.requests);
        setSupportActionBar(toolbar);
        users = new ArrayList<>();
        realtime_database=new Realtime_database(getApplicationContext()) ;
        sharedPreferences = getSharedPreferences("logged", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("logged", false) || sharedPreferences.getString("phone", "-1").equals("-1"))
         startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                findViewById(R.id.search_box).setVisibility(View.VISIBLE);
            }
        });

        if((!project_services.service_started) || project_services.context==null ) {
            startService(new Intent(getApplicationContext(), project_services.class));
            project_services.set_context(getApplicationContext());
        }
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent= new Intent(getApplicationContext(),Message.class) ;
                        intent.putExtra("phone",users.get(position).getPhone()) ;
                        intent.putExtra("name",users.get(position).getUsername()) ;
                        intent.putExtra("statues",users.get(position).getStatues()) ;
                        intent.putExtra("birth",users.get(position).getBirthday()) ;
                        startActivity(intent);
                        overridePendingTransition(R.transition.push_up_in,R.transition.push_up_out);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        project_services.main_activity_started=true ;
        findViewById(R.id.search_box).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text =  ((TextView)findViewById(R.id.search_box)).getText().toString();
                if(!text.isEmpty())
                {
                    if(project_services.service_started)
                        project_services.Send_request(getSharedPreferences("logged",MODE_PRIVATE).getString("username","not access"),text,"add",sharedPreferences.getString("birth","- - - "));
                }
            }
        });
        project_services.Monotor_request();
    }

    @Override
    protected void onStop() {
        super.onStop();
    project_services.main_activity_started=false ;
    }

    @Override
    protected void onResume() {
        super.onResume();
    onStart();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            return true;
        }
        if (id == R.id.search) {
        users.clear();
        realtime_database.get_my_contacts() ;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}