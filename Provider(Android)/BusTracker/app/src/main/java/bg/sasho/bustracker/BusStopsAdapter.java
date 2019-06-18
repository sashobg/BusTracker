package bg.sasho.bustracker;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import bg.sasho.bustracker.database.BusStop;
import bg.sasho.bustracker.R;


public class BusStopsAdapter extends RecyclerView.Adapter<BusStopsAdapter.MyViewHolder> {
    private Context context;
    private List<BusStop> busStopsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView dot;
        public TextView officialName;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            dot = view.findViewById(R.id.dot);
            officialName = view.findViewById(R.id.officialName);
        }
    }
    public BusStopsAdapter(Context context, List<BusStop> busStopsList) {
        this.context = context;
        this.busStopsList = busStopsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bus_stop_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        BusStop busStop = busStopsList.get(position);

        holder.name.setText(busStop.getName());

        // Displaying dot from HTML character code
        holder.dot.setText(busStop.getNumber());

        // Formatting and displaying timestamp
        holder.officialName.setText(busStop.getOfficialName());
    }

    @Override
    public int getItemCount() {
        return busStopsList.size();
    }



}
