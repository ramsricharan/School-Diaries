package com.akhil.schooldiaries;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class Fragment_Login extends Fragment  implements View.OnClickListener{

    private EditText userid;
    private EditText password;
    private Button Loginme;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;


    private RadioButton Radio_Teacher;
    private RadioButton Radio_Parent;


    public Fragment_Login() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_login, container, false);

        progressDialog = new ProgressDialog(getContext());
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        userid = (EditText) view.findViewById(R.id.UsernameField);
        password = (EditText) view.findViewById(R.id.PasswordField);

        Loginme = (Button) view.findViewById(R.id.Button_Submit);
        Loginme.setOnClickListener(this);

        Radio_Teacher = (RadioButton) view.findViewById(R.id.Radio_Teacher);
        Radio_Parent = (RadioButton) view.findViewById(R.id.Radio_Parent);

        return view;
    }


    public void LoginPressed(View view) {
        final String email = userid.getText().toString();
        String pass = password.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(getContext(), "User Id cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.isEmpty()) {
            Toast.makeText(getContext(), "Password cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Authenticating");
        progressDialog.setMessage("Loggin in...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful())
                {
                    String uid = firebaseAuth.getCurrentUser().getUid();
                    checkUserType(uid);
                }
                else
                {
                    Toast.makeText(getContext(), "Loggin failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void checkUserType(String uid)
    {
        final boolean result = false;

        if(Radio_Teacher.isChecked())
        {
            Intent i = new Intent(getActivity(), ParentMain.class);
            LoginAs("staff", uid, i);
        }

        else if (Radio_Parent.isChecked())
        {
            Intent i = new Intent(getActivity(), ParentMain.class);
            LoginAs("parents", uid, i);
        }
    }


    private void LoginAs(final String usertype, final String uid, final Intent i)
    {

        databaseReference.child(usertype).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    startActivity(i);
                    getActivity().finish();
                }
                else
                {
                    Toast.makeText(getContext(), "You do not have access to " + usertype + " account", Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.Button_Submit:
                LoginPressed(v);
        }
    }
}

