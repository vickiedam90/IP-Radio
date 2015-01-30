package chalmers.ip_radio;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends Activity {
    static final LatLng TutorialsPoint = new LatLng(21 , 57);
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        try{
            if (map == null) {
                map = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            Marker TP = map.addMarker(new MarkerOptions().
                    position(TutorialsPoint).title("TutorialsPoint"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
