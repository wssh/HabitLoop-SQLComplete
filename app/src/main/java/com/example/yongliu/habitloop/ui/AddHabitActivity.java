package com.example.yongliu.habitloop.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.yongliu.habitloop.R;
import com.example.yongliu.habitloop.models.Habit;
import com.example.yongliu.habitloop.models.Storage;
import com.example.yongliu.habitloop.models.WeekDays;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.example.yongliu.habitloop.models.JSONParser;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddHabitActivity extends AppCompatActivity {
    //habit name needs to save
    @Bind(R.id.habitNameInput) EditText habitNameEditText;

    @Bind(R.id.timeLabel) TextView timeLabelTextView;
    @Bind(R.id.daysLabel) TextView daysLabelTextView;
    //time needs to save
    @Bind(R.id.pickTimeStart) EditText pickTimeStartEdit;
    @Bind(R.id.pickTimeEnd) EditText pickTimeEndEdit;
    //checkbox value needs save
    @Bind(R.id.mondayCheck) CheckBox monCheck;
    @Bind(R.id.tuesdayCheck) CheckBox tueCheck;
    @Bind(R.id.wednesdayCheck) CheckBox wedCheck;
    @Bind(R.id.thursdayCheck) CheckBox thuCheck;
    @Bind(R.id.fridayCheck) CheckBox friCheck;
    @Bind(R.id.saturdayCheck) CheckBox satCheck;
    @Bind(R.id.sundayCheck) CheckBox sunCheck;

    private CheckBox [] mCheckBoxes;

    //input values from views
    private String mHabitName;
    private String mStartTime;
    private String mEndTime;
    private boolean [] mCheckDays;
    private ProgressDialog pDialog;
    private JSONParser jsonParser = new JSONParser();
    private String check_username;

    //change the URL in order to connect to your localhost or server. This is connected to my local RPI. -Ricky
    private static final String ADD_HABIT_URL = "http://192.168.29.203/addhabit.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    //Storage
    private Storage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AddHabitActivity.this);
        check_username = sp.getString("username", "anon");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);

        ButterKnife.bind(this);
        setOnclickTimeDialog(pickTimeStartEdit);
        setOnclickTimeDialog(pickTimeEndEdit);

        mStorage = new Storage(this);

        mCheckBoxes = new CheckBox[] {monCheck, tueCheck, wedCheck, thuCheck, friCheck,
                satCheck, sunCheck};
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStorage.saveToInternalStorage(Storage.mHabits);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_habit_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) { //saving data for new habit
            //getting the infos from the Views and set it in new habit for storage
            if(checkInfoError()) {
                WeekDays days = new WeekDays(mCheckDays);
                Habit hb = new Habit(mHabitName, 0, mStartTime, mEndTime, days);

                Storage.mHabits.add(hb);
                mStorage.saveToInternalStorage(Storage.mHabits);
                //Run the SQL command ONLY if someone is logged in.
                if(!check_username.equals("anon"))
                    new PostHabit().execute();

                finish();
            }
            else{
                //do nothing
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setOnclickTimeDialog(final EditText timeEdit){
        timeEdit.setFocusable(false);
        timeEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddHabitActivity.this, new TimePickerDialog.OnTimeSetListener
                        () {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timeEdit.setText( selectedHour + ":" + String.format("%02d",
                                selectedMinute) ); //01, 02... 2 digit representation for mins
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
    }

    //return which box for weekdays is checked
    private boolean [] getCheckedDays(){
        boolean [] checks = {false, false, false, false, false, false, false};
        for(int i = 0; i< checks.length; i++){
            if(mCheckBoxes[i].isChecked()){
                checks[i] = true;
            }
        }

        return checks;
    }

    private String [] getCheckedDaysStr(){
        String [] checks = {"0", "0", "0", "0", "0", "0", "0"};
        for(int i = 0; i< checks.length; i++){
            if(mCheckBoxes[i].isChecked()){
                checks[i] = "1";
            }
        }

        return checks;
    }

    //
    private boolean checkInfoError() {
        boolean allCorrect = true;
        mHabitName = habitNameEditText.getText().toString();
        mStartTime = pickTimeStartEdit.getText().toString();
        mEndTime = pickTimeEndEdit.getText().toString();
        mCheckDays = getCheckedDays();

        if(mHabitName.matches("")){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_name_empty_title)
                    .setMessage(R.string.dialog_name_empty_message)
                    .setPositiveButton(R.string.dialog_ok_button, null)
                    .show();
            allCorrect = false;
        }

        else if(!mStartTime.matches("") && !mEndTime.matches("")){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                Date startTime = sdf.parse(mStartTime);
                Date endTime = sdf.parse(mEndTime);
                if(startTime.compareTo(endTime) == 1){
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_time_error_title)
                            .setMessage(R.string.dialog_time_error_message)
                            .setPositiveButton(R.string.dialog_ok_button, null)
                            .show();
                    allCorrect = false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(mStartTime.matches("")){
            mStartTime = "Unset Time";
        }

        if(mEndTime.matches("")){
            mEndTime = "Unset Time";
        }

        return allCorrect;
    }

    class PostHabit extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddHabitActivity.this);
            pDialog.setMessage("Adding Habit...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            int success;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AddHabitActivity.this);
            String post_username = sp.getString("username", "anon");
            String post_habit = habitNameEditText.getText().toString();
            String post_timestart = pickTimeStartEdit.getText().toString();
            String post_timeend = pickTimeEndEdit.getText().toString();
            String post_mon = "0";
            String post_tue = "0";
            String post_wed = "0";
            String post_thu = "0";
            String post_fri = "0";
            String post_sat = "0";
            String post_sun = "0";
            String[] boolDays = getCheckedDaysStr();
            if (boolDays[0] == "1")
                post_mon = "1";
            if (boolDays[1] == "1")
                post_tue = "1";
            if (boolDays[2] == "1")
                post_wed = "1";
            if (boolDays[3] == "1")
                post_thu = "1";
            if (boolDays[4] == "1")
                post_fri = "1";
            if (boolDays[5] == "1")
                post_sat = "1";
            if (boolDays[6] == "1")
                post_sun = "1";

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", post_username));
                params.add(new BasicNameValuePair("habitname", post_habit));
                params.add(new BasicNameValuePair("times", post_timestart));
                params.add(new BasicNameValuePair("timef", post_timeend));
                params.add(new BasicNameValuePair("streak", "0"));
                params.add(new BasicNameValuePair("daym", post_mon));
                params.add(new BasicNameValuePair("dayt", post_tue));
                params.add(new BasicNameValuePair("dayw", post_wed));
                params.add(new BasicNameValuePair("dayth", post_thu));
                params.add(new BasicNameValuePair("dayf", post_fri));
                params.add(new BasicNameValuePair("days", post_sat));
                params.add(new BasicNameValuePair("daysu", post_sun));

                Log.d("request!", "starting");

                JSONObject json = jsonParser.makeHttpRequest(ADD_HABIT_URL, "POST", params);

                Log.d("Post Habit attempt", json.toString());

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Habit Added!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Habit failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(AddHabitActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }

}
