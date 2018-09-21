package com.akhil.schooldiaries;

import android.nfc.tech.NfcA;
import android.support.v4.media.session.IMediaControllerCallback;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chinn on 12/4/2017.
 */

public class MyChildFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<HashMap<String,String>> act_list = new ArrayList<>();
    private String childName="";
    MyChildFeedAdapter(String name){
        childName = name;
    }

    public void setActList(List<HashMap<String,String>> list){act_list = list;}
    public List<HashMap<String,String>> getActList(){return act_list;}
    public void addAct(HashMap<String,String> act){
        act_list.add(0,act);
        //notifyDataSetChanged();
        notifyItemInserted(0);
        //notifyItemRangeInserted(0,act_list.size());
    }

    @Override
    public int getItemViewType(int position) {
        switch (act_list.get(position).get("type"))
        {
            case "attendance": return 1;
            case "CustomActivity": return 2;
            case "Food": return 3;
            case "Nap": return 4;
            case "Meds": return 5;
            case "Photo": return 6;
        }
        return 0;
    }
    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch(viewType) {
            case 0:
                break;
            case 1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card_attendance,parent,false);
                return new AttendanceHolder(v);
            case 2:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card_custom_activity,parent,false);
                return new CustomActHolder(v);
            case 3:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card_food,parent,false);
                return new FoodActHolder(v);
            case 4:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card_nap,parent,false);
                return new NapActHolder(v);
            case 5:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card_meds,parent,false);
                return new MedActHolder(v);
            case 6:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card_photo,parent,false);
                return new PicActHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HashMap<String,String> act = act_list.get(position);
        switch (holder.getItemViewType()){
            case 1: // Attendance item
                AttendanceHolder atd_holder = (AttendanceHolder) holder;
                if(act.get("name").equalsIgnoreCase("absent")){
                    atd_holder.atd_img.setImageResource(R.drawable.x);
                } else {
                    atd_holder.atd_img.setImageResource(R.drawable.check);
                }
                atd_holder.atd_type.setText(act.get("name"));
                atd_holder.atd_time.setText(act.get("time"));
                atd_holder.atd_class.setText("Class: "+act.get("class"));
                break;
            case 2: //Custom activity item
                CustomActHolder act_holder = (CustomActHolder) holder;
                act_holder.act_name.setText(act.get("name"));
                act_holder.act_time_class.setText("Time: "+act.get("time")+"\nClass: "+act.get("class"));
                act_holder.act_kids.setText("Tagged kids: "+act.get("childnames"));
                act_holder.act_details.setText("Details: "+act.get("details"));
                break;
            case 3: //Food activity
                FoodActHolder food_holder = (FoodActHolder)holder;
                food_holder.act_meal_name.setText(act.get("meal_name"));
                food_holder.act_time_class.setText("Time: "+act.get("time")+"\nClass: "+act.get("class"));
                food_holder.act_items.setText("Food: "+act.get("food_items"));
                food_holder.act_ingredients.setText("Ingredients: "+act.get("ingredients"));
                break;
            case 4: //Nap
                NapActHolder nap = (NapActHolder)holder;
                nap.className.setText("Class: "+act.get("class"));
                nap.times.setText("Time: "+act.get("start_time")+" - "+act.get("end_time"));
                nap.details.setText("Details: "+act.get("details"));
                break;
            case 5: //Meds
                MedActHolder med = (MedActHolder)holder;
                med.time_class.setText("Time: "+act.get("time") + "\nClass: "+act.get("class"));
                med.med.setText("Meds: "+act.get("med_name"));
                med.symptoms.setText("Symptoms: "+act.get("symptoms"));
                break;
            case 6: //Photo
                PicActHolder pic = (PicActHolder)holder;
                pic.time_class.setText("Time: "+act.get("time") + "\nClass: "+act.get("class"));
                pic.kids.setText("Tagged kids: "+act.get("childnames"));
                pic.details.setText("Details: "+act.get("details"));
                pic.location.setText("Location: " + act.get("address"));
                StorageReference sr = FirebaseStorage.getInstance().getReference();
                StorageReference picRef = sr.child("PostImages").child(act.get("photo_name"));
                Glide.with(pic.itemView.getContext())
                        .using(new FirebaseImageLoader())
                        .load(picRef)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(pic.pic);
        }

    }

    @Override
    public int getItemCount() {
        return act_list.size();
    }

    static class AttendanceHolder extends RecyclerView.ViewHolder{
        private ImageView atd_img;
        private TextView atd_type,atd_time,atd_class;
        public AttendanceHolder(View v){
            super(v);
            atd_img = (ImageView)v.findViewById(R.id.attendance_img);
            atd_type = (TextView)v.findViewById(R.id.attendance_act_type);
            atd_time = (TextView)v.findViewById(R.id.attendance_time);
            atd_class = (TextView)v.findViewById(R.id.attendance_class);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Attendance item clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    static class CustomActHolder extends RecyclerView.ViewHolder{
        private TextView act_name,act_time_class,act_kids,act_details;
        boolean toggle = true;
        public CustomActHolder(View v){
            super(v);
            act_name = (TextView)v.findViewById(R.id.custom_act_name);
            act_time_class = (TextView)v.findViewById(R.id.custom_act_time_and_class);
            act_details = (TextView)v.findViewById(R.id.custom_act_details);
            act_kids = (TextView)v.findViewById(R.id.custom_act_kids);
            act_kids.setSingleLine(toggle);
            act_details.setSingleLine(toggle);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle = !toggle;
                    Toast.makeText(v.getContext(), "Custom Act item clicked", Toast.LENGTH_SHORT).show();
                    act_kids.setSingleLine(toggle);
                    act_details.setSingleLine(toggle);
                }
            });
        }
    }
    static class FoodActHolder extends RecyclerView.ViewHolder{
        private TextView act_meal_name,act_time_class,act_items,act_ingredients;
        boolean toggle = true;
        public FoodActHolder(View v){
            super(v);
            act_meal_name = (TextView)v.findViewById(R.id.food_name);
            act_time_class = (TextView)v.findViewById(R.id.food_time_and_class);
            act_items = (TextView)v.findViewById(R.id.food_items);
            act_ingredients = (TextView)v.findViewById(R.id.food_details);
//            act_kids.setSingleLine(toggle);
//            act_details.setSingleLine(toggle);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle = !toggle;
                    Toast.makeText(v.getContext(), "Food Act item clicked", Toast.LENGTH_SHORT).show();
//                    act_kids.setSingleLine(toggle);
//                    act_details.setSingleLine(toggle);
                }
            });
        }
    }

    static class NapActHolder extends RecyclerView.ViewHolder{
        private TextView className,times,details;
        public NapActHolder(View v){
            super(v);
            className = (TextView)v.findViewById(R.id.nap_class);
            times = (TextView)v.findViewById(R.id.nap_times);
            details =(TextView)v.findViewById(R.id.nap_details);
        }
    }

    static class MedActHolder extends RecyclerView.ViewHolder{
        private TextView time_class,med,symptoms;
        public MedActHolder(View v){
            super(v);
            time_class = (TextView)v.findViewById(R.id.med_time_and_class);
            med = (TextView)v.findViewById(R.id.med_name);
            symptoms =(TextView)v.findViewById(R.id.med_symptoms);
        }
    }

    static class PicActHolder extends RecyclerView.ViewHolder{
        private TextView time_class,kids,details, location;
        private ImageView pic;
        public PicActHolder(View v){
            super(v);
            time_class = (TextView)v.findViewById(R.id.photo_time_class);
            kids = (TextView)v.findViewById(R.id.photo_kids);
            details = (TextView)v.findViewById(R.id.photo_details);
            pic = (ImageView)v.findViewById(R.id.photo_holder);
            location = (TextView) v.findViewById(R.id.photo_location);
        }
    }
}
