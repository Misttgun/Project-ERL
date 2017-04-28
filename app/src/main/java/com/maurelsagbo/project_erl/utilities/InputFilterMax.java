package com.maurelsagbo.project_erl.utilities;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

public class InputFilterMax implements InputFilter{

    private int max;
    private Context context;

    public InputFilterMax(Context con, int max) {
        this.max = max;
        this.context = con;
    }

    public InputFilterMax(Context con, String max) {
        this.max = Integer.parseInt(max);
        this.context = con;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String replacement = source.subSequence(start, end).toString();

            String newVal = dest.toString().substring(0, dstart) + replacement +dest.toString().substring(dend, dest.toString().length());

            int input = Integer.parseInt(newVal);

            if (input<=max)
                return null;
        } catch (NumberFormatException nfe) {
            Toast.makeText(context, "Le nombre doit être inférieur ou égal à  " + max, Toast.LENGTH_SHORT).show();
            return null;
        }
        return "";
    }
}
