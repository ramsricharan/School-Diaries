package com.akhil.schooldiaries;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
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
 * Use the {@link Frag_ts_ActivityChildList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Frag_ts_ActivityChildList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private Button NextButton;
    private String CurrentClassId, ActivityType;
    private ListView ChildNameLS;
    private String[] AllChildIds;


    public Frag_ts_ActivityChildList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment Frag_ts_ActivityChildList.
     */
    // TODO: Rename and change types and number of parameters
    public static Frag_ts_ActivityChildList newInstance(String param1) {
        Frag_ts_ActivityChildList fragment = new Frag_ts_ActivityChildList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ActivityType = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_ts_activity_child_list, container, false);

        CurrentClassId = ClassFragment.classid;
        ChildNameLS = (ListView) view.findViewById(R.id.currclass_childList);
        getCheckinStudents();

        NextButton = (Button) view.findViewById(R.id.button_Next);
        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taggedchild = GetSelectedChild();
                if(!taggedchild.equals("error"))
                    LaunchIt(taggedchild);
            }
        });

        return view;
    }

    private String GetSelectedChild()
    {
        String TaggedChild = "";
        int itemCount = ChildNameLS.getChildCount();
        for(int i = 0; i < itemCount; i++)
        {
            CheckedTextView c = (CheckedTextView) ChildNameLS.getChildAt(i);
            if(c.isChecked())
            {
                String child = c.getText().toString();
                String childId = AllChildIds[i];
                TaggedChild += childId  + "/" + child + ",";
            }
        }
        if(TaggedChild.equals("")) {
            Toast.makeText(getContext(), "No child selected", Toast.LENGTH_SHORT).show();
            return "error";
        }
        else
            return TaggedChild;
    }

   private void getCheckinStudents()
   {
       DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
       ref.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               String AllChildIds = dataSnapshot.child("classes").child(CurrentClassId).child("children").getValue().toString();
               String[] AllChildIdsArray = AllChildIds.split(",");
               setChildIdsList(AllChildIdsArray);
               List<String> AllChildNamesList = new ArrayList<String>();
               for(String id : AllChildIdsArray)
               {
                   String name = dataSnapshot.child("children").child(id).child("name").getValue().toString();
                   AllChildNamesList.add(name);
               }
               setListView(AllChildNamesList);
           }
           @Override
           public void onCancelled(DatabaseError databaseError) {
           }
       });
   }

   private void setChildIdsList(String[] AllIds)
   {
       AllChildIds = AllIds;
   }

   private void setListView(List<String> NamesList)
   {
       ArrayAdapter adapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_checked, NamesList);
       ChildNameLS.setAdapter(adapter1);
       ChildNameLS.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
   }


    private void LaunchIt(String taggedChildren)
    {
        Fragment MyFragment;
        switch (ActivityType)
        {
            case "customActivity":
                MyFragment = Fragment_Child_CustomActivity.newInstance(taggedChildren);
                break;
            case "Note":
                MyFragment = Fragment_Note_activity.newInstance(taggedChildren);
                break;
            case "Food":
                MyFragment = Fragment_Food_Activity.newInstance(taggedChildren);
                break;
            case "Nap":
                MyFragment = Fragment_Nap_Activity.newInstance(taggedChildren);
                break;
            case "Meds":
                MyFragment = Fragment_Meds_Activity.newInstance(taggedChildren);
                break;
            case "Photo":
                MyFragment = Fragment_Photo_Activity.newInstance(taggedChildren);
                break;
            default:
                MyFragment = new Fragment_Child_CustomActivity();
                break;
        }
        getActivity().getSupportFragmentManager().beginTransaction().add(
                R.id.teacher_content_holder, MyFragment).addToBackStack("ActFrag").commit();
    }
}
