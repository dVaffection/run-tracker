package com.dvlab.runtracker.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dvlab.runtracker.R;
import com.dvlab.runtracker.entities.Run;
import com.dvlab.runtracker.receivers.LocationReceiver;
import com.dvlab.runtracker.services.RunManager;

public class RunFragment extends Fragment {

    final public static String TAG = RunFragment.class.getSimpleName();

    private RunManager runManager;
    private BroadcastReceiver locationReceiver = new LocationReceiver() {

        @Override
        protected void onLocationReceived(Context context, Location location) {
            lastLocation = location;
            if (isVisible())
                updateUI();
        }

        @Override
        protected void onProviderEnabledChanged(boolean enabled) {
            int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
            Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
        }
    };

    private Run run;
    private Location lastLocation;


    private Button startButton, stopButton;
    private TextView startedTextView, latitudeTextView, longitudeTextView, altitudeTextView, durationTextView;

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(locationReceiver, new IntentFilter(RunManager.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(locationReceiver);
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        runManager = RunManager.get(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);

        startedTextView = (TextView) view.findViewById(R.id.run_startedTextView);
        latitudeTextView = (TextView) view.findViewById(R.id.run_latitudeTextView);
        longitudeTextView = (TextView) view.findViewById(R.id.run_longitudeTextView);
        altitudeTextView = (TextView) view.findViewById(R.id.run_altitudeTextView);
        durationTextView = (TextView) view.findViewById(R.id.run_durationTextView);

        startButton = (Button) view.findViewById(R.id.run_startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runManager.startLocationUpdates();
                run = new Run();
                updateUI();
            }
        });

        stopButton = (Button) view.findViewById(R.id.run_stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runManager.stopLocationUpdates();
                updateUI();
            }
        });

        return view;
    }

    private void updateUI() {
        boolean started = runManager.isTrackingRun();

        if (run != null)
            startedTextView.setText(run.getStartDate().toString());

        int durationSeconds = 0;

        if (run != null && lastLocation != null) {
            durationSeconds = run.getDurationSeconds(lastLocation.getTime());
            latitudeTextView.setText(Double.toString(lastLocation.getLatitude()));
            longitudeTextView.setText(Double.toString(lastLocation.getLongitude()));
            altitudeTextView.setText(Double.toString(lastLocation.getAltitude()));
        }

        durationTextView.setText(Run.formatDuration(durationSeconds));


        startButton.setEnabled(!started);
        stopButton.setEnabled(started);
    }

}
