package chalmers.ip_radio;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;


public class MapActivity extends FragmentActivity  implements LocationListener, LocationSource {
    private LatLng latlng_gbg = new LatLng(57.7000, 11.9667);
    private LatLng latlng_gbg2 = new LatLng(57.7000, 11.9666);
    private GoogleMap map;
    private Marker mark, marker, tempMarker;
    private Location myLocation, location, mLastLocation;
    private LatLng myLatLng, myLatLng1, myLatLng2;
    private LocationManager locationManager;
    private GoogleApiClient.Builder mGoogleApiClient;
    private HashMap<String, UserOnMap> userLocations; //How too keep multiple user locations?
    private HashMap<String, Marker> usersOnMap = new HashMap<String, Marker>();
    private Context context = this;
    private LocationSource.OnLocationChangedListener mListener;

    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initiateMap();
        centerMapOnMyLocation();
        addMarker("User1", myLatLng);
        addMarker("User2", myLatLng1);
        addMarker("User3", myLatLng2);
    }

    private void initiateMap() {
        try {
            if (map == null) {
                map = ((SupportMapFragment) getSupportFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMapToolbarEnabled(false);
            map.getUiSettings().setTiltGesturesEnabled(false);
            map.getUiSettings().setRotateGesturesEnabled(false);

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

        //Only shows one, may redo with other icon
        //usersOnMap.get(user).showInfoWindow();

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            public void onInfoWindowClick(Marker mark) {
                builder = new AlertDialog.Builder(context);
                builder.setMessage(mark.getTitle())
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Do if yes
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
    public void updateMarker(String user, LatLng latLng){
        UserOnMap userOnMap = userLocations.get(user);

    }

    public void removeMarker(String user){ //remove user from
        usersOnMap.get(user).remove();

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
        myLatLng1 = new LatLng(myLocation.getLatitude() - 0.0001, myLocation.getLongitude() - 0.001);
        myLatLng2 = new LatLng(myLocation.getLatitude() + 0.0001, myLocation.getLongitude() + 0.001);

        try {
            map.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
            map.animateCamera(CameraUpdateFactory.zoomTo(16));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if( mListener != null )
        {
            mListener.onLocationChanged( location );

            //Move the camera to the user's location once it's available!
            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            //map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        }

        //map.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }
}
