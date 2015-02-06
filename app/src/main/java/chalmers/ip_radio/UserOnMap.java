package chalmers.ip_radio;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Magnus on 2015-02-06.
 */
public class UserOnMap {
    private GoogleMap map;
    private String user;
    private LatLng latLng;
    private double lat;
    private double lng;
    private Marker marker;


    private void UserOnMap(GoogleMap map, String user, LatLng latLng){
        this.map = map;
        this.user = user;
        this.latLng = latLng;
    }

    private void setMarker() {
        if (latLng != null) {
            marker = map.addMarker(new MarkerOptions().position(latLng)
                    .title("You")
                    .snippet("Probably"));
        }
    }
    private Marker getMarker(){
        return marker;
    }

    //SETS/GETS
    private void setLatLng(LatLng latLng){
        this.latLng = latLng;
    }
    private LatLng getLatLng(){
        return latLng;
    }


    private void setLng(double lng){
        this.lng = lng;
    }
    private double getLng(){
        return lng;
    }
    private void setLat(double lat){
        this.lat = lat;
    }
    private double getLat(){
        return lat;
    }

}
