package chalmers.ip_radio;

import android.app.Activity;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends Activity {
    static final LatLng point1 = new LatLng(57.7 , 11.9);
    static final LatLng point2 = new LatLng(57.6 , 12);
    GoogleMap map;
    Marker mark;
    Location location;
    CameraUpdateFactory CUF;
    /*
    CameraUpdate center = CUF.newLatLng(new LatLng(40.76793169992044,
                    -73.98180484771729));
    CameraUpdate zoom = CUF.zoomTo(15);
    */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        try{
            if (map == null) {
                map = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);

            //centerMapOnMyLocation();

            //map.moveCamera(center);
            //map.animateCamera(zoom);


            mark = map.addMarker(new MarkerOptions()
                .position(point2)
                .title("TestTitle")
                .snippet("TestSnippet"));


            Marker TP = map.addMarker(new MarkerOptions().
                    position(point1).title("TutorialsPoint"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void centerMapOnMyLocation() {

        map.setMyLocationEnabled(true);

        //LatLng location = (Location) map.getMyLocation();

        if (location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            map.animateCamera(CUF.newLatLngZoom(myLocation, 16));
        }
    }
    //while loop, uppdatera positioner?



}
