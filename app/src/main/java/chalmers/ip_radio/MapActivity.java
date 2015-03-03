package chalmers.ip_radio;

import chalmers.ip_radio.VoIP.*;
import chalmers.ip_radio.Traccar.*;

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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;

import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.content.Context;
import android.content.DialogInterface;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import java.util.HashMap;

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
    private HashMap<String, Marker> usersOnMap = new HashMap<String, Marker>();
    private Context context = this;
    private AlertDialog.Builder builder;
    private OnLocationChangedListener listener;
    private GoogleApiClient googleApiClient;
    private boolean requestingLocationUpdates = true;
    private LocationRequest locationRequest;
    private TelephonyManager telephonyManager;
    private boolean markerPressed;
    private Marker lastPressedMarker;

    private ClientController clientController;
    private String id;
    private final int port = 5005;
    private String address = "192.168.38.100";

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

        isGPSEnable();

        Log.d("ON CREATE222","=================");

        markerPressed = false;
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);

        initiateMap();

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        id = telephonyManager.getDeviceId();
        clientController = new ClientController(this, address, port, Protocol.createLoginMessage(id));
        clientController.start();



        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if(googleApiClient == null){
            Log.d("GoogleAPIClient = NULL","=================");
        }
    }

    public void isGPSEnable(){

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean GPSenabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean NETenabled = service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!NETenabled || !GPSenabled) {
            buildAlertMessageNoGps();
        }

    }
    private  void buildAlertMessageNoGps()    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You must enable GPS from both GPS and network sources for the app to work as intended, would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(callGPSSettingIntent);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                            }
                        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStart() {
        Log.d("ON START","%%%%%%%%%%");
        super.onStart();
        if (googleApiClient != null) {  // more about this later
            googleApiClient.connect();
        }
    }
    @Override
    protected void onStop() {
        Log.d("ON STOP","%%%%%%%%%%");
        if(googleApiClient != null)
            googleApiClient.disconnect();
        super.onStop();
    }
    @Override
    protected void onPause() {
        Log.d("ON PAUSE", "%%%%%%%%%%%%%%%%%%%");
        if(googleApiClient != null)
            stopLocationUpdates();
        super.onPause();
    }
    @Override
    public void onResume() {
        Log.d("ON RESUME", "%%%%%%%%%%%%%%%%%%%");
        if (googleApiClient != null && googleApiClient.isConnected() && !requestingLocationUpdates) {
            startLocationUpdates();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (clientController != null)
            clientController.stop();
        Log.d("ON DESTROY", "%%%%%%%%%%%%%%%%%%%");
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("ON CONF CHANGED", "%%%%%%%%%%%%%%%%%%%");
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
                    if(lastPressedMarker != null)
                        lastPressedMarker.hideInfoWindow();
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
                lastPressedMarker = marker;
                //stopLocationUpdates();
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
                                Intent i = new Intent(MapActivity.this, TalkActivity.class);
                                i.putExtra("STRING_I_NEED", "vivi@getonsip.com");
                                startActivity(i);
                                //talkActivity.setReceiver("vivi@getonsip.com");
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
    public void sendMyLocation(Location location){
        clientController.setNewLocation(Protocol.createLocationMessage(location));
    }
    public void removeMarker(String user){ //remove user from
       usersOnMap.get(user).remove();
    }

    /**
     *
     * @param location
     */
    public void onLocationChanged(Location location) {
        Log.d("on location changed", "%%%%%%%%%%%%%%%%%%%");
        if(location != null) {
            tv2.setText("Lati: "+String.valueOf(location.getLatitude()));
            tv1.setText("Long: "+String.valueOf(location.getLongitude())+"  ");

            if(lastPressedMarker != null && !lastPressedMarker.isInfoWindowShown()) {
                myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLng(myLatLng));
            }
            sendMyLocation(location);
        }
        else{Log.d("onLocationChanged location", "NULL");}

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
            Log.d("Requesting Location Updates","%%%%%%%%%%");
            startLocationUpdates();
        }
        if(googleApiClient == null)
            Log.d("CLIENT NULL", "%%%%%%%%%%%%%%%");

        myLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (myLastLocation != null) {
            Log.d("INSIDE %%%%%%%%%%%%%%%%%%%","");
            myLatLng = new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude());
            myLatLng1 = new LatLng(myLastLocation.getLatitude() + 0.005, myLastLocation.getLongitude()+ 0.005);
            myLatLng2 = new LatLng(myLastLocation.getLatitude() - 0.005, myLastLocation.getLongitude() - 0.005);
            addMarker("User1", myLatLng);
            addMarker("User2", myLatLng1);
            addMarker("User3", myLatLng2);
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
        Log.d("ON CONNECTION FAILED%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
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
