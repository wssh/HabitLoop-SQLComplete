package com.example.yongliu.habitloop.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.yongliu.habitloop.R;
import com.example.yongliu.habitloop.models.Habit;
import com.example.yongliu.habitloop.models.JSONParser;
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

import butterknife.Bind;
import butterknife.ButterKnife;

public class InfoEditActivity extends AppCompatActivity {

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
    //save delete buttons
    @Bind(R.id.infoEditDeleteButton) Button deleteButton;
    @Bind(R.id.infoEditSaveButton) Button saveButton;

    private CheckBox [] mCheckBoxes;
    private Habit mHabit; //current habit to edit
    private int mIndex; //current habit index

    private Storage mStorage;

    //editText view input values for error checking, editing and saving
    private String mHabitName;
    private String mStartTime;
    private String mEndTime;
    private String mHabitId;
    private boolean [] mCheckDays;

    static final String TAG = InfoEditActivity.class.toString();

    private ProgressDialog pDialog;
    private static final String DELETE_HABIT_URL = "http://192.168.29.203/deletehabit.php";
    private static final String UPDATE_HABIT_URL = "http://192.168.29.203/updatehabit.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_HABIT_ID = "habit_id";
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);

        ButterKnife.bind(this);
        setOnclickTimeDialog(pickTimeStartEdit);
        setOnclickTimeDialog(pickTimeEndEdit);

        mStorage = new Storage(this);
        //check boxes
        mCheckBoxes = new CheckBox[] {monCheck, tueCheck, wedCheck, thuCheck, friCheck,
                satCheck, sunCheck};
        //get the index extra from editActivity to get the habit needed to edit
        Intent intent = getIntent();
        int position = intent.getIntExtra(getString(R.string.EXTRA_HABIT_CLICKED_INDEX), -1);
        if(position != -1) {
            mHabit = Storage.mHabits.get(position);
            mIndex = position;
        }
        else{
            Log.e(TAG, getString(R.string.passing_extra_error));
        }
        //set actionbar title to name of habit
        this.setTitle(mHabit.getHabitName());
        //set habitId for SQL
        mHabitId = Storage.mHabits.get(mIndex).getHabitId();
        //set buttons onclick listener
        setOnclickDeleteButton();
        setOnclickSaveButton();
        //set up input infos
        putStartedInputInfos();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStorage.saveToInternalStorage(Storage.mHabits);
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
                mTimePicker = new TimePickerDialog(InfoEditActivity.this, new TimePickerDialog
                        .OnTimeSetListener
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

    public void setOnclickDeleteButton(){
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(InfoEditActivity.this)
                        .setTitle(R.string.dialog_delete_title)
                        .setMessage(R.string.dialog_delete_message)
                        .setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteHabit().execute();
                                Storage.mHabits.remove(mHabit);
                                mStorage.saveToInternalStorage(Storage.mHabits);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_negative_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        }).show();
            }
        });

    }

    public void setOnclickSaveButton(){
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Habit targetHb = Storage.mHabits.get(mIndex);
                //getting the infos from the Views and set it in new habit for storage
                if(checkInfoError()) {//true no error

                    WeekDays days = new WeekDays(mCheckDays);
                    //set the habit chosen to new infos
                    targetHb.setHabitName(mHabitName);
                    targetHb.setStartTime(mStartTime);
                    targetHb.setEndTime(mEndTime);
                    targetHb.setDays(days);

                    new UpdateHabit().execute();

                    mStorage.saveToInternalStorage(Storage.mHabits);
                    finish();
                }
                else{
                    // do nothing
                }
            }
        });
    }

    //informations on the habit put it in the inputs
    private void putStartedInputInfos(){
        boolean [] daysChecked = mHabit.getDays().getDayBools();
        habitNameEditText.setText(mHabit.getHabitName());
        pickTimeStartEdit.setText(mHabit.getStartTime());
        pickTimeEndEdit.setText(mHabit.getEndTime());
        for(int i=0; i< mCheckBoxes.length; i++){
            if(daysChecked[i]){
                mCheckBoxes[i].setChecked(true);
            }
        }
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

    //check for text edit view empty, compare time
    private boolean checkInfoError() { //true no error, false error
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

        else if(!mStartTime.matches("") && !mEndTime.matches("")
                && !mStartTime.matches("Unset Time") && !mEndTime.matches("Unset Time")){
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

    class DeleteHabit extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(InfoEditActivity.this);
            pDialog.setMessage("Deleting Habit...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("habit_id", mHabitId));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        DELETE_HABIT_URL, "POST", params);

                // full json response
                Log.d("Deleting Habit ID: " + mHabitId, json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Habit Deleted!", json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Delete Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(InfoEditActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
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

    class UpdateHabit extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(InfoEditActivity.this);
            pDialog.setMessage("Adding Habit...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            int success;
            String post_habitid = mHabitId;
            String post_habit = mHabitName;
            String post_timestart = mStartTime;
            String post_timeend = mEndTime;
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
                params.add(new BasicNameValuePair("habit_id", post_habitid));
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

                JSONObject json = jsonParser.makeHttpRequest(UPDATE_HABIT_URL, "POST", params);

                Log.d("Update Habit attempt", json.toString());

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Habit Updated!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Update failure!", json.getString(TAG_MESSAGE));
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
                Toast.makeText(InfoEditActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }

}
