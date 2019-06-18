package bg.sasho.bustracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bg.sasho.bustracker.database.BusStop;
import bg.sasho.bustracker.database.DatabaseHelper;
import bg.sasho.bustracker.utils.MyDividerItemDecoration;
import bg.sasho.bustracker.utils.RecyclerTouchListener;

public class MainActivity extends AppCompatActivity {
    private BusStopsAdapter mAdapter;
    private List<BusStop> busStopsList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noStopsView;

    private DatabaseHelper db;

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noStopsView = findViewById(R.id.empty_busstops_view);

        db = new DatabaseHelper(this);
        try {
            db.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = db.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        busStopsList.addAll(db.getAllBusStops());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBusStopDialog(false, null, -1);
            }
        });

        mAdapter = new BusStopsAdapter(this, busStopsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyStops();


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }


    private void createBusStop(String name, String number) {

        long id = db.insertBusStop(name,number,"", "", "");


        BusStop n = db.getBusStopById(id);

        if (n != null) {
            busStopsList.add(0, n);

            mAdapter.notifyDataSetChanged();

            toggleEmptyStops();
        }
    }


    private void updateBusStop(String name, String number, int position) {
        BusStop n = busStopsList.get(position);
        // updating bus stop name and number
        n.setName(name);
        n.setNumber(number);
        db.updateBusStop(n);



        // refreshing the list
        busStopsList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyStops();
    }


    private void deleteBusStop(int position) {
        db.deleteBusStop(busStopsList.get(position));

        busStopsList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyStops();
    }


    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showBusStopDialog(true, busStopsList.get(position), position);
                } else {
                    deleteBusStop(position);
                }
            }
        });
        builder.show();
    }


    private void showBusStopDialog(final boolean shouldUpdate, final BusStop busStop, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.bus_stop_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputBusStopName = view.findViewById(R.id.name);
        final EditText inputBusStopNumber = view.findViewById(R.id.number);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);

        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_busstop_title) : getString(R.string.lbl_new_busstop_title));

        if (shouldUpdate && busStop != null) {
            inputBusStopName.setText(busStop.getName());
            inputBusStopNumber.setText(busStop.getNumber());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputBusStopName.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter bus stop custom name!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                if (TextUtils.isEmpty(inputBusStopNumber.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter bus stop number!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating bus stop
                if (shouldUpdate && busStop != null) {
                    updateBusStop(inputBusStopName.getText().toString(), inputBusStopNumber.getText().toString(), position);
                } else {
                    // create new Bus stop
                    createBusStop(inputBusStopName.getText().toString(), inputBusStopNumber.getText().toString());
                }
            }
        });
    }


    private void toggleEmptyStops() {
        // you can check busStopsList.size() > 0

        if (db.getBusStopsCount() > 0) {
            noStopsView.setVisibility(View.GONE);
        } else {
            noStopsView.setVisibility(View.VISIBLE);
        }
    }
}
