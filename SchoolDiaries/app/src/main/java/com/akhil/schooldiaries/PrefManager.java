package com.akhil.schooldiaries;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by chinn on 12/6/2017.
 */

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    private static final String PREF_NAME = "Remember";

    private static final String ID = "id";
    private static final String PASS = "pass";
    private static final String TYPE = "type";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME,_context.MODE_PRIVATE);
        editor = pref.edit();
    }
    public void setLoginData(String id, String pass, String type){
        editor.putBoolean("SET",true);
        editor.putString(ID,id);
        editor.putString(PASS,pass);
        editor.putString(TYPE,type);
        editor.commit();
    }

    public boolean isDataSet(){
        return pref.getBoolean("SET",false);
    }

    public void resetData(){
        editor.putBoolean("SET",false);
        editor.commit();
    }

    public String[] getLoginData()
    {
        String [] data = {pref.getString(ID,""), pref.getString(PASS,""), pref.getString(TYPE,"")};
        return data;
    }
}
