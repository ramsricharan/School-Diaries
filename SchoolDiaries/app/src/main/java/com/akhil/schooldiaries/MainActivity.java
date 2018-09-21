package com.akhil.schooldiaries;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentmanager;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PrefManager pref = new PrefManager(this);

        if(pref.isDataSet()){
            LoginButtonPressed(findViewById(R.id.Button_Login));
        }
        serviceIntent = new Intent(getApplicationContext(), LocationHelper.class);
        startService(serviceIntent);
    }

    public void CreateAccountClicked(View v)
    {
        fragmentmanager = getFragmentManager();
        Fragment_CreateAccount createFrag = new Fragment_CreateAccount();
        FragmentTransaction trans = fragmentmanager.beginTransaction();
        trans.add(R.id.MyFrame, createFrag);
        trans.addToBackStack(null);
        trans.commit();
    }

    public void LoginButtonPressed(View v)
    {
        fragmentmanager = getFragmentManager();
        Fragment_Login LoginFrag = new Fragment_Login();
        FragmentTransaction trans = fragmentmanager.beginTransaction();
        trans.add(R.id.MyFrame, LoginFrag);
        trans.addToBackStack(null);
        trans.commit();
    }

}
