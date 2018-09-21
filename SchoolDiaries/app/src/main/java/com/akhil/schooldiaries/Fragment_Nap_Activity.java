package com.akhil.schooldiaries;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Nap_Activity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Nap_Activity extends Fragment {
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
    private Spinner Start_AmPmSelector, End_AmPmSelector;
    private EditText Start_hours, Start_minutes, End_hours, End_minutes, Nap_Details;





    public Fragment_Nap_Activity() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment Fragment_Nap_Activity.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Nap_Activity newInstance(String param1) {
        Fragment_Nap_Activity fragment = new Fragment_Nap_Activity();
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
        final View view =  inflater.inflate(R.layout.fragment_nap_activity, container, false);

        ClassId = ClassFragment.className;
        TaggedKids_TV = (TextView) view.findViewById(R.id.Nap_taggedKids);
        SetChildData();

        // Spinners
        Start_AmPmSelector = (Spinner) view.findViewById(R.id.Nap_Start_ampm_dropdown);
        End_AmPmSelector = (Spinner) view.findViewById(R.id.Nap_End_ampm_dropdown);

        // Edittext fields
        Start_hours = (EditText) view.findViewById(R.id.Nap_Start_hours);
        Start_minutes = (EditText) view.findViewById(R.id.Nap_Start_minutes);

        End_hours = (EditText) view.findViewById(R.id.Nap_End_hours);
        End_minutes = (EditText) view.findViewById(R.id.Nap_End_minutes);

        Nap_Details = (EditText) view.findViewById(R.id.Nap_Details);

        Button addPost = (Button) view.findViewById(R.id.Nap_AddPost);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckAndAddPost();
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

    private void CheckAndAddPost()
    {
        String tempDetails = Nap_Details.getText().toString();
        if(!tempDetails.isEmpty())
        {
            NapDetails = tempDetails;
        }

        String getStartTime = TimeFormatOK("Start Time");
        if(!getStartTime.equals("error"))
        {
            String getEndTime = TimeFormatOK("End Time");
            if(!getEndTime.equals("error"))
            {
                AddNapPost(getStartTime, getEndTime);
            }
        }
    }

    private String TimeFormatOK(String Type)
    {
        String GivenHours, GivenMinutes, AMorPM;
        if(Type.equals("Start Time")) {
             GivenHours = Start_hours.getText().toString();
             GivenMinutes = Start_minutes.getText().toString();
             AMorPM = Start_AmPmSelector.getSelectedItem().toString();
        }
        else if(Type.equals("End Time"))
        {
            GivenHours = End_hours.getText().toString();
            GivenMinutes = End_minutes.getText().toString();
            AMorPM = End_AmPmSelector.getSelectedItem().toString();
        }
        else
        {
            return "wrong type";
        }

        if(GivenHours.isEmpty())
        {
            Toast.makeText(getContext(), "Hours in " + Type + " cannot be empty", Toast.LENGTH_SHORT).show();
            return "error";
        }

        if(GivenMinutes.isEmpty())
        {
            Toast.makeText(getContext(), "Minutes in " + Type + " cannot be empty", Toast.LENGTH_SHORT).show();
            return "error";
        }

        int GivenHours_int = Integer.parseInt(GivenHours);
        int GivenMinutes_int = Integer.parseInt(GivenMinutes);

        if(GivenHours_int > 12)
        {
            Toast.makeText(getContext(), "Hours in " + Type + " needs to be less than 12", Toast.LENGTH_SHORT).show();
            return "error";
        }
        else if(GivenMinutes_int > 59)
        {
            Toast.makeText(getContext(), "Minutes in " + Type + " cannot be greater than 60", Toast.LENGTH_SHORT).show();
            return "error";
        }

        String customTime = GivenHours + ":" + GivenMinutes + " " + AMorPM;
        return customTime;
    }

    private void AddNapPost(String startTime, String EndTime)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("activities");
        String MyActId = databaseReference.push().getKey();
        databaseReference.child(MyActId).child("type").setValue("Nap");
        databaseReference.child(MyActId).child("start_time").setValue(startTime);
        databaseReference.child(MyActId).child("end_time").setValue(EndTime);
        databaseReference.child(MyActId).child("nap_details").setValue(NapDetails);
        databaseReference.child(MyActId).child("class").setValue(ClassId);
        databaseReference.child(MyActId).child("childnames").setValue(TaggedKidsString);
        AddNapPostToTaggedKids(MyActId);
    }

    private void AddNapPostToTaggedKids(final String key)
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
        Toast.makeText(getContext(), "Added Nap Post", Toast.LENGTH_SHORT).show();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack ("ActFrag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.popBackStack ("TagList", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }


}
