package com.example.yongliu.habitloop.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by YongLiu on 1/11/16.
 */
public class Habit implements Serializable{
    private String mHabitName;
    private Integer mHabitID;
    private String mTime; //when they want to do the habit at that day
    private String mStartTime;
    private String mEndTime;
    private int mStreak;
    private WeekDays mDays;

    private ArrayList<Date> mCompleteDays;
    private ArrayList<Date> mIncompleteDays;
    //private String mDays;

    public Habit() {

    }

    public Habit(String habitName, int streak, String startTime, String endTime, WeekDays days){
        mHabitName = habitName;
        mHabitID = -1;
        mStartTime = startTime;
        mEndTime = endTime;
        mStreak = streak;
        mDays = days;
        mCompleteDays = new ArrayList<Date>();
        mIncompleteDays = new ArrayList<Date>();
    }

    public Habit(String habitName, int habitID, int streak, String startTime, String endTime, WeekDays days){
        mHabitName = habitName;
        mHabitID = habitID;
        mStartTime = startTime;
        mEndTime = endTime;
        mStreak = streak;
        mDays = days;
        mCompleteDays = new ArrayList<Date>();
        mIncompleteDays = new ArrayList<Date>();
    }

    public String getHabitName() {
        return mHabitName;
    }

    public void setHabitName(String habitName) {
        mHabitName = habitName;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public int getStreak() {
        return mStreak;
    }

    public void setStreak(int streak) {
        mStreak = streak;
    }

    public WeekDays getDays() {
        return mDays;
    }

    public void setDays(WeekDays days) {
        mDays = days;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public String getHabitId() { return mHabitID.toString(); }

    public ArrayList<Date> getCompleteDays() {
        return mCompleteDays;
    }

    public void addCompleteDays(Date date){
        mCompleteDays.add(date);
    }

    public void removeCompleteDays(Date date){
        mCompleteDays.remove(date);
    }

    public ArrayList<Date> getIncompleteDays() {
        return mIncompleteDays;
    }

    public void addIncompleteDays(Date date){
        mIncompleteDays.add(date);
    }

    public void removeIncompleteDays(Date date){
        mIncompleteDays.remove(date);
    }

    //calculate current streak
    public void calculateStreak(){
        //mStreak = mCompleteDays.size();
        int streak = 0;
        ArrayList<Date> allCountedDays = new ArrayList<Date>();
        allCountedDays.addAll(mCompleteDays);
        allCountedDays.addAll(mIncompleteDays);
        //sort complete and incomplete days from early to late
        Collections.sort(allCountedDays, new Comparator<Date>() {
            @Override
            public int compare(Date d1, Date d2) {
                return d1.compareTo(d2);
            }
        });

        //from the most current/late days, count how many consecutive complete days without meeting a incomplete day
        for(int i= allCountedDays.size()-1; i >= 0; i--){
            Date day = allCountedDays.get(i);
            if(mIncompleteDays.contains(day)){
                break;
            }
            else if(mCompleteDays.contains(day)){
                streak++;
            }
        }

        mStreak = streak;
    }

    //get number of completes for that month
    public int monthlyComplete(Date date){
        int month = date.getMonth();
        int year = date.getYear();
        int count = 0;

        for(Date comp : mCompleteDays){
            if(comp.getYear() == year && comp.getMonth() == month){
                count++;
            }
        }

        return count;
    }

    //get number of incompletes for that month
    public int monthlyIncomplete(Date date){
        int month = date.getMonth();
        int year = date.getYear();
        int count = 0;

        for(Date incomp : mIncompleteDays){
            if(incomp.getYear() == year && incomp.getMonth() == month){
                count++;
            }
        }

        return count;
    }

    //get number of completes for that week
    public int weeklyComplete(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int month = date.getMonth();
        int year = date.getYear();
        int count = 0;

        for(Date comp : mCompleteDays){
            Calendar compCal = Calendar.getInstance();
            compCal.setTime(comp);
            int compWeek = compCal.get(Calendar.WEEK_OF_YEAR);
            if(comp.getYear() == year && comp.getMonth() == month && compWeek == week){
                count++;
            }
        }

        return count;
    }

    //get number of incompletes for that week
    public int weeklyIncomplete(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int month = date.getMonth();
        int year = date.getYear();
        int count = 0;

        for(Date incomp : mIncompleteDays){
            Calendar incompCal = Calendar.getInstance();
            incompCal.setTime(incomp);
            int incompWeek = incompCal.get(Calendar.WEEK_OF_YEAR);
            if(incomp.getYear() == year && incomp.getMonth() == month && incompWeek == week){
                count++;
            }
        }

        return count;
    }

}
