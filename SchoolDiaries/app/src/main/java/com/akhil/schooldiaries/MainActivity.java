package com.akhil.schooldiaries;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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


   // Remove this method later on
    public void ByPassLoginPressed(View v)
    {
        Intent intent = new Intent(this,ParentMain.class);
        startActivity(intent);
    }
}
