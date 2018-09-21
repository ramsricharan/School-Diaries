package com.akhil.schooldiaries;

import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;

import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ParentMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.parent_toolbar);
        toolbar.setTitle("School Diaries");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final View hView =  navigationView.getHeaderView(0);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        String email_curr = firebaseAuth.getCurrentUser().getEmail();
        String uid = firebaseAuth.getCurrentUser().getUid();
        databaseReference.child("parents").child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView nav_username = (TextView) hView.findViewById(R.id.Nav_username);
                String name_small = dataSnapshot.getValue().toString();
                String name_big = name_small.substring(0, 1).toUpperCase() + name_small.substring(1);
                nav_username.setText(name_big);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        TextView nav_email = (TextView) hView.findViewById(R.id.Nav_email);
        nav_email.setText(email_curr);

        getSupportFragmentManager().beginTransaction().replace(R.id.parent_content_holder,new ParentChildList()).commit();

        String action = getIntent() != null ? getIntent().getAction() : null;
        if (action!=null && action.equals("SHORTCUT")) {
            Bundle extras = getIntent().getExtras();
            if(extras != null){
                String id = extras.getString("childid");
                String name = extras.getString("childName");
                String pic = extras.getString("childPic");
                Fragment_ChildFeed MyFrag = Fragment_ChildFeed.newInstance(id,name,pic);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.parent_content_holder, MyFrag)
                        .addToBackStack(null).commit();
                //Toast.makeText(this,"Feed by shortcut : "+ extras.getString("childName"),Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.parent_logout) {
            PrefManager pref = new PrefManager(this);
            pref.resetData();
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            shortcutManager.removeAllDynamicShortcuts();
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
            finish();
        }
        else if (id == R.id.parent_clear_shotcuts){
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            shortcutManager.removeAllDynamicShortcuts();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
