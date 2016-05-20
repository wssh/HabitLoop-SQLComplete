package com.example.yongliu.habitloop.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.example.yongliu.habitloop.R;
import com.example.yongliu.habitloop.models.Habit;
import com.example.yongliu.habitloop.models.Storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by YongLiu on 4/11/16.
 */
public class GridCellAdapter extends BaseAdapter {

    private final Context mContext;
    private ArrayList<Date> mDays;
    private Date mCurrentDate;
    private int mHabitPosition;
    private Storage mStorage;

    public GridCellAdapter(Context context, ArrayList<Date> days, Date currentDate, int habitPosition) {
        mContext = context;
        mDays = days;
        mCurrentDate = currentDate;
        mHabitPosition = habitPosition;
        mStorage = new Storage(mContext);
    }

    @Override
    public int getCount() {
        return mDays.size();
    }

    @Override
    public Object getItem(int position) {
        return mDays.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Date currentCellDate = (Date)getItem(position);
        int day = currentCellDate.getDate();
        int month = currentCellDate.getMonth();
        int year = currentCellDate.getYear();
        Date today = new Date();

        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.calendar_day_gridcell,
                    null);
            holder = new ViewHolder();
            holder.gridcellButton = (Button)convertView.findViewById(R.id.calendarGridcellButton);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }
        //give day text of the month
        holder.gridcellButton.setText(String.valueOf(currentCellDate.getDate()));
        if (month != mCurrentDate.getMonth() || year != mCurrentDate.getYear()){
            holder.gridcellButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorGridcellTextGreyOut));
        }
        if (day == today.getDate() && month == today.getMonth() && year == today.getYear()){
            holder.gridcellButton.setTypeface(null, Typeface.BOLD);
            holder.gridcellButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorGridcellToday));
        }

        //check complete fill background
        int status = checkComplete(currentCellDate);
        if(status == 0){
            holder.gridcellButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorGridcellIncomplete));
        }
        else if(status == 1){
            holder.gridcellButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorGridcellComplete));
        }

        //set onclick button
        holder.gridcellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Habit hb = Storage.mHabits.get(mHabitPosition);
                Date comp = checkContain(hb.getCompleteDays(), currentCellDate);
                Date incomp = checkContain(hb.getIncompleteDays(), currentCellDate);
                if(comp != null){ //currently filled with complete color, change to incomplete
                    hb.removeCompleteDays(comp);
                    hb.addIncompleteDays(currentCellDate);
                    v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorGridcellIncomplete));
                    mStorage.saveToInternalStorage(Storage.mHabits);
                }
                else if(incomp != null){//currently filled with incomplete color, change to blank/unfilled
                    hb.removeIncompleteDays(incomp);
                    v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorGridcellBackground));
                    mStorage.saveToInternalStorage(Storage.mHabits);
                }
                else if(incomp == null && comp ==null){ //currently unfilled, change to complete
                    hb.addCompleteDays(currentCellDate);
                    v.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorGridcellComplete));
                    mStorage.saveToInternalStorage(Storage.mHabits);
                }

            }
        });

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private int checkComplete(Date currentCellDate){
        //0= incomplete, 1= complete, 2= neither/empty
        int status = 2;
        ArrayList<Date> completes = Storage.mHabits.get(mHabitPosition).getCompleteDays();
        ArrayList<Date> incompletes = Storage.mHabits.get(mHabitPosition).getIncompleteDays();

        Date comp = checkContain(completes, currentCellDate);
        Date incomp = checkContain(incompletes, currentCellDate);

        if(comp != null){
            status = 1;
        }
        else if(incomp != null){
            status = 0;
        }

        return status;
    }

    private Date checkContain(ArrayList<Date> dateList, Date target){

        for(Date date : dateList){
            Calendar dateObj = Calendar.getInstance();
            Calendar current = Calendar.getInstance();
            dateObj.setTime(date);
            current.setTime(target);
            boolean sameDay = dateObj.get(Calendar.YEAR) == current.get(Calendar.YEAR) && dateObj.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR);

            if(sameDay){
                return date;
            }
        }

        return null;
    }


    public static class ViewHolder{
        Button gridcellButton;
    }

}
