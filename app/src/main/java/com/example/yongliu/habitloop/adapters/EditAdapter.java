package com.example.yongliu.habitloop.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yongliu.habitloop.R;
import com.example.yongliu.habitloop.models.Habit;
import com.example.yongliu.habitloop.models.JSONParser;
import com.example.yongliu.habitloop.models.Storage;
import com.example.yongliu.habitloop.ui.EditActivity;
import com.example.yongliu.habitloop.ui.InfoEditActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YongLiu on 3/24/16.
 */
public class EditAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Habit> mHabits;
    private Storage mStorage;
    private ProgressDialog pDialog;
    private static final String DELETE_HABIT_URL = "http://192.168.29.203/deletehabit.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_HABIT_ID = "habit_id";
    private String habitId;
    JSONParser jsonParser = new JSONParser();

    public EditAdapter(Context context, ArrayList<Habit> habits) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        //convertView (2nd parameter) is the view we reuse
        ViewHolder holder;

        if (convertView == null) {
            //its brand new, create view by inflating it from the context
            //layoutInflater is an android obj that takes xml layouts, and turns them into views
            // and codes we can use
            convertView = LayoutInflater.from(mContext).inflate(R.layout.edit_list_item,
                    null);
            holder = new ViewHolder();
            holder.habitNameView = (TextView) convertView.findViewById(R.id.habitNameViewEdit);
            holder.delButtonView = (ImageButton) convertView.findViewById(R.id.itemDelButton);
            holder.editButtonView = (ImageButton) convertView.findViewById(R.id.itemEditButton);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //get and set habit infos here
        Habit habit = mStorage.mHabits.get(position);
        Log.d("building: position: " + position, "habit id: " + habitId);
        holder.habitNameView.setText(habit.getHabitName());
        holder.delButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.dialog_delete_title)
                        .setMessage(R.string.dialog_delete_message)
                        .setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                habitId = Storage.mHabits.get(position).getHabitId();
                                Log.d("***position: " + position, "habit id: " + habitId);
                                new DeleteHabit().execute();
                                Storage.mHabits.remove(position);
                                mStorage.saveToInternalStorage(Storage.mHabits);
                                notifyDataSetChanged();
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

        holder.editButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, InfoEditActivity.class);
                intent.putExtra(mContext.getString(R.string.EXTRA_HABIT_CLICKED_INDEX), position);
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public static class ViewHolder {
        TextView habitNameView;
        ImageButton delButtonView;
        ImageButton editButtonView;
    }

    class DeleteHabit extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
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
                params.add(new BasicNameValuePair("habit_id", habitId));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        DELETE_HABIT_URL, "POST", params);

                // full json response
                Log.d("Deleting Habit ID: " + habitId, json.toString());

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
                Toast.makeText(mContext, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}
