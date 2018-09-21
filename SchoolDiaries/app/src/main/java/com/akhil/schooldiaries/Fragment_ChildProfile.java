package com.akhil.schooldiaries;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_ChildProfile extends Fragment {


    public Fragment_ChildProfile() {
        // Required empty public constructor
    }

    public static Fragment_ChildProfile newInstance(String id,String name) {

        Bundle args = new Bundle();
        args.putString("id",id);
        args.putString("name",name);
        Fragment_ChildProfile fragment = new Fragment_ChildProfile();
        fragment.setArguments(args);
        return fragment;
    }

    private String childid,childName;
    private TextView tv_childname,tv_dob,tv_parent,tv_num,tv_meds,tv_allergy,tv_notes;
    private ImageView iv_child;
    private Button saveBtn,delBtn;
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    DatabaseReference childrenTb = db.child("children");
    DatabaseReference child;
    private Uri myUri;
    private static final int CAMERA_INT = 1;
    private static final int GALLERY_INT = 2;
    private Uri CurrImageURI;
    private boolean isImageUploaded = false;
    private ProgressDialog myProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Some permissions for VM to allow local storage usage
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_child_profile, container, false);
        setHasOptionsMenu(true);
        tv_childname = (TextView) v.findViewById(R.id.et_childName);
        tv_dob = (TextView) v.findViewById(R.id.et_childDOB);
        tv_parent = (TextView) v.findViewById(R.id.et_childParentName);
        tv_num = (TextView) v.findViewById(R.id.et_childParentNum);
        tv_meds = (TextView) v.findViewById(R.id.et_childMeds);
        tv_allergy = (TextView) v.findViewById(R.id.et_childAllergy);
        tv_notes= (TextView) v.findViewById(R.id.et_childNotes);
        iv_child = (ImageView)v.findViewById(R.id.child_profile_edit_pic) ;
        saveBtn = (Button)v.findViewById(R.id.btn_child_profile_save);
        delBtn = (Button)v.findViewById(R.id.btn_child_profile_delete);

        childid = getArguments().getString("id");
        childName = getArguments().getString("name");
        child = childrenTb.child(childid);

        myProgress = new ProgressDialog(getContext());
        myProgress.setTitle("Updating Profile");

        Toolbar toolbar;
        int toolbar_id = (getActivity().getClass()== Activity_TeacherMain.class) ? R.id.teacher_toolbar : R.id.parent_toolbar;
        toolbar = (Toolbar) getActivity().findViewById(toolbar_id);
        toolbar.setTitle(childName + "'s Profile");
        setCurrentData();

        if(getActivity().getClass() != Activity_TeacherMain.class)
            delBtn.setVisibility(View.GONE);

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                child.removeValue();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tv_childname.getText().toString().isEmpty())
                    child.child("name").setValue(tv_childname.getText().toString());
                if(!tv_dob.getText().toString().isEmpty())
                    child.child("dob").setValue(tv_dob.getText().toString());
                if(!tv_parent.getText().toString().isEmpty())
                    child.child("parent").setValue(tv_parent.getText().toString());
                if(!tv_num.getText().toString().isEmpty())
                    child.child("parent_num").setValue(tv_num.getText().toString());
                if(!tv_meds.getText().toString().isEmpty())
                    child.child("meds").setValue(tv_meds.getText().toString());
                if(!tv_allergy.getText().toString().isEmpty())
                    child.child("allergy").setValue(tv_allergy.getText().toString());
                if(!tv_notes.getText().toString().isEmpty())
                    child.child("notes").setValue(tv_notes.getText().toString());

                CheckAndAddPic();
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                Toast.makeText(getContext(), "All changes saved!", Toast.LENGTH_SHORT).show();
            }
        });

        iv_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage();
            }
        });
        return v;
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
            iv_child.setImageURI(CurrImageURI);
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

    private void CheckAndAddPic(){
        if(!isImageUploaded){
            //Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        } else {
            myProgress.setMessage("Uploading Photo");
            myProgress.show();
            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("children");
            String ImageName = childid+"_profilePic";
            db.child(childid).child("pic").setValue("");
            db.child(childid).child("pic").setValue(ImageName);

            StorageReference storagePref = FirebaseStorage.getInstance().getReference().child("PostImages").child(ImageName);
            storagePref.delete();
            storagePref.putFile(CurrImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        myProgress.dismiss();
                        //Toast.makeText(getContext(), "Photo Uploaded", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void setCurrentData(){
        child.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tv_childname.setText((dataSnapshot.child("name").getValue() != null)? dataSnapshot.child("name").getValue().toString() : "");
                tv_dob.setText((dataSnapshot.child("dob").getValue() != null)? dataSnapshot.child("dob").getValue().toString() : "");
                tv_parent.setText((dataSnapshot.child("parent").getValue() != null)? dataSnapshot.child("parent").getValue().toString() : "");
                tv_num.setText((dataSnapshot.child("parent_num").getValue() != null)? dataSnapshot.child("parent_num").getValue().toString() : "");
                tv_meds.setText((dataSnapshot.child("meds").getValue() != null)? dataSnapshot.child("meds").getValue().toString() : "");
                tv_allergy.setText((dataSnapshot.child("allergy").getValue() != null)? dataSnapshot.child("allergy").getValue().toString() : "");
                tv_notes.setText((dataSnapshot.child("notes").getValue() != null)? dataSnapshot.child("notes").getValue().toString() : "");
                String pic_name = (dataSnapshot.child("pic").getValue() != null)? dataSnapshot.child("pic").getValue().toString() : "";
                if(!pic_name.isEmpty()||pic_name.equalsIgnoreCase("0")) {
                    StorageReference pic = FirebaseStorage.getInstance().getReference().child("PostImages").child(pic_name);
                    Glide.with(getContext()).using(new FirebaseImageLoader())
                            .load(pic)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(iv_child);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.empty_menu,menu);
    }
}
