package com.example.admin.newapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
/*import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
*/
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
     public ImageButton  loginButton,signupButton;

    EditText email,pass;
    LoginButton facebooklogin;
    CallbackManager callbackManager;
    Boolean loggedIn = false;
    String user;
    Button near;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //xml ki views(like textview,etc) k variable bnarhe hen hum yhan
        email = (EditText) findViewById(R.id.emailtext);
        pass = (EditText) findViewById(R.id.emailpass);
        facebooklogin = (LoginButton) findViewById(R.id.login_button);
        loginButton=(ImageButton)findViewById(R.id.loginbutton);
        signupButton = (ImageButton) findViewById(R.id.signupbutton);
        near = (Button) findViewById(R.id.near);


        //Goto Maps
        near.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nearby = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(nearby);
            }
        });




        //checking if user is logged in or not, through a variable which is saved in sharedprefrences
        loggedIn = getSharedPreferences(Publicvars.UserSession, Context.MODE_PRIVATE).getBoolean(Publicvars.SessionState,false);
        user = getSharedPreferences(Publicvars.UserSession, Context.MODE_PRIVATE).getString(Publicvars.KEY_USERNAME,"nothing");
        Boolean firsttime = getSharedPreferences("applevel", Context.MODE_PRIVATE).getBoolean("firstime",false);

        if(loggedIn||!firsttime)
        {
            getSharedPreferences("applevel",Context.MODE_PRIVATE).edit().putBoolean("firstime",true).apply();
            Intent toy = new Intent(MainActivity.this, Profile.class);
            startActivity(toy);
        }

        signupButton.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toy = new Intent(MainActivity.this, Signup_register.class);
                startActivity(toy);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().length()>0&& pass.getText().length()>0)
                {
                    checkuser(email.getText().toString(),"nothing",pass.getText().toString(),"normal");
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"make sure you have filled all the fields",Toast.LENGTH_SHORT).show();
                }
            }
        });



        //facebook login
        callbackManager = CallbackManager.Factory.create();
        facebooklogin.setReadPermissions(Arrays.asList("user_friends", "email", "public_profile")); //-permission  from facebook
        facebooklogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() { //
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App codes
                setFacebookData(loginResult);
                System.out.println("success");
            }

            @Override
            public void onCancel() {
                System.out.println("cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println(exception);
            }
        });

    }
    //  volley function to check if user exist in db or not, if it is, then login
    private void checkuser(final String useremail, final String username, String userpassword, final String type) {
        String us = username;
        us = us.replaceAll(" ", "%20");
        if(isOnline())
        {
            StringRequest stringRequest = new StringRequest(Publicvars.Globals + "user_signin.php?UN="+us+"&EM=" + useremail + "&PW=" + userpassword + "&type="+ type,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("unclear")) {
                                Toast.makeText(getApplicationContext(), "invalid credentials", Toast.LENGTH_SHORT).show();
                            } else {
                                SharedPreferences sf = getSharedPreferences(Publicvars.UserSession, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sf.edit();
                                editor.putBoolean(Publicvars.SessionState, true);
                                editor.putString(Publicvars.KEY_EMAIL, username);
                                if(type.equals("facebook"))
                                {
                                    editor.putString(Publicvars.KEY_USERNAME, username);
                                }
                                else
                                {
                                    editor.putString(Publicvars.KEY_USERNAME, response);
                                }
                                editor.apply();
                                LoginManager.getInstance().logOut();
                                Intent toy = new Intent(MainActivity.this, Profile.class);
                                startActivity(toy);
                            }
                        }
                    }
                    ,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
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
    //after login, with successful result, this function will get user personal info through graph api, and then call the checkuser() function to login
  private void setFacebookData(final LoginResult loginResult)
    {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            Log.i("Response",response.toString());

                            String email = response.getJSONObject().getString("email");
                            String firstName = response.getJSONObject().getString("first_name");
                            String lastName = response.getJSONObject().getString("last_name");

                            checkuser(email,firstName+" "+lastName,"nothing","facebook");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,first_name,last_name,gender"); //
        request.setParameters(parameters);
        request.executeAsync();
    }
// checking if the device has an active internet connection or not
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}



