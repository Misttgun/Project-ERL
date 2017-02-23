package com.maurelsagbo.project_erl.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maurelsagbo.project_erl.R;
import com.maurelsagbo.project_erl.activities.FlightPDetailActivity;
import com.maurelsagbo.project_erl.holders.FlightPViewHolder;
import com.maurelsagbo.project_erl.models.FlightPlan;

import java.util.List;

public class FlightPAdapter extends RecyclerView.Adapter<FlightPViewHolder> {

    private List<FlightPlan> mFlightPlans;
    private Context context;

    public FlightPAdapter(List<FlightPlan> flightPlans, Context context){
        this.mFlightPlans = flightPlans;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(FlightPViewHolder holder, int position) {
        final FlightPlan flightPlan = mFlightPlans.get(position);
        holder.updateUI(flightPlan);

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FlightPDetailActivity.class);
                intent.putExtra("flightPlanID", flightPlan.getId());
                context.startActivity(intent);
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
