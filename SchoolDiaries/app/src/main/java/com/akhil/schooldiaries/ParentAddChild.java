package com.akhil.schooldiaries;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ParentAddChild extends Fragment {

    private Toolbar toolbar;

    public ParentAddChild() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_parent_add_child, container, false);
        setHasOptionsMenu(true);
        toolbar = (Toolbar) getActivity().findViewById(R.id.parent_toolbar);
        toolbar.setTitle("Add Child");

        final EditText childID = (EditText) v.findViewById(R.id.editText_addChild);
        Button addChildButton = (Button) v.findViewById(R.id.button_addChild);
        addChildButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(childID.getText().toString().isEmpty()){
                    Toast.makeText(getContext(),"Enter Child ID",Toast.LENGTH_SHORT).show();
                } else {
                    addChildIfExixts(childID.getText().toString());
                }
            }
        });
        return v;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        toolbar.getMenu().clear();
        inflater.inflate(R.menu.empty_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void addChildIfExixts(final String childID){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        DatabaseReference childrenTb = db.child("children");
        childrenTb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(childID)){
                    addChildToParent(childID);
                } else {
                    Toast.makeText(getContext(),"Couldn't find Child with give ID",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void addChildToParent(final String childId){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference parent = db.child("parents").child(user.getUid());
        parent.child("children").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentChildren = dataSnapshot.getValue().toString();
                currentChildren += childId+",";
                parent.child("children").setValue(currentChildren);
                Toast.makeText(getContext(),"Child added",Toast.LENGTH_SHORT).show();
                GoBackToChildList();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    private void GoBackToChildList()
    {
        getActivity().getSupportFragmentManager().beginTransaction().replace(
                R.id.parent_content_holder, new ParentChildList()).addToBackStack(null).commit();
        getFragmentManager().popBackStack(null, getFragmentManager().POP_BACK_STACK_INCLUSIVE);
    }
    @Override
    public void onDestroy() {
        toolbar.setTitle("My Children");
        super.onDestroy();
    }
}
