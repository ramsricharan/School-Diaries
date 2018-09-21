package com.akhil.schooldiaries;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShareChildCode#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareChildCode extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String ChildName;
    private String ChildId;

    private TextView Heading, ChildCode;
    private ImageButton ShareCodeButton;

    public ShareChildCode() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShareChildCode.
     */
    // TODO: Rename and change types and number of parameters
    public static ShareChildCode newInstance(String param1, String param2) {
        ShareChildCode fragment = new ShareChildCode();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ChildId = getArguments().getString(ARG_PARAM1);
            ChildName = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_share_child_code, container, false);
        
        Heading = (TextView) view.findViewById(R.id.ShareCode_intro);
        String title = "The 'Child Code' for '" + ChildName + "' is given below.";
        Heading.setText(title);

        ChildCode = (TextView) view.findViewById(R.id.ShareCode_childCode);
        ChildCode.setText(ChildId);

        ShareCodeButton = (ImageButton) view.findViewById(R.id.ShareCode_sendButton);
        ShareCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myMessage = "Hello there, \nHere is the ChildCode for the student '" + ChildName + "'. \n" +
                        "ChildCode = '" + ChildId + "'. \n" +
                        "You can use this code to add this child to your list." +
                        "You can also share this with your other family members who has the app 'School Diaries'. \nThank you.";

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, myMessage);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share via.."));

//                Intent intentsms = new Intent( Intent.ACTION_VIEW, Uri.parse( "sms:" + "" ) );
//                intentsms.putExtra( "sms_body", myMessage );
//                startActivity( intentsms );
            }
        });

        return view;
    }

}
