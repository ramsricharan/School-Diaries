package com.akhil.schooldiaries;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_ActivitiesGrid#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_ActivitiesGrid extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public Fragment_ActivitiesGrid() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_ActivitiesGrid.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_ActivitiesGrid newInstance(String param1, String param2) {
        Fragment_ActivitiesGrid fragment = new Fragment_ActivitiesGrid();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_activities_grid, container, false);

        // Registering all buttons
        ImageButton PhotoButton = (ImageButton) view.findViewById(R.id.imgBtn_photo);
        PhotoButton.setOnClickListener(this);

        ImageButton ActivitiesButton = (ImageButton) view.findViewById(R.id.imgBtn_Activities);
        ActivitiesButton.setOnClickListener(this);

        ImageButton NoteButton = (ImageButton) view.findViewById(R.id.imgBtn_Note);
        NoteButton.setOnClickListener(this);

        ImageButton FoodButton = (ImageButton) view.findViewById(R.id.imgBtn_Food);
        FoodButton.setOnClickListener(this);

        ImageButton NapButton = (ImageButton) view.findViewById(R.id.imgBtn_Nap);
        NapButton.setOnClickListener(this);

        ImageButton MedsButton = (ImageButton) view.findViewById(R.id.imgBtn_Meds);
        MedsButton.setOnClickListener(this);



        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.imgBtn_photo:
                Fragment PhotoFrag = Frag_ts_ActivityChildList.newInstance("Photo");
                getActivity().getSupportFragmentManager().beginTransaction().add(
                        R.id.teacher_content_holder, PhotoFrag).addToBackStack("TagList").commit();
                break;
            case R.id.imgBtn_Activities:
                Fragment ActFrag = Frag_ts_ActivityChildList.newInstance("customActivity");
                getActivity().getSupportFragmentManager().beginTransaction().add(
                        R.id.teacher_content_holder, ActFrag).addToBackStack("TagList").commit();
                break;
            case R.id.imgBtn_Note:
                Fragment NoteFrag = Frag_ts_ActivityChildList.newInstance("Note");
                getActivity().getSupportFragmentManager().beginTransaction().add(
                        R.id.teacher_content_holder, NoteFrag).addToBackStack("TagList").commit();
                break;
            case R.id.imgBtn_Food:
                Fragment FoodFrag = Frag_ts_ActivityChildList.newInstance("Food");
                getActivity().getSupportFragmentManager().beginTransaction().add(
                        R.id.teacher_content_holder, FoodFrag).addToBackStack("TagList").commit();
                break;
            case R.id.imgBtn_Nap:
                Fragment NapFrag = Frag_ts_ActivityChildList.newInstance("Nap");
                getActivity().getSupportFragmentManager().beginTransaction().add(
                        R.id.teacher_content_holder, NapFrag).addToBackStack("TagList").commit();
                break;
            case R.id.imgBtn_Meds:
                Fragment MedsFrag = Frag_ts_ActivityChildList.newInstance("Meds");
                getActivity().getSupportFragmentManager().beginTransaction().add(
                        R.id.teacher_content_holder, MedsFrag).addToBackStack("TagList").commit();
                break;
        }
    }
}
