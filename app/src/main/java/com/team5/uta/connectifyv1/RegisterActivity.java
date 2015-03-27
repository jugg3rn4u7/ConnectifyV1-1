package com.team5.uta.connectifyv1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.team5.uta.connectifyv1.adapter.IMyCallbackInterface;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "UserData";
    public HTTPWrapper request = null;
    public Object output = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f5793f")));

        final EditText first_name = (EditText) findViewById(R.id.first_name);
        final EditText last_name= (EditText) findViewById(R.id.last_name);
        final EditText password = (EditText) findViewById(R.id.password);
        final EditText confirm_password = (EditText) findViewById(R.id.confirm_password);
        final EditText email= (EditText) findViewById(R.id.email);
        final Button sign_up=(Button) findViewById(R.id.signup_button);
        final int duration=Toast.LENGTH_SHORT;
        final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_APPEND);

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((first_name.getText().toString().trim().equals(""))||(last_name.getText().toString().trim().equals(""))||
                        (password.getText().toString().trim().equals(""))||(confirm_password.getText().toString().trim().equals(""))||
                        (email.getText().toString().trim().equals("")))
                {
                   Toast.makeText(getApplicationContext(),"All Fields are Required.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(!(password.getText().toString().equals(confirm_password.getText().toString()))) {
                        Toast.makeText(getApplicationContext(),"Passwords do not match.",Toast.LENGTH_SHORT).show();
                    }
                    else {

                        if(checkEmail(email.getText().toString())) {

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("FirstName",first_name.getText().toString());
                            editor.putString("LastName",last_name.getText().toString());
                            editor.putString("Password",password.getText().toString());
                            editor.putString("Email",email.getText().toString());
                            editor.commit();

                            String str1 = "firstName=" + first_name.getText().toString();
                            String str2 = "lastName=" + last_name.getText().toString();
                            String str3 = "email=" + email.getText().toString();

                            URL url = null;
                            JSONArray jsonArray = null;
                            try {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);

                                url = new URL(  "http://localhost/connectify/registerUser.php?"+
                                                    "firstName=" + first_name.getText().toString() +"&"+
                                                    "lastName=" + last_name.getText().toString() +"&"+
                                                    "email=" + email.getText().toString()
                                );
                                HttpClient client = new DefaultHttpClient();
                                HttpGet request = new HttpGet();
                                request.setURI(url.toURI());
                                HttpResponse response = client.execute(request);
                                BufferedReader in = new BufferedReader
                                        (new InputStreamReader(response.getEntity().getContent()));

                                StringBuffer sb = new StringBuffer("");
                                String line="";
                                while ((line = in.readLine()) != null) {
                                    sb.append(line);
                                    break;
                                }
                                in.close();
                                jsonArray = new JSONArray(sb.toString());

                                if(!Boolean.parseBoolean(jsonArray.getJSONObject(0).get("result").toString())) {
                                    return;
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ClientProtocolException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }

                            String fname = first_name.getText().toString();
                            String lname = last_name.getText().toString();
                            String pwd = password.getText().toString();
                            String uemail = email.getText().toString();

                            User user = new User(fname, lname, pwd, uemail, null, null);

                            Intent securityQuestionsActivity = new Intent(RegisterActivity.this, SecurityQuestions.class);
                            securityQuestionsActivity.putExtra("user", user);
                            startActivity(securityQuestionsActivity);

/*
                            HTTPWrapper request = new HTTPWrapper(new IMyCallbackInterface() {
                                @Override
                                public void onComplete(Object data) {

                                    JSONArray res = (JSONArray)data;

                                    System.out.println("res : " + res.toString());

                                    try {
                                        if(!Boolean.parseBoolean(res.getJSONObject(0).get("result").toString())) {
                                            return;
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        return;
                                    }

                                    String fname = first_name.getText().toString();
                                    String lname = last_name.getText().toString();
                                    String pwd = password.getText().toString();
                                    String uemail = email.getText().toString();

                                    User user = new User(fname, lname, pwd, uemail, null, null);

                                    Intent securityQuestionsActivity = new Intent(RegisterActivity.this, SecurityQuestions.class);
                                    securityQuestionsActivity.putExtra("user", user);
                                    startActivity(securityQuestionsActivity);
                                }
                            });

                            request.execute(
                                    "GET",
                                    "http://localhost/connectify/registerUser.php",
                                    "firstName=" + first_name.getText().toString() +"&"+
                                    "lastName=" + last_name.getText().toString() +"&"+
                                    "email=" + email.getText().toString()
                            );
*/
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Invalid email id.",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            public boolean checkEmail(String email) {
                //Pattern pattern = Pattern.compile(".+@.+\\.[a-z]+");
                Pattern pattern = Pattern.compile("[A-Za-z]+\\.[A-Za-z]+@mavs\\.uta\\.edu$");
                Matcher matcher = pattern.matcher(email);
                return matcher.matches();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}