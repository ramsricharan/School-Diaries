package com.akhil.schooldiaries;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


public class ParentChildList extends Fragment {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = firebaseAuth.getCurrentUser();
    final DatabaseReference parent = databaseReference.child("parents").child(user.getUid());
    private android.support.v7.widget.RecyclerView recyclerView;
    private MyRecyclerViewAdapter adapter;

    private Toolbar toolbar;

    public ParentChildList() {
        // Required empty public constructor
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.parent_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_child) {
            getActivity().getSupportFragmentManager().beginTransaction().add(
                    R.id.parent_content_holder, new ParentAddChild()).addToBackStack(null).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_parent_child_list, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.parent_toolbar);
        toolbar.setTitle("My Children");
        setHasOptionsMenu(true);



        recyclerView =(android.support.v7.widget.RecyclerView) v.findViewById(R.id.parent_child_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        adapter = new MyRecyclerViewAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), layoutManager.getOrientation());
       recyclerView.addItemDecoration(dividerItemDecoration);
        //rv.setItemAnimator(new LandingAnimator());
        //rv.getItemAnimator().setAddDuration(1000);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
        scaleAdapter.setDuration(1000);
        recyclerView.setAdapter(scaleAdapter);
        adapter.setItemClickListener(new MyRecyclerViewAdapter.RecyclerItemClickListener() {
            @Override
            public void onItemClick(View v, HashMap<String,String> child) {

                ImageView myPic = (ImageView) v.findViewById(R.id.child_list_pic);
                myPic.setTransitionName("ProPicShared");
                Fragment MyFrag =  Fragment_ChildFeed.newInstance(child.get("id"),child.get("name"), child.get("pic"));
                // Exit transition
                Fade exitFade = new Fade();
                exitFade.setDuration(500);
                setExitTransition(exitFade);

                TransitionSet MoveImage = new TransitionSet();
                MoveImage.addTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
                MoveImage.setDuration(300);
                MoveImage.setStartDelay(100);
                MyFrag.setSharedElementEnterTransition(MoveImage);

                // Transition for entire fragment
                Fade enterFade = new Fade();
                enterFade.setStartDelay(300);
                enterFade.setDuration(300);
                MyFrag.setEnterTransition(enterFade);

                getActivity().getSupportFragmentManager().beginTransaction().addSharedElement(myPic, myPic.getTransitionName())
                        .replace(R.id.parent_content_holder, MyFrag)
                        .addToBackStack(null).commit();
            }
        });


        parent.child("children").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String s =(String)dataSnapshot.getValue();
                if(s.equalsIgnoreCase("0") || s.isEmpty()) return;
                String[] ids = s.split(",");
                final List<HashMap<String,String>> children = new ArrayList<>();// = getChildren(s);
                for(final String id : ids){
                    final HashMap<String,String> child = new HashMap<>();
                    databaseReference.child("children").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.hasChildren())
                                return;
                            child.put("name",dataSnapshot.child("name").getValue().toString());
                            child.put("id",id);
                            child.put("pic",dataSnapshot.child("pic").getValue().toString());
                            //adapter.addChild(child);
                            children.add(child);
                            adapter.setChildren(children);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        recyclerView.setAdapter(adapter);
        return v;
    }
}
