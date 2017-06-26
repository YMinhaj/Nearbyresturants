package com.example.admin.newapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Signup_register extends AppCompatActivity {

    EditText firstname,lastname,email,password,contact;
    Boolean loggedIn = false;
    String user;
    ImageButton register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_register);

        email = (EditText) findViewById(R.id.email);
        firstname = (EditText) findViewById(R.id.firstname);
        lastname = (EditText) findViewById(R.id.lastname);
        password = (EditText) findViewById(R.id.password);
        contact = (EditText) findViewById(R.id.contact);
        register=(ImageButton)findViewById(R.id.register);

        loggedIn = getSharedPreferences(Publicvars.UserSession, Context.MODE_PRIVATE).getBoolean(Publicvars.SessionState,false);
        user = getSharedPreferences(Publicvars.UserSession, Context.MODE_PRIVATE).getString(Publicvars.KEY_USERNAME,"nothing");

        if (loggedIn)
        {
            startActivity(new Intent(Signup_register.this,Profile.class));
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().length()>0&&firstname.getText().length()>0&&lastname.getText().length()>0&&password.getText().length()>0&&contact.getText().length()>0)
                {
                    registeruser(email.getText().toString(),firstname.getText().toString(),lastname.getText().toString(),password.getText().toString(),contact.getText().toString());
                }
            }
        });
    }
 //volley request to register a user.
    private void registeruser(final String emailer, final String firstnamer, final String lastnamer, final String passworder, final String contacter) {
        if(isOnline()) {
            StringRequest stringRequest = new StringRequest(Publicvars.Globals + "user_signup.php?emailer=" + emailer + "&firstnamer=" + firstnamer + "&lastnamer=" + lastnamer + "&passworder=" + passworder + "&contacter=" + contacter,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("unclear")) {
                                Toast.makeText(getApplicationContext(), "invalid text", Toast.LENGTH_SHORT).show();
                            } else {
                                SharedPreferences sf = getSharedPreferences(Publicvars.UserSession, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sf.edit();
                                editor.putBoolean(Publicvars.SessionState, true);
                                editor.putString(Publicvars.KEY_EMAIL, emailer);
                                editor.putString(Publicvars.KEY_USERNAME, firstnamer+" "+lastnamer);
                                editor.apply();
                                startActivity(new Intent(Signup_register.this,Profile.class));
                            }
                        }
                    }
                    ,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(Signup_register.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }
        else
        {
            Toast.makeText(getApplicationContext(),"make sure you have an active internet connection",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
