package com.example.yongliu.habitloop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.example.yongliu.habitloop.R;
import com.example.yongliu.habitloop.adapters.GridCellAdapter;
import com.example.yongliu.habitloop.models.Habit;
import com.example.yongliu.habitloop.models.Storage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CalendarActivity extends AppCompatActivity {

    @Bind(R.id.calendarMonthYearTextView)
    TextView monthYearTitleView;
    @Bind(R.id.calendarLeftArrowButton)
    Button leftArrowButton;
    @Bind(R.id.calendarRightArrowButton)
    Button rightArrowButton;
    @Bind(R.id.calendarGridView)
    GridView calendarGridView;

    GridCellAdapter gridAdapter;

    private static final int DAYS_NUM = 42;
    // current displayed month
    private Calendar currentDate = Calendar.getInstance();

    private Habit mHabit; //current habit to display calendar
    private int mIndex; //current habit index

    private Storage mStorage;

    static final String TAG = CalendarActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);
        assignClickHandlers();

        mStorage = new Storage(this);

        //which habit from MainActivity
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
        updateCalendar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateCalendar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCalendar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStorage.saveToInternalStorage(Storage.mHabits);
    }

    private void updateCalendar() {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar) currentDate.clone();
        Date titleDate = currentDate.getTime();
        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_NUM) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        //update grid
        gridAdapter = new GridCellAdapter(this, cells, titleDate, mIndex);
        calendarGridView.setAdapter(gridAdapter);
        // update title
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        monthYearTitleView.setText(sdf.format(currentDate.getTime()));
    }

    private void assignClickHandlers() {
        // add one month and refresh UI
        rightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        // subtract one month and refresh UI
        leftArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });
        /*
        calendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });*/

    }


}