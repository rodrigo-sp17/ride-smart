package com.github.ridesmart;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.RouteViewHolder>  {
    public class RouteViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerView;
        TextView routeNameView;

        RouteViewHolder(View view) {
            super(view);
            containerView = view.findViewById(R.id.routes_row);
            routeNameView = view.findViewById(R.id.route_name_text);
        }
    }

    private List<Route> routes = RoutesActivity.database.routeDAO().getAllRoutes();

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.routes_row, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route current = routes.get(position);
        holder.routeNameView.setText(String.format(Locale.getDefault(),"%d",current.details.routeId));
        holder.containerView.setTag(current);
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

}
