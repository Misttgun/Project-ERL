package com.maurelsagbo.project_erl.utilities;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class OnFocusChangeListenerMin implements View.OnFocusChangeListener {

    private int min;
    private Context context;

    public OnFocusChangeListenerMin(Context con, int min) {
        this.min = min;
        this.context = con;
    }

    public OnFocusChangeListenerMin(Context con, String min) {
        this.min = Integer.parseInt(min);
        this.context = con;
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus) {
            String val = ((EditText)v).getText().toString();
            if(!TextUtils.isEmpty(val)){
                if(Integer.valueOf(val)<min){
                    Toast.makeText(context, "Le nombre doit être supérieur ou égal à " + min, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
}
