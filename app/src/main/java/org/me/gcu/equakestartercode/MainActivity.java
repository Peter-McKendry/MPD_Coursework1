//NAME:         Peter McKendry
//STUDENT ID:   S1915350

package org.me.gcu.equakestartercode;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.SystemClock;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.AdapterView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class MainActivity extends AppCompatActivity implements OnClickListener, OnMapReadyCallback
{

//Flipper Container

    private ViewFlipper flipper;


    private Button searchButton;
    private Button filterButton;
    private EditText searchInput;

//Data Variables

    //The original parsed earthquake list
    private ArrayList<Earthquake> originEarthquakeList;
    //The earthquake list for use when manipulating, ie searching or filtering
    private ArrayList<Earthquake> earthquakeList;
    //Empty String for use in parsing
    private String result = "";
    //The selected used sort option
    private String sortOption;
    //User inputed search string
    private String searchParam;
    //The user selected earthquake for use on maps page
    private Earthquake focusEarthquake;

//List View Variables

    private ListView listView;
    private TextView listCount;
    private ListViewAdapter adapter;

//Progress Bar

    private ProgressBar progressBar;
    private Button mapViewButton;



    private Button mapsHomeButton;
    private TextView mapsText;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("MyTag","in onCreate");

        // Set up the raw links to the graphical components
        filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(this);

        listCount = findViewById(R.id.listCount);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
        searchInput = findViewById(R.id.searchInput);
        searchInput.setWidth(120);
        searchInput.setFocusable(true);

        progressBar = findViewById(R.id.progress);
        mapViewButton = findViewById(R.id.mapViewButton);
        mapViewButton.setOnClickListener(this);

        listView = findViewById(R.id.listView);

        mapsHomeButton= findViewById(R.id.mapsHomeButton);
        mapsHomeButton.setOnClickListener(this);
        flipper= findViewById(R.id.flipper);
        //when a view is displayed
        flipper.setInAnimation(this,android.R.anim.fade_in);
        //when a view disappears
        flipper.setOutAnimation(this, android.R.anim.fade_out);
        mapsText=findViewById(R.id.mapsText);

        SupportMapFragment test = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map));
        test.getMapAsync(this);

        startProgress();

    }


    @SuppressLint("ResourceType")
    public void onClick(View aview)
    {
        if(aview == searchButton){
            System.out.println("Search button pressed!");
            searchParam = searchInput.getText().toString();
            searchFunc(searchParam);
        } else if (aview == filterButton){

         //Opens popup filter menu

            //Create popup menu
            PopupMenu popup = new PopupMenu(MainActivity.this, filterButton);
            popup.getMenuInflater().inflate(R.layout.filter_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                //on click handlers
                public boolean onMenuItemClick(MenuItem item) {
                    sortOption = item.getTitle().toString();
                    sorting(sortOption);
                    return true;
                }
            });

            popup.show();
        }
        else if (aview == mapsHomeButton){

            flipper.showPrevious();
        }
        else if(aview == mapViewButton){
            Log.e("User Event","Map view button clicked!");
            flipper.showNext();
            //Clear the map of any existing markers
            mMap.clear();
            //set the heading text for the next page
            mapsText.setText("Results:");
            for(Earthquake e : earthquakeList){
                LatLng quakeLocation = new LatLng(e.getLatitude(), e.getLongitude());
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(quakeLocation).title(e.getLocation() + ". Magnitude:  " + e.getMagnitude() + ", Depth: " + e.getDepth()));
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);

            }
        }
    }

    public void startProgress()
    {
        //start main thread
        new Thread(new Task()).start();

    }
//Starting point of the map, no earthquake has been selected so it is centered on London

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng london = new LatLng(51.509865, -0.118092);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(london));

    }


    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class Task implements Runnable
    {
        public Task(){}

        @Override
        public void run(){

            //Call fetch data once to populate the app with data. This will then be handled by ASYNC
            fetchData();

            //Set up UI thread
            MainActivity.this.runOnUiThread(new Runnable()
            {

                //Main function
                public void run() {

                    //Call the search function to set the defaults.
                    searchFunc("");

                    //Create listener for each earthquake item onclick
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Earthquake dataModel= earthquakeList.get(position);
                            //On item click log particular item for testing
                            Log.e("UserEvent", "List Item clicked:" + dataModel.getTitle());
                            //Set the focussed earthquake to the one clicked by the user
                            focusEarthquake = dataModel;
                            //Navigate to maps page
                            flipper.showNext();
                            //set the heading text for the next page
                            mapsText.setText("Location: " + dataModel.getLocation() + ", Magnitude: " + dataModel.getMagnitude() + ", Depth: " + dataModel.getDepth() + ", on " + dataModel.getPubDate()+".");
                            //Clear the map of any existing markers
                            mMap.clear();
                            //Set the map location to the lat and long of the earthquake
                            LatLng quakeLocation = new LatLng(focusEarthquake.getLatitude(), focusEarthquake.getLongitude());
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(quakeLocation).title("Marker in " + focusEarthquake.getLocation() + ". Magnitude:  " + focusEarthquake.getMagnitude()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(quakeLocation));
                            mMap.getUiSettings().setZoomControlsEnabled(true);
                            mMap.getUiSettings().setZoomGesturesEnabled(true);

                        }
                    });

                    //Start a new thread to run in the background to Async refresh the data
                    new Thread(new BackgroundTask()).start();
                }

            });
        }
    }
