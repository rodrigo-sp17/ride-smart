package com.github.ridesmart;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

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
        holder.routeNameView.setText(String.format(Locale.getDefault(),"%d",current.details.routeId));
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

        dao.deleteRouteDetails(recentlyDeletedRoute.details);

        for (Turn t : recentlyDeletedRoute.turns) {
            dao.deleteTurns(t);
        }

        for (RouteNode n : recentlyDeletedRoute.routeNodes) {
            dao.deleteRouteNodes(n);
        }

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
        dao.insertRouteDetails(recentlyDeletedRoute.details);
        for (Turn t : recentlyDeletedRoute.turns) {
            dao.insertTurns(t);
        }
        for (RouteNode n : recentlyDeletedRoute.routeNodes) {
            dao.insertRouteNodes(n);
        }

        notifyDataSetChanged();
    }

}
