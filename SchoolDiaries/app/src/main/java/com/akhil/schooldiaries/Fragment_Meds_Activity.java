package com.akhil.schooldiaries;


import android.content.Context;
import java.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Meds_Activity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Meds_Activity extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String ClassId;
    private String NapDetails = "Not mentioned";
    private String TaggedData;
    private String TaggedKidsString;
    private TextView TaggedKids_TV;
    private List<ChildDataElement> TaggedChildList = new ArrayList<>();

    private EditText Med_Name, Med_Symptoms, Med_Hours, Med_Minutes;
    private Spinner Med_AmPmSpinner;
    private RadioButton CurrentTime, CustomTime;



    public Fragment_Meds_Activity() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment Fragment_Meds_Activity.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Meds_Activity newInstance(String param1) {
        Fragment_Meds_Activity fragment = new Fragment_Meds_Activity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            TaggedData = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_meds_activity, container, false);

        ClassId = ClassFragment.className;
        TaggedKids_TV = (TextView) view.findViewById(R.id.Meds_taggedKids);
        SetChildData();

        //Edit texts
        Med_Name = (EditText) view.findViewById(R.id.Meds_MedName);
        Med_Symptoms = (EditText) view.findViewById(R.id.Meds_Symptoms);
        Med_Hours = (EditText) view.findViewById(R.id.Meds_hours);
        Med_Minutes = (EditText) view.findViewById(R.id.Meds_minutes);

        // Spinner
        Med_AmPmSpinner = (Spinner) view.findViewById(R.id.Meds_ampm_dropdown);

        final LinearLayout CustomTimeEdit = (LinearLayout) view.findViewById(R.id.Meds_CustomTimeLayout);
        CustomTimeEdit.setVisibility(view.GONE);

        RadioGroup TimeSelector = (RadioGroup) view.findViewById(R.id.Meds_timegroup);
        CurrentTime = (RadioButton) view.findViewById(R.id.Meds_CurrTimeRadio);
        CustomTime = (RadioButton) view.findViewById(R.id.Meds_CustTimeRadio);
        TimeSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if(checkedId == R.id.Meds_CurrTimeRadio)
                {
                    CustomTimeEdit.setVisibility(view.GONE);
                }
                else if(checkedId == R.id.Meds_CustTimeRadio)
                {
                    CustomTimeEdit.setVisibility(view.VISIBLE);
                }
            }
        });

        Button addButton = (Button) view.findViewById(R.id.Meds_AddPost);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckAndAddInputs();
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        return view;
    }

    private void SetChildData()
    {
        String TaggedKids = "";
        TaggedChildList.clear();
        String[] nameAndId = TaggedData.split(",");
        for(String nameid : nameAndId)
        {
            ChildDataElement tempelem = new ChildDataElement();
            String[] nameidpair = nameid.split("/");
            tempelem.ChildId = nameidpair[0];
            tempelem.ChildName = nameidpair[1];
            TaggedKids += nameidpair[1] + ", ";
            TaggedChildList.add(tempelem);
        }
        TaggedKidsString = TaggedKids;
        TaggedKids_TV.setText(TaggedKids);
    }


    private void CheckAndAddInputs()
    {
        if(CheckEmpty())
        {
            if(CurrentTime.isChecked())
            {
                SimpleDateFormat MyFormat = new SimpleDateFormat("hh:mm a");
                String CurrentTimeis = MyFormat.format(new Date());
                AddNewMedsPost(CurrentTimeis);
            }
            else if(CustomTime.isChecked())
            {
                String CustomTimeis = TimeFormatOK();
                if(!CustomTimeis.equals("error"))
                {
                    AddNewMedsPost(CustomTimeis);
                }
            }
        }
    }

    private boolean CheckEmpty()
    {
        String Name = Med_Name.getText().toString();
        String Details = Med_Symptoms.getText().toString();

        if(Name.isEmpty())
        {
            Toast.makeText(getContext(), "Medicine Name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(Details.isEmpty())
        {
            Toast.makeText(getContext(), "Symptoms cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private String TimeFormatOK()
    {
        String GivenHours = Med_Hours.getText().toString();
        String GivenMinutes = Med_Minutes.getText().toString();
        String AMorPM = Med_AmPmSpinner.getSelectedItem().toString();

        if(GivenHours.isEmpty())
        {
            Toast.makeText(getContext(), "Hours cannot be empty", Toast.LENGTH_SHORT).show();
            return "error";
        }
        if(GivenMinutes.isEmpty())
        {
            Toast.makeText(getContext(), "Minutes cannot be empty", Toast.LENGTH_SHORT).show();
            return "error";
        }
        int GivenHours_int = Integer.parseInt(GivenHours);
        int GivenMinutes_int = Integer.parseInt(GivenMinutes);

        if(GivenHours_int > 12)
        {
            Toast.makeText(getContext(), "Hours needs to be less than 12", Toast.LENGTH_SHORT).show();
            return "error";
        }
        else if(GivenMinutes_int > 59)
        {
            Toast.makeText(getContext(), "Minutes cannot be greater than 60", Toast.LENGTH_SHORT).show();
            return "error";
        }
        String customTime = GivenHours + ":" + GivenMinutes + " " + AMorPM;
        return customTime;
    }




    private void AddNewMedsPost(String Med_time)
    {
        String med_name = Med_Name.getText().toString();
        String medSymptoms = Med_Symptoms.getText().toString();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("activities");
        String MyActId = databaseReference.push().getKey();
        databaseReference.child(MyActId).child("type").setValue("Meds");
        databaseReference.child(MyActId).child("med_name").setValue(med_name);
        databaseReference.child(MyActId).child("med_symptoms").setValue(medSymptoms);
        databaseReference.child(MyActId).child("time").setValue(Med_time);
        databaseReference.child(MyActId).child("class").setValue(ClassId);
        databaseReference.child(MyActId).child("childnames").setValue(TaggedKidsString);
        AddMedsPostToTaggedKids(MyActId);
    }



    private void AddMedsPostToTaggedKids(final String key)
    {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("children");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(ChildDataElement CurrElem : TaggedChildList)
                {
                    String currActs = dataSnapshot.child(CurrElem.ChildId).child("act_ids").getValue().toString();
                    if(currActs.equals("0"))
                    {
                        databaseReference.child(CurrElem.ChildId).child("act_ids").setValue(key);
                    }
                    else
                    {
                        String acts = currActs + "," + key;
                        databaseReference.child(CurrElem.ChildId).child("act_ids").setValue(acts);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        CompletedAllTasks();
    }


    private void CompletedAllTasks()
    {
        Toast.makeText(getContext(), "Posted Medicine Event", Toast.LENGTH_SHORT).show();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack ("ActFrag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.popBackStack ("TagList", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }



}
