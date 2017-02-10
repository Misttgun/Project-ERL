package com.maurelsagbo.project_erl.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.holders.FlightPViewHolder;
import com.maurelsagbo.project_erl.models.FlightPData;

import java.util.ArrayList;

public class FlightPAdapter extends RecyclerView.Adapter<FlightPViewHolder> {

    private ArrayList<FlightPData> mFlightPlans;

    public FlightPAdapter(ArrayList<FlightPData> flightPlans){
        this.mFlightPlans = flightPlans;
    }

    @Override
    public void onBindViewHolder(FlightPViewHolder holder, int position) {
        final FlightPData flightPData = mFlightPlans.get(position);
        holder.updateUI(flightPData);

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // Todo load details page
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFlightPlans.size();
    }

    @Override
    public FlightPViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new card view and inflate it
        View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.flight_plan_location_cardview, parent, false);

        return new FlightPViewHolder(card);
    }
}
