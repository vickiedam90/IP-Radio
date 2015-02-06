package chalmers.ip_radio;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

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
    private GoogleMap map;
    private Marker mark;
    private Location myLocation, location, mLastLocation;
    private LatLng myLatLng;
    private LocationManager locationManager;
    private GoogleApiClient.Builder mGoogleApiClient;
    public HashMap<String, UserOnMap> userLocations; //How too keep multiple user locations?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initiateMap();
        centerMapOnMyLocation();
    }

    private void initiateMap() {
        try {
            if (map == null) {
                map = ((SupportMapFragment) getSupportFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMarker(String user, LatLng latLng){
        //userLocations.put(user, new UserOnMap(map, user, latLng));
    }

    public void updateMarker(String user, LatLng latLng){

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

        try {
            map.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
            map.animateCamera(CameraUpdateFactory.zoomTo(16));
            /*
            mark = map.addMarker(new MarkerOptions().position(myLatLng)
                    .title("You")
                    .snippet("Probably"));
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