private class BackgroundTask implements Runnable{

    public BackgroundTask(){}

    @Override
    public void run() {
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new FetchData().execute();
            }

        }, 0, 60000); // Refreshes every minute

    }
}


     //search functionality using @param searchParam input string for searching

    private void searchFunc(String searchParam) {

        //if the search is not empty
        if(searchParam.length() >0){

            if(earthquakeList == null){
                earthquakeList.addAll(originEarthquakeList);
            }
            //creates arraylist of search results
            ArrayList<Earthquake> searchResults = new ArrayList<>();

            //iterate through earthquakes, if earthquake title contains the search string add to search results
            for (Earthquake e : earthquakeList) {
                if (e.getTitle().contains(searchParam)) {
                    searchResults.add(e);
                }
            }
            earthquakeList = searchResults;
            listCount.setText("(" + searchResults.size() + ")");
            adapter = new ListViewAdapter(earthquakeList, getApplicationContext());
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        }

        //Returns a full list of earthquakes when search is empty
        else{
            listCount.setText("(" + earthquakeList.size() + ")");
            adapter = new ListViewAdapter(originEarthquakeList, getApplicationContext());
            //assign the new adapter to the listview
            listView.setAdapter(adapter);
            //update the adapter's dataset.
            adapter.notifyDataSetChanged();
        }

    }


     //Provides sorting functionality using a switch statement

    private void sorting(String sortOption){
        switch(sortOption){
            case "Clear Filters":
                earthquakeList = new ArrayList<>();
                earthquakeList.addAll(originEarthquakeList);
                searchParam = "";
                adapter = new ListViewAdapter(earthquakeList, getApplicationContext());
                adapter.notifyDataSetChanged();
                listCount.setText("(" + earthquakeList.size() + ")");
                listView.setAdapter(adapter);
                Toast.makeText(MainActivity.this, "Filtered by Default", Toast.LENGTH_SHORT).show();
                break;
            case "Magnitude (Ascending)":
                Collections.sort(earthquakeList, Earthquake.magAscComparator);
                adapter.sort(Earthquake.magAscComparator);
                adapter.notifyDataSetChanged();
                listCount.setText("(" + earthquakeList.size() + ")");
                Toast.makeText(MainActivity.this, "Filtered by Magnitude (Ascending)", Toast.LENGTH_SHORT).show();
                break;
            case "Magnitude (Descending)":
                Collections.sort(earthquakeList, Earthquake.magDescComparator);
                adapter.sort(Earthquake.magDescComparator);
                adapter.notifyDataSetChanged();
                listCount.setText("(" + earthquakeList.size() + ")");
                Toast.makeText(MainActivity.this, "Filtered by Magnitude (Descending)", Toast.LENGTH_SHORT).show();
                break;
            case "Depth (Ascending)":
                Collections.sort(earthquakeList, Earthquake.depthAscComparator);
                adapter.sort(Earthquake.depthAscComparator);
                adapter.notifyDataSetChanged();
                listCount.setText("(" + earthquakeList.size() + ")");
                Toast.makeText(MainActivity.this, "Filtered by Depth (Ascending)", Toast.LENGTH_SHORT).show();
                break;
            case "Depth (Descending)":
                Collections.sort(earthquakeList, Earthquake.depthDescComparator);
                adapter.sort(Earthquake.depthDescComparator);
                adapter.notifyDataSetChanged();
                listCount.setText("(" + earthquakeList.size() + ")");
                Toast.makeText(MainActivity.this, "Filtered by Depth (Descending)", Toast.LENGTH_SHORT).show();
                break;


        }

    }

//function to fetch all data

private class FetchData extends AsyncTask<Void, Integer, Void> {

    int progress_status;

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        progress_status = 0;
        progressBarVisibility(true);

    }

    @Override
    protected Void doInBackground(Void... voids) {

        //Call fetch data function to refetch the earthquake data
        fetchData();
        //Since this is basically instant, set up a dummy timer for the UI and have it count down
        while(progress_status<100)
        {
            progress_status += 10;
            publishProgress(progress_status);
            SystemClock.sleep(100); // or Thread.sleep(300);
        }
        callSort();

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        progressBar.setProgress(values[0]);

    }

    @Override
    protected void onPostExecute(Void result)
    {
        super.onPostExecute(result);
        progressBarVisibility(false);


    }
}

