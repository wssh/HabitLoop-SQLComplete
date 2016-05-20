package com.example.yongliu.habitloop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.yongliu.habitloop.R;
import com.example.yongliu.habitloop.models.Habit;
import com.example.yongliu.habitloop.models.Storage;

import java.util.ArrayList;

/**
 * Created by YongLiu on 1/11/16.
 */
public class HabitAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<Habit> mHabits;
    private Storage mStorage;

    public HabitAdapter(Context context, ArrayList<Habit> habits){
       mContext = context;
        mHabits = habits;
        mStorage = new Storage(mContext);
    }

    @Override
    public int getCount() {
        return mStorage.mHabits.size();//mHabits.length;
    }

    @Override
    public Object getItem(int position) {
        return mStorage.mHabits.get(position);//mHabits[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //convertView (2nd parameter) is the view we reuse
        ViewHolder holder;

        if(convertView == null){
            //its brand new, create view by inflating it from the context
            //layoutInflater is an android obj that takes xml layouts, and turns them into views
            // and codes we can use
            convertView = LayoutInflater.from(mContext).inflate(R.layout.habit_list_item,
                    null);
            holder = new ViewHolder();
            holder.habitNameView = (TextView) convertView.findViewById(R.id.habitNameViewMain);
            holder.habitStreakView = (TextView) convertView.findViewById(R.id.habitStreakView);
            holder.habitTimeView = (TextView) convertView.findViewById(R.id.habitTimeView);
            holder.habitDaysView = (TextView) convertView.findViewById(R.id.habitDaysView);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        //get and set habit infos here
        Habit habit = mStorage.mHabits.get(position);
        habit.calculateStreak();
        holder.habitNameView.setText(habit.getHabitName());
        holder.habitStreakView.setText(habit.getStreak() + "");
        holder.habitTimeView.setText(habit.getStartTime() + " - " + habit.getEndTime());
        holder.habitDaysView.setText(habit.getDays().getDisplayString());

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public static class ViewHolder{
        TextView habitNameView;
        TextView habitStreakView;
        TextView habitTimeView;
        TextView habitDaysView;
    }

}
