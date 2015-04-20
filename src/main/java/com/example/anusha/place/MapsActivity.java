package com.example.anusha.place;



import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import java.io.IOException;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity  implements
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>,LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    protected static final String TAG = "creating-and-monitoring-geofences";
    private List<Marker> markers;
    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mApiClient;
    private SharedPreferences mSharedPreferences;
    private boolean mGeofencesAdded;
    public static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";
    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    protected Location mCurrentLocation;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    protected LocationRequest mLocationRequest;
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;
    protected LocationListener myLocationListener;
    DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        try {
            setUpMapIfNeeded();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, Address.class);
                startActivity(intent);
            }
        });
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        mGeofencePendingIntent = null;
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);

        // Get the value of mGeofencesAdded from SharedPreferences. Set to false as a default.
        mGeofencesAdded = mSharedPreferences.getBoolean(GEOFENCES_ADDED_KEY, false);


        // Get the geofences used. Geofence data is hard coded in this sample.

        buildGoogleApiClient();
        updateValuesFromBundle(savedInstanceState);


    }
    @Override
    protected  void onStart(){
    super.onStart();
    mApiClient.connect();


    }

    @Override
    protected void onStop() {
        super.onStop();
        mApiClient.disconnect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mApiClient.isConnected()){
            stopLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMap();

        if (mApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();

        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();

        Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {

        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);

            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);

            }
            updateUI();

        }
    }
    private void updateUI() {
        if (mCurrentLocation != null) {
            System.out.print(String.valueOf(mCurrentLocation.getLatitude()));
            System.out.print(String.valueOf(mCurrentLocation.getLongitude()));
            System.out.print(mLastUpdateTime);
            getGeofencingRequest();
            getGeofencePendingIntent();
        }
    }


    protected synchronized void buildGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, this);

    }
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }




    private void setUpMapIfNeeded() throws IOException {
        // Do a null check to confirm that we have not already instantiated the map.

        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

//SET UP MAP
    private void setUpMap() {
        db = new DatabaseHandler(this);
        Cursor c = db.fetchAllAddresses();
        markers = new ArrayList<Marker>();
        mGeofenceList = new ArrayList<Geofence>();
        if (c.getCount() > 0) {
            for (int i = 0; i < c.getCount(); i++) {
                StringBuilder sb = new StringBuilder();
                sb.append("");
                sb.append(i);
                String strI = sb.toString();
                String place = c.getString(c.getColumnIndex("name"));
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<android.location.Address> addresses = new ArrayList();
                try {
                    addresses = geocoder.getFromLocationName(place, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                android.location.Address add = addresses.get(0);
                double lat = add.getLatitude();
                double lng = add.getLongitude();
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(place));
                markers.add(marker);
                CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(lat, lng))
                        .radius(100)
                        .fillColor(0x40ff0000)
                        .strokeColor(Color.TRANSPARENT)
                        .strokeWidth(2);

                mMap.addCircle(circleOptions);
                mGeofenceList.add(new Geofence.Builder()
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setRequestId(strI)
                        .setCircularRegion(lat, lng, 100)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                        .setLoiteringDelay(10000)
                        .build());


                c.moveToNext();
            }
        }
        c.close();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();


        if (markers.size() > 0) {
            for (Marker marker : markers)
                builder.include(marker.getPosition());
            final LatLngBounds bounds = builder.build();
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition arg0) {
                    // Move camera.
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                    // Remove listener to prevent position reset on camera move.
                    mMap.setOnCameraChangeListener(null);
                }
            });
        }

    }



    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);
        return builder.build();

    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceManager.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

    }
    public void addGeofencesButtonHandler(View view) {
        if (!mApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.

        }
    }
    public void removeGeofencesButtonHandler(View view) {

        if (!mApiClient.isConnected()) {
            Toast.makeText(this,getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.


        }
    }
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.commit();

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.

            Toast.makeText(
                    this,
                    getString(mGeofencesAdded ? R.string.geofences_added :
                            R.string.geofences_removed),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage =GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }



        }