//creates connection to url and parses data

    private void fetchData(){
        URL aurl;
        URLConnection yc;
        BufferedReader in = null;
        String inputLine = "";

        try
        {
            // Log.e("MyTag","in try");
            String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
            aurl = new URL(urlSource);
            yc = aurl.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));


            while ((inputLine = in.readLine()) != null)
            {
                result = result + inputLine;
                //Log.e("MyTag",inputLine);

            }
            in.close();
        }
        catch (IOException ae)
        {
            Log.e("PARSING", "ioexception");
        }

        //establish the original dataset
        originEarthquakeList = parseData(result);
        //set up another list of earthquakes for filtering/searching
        earthquakeList = new ArrayList<>();
        earthquakeList.addAll(originEarthquakeList);
    }


    //parses all data recieved, returns the arraylist of earthquake objects

    public ArrayList<Earthquake> parseData(String dataToParse)
    {
        Earthquake earthquake = null;
        ArrayList <Earthquake> alist = null;
        try
        {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader( dataToParse ) );
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                // Found a start tag
                if(eventType == XmlPullParser.START_TAG)
                {
                    // Check which Tag we have
                    if (xpp.getName().equalsIgnoreCase("channel"))
                    {
                        alist  = new ArrayList<>();
                    }
                    else
                    if (xpp.getName().equalsIgnoreCase("item"))
                    {
                        //Log.e("MyTag","Item Start Tag found");
                        earthquake = new Earthquake();
                    }
                    else
                    if (xpp.getName().equalsIgnoreCase("title"))
                    {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        // Log.e("MyTag","Title is " + temp);
                        if(earthquake!=null){
                            earthquake.setTitle(temp);
                        }

                    }
                    else
                        // Check which Tag we have
                        if (xpp.getName().equalsIgnoreCase("description"))
                        {
                            // Now just get the associated text
                            String temp = xpp.nextText();
                            // Do something with text
                            //Log.e("MyTag","Description is " + temp);
                            if (earthquake!=null) {
                                earthquake.setDescription(temp);
                            }
                        }
                        else
                            // Check which Tag we have
                            if (xpp.getName().equalsIgnoreCase("link"))
                            {
                                // Now just get the associated text
                                String temp = xpp.nextText();
                                // Do something with text
                                // Log.e("MyTag","Link is " + temp);
                                if (earthquake!=null) {
                                    earthquake.setLink(temp);
                                }
                            }
                    // Check which Tag we have
                    if (xpp.getName().equalsIgnoreCase("pubDate"))
                    {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        // Log.e("MyTag","pubDate is " + temp);
                        if (earthquake!=null) {
                            earthquake.setPubDate(temp);
                        }
                    }
                    // Check which Tag we have
                    if (xpp.getName().equalsIgnoreCase("category"))
                    {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        // Log.e("MyTag","Category is " + temp);
                        if (earthquake!=null) {
                            earthquake.setCategory(temp);
                        }
                    }
                    // Check which Tag we have
                    if (xpp.getName().equalsIgnoreCase("lat"))
                    {
                        // Now just get the associated text
                        float temp = Float.parseFloat(xpp.nextText());
                        // Do something with text
                        //Log.e("MyTag","Lat is " + temp);
                        if (earthquake!=null) {
                            earthquake.setLatitude(temp);
                        }
                    }
                    if (xpp.getName().equalsIgnoreCase("long"))
                    {
                        // Now just get the associated text
                        float temp = Float.parseFloat(xpp.nextText());
                        // Do something with text
                        //Log.e("MyTag","long is " + temp);
                        if (earthquake!=null) {
                            earthquake.setLongitude(temp);
                        }
                    }
                }
                else
                if(eventType == XmlPullParser.END_TAG)
                {
                    if (xpp.getName().equalsIgnoreCase("item"))
                    {
                        assert earthquake != null;
                        // Log.e("MyTag","earthquake is " + earthquake.toString());
                        if(alist!=null){
                            alist.add(earthquake);
                        }
                    }

                    else
                    if (xpp.getName().equalsIgnoreCase("channel"))
                    {
                        int size;
                        if(alist!=null) {
                            size = alist.size();
                            //Log.e("MyTag", "earthquakelist size is " + size);

                        }
                        break;
                    }
                }

                // Get the next event
                eventType = xpp.next();


            } // End of while

            //return alist;
        }
        catch (XmlPullParserException e)
        {
            Log.e("MyTag","Pull Parser Exception");
            //e.printStackTrace();
        }
        catch (IOException ae1)
        {
            Log.e("MyTag","IO error during parsing");
        }

        // Log.e("MyTag","End document");

        return alist;

    }


    public void progressBarVisibility(final boolean val){

        runOnUiThread(new Runnable() {

            @Override
            public void run() {


                if(val){

                    // Stuff that updates the UI
                    progressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this,"Auto Fetching data..", Toast.LENGTH_SHORT).show();

                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this,"Updated.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void callSort(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(sortOption!=null && !sortOption.equals("")){
                    sorting(sortOption);
                }
                if(searchParam != null && !searchParam.equals("")){
                    searchFunc(searchParam);
                }

            }
        });
    }

}