package com.akhil.schooldiaries;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_TeacherClassList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_TeacherClassList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView ClassListView;
    private List<String> AllClassesList = new ArrayList<>();
    double logitude, latitude;
    private boolean HasClasses = false;

    TextView cityField, detailsField, currentTemperatureField, humidity_field, pressure_field, weatherIcon, updatedField;
    Typeface weatherFont;

    private Toolbar coll_tool;

    public Fragment_TeacherClassList() {
        // Required empty public constructor
    }

    public static Fragment_TeacherClassList newInstance(String param1, String param2) {
        Fragment_TeacherClassList fragment = new Fragment_TeacherClassList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("GPS_COORDINATES");
        getActivity().registerReceiver(mMessageReceiver, filter);
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("status").equalsIgnoreCase("fail"))
            {
                Intent newintent = new Intent();
                intent.setAction("GPS_REQUEST");
                getActivity().sendBroadcast(newintent);
            }
            logitude = intent.getDoubleExtra("long",0f);
            latitude = intent.getDoubleExtra("lat",0f);
            SetWeather();
        }
    };
    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        getActivity().unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_teacher_class_list, container, false);
        setHasOptionsMenu(true);
        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        weatherFont = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "fonts/weathericons-regular-webfont.ttf");

//        cityField = (TextView) view.findViewById(R.id.city_field);
//        updatedField = (TextView) view.findViewById(R.id.updated_field);
        detailsField = (TextView) view.findViewById(R.id.details_field);
        currentTemperatureField = (TextView) view.findViewById(R.id.current_temperature_field);

        weatherIcon = (TextView) view.findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);


        coll_tool = (Toolbar) view.findViewById(R.id.toolbar_collapse);
        coll_tool.setTitleTextColor(getResources().getColor(R.color.white));
