package model;
/* Ce code a ete inspire de https://stackoverflow.com/questions/11745314/why-retrieving-google-directions-for-android-using-kml-data-is-not-working-anymo */

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class GoogleDirections extends FileDownloader {
    private final Context context;
    private final GoogleMap myMap;
    /** Distance covered. **/
    private int distance;
    private Polyline mLine;
    private final LatLng mSource;
    private final LatLng mDestination;

    public GoogleDirections(Context context, GoogleMap myMap, LatLng source, LatLng destination) {
        super(context);
        this.context = context;
        this.myMap = myMap;
        mSource = source;
        mDestination = destination;
    }

    @Override
    protected void onPostExecute(String file_url) {
        JSONObject data = null;
        try {
            InputStream is = null;
            //verify if its in cache
            is = new FileInputStream(context.getFilesDir() + "/directions.json");
            //reading the file
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String res = new String(buffer, StandardCharsets.UTF_8);
            Route route = parse(res);
            drawPath(route.getPoints());
            mProgress.setProgress(100);
            mProgress.dismiss();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parses a url pointing to a Google JSON object to a Route object.
     * @return a Route object based on the JSON object.
     */



    public Route parse(String result) {
        //Create an empty route
        final Route route = new Route();
        //Create an empty segment
        final Segment segment = new Segment();
        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            //Get the route object
            final JSONObject jsonRoute = json.getJSONArray("routes").getJSONObject(0);
            //Get the leg, only one leg as we don't support waypoints
            final JSONObject leg = jsonRoute.getJSONArray("legs").getJSONObject(0);
            //Get the steps for this leg
            final JSONArray steps = leg.getJSONArray("steps");
            //Number of steps for use in for loop
            final int numSteps = steps.length();
            //Set the name of this route using the start & end addresses
            route.setName(leg.getString("start_address") + " to " + leg.getString("end_address"));
            //Get google's copyright notice (tos requirement)
            route.setCopyright(jsonRoute.getString("copyrights"));
            //Get the total length of the route.
            route.setLength(leg.getJSONObject("distance").getInt("value"));
            //Get any warnings provided (tos requirement)
            if (!jsonRoute.getJSONArray("warnings").isNull(0)) {
                route.setWarning(jsonRoute.getJSONArray("warnings").getString(0));
            }
            /* Loop through the steps, creating a segment for each one and
             * decoding any polylines found as we go to add to the route object's
             * map array. Using an explicit for loop because it is faster!
             */
            for (int i = 0; i < numSteps; i++) {
                //Get the individual step
                final JSONObject step = steps.getJSONObject(i);
                //Get the start position for this step and set it on the segment
                final JSONObject start = step.getJSONObject("start_location");
                final LatLng position = new LatLng((double) (start.getDouble("lat")),
                        (double) (start.getDouble("lng")));
                segment.setPoint(position);
                //Set the length of this segment in metres
                final int length = step.getJSONObject("distance").getInt("value");
                distance += length;
                segment.setLength(length);
                segment.setDistance(distance/1000);
                //Strip html from google directions and set as turn instruction
                segment.setInstruction(step.getString("html_instructions").replaceAll("<(.*?)*>", ""));
                //Retrieve & decode this segment's polyline and add it to the route.
                route.addPoints(decodePolyLine(step.getJSONObject("polyline").getString("points")));
                //Push a copy of the segment to the route
                route.addSegment(segment.copy());
            }
        } catch (JSONException e) {

            Log.e( "Google Parser",  "Google JSON Parser");
        }
        return route;
    }

    /**
     * Decode a polyline string into a list of GeoPoints.
     * @param poly polyline encoded string to decode.
     * @return the list of GeoPoints represented by this polystring.
     */

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    (double) (lat / 1E5), (double) (lng / 1E5)));
        }

        return decoded;
    }

    public void drawPath(List<LatLng> list) {
        try {

            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            options.add(mSource);
            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                options.add(point);
            }
            options.add(mDestination);

            mLine = myMap.addPolyline(options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        if (mLine != null){
            mLine.remove();
        }
    }
}