package chalmers.ip_radio;

import chalmers.ip_radio.VoIP.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MapActivity extends FragmentActivity implements
        LocationListener,
        LocationSource,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private LatLng latlng_gbg = new LatLng(57.7000, 11.9667);
    private LatLng latlng_gbg2 = new LatLng(57.7000, 11.9666);
    private GoogleMap map;
    private Location myLocation, location, myLastLocation;
    private LatLng myLatLng, myLatLng1, myLatLng2;
    private LocationManager locationManager;
    private HashMap<String, UserOnMap> userLocations; //How too keep multiple user locations?
    private HashMap<String, Marker> usersOnMap = new HashMap<String, Marker>();
    private Context context = this;
    private AlertDialog.Builder builder;
    private OnLocationChangedListener listener;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private boolean requestingLocationUpdates = true;
    private LocationRequest locationRequest;

    private TextView tv1, tv2;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean resolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, resolvingError);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ON CREATE","=================");
        super.onCreate(savedInstanceState);
        resolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        setContentView(R.layout.activity_map);
        tv1 = (Button) findViewById(R.id.tv1);
        tv2 = (Button) findViewById(R.id.tv2);
        initiateMap();

        if(googleApiClient == null){
            Log.d("GoogleAPIClient = NULL","=================");
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (true) {  // more about this later
            googleApiClient.connect();
        }
    }
    @Override
    protected void onStop() {
        Log.d("onStop","%%%%%%%%%%");
        googleApiClient.disconnect();
        super.onStop();
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected() && !requestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("ON CONF CHANGED","%%%%%%%%%%%%%%%%%%%");
        //setContentView(R.layout.activity_map);
    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void initiateMap() {
        try {
            if (map == null) {
                map = ((SupportMapFragment) getSupportFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMapToolbarEnabled(false);
            map.getUiSettings().setTiltGesturesEnabled(false);
            map.getUiSettings().setRotateGesturesEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);

            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    startLocationUpdates();
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMarker(String user, LatLng latLng){
        //Creating mark
        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Call " + user + "?")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck32px)));
        usersOnMap.put(user, marker);

        //When pressing an icon the map stops following your location
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                stopLocationUpdates();
                return false;
            }
        });
        //onClickListener for the information window that appears after pressing an icon
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            public void onInfoWindowClick(Marker mark) {
                builder = new AlertDialog.Builder(context);
                builder.setMessage(mark.getTitle())
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Should call that person
                                //TalkActivity ta = new TalkActivity();
                                //ta.initCall();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
    public void getMarkers() {
        //Some kind of call/request to the server
        usersOnMap.put("User1", map.addMarker(new MarkerOptions()
                .position(myLatLng)
                .title("Call " + "User1" + "?")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck32px))));
    }
    public void updateMarker(String user, LatLng latLng){
        usersOnMap.get(user).setPosition(latLng);
    }
    public void sendMyLocation(){

    }
    public void removeMarker(String user){ //remove user from
       usersOnMap.get(user).remove();
    }
    public void onLocationChanged(Location location) {
        Log.d("on location changed","%%%%%%%%%%%%%%%%%%%");
        if(location == null)
            Log.d("onLocationChanged location", "NULL");
        tv1.setText(String.valueOf(location.getLatitude()));
        tv2.setText(String.valueOf(location.getLongitude()));


        myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(myLatLng));
        //map.animateCamera(CameraUpdateFactory.zoomTo(14));
        //map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        //map.animateCamera(CameraUpdateFactory.zoomTo(16));
    }
    @Override
    public void activate(OnLocationChangedListener listener){

        this.listener = listener;
        /*
        LocationProvider gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        if(gpsProvider != null){
            locationManager.requestLocationUpdates(gpsProvider.getName(), 0, 10, (android.location.LocationListener) this);
        }

        LocationProvider networkProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);;
        if(networkProvider != null){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60 * 5, 0, (android.location.LocationListener) this);
        }
        */

    }
    @Override
    public void deactivate() {
         listener = null;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.
        Log.d("ON CONNECTED%%%%%%%%%%%%%%%%%%%","");
        createLocationRequest();
        if(requestingLocationUpdates) {
            //Starts requesting location updates
            startLocationUpdates();
        }
        myLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (myLastLocation != null) {
            Log.d("INSIDE %%%%%%%%%%%%%%%%%%%","");
            myLatLng = new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude());
            addMarker("User1", myLatLng);
            try {
                map.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
                map.animateCamera(CameraUpdateFactory.zoomTo(14));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            Log.d("OUTSIDE %%%%%%%%%%%%%%%%%%%","");
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        Log.d("ON CONNECTION SUSPENDED %%%%%%%%%%%%%%%%%%%","");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                resolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            resolvingError = true;
        }
        Log.d("ON CONNECTION FAILED%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%","%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    }
    private Location bestLastKnownLocation(float minAccuracy, long minTime) {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        // Get the best most recent location currently available
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (mCurrentLocation != null) {
            float accuracy = mCurrentLocation.getAccuracy();
            long time = mCurrentLocation.getTime();

            if (accuracy < bestAccuracy) {
                bestResult = mCurrentLocation;
                bestAccuracy = accuracy;
                bestTime = time;
            }
        }

        // Return best reading or null
        if (bestAccuracy > minAccuracy || bestTime < minTime) {
            return null;
        }
        else {
            return bestResult;
        }
    }
    private boolean servicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        }
        else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
            return false;
        }
    }

    private void centerMapOnMyLocation(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Criteria object to retrieve provider
        Criteria criteria = new Criteria();

        //Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        //Get current location
        myLocation = locationManager.getLastKnownLocation(provider);

        //"Convert" to LatLng
        myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        myLatLng1 = new LatLng(myLocation.getLatitude() - 0.005, myLocation.getLongitude() - 0.005);
        myLatLng2 = new LatLng(myLocation.getLatitude() + 0.005, myLocation.getLongitude() + 0.005);

        try {
            map.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
            //map.animateCamera(CameraUpdateFactory.zoomTo(14));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        //dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        resolvingError = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            resolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!googleApiClient.isConnecting() &&
                        !googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }
        }
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MapActivity)getActivity()).onDialogDismissed();
        }
    }

}
