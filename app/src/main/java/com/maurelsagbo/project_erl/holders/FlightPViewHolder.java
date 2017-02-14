package com.maurelsagbo.project_erl.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.models.FlightPlan;

public class FlightPViewHolder extends RecyclerView.ViewHolder{

    private TextView locationName;

    public FlightPViewHolder(View itemView){
        super(itemView);
        locationName = (TextView) itemView.findViewById(R.id.location_name);
    }

    public void updateUI(FlightPlan flightPlan){
        locationName.setText(flightPlan.getLocationName());
    }
}
