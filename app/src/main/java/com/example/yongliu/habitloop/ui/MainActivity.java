package com.example.yongliu.habitloop.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yongliu.habitloop.R;
import com.example.yongliu.habitloop.adapters.HabitAdapter;
import com.example.yongliu.habitloop.models.Habit;
import com.example.yongliu.habitloop.models.JSONParser;
import com.example.yongliu.habitloop.models.Storage;
import com.example.yongliu.habitloop.models.WeekDays;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.mainHabitsList) ListView mHabitListView;
    HabitAdapter mHabitAdapter;
    Storage mStorage;
    String post_username;
    ProgressDialog pDialog;
    private JSONArray jHabits = null;

    private static final String VIEW_HABITS_URL = "http://192.168.29.203/viewhabits.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_HABIT_NAME = "habitname";
    private static final String TAG_HABIT = "habit";
    private static final String TAG_HABIT_ID = "habit_id";
    private static final String TAG_TIME_START = "timestart";
    private static final String TAG_TIME_FINISH = "timefinish";
    private static final String TAG_STREAK = "streak";
    private static final String TAG_MON = "day_mon";
    private static final String TAG_TUES = "day_tues";
    private static final String TAG_WED = "day_wed";
    private static final String TAG_THURS = "day_thurs";
    private static final String TAG_FRI = "day_fri";
    private static final String TAG_SAT = "day_sat";
    private static final String TAG_SUN = "day_sun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("main activity", "oncreate called");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_toolbar_edit_48);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        ButterKnife.bind(this);
        mStorage = new Storage(this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        post_username = sp.getString("username", "anon");
        if(!post_username.equals("anon"))
            new loadHabits().execute();
        mHabitAdapter = new HabitAdapter(this, Storage.mHabits);
        mHabitListView.setAdapter(mHabitAdapter);
        setListViewClickListener();
    }

    @Override
    protected void onStart() {
        Log.d("main activity", "onstart called");
        super.onStart();
        if(post_username.equals("anon")) {
            Storage.mHabits = mStorage.readFromInternalStorage();
            mHabitAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        Log.d("main activity", "onresume called");
        super.onResume();
        if(post_username.equals("anon")) {
            Storage.mHabits = mStorage.readFromInternalStorage();
            mHabitAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        Log.d("main activity", "onpause called");
        super.onPause();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        mStorage.saveToInternalStorage(Storage.mHabits);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("main activity", "creatoptionsmenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("main activity","onoptionsitemselected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_habit) {
            Intent intent = new Intent(this, AddHabitActivity.class);
            startActivity(intent);
        }

        else if (id == R.id.action_check_statistics) {
            if(Storage.mHabits.isEmpty()){
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_empty_habit_title)
                        .setMessage(R.string.dialog_empty_habit_message)
                        .setPositiveButton(R.string.dialog_ok_button, null)
                        .show();
            }
            else {
                Intent intent = new Intent(this, GraphActivity.class);
                startActivity(intent);
            }
        }

        else if (id == R.id.action_edit_user) {
            if(post_username.equals("anon")) {
                Intent intent = new Intent(this, login.class);
                startActivity(intent);
            }
            else{
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Would you like to log out?")
                        .setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor edit = sp.edit();
                                String logoutUser = "anon";
                                edit.putString("username", logoutUser);
                                edit.commit();
                                Storage.mHabits.clear();
                                Toast.makeText(MainActivity.this, post_username + " has logged out.", Toast.LENGTH_LONG).show();
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.dialog_negative_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        }).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setListViewClickListener(){
        mHabitListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                intent.putExtra(getString(R.string.EXTRA_HABIT_CLICKED_INDEX), position);
                startActivity(intent);
            }
        });
    }

    public class loadHabits extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            Log.d("main activity","loading preexecute");
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading Habits...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected Boolean doInBackground(Void... arg0){
            Log.d("main activity","loading background");
            updateJSONdata();
            return null;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("main activity", "loading post exe");
            super.onPostExecute(result);
            pDialog.dismiss();
            mHabitAdapter = new HabitAdapter(MainActivity.this, Storage.mHabits);
            mHabitListView.setAdapter(mHabitAdapter);
        }
    }

    public void updateJSONdata(){
        Storage.mHabits.clear();
        JSONParser jsonParser = new JSONParser();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", post_username));
        JSONObject json = JSONParser.makeHttpRequest(
                VIEW_HABITS_URL, "POST", params);

        try{
            jHabits = json.getJSONArray(TAG_HABIT);
            Storage.mHabits.clear();
            for(int i = 0; i < jHabits.length(); i++){
                JSONObject c = jHabits.getJSONObject(i);

                String habitName = c.getString(TAG_HABIT_NAME);
                int habitID = c.getInt(TAG_HABIT_ID);
                String sTime = c.getString(TAG_TIME_START);
                String fTime = c.getString(TAG_TIME_FINISH);
                int streak = c.getInt(TAG_STREAK);
                int mon = c.getInt(TAG_MON);
                int tue = c.getInt(TAG_TUES);
                int wed = c.getInt(TAG_WED);
                int thu = c.getInt(TAG_THURS);
                int fri = c.getInt(TAG_FRI);
                int sat = c.getInt(TAG_SAT);
                int sun = c.getInt(TAG_SUN);
                boolean [] days = {false, false, false, false, false, false, false};
                if (mon == 1){
                    days[0] = true;
                }
                if (tue == 1){
                    days[1] = true;
                }
                if (wed == 1){
                    days[2] = true;
                }
                if (thu == 1){
                    days[3] = true;
                }
                if (fri == 1){
                    days[4] = true;
                }
                if (sat == 1){
                    days[5] = true;
                }
                if (sun == 1){
                    days[6] = true;
                }
                WeekDays wd = new WeekDays(days);

                Habit hb = new Habit(habitName, habitID, streak, sTime, fTime, wd);
                Storage.mHabits.add(hb);

            }
            mStorage.saveToInternalStorage(Storage.mHabits);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }

}
