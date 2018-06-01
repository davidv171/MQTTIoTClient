package com.example.davidv7.mqttiot;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity {
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private MqttHelper mqttHelper;
        private LineGraphSeries<DataPoint> series;
        private TextView tempNow;
        private TextView humNow;
        private TextView humLast;
        private TextView tempLast;
        private GraphView graph;
        private HashMap<Date, String> data;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            data = new HashMap<>();

            graph = rootView.findViewById(R.id.graph);
            graph.setBackgroundColor(Color.TRANSPARENT);
            graph.setTitleColor(Color.WHITE);

            if(getArguments().getInt(ARG_SECTION_NUMBER)==1){
                // Save data point
                // Get last data point and put it into last... textViews
                //Save date, save temperature for time
                startMqtt();
                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String dateString = df.format(date);
                Date formattedDate = null;
                try {
                    formattedDate = df.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                graph.setTitle(dateString);
                //TODO: Add to series from map if the day is today
                //TODO: Check if no data was caught
                System.out.println("CALLING INIT");

                graph.setTitleTextSize(55);
                series = new LineGraphSeries<>();

                //TODO: print out today's temperatures out of the file
                //TODO: put the values into the graph
                HashMap<Date,String>tempHash;
                tempHash = getData();

                try{
                    System.out.println("Today's tempHash:" + tempHash.getOrDefault(formattedDate,""));
                    for (Map.Entry<Date, String> e : tempHash.entrySet()) {
                        //TODO: Test this further,
                        //Both dates need to be the same format!
                        //day-month-year
                        System.out.println("Comparing :" + e.getKey().toString()+" ");
                        System.out.println(date.toString());
                        //If xx-xx-xxxx == yy-yy-yyyy
                        if(e.getKey().toString().equals(date.toString())){

                        }

                    }

                }catch (NullPointerException e){
                    Toast.makeText(getActivity().getApplicationContext(),"No dates!",Toast.LENGTH_SHORT);
                }



            }
            GridLabelRenderer glr = graph.getGridLabelRenderer();
            glr.setHorizontalLabelsVisible(false);
            glr.setTextSize(44);
            glr.setGridColor(Color.WHITE);
            glr.setVerticalLabelsColor(Color.WHITE);
            glr.setHorizontalLabelsColor(Color.WHITE);
            tempNow = rootView.findViewById(R.id.tempNow);
            humNow = rootView.findViewById(R.id.humNow);
            tempLast = rootView.findViewById(R.id.tempLast);
            humLast = rootView.findViewById(R.id.humLast);

            FloatingActionButton fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startMqtt();
                }
            });

            return rootView;
        }
        //Finds today's temperatures and hours in the file



        private void startMqtt() {
            mqttHelper = new MqttHelper(getActivity());

            mqttHelper.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) {
                    System.out.println("Connect complete");
                }


                @Override
                public void connectionLost(Throwable throwable) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    //TODO: Save every message into a file as a hashmap
                    //Hashmap has the form of (Date,String)
                    //Date is the date of the measurement, and String is the temperature


                    Log.w("Debug", mqttMessage.toString());
                    Log.w("Topic",topic);


                    if(topic.equals("sensor/hum")){
                        Log.w("Hum","Humidity received ");
                        humLast.setText(humNow.getText());
                        humNow.setText(mqttMessage.toString());
                    }
                    else {
                        tempLast.setText(tempNow.getText());

                        int temp = Integer.parseInt(mqttMessage.toString());

                        series.appendData(new DataPoint(series.getHighestValueX()+1,temp), false, 25);
                        series.setAnimated(true);
                        series.setColor(Color.WHITE);
                        if(series.getHighestValueX()>99){
                            series.resetData(new DataPoint[]{new DataPoint(0,temp)});
                            Toast.makeText(getActivity().getApplicationContext(),"Too many data points, resetting",Toast.LENGTH_SHORT);
                        }
                        graph.addSeries(series);
                        data.clear();
                        Date date = Calendar.getInstance().getTime();
                        Log.w("DATE",date.toString());
                        System.out.println("DATE " + date);
                        Date formattedDate=null;
                        SimpleDateFormat df = new SimpleDateFormat("-mm-hh-dd-MMM-yyyy");
                        String dateString = df.format(date);
                        try {
                            formattedDate = df.parse(dateString);
                            data.put(formattedDate,mqttMessage.toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        //append to the temp.mqtt file
                        System.out.println("DATA SIZE" + data.size());
                        tempNow.setText(mqttMessage.toString()+"°C");
                        HashMap<Date,String>dateStringHashMap = new HashMap<>();
                        dateStringHashMap.put(formattedDate,mqttMessage.toString());
                        System.out.println(dateStringHashMap.size());
                        writeFile(formattedDate,mqttMessage.toString());
                        getData();
                        // Log.w("Parse test",df.parse(dateString).toString());


                    }
                    System.out.println("A message has arrived!");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });

        }

        private void writeFile(Date date, String message){
            //Read from the file and write it back into the file together with the new
            //TODO: Optimize this if possible, make it not ask
            //TODO: Sort by date
            //Hope there are no errors and that HashMap puts in new elements to the end!
            try
            {
                HashMap<Date,String>tempMap=getData();
                tempMap.put(date,message);
                System.out.println("Appended:" +tempMap.toString());
                FileOutputStream fos = getActivity().getApplicationContext().openFileOutput("map.ser",MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(tempMap);
                System.out.println("Written: " +tempMap.toString());
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private HashMap<Date,String> getData(){
            HashMap<Date,String> myHashMap=null;
            try
            {
                FileInputStream fileInputStream = new FileInputStream(getActivity().getApplicationContext().getFilesDir()+"/map.ser");
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                myHashMap = (HashMap<Date, String>) objectInputStream.readObject();
            }
            catch(ClassNotFoundException | IOException | ClassCastException e) {
                e.printStackTrace();
            }
            System.out.println("READ: " + myHashMap.toString());
            return myHashMap;
        }



    }

        /**x
         * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
         * one of the sections/tabs/pages.
         */
        public class SectionsPagerAdapter extends FragmentPagerAdapter {

            public SectionsPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                // getItem is called to instantiate the fragment for the given page.
                // Return a PlaceholderFragment (defined as a static inner class below).
                return PlaceholderFragment.newInstance(position + 1);
            }

            @Override
            public int getCount() {
                // Show 3 total pages.
                return 3;
            }
        }
    }




