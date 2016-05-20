package com.example.yongliu.habitloop.ui;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.yongliu.habitloop.R;

import com.example.yongliu.habitloop.models.JSONParser;

public class register extends Activity implements OnClickListener{

    private EditText user, pass;
    private Button mRegister;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    //change the URL in order to connect to your localhost or server. This is connected to my local RPI. -Ricky
    private static final String REGISTER_URL = "http://192.168.29.203/registerhabit.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        user = (EditText)findViewById(R.id.username);
        pass = (EditText)findViewById(R.id.password);

        mRegister =(Button)findViewById(R.id.register);
        mRegister.setOnClickListener(this);
    }

    public void onClick(View v){
        new CreateUser().execute();
    }

    class CreateUser extends AsyncTask<String, String, String> {
        boolean failure =  false;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(register.this);
            pDialog.setMessage("Creating User...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args){
            int success;
            String username = user.getText().toString();
            String password = pass.getText().toString();
            try{
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));

                Log.d("request!", "starting");

                JSONObject json = jsonParser.makeHttpRequest(REGISTER_URL, "POST", params);
                Log.d("Login attempt", json.toString());

                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("User Created", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String file_url){
            pDialog.dismiss();
            if(file_url != null){
                Toast.makeText(register.this, file_url, Toast.LENGTH_LONG).show();
            }
        }

    }
}
