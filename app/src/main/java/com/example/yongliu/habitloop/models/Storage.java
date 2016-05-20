package com.example.yongliu.habitloop.models;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by YongLiu on 3/24/16.
 * temp storage
 */
public class Storage {
    private static final String STORAGE_FILENAME = "habits_storage";
    public static ArrayList<Habit> mHabits= new ArrayList<Habit>();
    private final Context mContext;

    public Storage(Context cxt){
        mContext = cxt;
        mHabits = readFromInternalStorage();
    }

    public void saveToInternalStorage(ArrayList<Habit> listToSave) {
        try {
            FileOutputStream fos = mContext.openFileOutput(STORAGE_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream of = new ObjectOutputStream(fos);
            of.writeObject(listToSave);
            of.flush();
            of.close();
            fos.close();
        }
        catch (Exception e) {
            Log.e("InternalStorage", e.getMessage());
        }
    }


    public ArrayList<Habit> readFromInternalStorage() {
        ArrayList<Habit> listReturn = new ArrayList<Habit>();
        FileInputStream fis;
        try {
            fis = mContext.openFileInput(STORAGE_FILENAME);
            ObjectInputStream oi = new ObjectInputStream(fis);
            listReturn = (ArrayList<Habit>) oi.readObject();
            oi.close();
        } catch (FileNotFoundException e) {
            Log.e("InternalStorage", e.getMessage());
        } catch (IOException e) {
            Log.e("InternalStorage", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return listReturn;
    }
}
