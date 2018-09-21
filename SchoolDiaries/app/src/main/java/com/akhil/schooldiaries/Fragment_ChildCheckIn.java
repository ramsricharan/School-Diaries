package com.akhil.schooldiaries;


import java.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_ChildCheckIn extends Fragment {
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private android.support.v7.widget.RecyclerView recyclerView;
    private MyGridRecyclerAdapter adapter;
    DatabaseReference classesTb = databaseReference.child("classes");
    DatabaseReference childrenTb = databaseReference.child("children");
    String currentClass = "";
    String currentClassName = "";
    private List<String> checked_in_list = new ArrayList<>();
    private List<String> checked_out_list = new ArrayList<>();
    private List<String> absent_list = new ArrayList<>();


    public Fragment_ChildCheckIn() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Toolbar toolbar;
        final View v = inflater.inflate(R.layout.fragment_child_check_in, container, false);
        currentClass = ClassFragment.classid;
        currentClassName = ClassFragment.className;
        recyclerView =(android.support.v7.widget.RecyclerView) v.findViewById(R.id.child_checkin_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(),3));
        adapter = new MyGridRecyclerAdapter();
        adapter.setGridItemClickListner(new MyGridRecyclerAdapter.GridItemClickListener(){
            @Override
            public void onGridItemClick(View v,HashMap<String,String> child) {
                // Switch to anime details fragment using host activity
                Toast.makeText(getContext(),"Child clicked : "+child.get("name"),Toast.LENGTH_SHORT).show();
            }
        });

        classesTb.child(currentClass).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                checked_in_list = new ArrayList<String>(Arrays.asList(dataSnapshot.child("checked_in").getValue().toString().split(",")));
                checked_out_list =  new ArrayList<String>(Arrays.asList(dataSnapshot.child("checked_out").getValue().toString().split(",")));
                absent_list =  new ArrayList<String>(Arrays.asList(dataSnapshot.child("absent").getValue().toString().split(",")));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        classesTb.child(currentClass).child("children").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String class_children_string =  dataSnapshot.getValue().toString();
                String[] class_children_arr = class_children_string.split(",");
                final List<HashMap<String,String>> childrenList = new ArrayList<>();
                for(final String childInClass : class_children_arr){
                    childrenTb.child(childInClass).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child("name").getValue().toString();
                            HashMap<String, String> childInfo = new HashMap<String, String>();
                            childInfo.put("name",name);
                            childInfo.put("id",childInClass);
                            childInfo.put("pic",dataSnapshot.child("pic").getValue().toString());
                            if(checked_in_list.contains(childInClass)){
                                childInfo.put("status", "Checked In");
                            } else if(checked_out_list.contains(childInClass)){
                                childInfo.put("status", "Checked Out");
                            } else if(absent_list.contains(childInClass)){
                                childInfo.put("status", "Absent");
                            } else { childInfo.put("status", "none");}
                            childrenList.add(childInfo);
                            adapter.setChildren(childrenList);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        recyclerView.setAdapter(adapter);

        v.findViewById(R.id.btn_checkin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<HashMap<String,String>> selectedKids = adapter.getSelectedKids();
                adapter.resetSelection();
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                changeStatus(selectedKids,"checked_in");
                addActivity(selectedKids,"checked_in");
            }
        });
        v.findViewById(R.id.btn_checkout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<HashMap<String,String>> selectedKids = adapter.getSelectedKids();
                adapter.resetSelection();
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                changeStatus(selectedKids,"checked_out");
                addActivity(selectedKids,"checked_out");
            }
        });
        v.findViewById(R.id.btn_absent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<HashMap<String,String>> selectedKids = adapter.getSelectedKids();
                adapter.resetSelection();
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                changeStatus(selectedKids,"absent");
                addActivity(selectedKids,"absent");
            }
        });
        return v;
    }

    private String listToString(List<String> list){
        String str = "";
        for(String item : list)
            str += item + ",";
        return str;
    }
    private void changeStatus(List<HashMap<String,String>> selectedKids, String targerStatus){
        List<String> in_list = checked_in_list;
        List<String> out_list = checked_out_list;
        List<String> absent_list_this = absent_list;
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        DatabaseReference thisClass = db.child("classes").child(currentClass);
        for(HashMap<String,String> kid : selectedKids){
            String kidId = kid.get("id");
            in_list.remove(kidId);
            out_list.remove(kidId);
            absent_list_this.remove(kidId);
            switch (targerStatus){
                case "checked_in":
                    in_list.add(kidId);
                    break;
                case "checked_out":
                    out_list.add(kidId);
                    break;
                case "absent":
                    absent_list_this.add(kidId);
                    break;
            }
        }
        thisClass.child("checked_in").setValue(listToString(in_list));
        thisClass.child("checked_out").setValue(listToString(out_list));
        thisClass.child("absent").setValue(listToString(absent_list_this));
        List<HashMap<String,String>> children = adapter.getChildren();
        List<HashMap<String,String>> newChildren = new ArrayList<>();
        for(HashMap<String,String> child : children){
            String childID = child.get("id");
            if(in_list.contains(childID)){
                child.put("status","Checked In");
            } else if (out_list.contains(childID)){
                child.put("status", "Checked Out");
            } else if (absent_list_this.contains(childID)){
                child.put("status", "Absent");
            } else {
                child.put("status", "none");
            }
            newChildren.add(child);
        }
        adapter.setChildren(newChildren);
    }

    private void addActivity(final List<HashMap<String,String>> selectedKids, String activityType){
        SimpleDateFormat MyFormat = new SimpleDateFormat("hh:mm a");
        String currentTime = MyFormat.format(new Date());
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        DatabaseReference activitiesTb = db.child("activities");
        final DatabaseReference childrenTb = db.child("children");

        final String actId = activitiesTb.push().getKey();
        DatabaseReference newAct = activitiesTb.child(actId);
        newAct.child("type").setValue("attendance");
        newAct.child("time").setValue(currentTime);
        newAct.child("class").setValue(ClassFragment.className);
        switch (activityType){
            case "checked_in": newAct.child("name").setValue("Checked In"); break;
            case "checked_out": newAct.child("name").setValue("Checked Out"); break;
            case "absent": newAct.child("name").setValue("Absent"); break;
        }

        for(HashMap<String,String> child : selectedKids) {
            final String childId = child.get("id");
            childrenTb.child(childId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String currentActs = dataSnapshot.child("act_ids").getValue().toString();
                    if (currentActs.equalsIgnoreCase("0")) {
                        childrenTb.child(childId).child("act_ids").setValue(actId);
                    } else {
                        childrenTb.child(childId).child("act_ids").setValue(currentActs + "," + actId);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

}
