package com.akhil.schooldiaries;


import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.LandingAnimator;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_ChildFeed extends Fragment {
    RecyclerView rv;
    MyChildFeedAdapter adapter;
    String childid = "",childName ="", childPic = "";
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    DatabaseReference activitiesTb = db.child("activities");
    DatabaseReference childNode;
    Toolbar toolbar;
    ImageView propic;
    Boolean firstFetch = true;


    public static Fragment_ChildFeed newInstance(String id,String name, String picName) {

        Bundle args = new Bundle();
        args.putString("id",id);
        args.putString("name",name);
        args.putString("picName", picName);
        Fragment_ChildFeed fragment = new Fragment_ChildFeed();
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_ChildFeed() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        toolbar.getMenu().clear();
        if (getActivity().getClass() == ParentMain.class) {
            inflater.inflate(R.menu.child_feed_menu_parent, menu);
        } else {
            inflater.inflate(R.menu.child_feed_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int container = (getActivity().getClass()== Activity_TeacherMain.class) ? R.id.teacher_content_holder : R.id.parent_content_holder;
        switch (item.getItemId()) {
            case R.id.btn_child_profile_edit:
                //Toast.makeText(getContext(),"Edit pressed",Toast.LENGTH_LONG).show();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(container,Fragment_ChildProfile.newInstance(childid,childName))
                        .addToBackStack(null).commit();
                break;

            case R.id.btn_share_child_code:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(container, ShareChildCode.newInstance(childid,childName))
                        .addToBackStack(null).commit();
                break;

            case R.id.btn_child_shortcut:
                StorageReference pic = FirebaseStorage.getInstance().getReference().child("PostImages").child(childPic);
                        Glide.with(getActivity().getApplicationContext()).using(new FirebaseImageLoader())
                                .load(pic)
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(new SimpleTarget<Bitmap>(100,100) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                        ShortcutManager shortcutManager = getActivity().getSystemService(ShortcutManager.class);
                                        Intent intent =  new Intent(getActivity().getApplicationContext(), ParentMain.class);
                                        intent.setAction("SHORTCUT");
                                        intent.putExtra("childid",childid);
                                        intent.putExtra("childName",childName);
                                        intent.putExtra("childPic",childPic);
                                        ShortcutInfo shortcut = new ShortcutInfo.Builder(getActivity().getApplicationContext(), childid)
                                                .setShortLabel(childName)
                                                .setLongLabel("Open "+childName+ " Activity Feed")
                                                .setIcon(Icon.createWithBitmap(resource))
                                                .setIntent(intent)
                                                .build();
                                        if(shortcutManager.getDynamicShortcuts().contains(shortcut)) {
                                            Toast.makeText(getContext(), childName+"'s Shortcut Already Exist", Toast.LENGTH_SHORT).show();
                                        } else {
                                            shortcutManager.addDynamicShortcuts(Arrays.asList(shortcut));
                                        }
                                    }
                                });
        }
        return true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_child_feed, container, false);
        firstFetch = true;
        childid = getArguments().getString("id");
        childName = getArguments().getString("name");
        childPic = getArguments().getString("picName");
        int toolbar_id = (getActivity().getClass()== Activity_TeacherMain.class) ? R.id.teacher_toolbar : R.id.parent_toolbar;
        toolbar = (Toolbar) getActivity().findViewById(toolbar_id);
        toolbar.setTitle("Activity Feed");

        Toolbar coll_tool = (Toolbar) v.findViewById(R.id.toolbar_collapse);
        coll_tool.setTitleTextColor(getResources().getColor(R.color.white));
        coll_tool.setTitle(childName);
        CollapsingToolbarLayout myCollaps = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);
        myCollaps.setExpandedTitleTextAppearance(R.style.MyToolbarTheme);
        myCollaps.setCollapsedTitleTextAppearance(R.style.MyToolbarTheme);

        propic = (ImageView) v.findViewById(R.id.expandedImage);
        propic.setImageResource(R.drawable.blank_profile_pic);



        //System.out.println("clicked : id = " + childid+ " name="+childName);
        childNode = db.child("children").child(childid);
        adapter = new MyChildFeedAdapter(childName);
        rv = (android.support.v7.widget.RecyclerView) v.findViewById(R.id.child_feed_rv);
        //rv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);
        rv.setItemAnimator(new LandingAnimator());
        rv.getItemAnimator().setAddDuration(1000);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
        scaleAdapter.setDuration(600);
        scaleAdapter.setFirstOnly(false);
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(scaleAdapter);
        childNode.child("pic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String picname = dataSnapshot.getValue().toString();
                if(!picname.isEmpty()||picname.equalsIgnoreCase("0")) {
                    StorageReference pic = FirebaseStorage.getInstance().getReference().child("PostImages").child(picname);
                    Glide.with(getContext()).using(new FirebaseImageLoader())
                            .load(pic)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(propic);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        childNode.child("act_ids").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String ids_str = dataSnapshot.getValue().toString();
                if(!ids_str.equalsIgnoreCase("0"))
                {
                    if(firstFetch){
                        //rv.getRecycledViewPool().clear();
                        setActivitiesList(ids_str);
                        firstFetch = false;
                    } else {
                        List<String> ids_list = Arrays.asList(dataSnapshot.getValue().toString().split(","));
                        setActivitiesList(ids_list.get(ids_list.size()-1));
                        firstFetch = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        return v;
    }

    private void setActivitiesList(final String act_ids){
        final List<HashMap<String,String>> acts = new ArrayList<>();
        List<String> ids_list = Arrays.asList(act_ids.split(","));
        for(final String id : ids_list){
            activitiesTb.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild("type"))
                        return;
                    switch (dataSnapshot.child("type").getValue().toString()){
                        case "attendance":
                            adapter.addAct(getAttendanceAct(dataSnapshot));
                            break;
                        case "CustomActivity":
                            adapter.addAct(getCustomAct(dataSnapshot));
                            break;
                        case "Food":
                            adapter.addAct(getFoodAct(dataSnapshot));
                            break;
                        case "Nap":
                            adapter.addAct(getNapAct(dataSnapshot));
                            break;
                        case "Meds":
                            adapter.addAct(getMedAct(dataSnapshot));
                            break;
                        case "Photo":
                            adapter.addAct(getPicAct(dataSnapshot));
                            break;
                        default: break;
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    private HashMap<String,String> getAttendanceAct(DataSnapshot act_node){
        HashMap<String,String> act = new HashMap<>();
        act.put("type",act_node.child("type").getValue().toString());
        act.put("name",act_node.child("name").getValue().toString());
        act.put("class",act_node.child("class").getValue().toString());
        act.put("time",act_node.child("time").getValue().toString());
        return act;
    }

    private HashMap<String,String> getCustomAct(DataSnapshot act_node){
        HashMap<String,String> act = new HashMap<>();
        act.put("type",act_node.child("type").getValue().toString());
        act.put("name",act_node.child("name").getValue().toString());
        act.put("class",act_node.child("class").getValue().toString());
        act.put("time",act_node.child("time").getValue().toString());
        act.put("details",act_node.child("details").getValue().toString());
        act.put("childnames", act_node.child("childnames").getValue().toString());
        return act;
    }

    private HashMap<String,String> getFoodAct(DataSnapshot act_node){
        HashMap<String,String> act = new HashMap<>();
        act.put("type",act_node.child("type").getValue().toString());
        act.put("meal_name",act_node.child("meal_type").getValue().toString());
        act.put("class",act_node.child("class").getValue().toString());
        act.put("time",act_node.child("time").getValue().toString());
        //act.put("details",act_node.child("details").getValue().toString());
        act.put("childnames", act_node.child("childnames").getValue().toString());
        act.put("food_items",act_node.child("food_name").getValue().toString());
        act.put("ingredients",act_node.child("ingredients").getValue().toString());
        return act;
    }

    private HashMap<String,String> getNapAct(DataSnapshot act_node){
        HashMap<String,String> act = new HashMap<>();
        act.put("type",act_node.child("type").getValue().toString());
        act.put("class",act_node.child("class").getValue().toString());
        act.put("start_time",act_node.child("start_time").getValue().toString());
        act.put("end_time",act_node.child("end_time").getValue().toString());
        act.put("details",act_node.child("nap_details").getValue().toString());
        act.put("childnames", act_node.child("childnames").getValue().toString());
        return act;
    }

    private HashMap<String,String> getMedAct(DataSnapshot act_node){
        HashMap<String,String> act = new HashMap<>();
        act.put("type",act_node.child("type").getValue().toString());
        act.put("class",act_node.child("class").getValue().toString());
        act.put("time",act_node.child("time").getValue().toString());
        act.put("med_name",act_node.child("med_name").getValue().toString());
        act.put("symptoms",act_node.child("med_symptoms").getValue().toString());
        return act;
    }

    private HashMap<String,String> getPicAct(DataSnapshot act_node){
        HashMap<String,String> act = new HashMap<>();
        act.put("type",act_node.child("type").getValue().toString());
        act.put("class",act_node.child("class").getValue().toString());
        act.put("time",act_node.child("time").getValue().toString());
        act.put("childnames",act_node.child("childnames").getValue().toString());
        act.put("details",act_node.child("description").getValue().toString());
        act.put("photo_name",act_node.child("photo_name").getValue().toString());
        act.put("address",act_node.child("address").getValue().toString());
        return act;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
