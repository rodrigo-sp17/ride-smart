package com.github.ridesmart;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ridesmart.activities.DisplayActivity;
import com.github.ridesmart.activities.RoutesActivity;
import com.github.ridesmart.entities.Route;
import com.github.ridesmart.entities.RouteDAO;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.RouteViewHolder>  {
    public class RouteViewHolder extends RecyclerView.ViewHolder {

        LinearLayout containerView;
        TextView routeIdView;
        TextView routeNameView;
        TextView routeDistanceView;
        TextView routeDurationView;

        RouteViewHolder(View view) {
            super(view);
            containerView = view.findViewById(R.id.routes_row);
            containerView.setOnClickListener(v -> {
                Route current = (Route) containerView.getTag();
                long routeId = current.getRouteId();
                Intent intent = new Intent(v.getContext(), DisplayActivity.class);
                intent.putExtra("id", routeId);

                v.getContext().startActivity(intent);
            });

            routeIdView = view.findViewById(R.id.route_id_text);
            routeNameView = view.findViewById(R.id.route_name_text);
            routeDistanceView = view.findViewById(R.id.route_total_distance_text);
            routeDurationView = view.findViewById(R.id.route_total_duration_text);
        }
    }

    RecyclerView attachedRecyclerView;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        attachedRecyclerView = recyclerView;
    }

    private List<Route> routes = RoutesActivity.database.routeDAO().getAllRoutes();

    Route recentlyDeletedRoute;
    int recentlyDeletedRoutePosition;

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

        holder.routeIdView.setText(String.format(
                Locale.getDefault(),
                "%d",
                current.getRouteId()));

        holder.routeNameView.setText(current.getName());

        double durationMinutes = current.getDuration() / (1000.0 * 60.0);
        holder.routeDurationView.setText(String.format(
                Locale.getDefault(),
                "%.1f min",
                durationMinutes
        ));

        double distanceInKM = current.getTotalDistance();
        holder.routeDistanceView.setText(String.format(
                Locale.getDefault(),
                "%.2f km",
                distanceInKM
        ));

        holder.containerView.setTag(current);
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public void deleteItem(int position) {
        recentlyDeletedRoute = routes.get(position);
        recentlyDeletedRoutePosition = position;
        RouteDAO dao = RoutesActivity.database.routeDAO();

        dao.deleteRoute(recentlyDeletedRoute);

        routes.remove(position);

        notifyDataSetChanged();

        showUndoSnackbar();
    }

    private void showUndoSnackbar() {
        View v = attachedRecyclerView.getRootView().findViewById(R.id.coordinator_layout);

        Snackbar snack = Snackbar.make(v, R.string.snackbar_undo_text, Snackbar.LENGTH_LONG);
        snack.setAction(R.string.snackbar_undo_action, a -> undoDelete());
        snack.show();
    }

    private void undoDelete() {
        routes.add(recentlyDeletedRoutePosition, recentlyDeletedRoute);

        // Adds the deleted item to the database again
        RouteDAO dao = RoutesActivity.database.routeDAO();
        dao.saveRoute(recentlyDeletedRoute);

        notifyDataSetChanged();
    }

}
