package chalmers.ip_radio;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
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


public class MapActivity extends FragmentActivity {
    private LatLng latlng_gbg = new LatLng(57.7000, 11.9667);
    private LatLng latlng_gbg2 = new LatLng(57.7000, 11.9666);
    private GoogleMap map;
    private Marker mark, marker;
    private Location myLocation, location, mLastLocation;
    private LatLng myLatLng, myLatLng1, myLatLng2;
    private LocationManager locationManager;
    private GoogleApiClient.Builder mGoogleApiClient;
    public HashMap<String, UserOnMap> userLocations; //How too keep multiple user locations?
    private HashMap<String, Marker> usersOnMap = new HashMap<String, Marker>();

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
        //userLocations.put(user, new UserOnMap(map, user, latLng));
        /*
        if (latLng != null) {
            marker = map.addMarker(new MarkerOptions().position(latLng)
                    .title(user)
                            //.snippet("Probably")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck32px)));
            marker.showInfoWindow();
        }
        */

        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Call " + user + "?")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck32px)));
        usersOnMap.put(user, marker);
        usersOnMap.get(user).showInfoWindow();


        bla(user);

        /*
        map.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        return false;
                    }
                }
        );
        */
    }
    public void bla(String user){

        builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to call " + user + "?")
                .setCancelable(false)
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

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            public void onInfoWindowClick(Marker mark) {
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public void updateMarker(String user, LatLng latLng){
        UserOnMap userOnMap = userLocations.get(user);

    }

    public void removeMarker(String user){ //remove user from

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
}