//        coll_tool.setTitle("Hello");
        CollapsingToolbarLayout myCollaps = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        myCollaps.setExpandedTitleTextAppearance(R.style.MyToolbarTheme);
        myCollaps.setCollapsedTitleTextAppearance(R.style.MyToolbarTheme);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction("GPS_REQUEST");
                getActivity().sendBroadcast(intent);
            }
        }, 1000);


        Toolbar toolbar =(Toolbar) getActivity().findViewById(R.id.teacher_toolbar);
        toolbar.setTitle("Classes List");
        ClassListView = (ListView) view.findViewById(R.id.teacher_class_list);



        GetClassList();
        ClassListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(HasClasses) {
                    String currClassId = AllClassesList.get(position);
                    Fragment classFrag = ClassFragment.newInstance(currClassId);
                    getFragmentManager().beginTransaction().replace(
                            R.id.teacher_content_holder, classFrag)
                            .addToBackStack(null).commit();
                }
            }
        });
        return view;
    }

    private void SetWeather()
    {
        Async_MyWeather.placeIdTask asyncTask =new Async_MyWeather.placeIdTask(new Async_MyWeather.AsyncResponse() {
            public void processFinish(String weather_city, String weather_description, String weather_temperature, String weather_humidity, String weather_pressure, String weather_updatedOn, String weather_iconText, String sun_rise) {

                //cityField.setText(weather_city);
               // updatedField.setText(weather_updatedOn);
                coll_tool.setTitle(weather_city);
                detailsField.setText(weather_description);
                currentTemperatureField.setText(weather_temperature);
//                humidity_field.setText("Humidity: "+weather_humidity);
//                pressure_field.setText("Pressure: "+weather_pressure);
                weatherIcon.setText(Html.fromHtml(weather_iconText));
            }
        });

        String longitude_str = Double.toString(logitude);
        String latitude_str = Double.toString(latitude);
        asyncTask.execute(latitude_str, longitude_str); //  asyncTask.execute("Latitude", "Longitude")
    }

    private void GetClassList()
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final String uid = firebaseAuth.getCurrentUser().getUid();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String AllClasses = dataSnapshot.child("staff").child(uid).child("class_ids").getValue().toString();
                if(!AllClasses.equals("0")) {
                    HasClasses = true;
                    String[] myList = AllClasses.split(",");
                    SetClassIdsList(myList);

                    List<String> ClassNameList = new ArrayList<String>();
                    List<String> ClassTimingsList = new ArrayList<String>();
                    for (String currClassID : myList) {
                        ClassNameList.add(dataSnapshot.child("classes").child(currClassID).child("class_name").getValue().toString());
                        ClassTimingsList.add(dataSnapshot.child("classes").child(currClassID).child("class_timings").getValue().toString());
                    }
                    String[] ClassName = new String[ClassNameList.size()];
                    ClassNameList.toArray(ClassName);
                    String[] ClassTimings = new String[ClassTimingsList.size()];
                    ClassTimingsList.toArray(ClassTimings);
                    SetListVIew(ClassName, ClassTimings);
                }
                else
                {
                    HasClasses = false;
                    String[] noclasses = {"You are not assigned to any class yet"};
                    ListAdapter myListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, noclasses);
                    ClassListView.setAdapter(myListAdapter);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SetClassIdsList(String[] ClassIds)
    {
        AllClassesList.clear();
        Collections.addAll(AllClassesList, ClassIds);
    }

    private void SetListVIew(String[] ClassNames, String[] ClassTimings)
    {
        ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < ClassNames.length; i ++ )
        {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("one", ClassNames[i]);
            map.put("two", ClassTimings[i]);
            mylist.add(map);
        }
        // Keys used in Hashmap
        String[] from1 = {"one", "two"};
        // Ids of views in listview_layout
        int[] to1 = { R.id.ListItem_one, R.id.ListItem_two};

        SimpleAdapter adapter1 = new SimpleAdapter(getContext(), mylist, R.layout.custom_listview_frame, from1, to1);
        ClassListView.setAdapter(adapter1);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.activity__teacher_main,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_addClass) {
            AddNewClass();
        }
        return super.onOptionsItemSelected(item);
    }

    private void AddNewClass()
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final String uid = firebaseAuth.getCurrentUser().getUid();
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Add new class");
        alert.setMessage("Enter a valid class code of the class assigned to you");

        final EditText input = new EditText(getContext());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String Classcode = input.getText().toString();
                if(Classcode.isEmpty())
                    Toast.makeText(getContext(), "Class Code cannot be empty!", Toast.LENGTH_SHORT).show();
                else
                {
                    final DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference();
                    dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child("classes").child(Classcode).exists())
                            {
                                String currClasses = dataSnapshot.child("staff").child(uid).child("class_ids").getValue().toString();
                                if(currClasses.equals("0"))
                                {
                                    dataRef.child("staff").child(uid).child("class_ids").setValue(Classcode + ",");
                                    RefreshClassList();
                                }
                                else if(!isClassAssigned(currClasses, Classcode)) {
                                    currClasses += Classcode + ",";
                                    dataRef.child("staff").child(uid).child("class_ids").setValue(currClasses);
                                    Toast.makeText(getContext(), "Your new class '" + Classcode + "' has been added", Toast.LENGTH_SHORT).show();
                                    RefreshClassList();
                                }
                                else {
                                    Toast.makeText(getContext(), "You have been assigned to this class already!", Toast.LENGTH_LONG).show();
                                }
                            }
                            else
                            {
                                Toast.makeText(getContext(), "Course code you have entered is wrong.", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    private boolean isClassAssigned(String classList, String NewClass)
    {
        return classList.contains(NewClass);
    }

    private void RefreshClassList()
    {
        getActivity().getSupportFragmentManager().beginTransaction().replace(
                R.id.teacher_content_holder, new Fragment_TeacherClassList()).commit();
        //getFragmentManager().popBackStack(null, getFragmentManager().POP_BACK_STACK_INCLUSIVE);
    }
}
