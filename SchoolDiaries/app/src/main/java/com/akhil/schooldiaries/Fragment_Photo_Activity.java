package com.akhil.schooldiaries;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Photo_Activity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Photo_Activity extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private String ClassId;
    private String Photo_Description = "No Description";
    private String TaggedData;
    private String TaggedKidsString;
    private TextView TaggedKids_TV;
    private TextView Location_TV;
    private List<ChildDataElement> TaggedChildList = new ArrayList<>();
    private RadioButton CurrentTime, CustomTime;

    private ProgressDialog progressDialog;
    private EditText PhotoDescription, Custom_hour, Custom_minutes;
    private Spinner AmorPmSelector;

    private ImageView Post_Image;
    private static final int CAMERA_INT = 1;
    private static final int GALLERY_INT = 2;
    private Uri CurrImageURI;
    private boolean isImageUploaded = false;

    private Uri myUri;
    private String address = "";


    public Fragment_Photo_Activity() {
        // Required empty public constructor
    }

    public static Fragment_Photo_Activity newInstance(String param1) {
        Fragment_Photo_Activity fragment = new Fragment_Photo_Activity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Some permissions for VM to allow local storage usage
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (getArguments() != null) {
            TaggedData = getArguments().getString(ARG_PARAM1);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("GPS_ADDR");
        getActivity().registerReceiver(mMessageReceiver, filter);
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String addr = intent.getStringExtra("location");
            address = addr;
            Location_TV.setText(addr);
        }
    };
    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        getActivity().unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_photo_activity, container, false);


        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Posting");

        ClassId = ClassFragment.className;
        TaggedKids_TV = (TextView) view.findViewById(R.id.Photo_taggedKids);
        SetChildData();
        Location_TV = (TextView) view.findViewById(R.id.Photo_locationTV);

        // Edit Texts
        PhotoDescription = (EditText) view.findViewById(R.id.Photo_Description);
        Custom_hour = (EditText) view.findViewById(R.id.Photo_hours);
        Custom_minutes = (EditText) view.findViewById(R.id.Photo_minutes);

        // SPinner
        AmorPmSelector = (Spinner) view.findViewById(R.id.Photo_ampm_dropdown);

        Post_Image = (ImageView) view.findViewById(R.id.Photo_image);
        Post_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("GPS_REQUEST");
                getActivity().sendBroadcast(intent);
                UploadImage();
            }
        });

        final LinearLayout CustomTimeEdit = (LinearLayout) view.findViewById(R.id.Photo_CustomTimeLayout);
        CustomTimeEdit.setVisibility(view.GONE);

        RadioGroup TimeSelector = (RadioGroup) view.findViewById(R.id.Photo_timegroup);
        CurrentTime = (RadioButton) view.findViewById(R.id.Photo_CurrTimeRadio);
        CustomTime = (RadioButton) view.findViewById(R.id.Photo_CustTimeRadio);
        TimeSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if(checkedId == R.id.Photo_CurrTimeRadio)
                {
                    CustomTimeEdit.setVisibility(view.GONE);
                }
                else if(checkedId == R.id.Photo_CustTimeRadio)
                {
                    CustomTimeEdit.setVisibility(view.VISIBLE);
                }
            }
        });


        Button AddPost = (Button) view.findViewById(R.id.Photo_PostButton);
        AddPost.setOnClickListener(new View.OnClickListener() {
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

    private void SetChildData() {
        String TaggedKids = "";
        TaggedChildList.clear();
        String[] nameAndId = TaggedData.split(",");
        for (String nameid : nameAndId) {
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

    private void UploadImage() {

        final CharSequence[] items = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Upload your Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory(), "POST_IMAGE.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    myUri = Uri.fromFile(f);
                    startActivityForResult(intent, CAMERA_INT);
                } else if (items[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_INT);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INT && resultCode == RESULT_OK) {
            Uri thisUri = data.getData();
            CompressAndSetImage(thisUri);
        }
        else if (requestCode == CAMERA_INT && resultCode == RESULT_OK) {
            Uri thisUri = myUri;
            CompressAndSetImage(thisUri);
        }
    }


    private void CompressAndSetImage(Uri uri)
    {
        Bitmap thumbnail = null;
        try {
            thumbnail = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), uri);
            int nh = (int) ( thumbnail.getHeight() * (1024.0 / thumbnail.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(thumbnail, 1024, nh, true);
            Bitmap squareImage = cropToSquare(scaled);
            CurrImageURI =  getImageUri(getActivity().getApplicationContext(), squareImage);
            Post_Image.setImageURI(CurrImageURI);
            isImageUploaded = true;

        } catch (IOException e) {
            Toast.makeText(getContext(), "Some error occured", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);

        return cropImg;
    }




    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private void CheckAndAddPost()
    {
        String des = PhotoDescription.getText().toString();
        if(!isImageUploaded)
        {
            Toast.makeText(getActivity(), "No image is selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!des.isEmpty())
        {
            Photo_Description = des;
        }
        if(address.isEmpty())
        {
            address = "No address provided";
        }
        if(CurrentTime.isChecked())
        {
            SimpleDateFormat MyFormat = new SimpleDateFormat("hh:mm a");
            String CurrentTimeis = MyFormat.format(new Date());
            AddImagePost(CurrentTimeis);
        }
        else if(CustomTime.isChecked())
        {
            String CustomTimeis = TimeFormatOK();
            if(!CustomTimeis.equals("error"))
            {
                AddImagePost(CustomTimeis);
            }
        }
    }

    private String TimeFormatOK()
    {
        String GivenHours = Custom_hour.getText().toString();
        String GivenMinutes = Custom_minutes.getText().toString();
        String AMorPM = AmorPmSelector.getSelectedItem().toString();

        if(GivenHours.isEmpty())
        {
            Toast.makeText(getActivity(), "Hours cannot be empty", Toast.LENGTH_SHORT).show();
            return "error";
        }
        if(GivenMinutes.isEmpty())
        {
            Toast.makeText(getActivity(), "Minutes cannot be empty", Toast.LENGTH_SHORT).show();
            return "error";
        }
        int GivenHours_int = Integer.parseInt(GivenHours);
        int GivenMinutes_int = Integer.parseInt(GivenMinutes);

        if(GivenHours_int > 12)
        {
            Toast.makeText(getActivity(), "Hours needs to be less than 12", Toast.LENGTH_SHORT).show();
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



    private void AddImagePost(String Act_time)
    {
        progressDialog.setMessage("Adding data to cloud");
        progressDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("activities");
        String MyActId = databaseReference.push().getKey();
        databaseReference.child(MyActId).child("type").setValue("Photo");
        databaseReference.child(MyActId).child("time").setValue(Act_time);
        databaseReference.child(MyActId).child("description").setValue(Photo_Description);
        databaseReference.child(MyActId).child("class").setValue(ClassId);
        databaseReference.child(MyActId).child("childnames").setValue(TaggedKidsString);
        databaseReference.child(MyActId).child("address").setValue(address);

        progressDialog.setMessage("Uploading image to cloud");
        String ImageName = "Image_" + MyActId;
        databaseReference.child(MyActId).child("photo_name").setValue(ImageName);

        StorageReference storagePref = FirebaseStorage.getInstance().getReference().child("PostImages").child(ImageName);
        storagePref.putFile(CurrImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Added Photo activity to the feed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AddPhotoToTaggedKids(MyActId);
    }



    private void AddPhotoToTaggedKids(final String key)
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
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack ("ActFrag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.popBackStack ("TagList", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }


}