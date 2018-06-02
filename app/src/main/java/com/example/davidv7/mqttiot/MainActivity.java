package com.example.davidv7.mqttiot;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import org.w3c.dom.Text;

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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
        private LinkedHashMap<Date, String> data;
        private TextView minTemp;
        private TextView maxTemp;
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
                writeFile(Calendar.getInstance().getTime(),"15");

            tempNow = rootView.findViewById(R.id.tempNow);
            humNow = rootView.findViewById(R.id.humNow);
            tempLast = rootView.findViewById(R.id.tempLast);
            humLast = rootView.findViewById(R.id.humLast);
             maxTemp = rootView.findViewById(R.id.tempMax);
             minTemp = rootView.findViewById(R.id.tempMin);
            if(getArguments().getInt(ARG_SECTION_NUMBER)==1){
                // Save data point
                // Get last data point and put it into last... textViews
                //Save date, save temperature for time
                data = new LinkedHashMap<>();
                LinkedHashMap<Date,String>tempHash;
                tempHash = getData();
                graph = rootView.findViewById(R.id.graph);
                graph.setBackgroundColor(Color.TRANSPARENT);
                graph.setTitleColor(Color.WHITE);
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

                // print out today's temperatures out of the file
                // put the values into the graph

                System.out.println("TEMPHASH" + tempHash);

                try{
                    System.out.println("Today's tempHash:" + tempHash.getOrDefault(formattedDate,""));
                    //Used to find the highest temperature and lowest temperature in the LinkedHashMap
                    ArrayList<Integer>integerArrayList = new ArrayList<>();
                    for (Map.Entry<Date, String> e : tempHash.entrySet()) {
                        //TODO: Test this further,
                        //Both dates need to be the same format!
                        //day-month-year
                        System.out.println("Comparing :" + e.getKey().toString()+" ");
                        System.out.println(date.toString());
                        //If xx-xx-xxxx == yy-yy-yyyy
                        int fileDay = e.getKey().getDate();
                        int fileMonth = e.getKey().getMonth();
                        int fileYear = e.getKey().getYear();
                        int nowDay = date.getDate();
                        System.out.println("Comparing days" + fileDay + " " + nowDay);
                        int nowMonth = date.getMonth();
                        int nowYear = date.getYear();
                        if(fileDay==nowDay&&fileMonth==nowMonth&&fileYear==nowYear){
                            System.out.println("Temperature" + e.getValue());
                            int temp = Integer.parseInt(e.getValue());
                            integerArrayList.add(Integer.parseInt(e.getValue()));

                            series.appendData(new DataPoint(series.getHighestValueX()+1,temp),false,25000);
                        }
                        }

                    int min = Collections.min(integerArrayList);
                    int max = Collections.max(integerArrayList);

                    maxTemp.setText(String.valueOf(max));
                    minTemp.setText(String.valueOf(min));


                }catch (NullPointerException e){
                    Toast.makeText(getActivity().getApplicationContext(),"No dates!",Toast.LENGTH_SHORT).show();
                }

                GridLabelRenderer glr = graph.getGridLabelRenderer();
                glr.setHorizontalLabelsVisible(false);
                glr.setTextSize(44);
                glr.setGridColor(Color.WHITE);
                glr.setVerticalLabelsColor(Color.WHITE);
                glr.setHorizontalLabelsColor(Color.WHITE);
                startMqtt();


            }
            if(getArguments().getInt(ARG_SECTION_NUMBER)==2){
                final SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("prefs",Context.MODE_PRIVATE);
                sharedPref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        tempNow.setText(sharedPref.getString("tempNow","0"));
                        humNow.setText(sharedPref.getString("humNow","0"));
                        tempLast.setText(sharedPref.getString("tempLast","0"));
                        humLast.setText(sharedPref.getString("humLast","0"));
                    }
                });

                System.out.println("TEMP NOW IN SECTION 2" + tempNow.getText());
                data = new LinkedHashMap<>();
                LinkedHashMap<Date,String>tempHash;
                tempHash = getData();
                graph = rootView.findViewById(R.id.graph);
                graph.setBackgroundColor(Color.TRANSPARENT);
                graph.setTitleColor(Color.WHITE);
                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String dateString = df.format(date);
                Date formattedDate = null;
                try {
                    formattedDate = df.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR);
                graph.setTitle(month + "-" + year);
                System.out.println("CALLING INIT");

                graph.setTitleTextSize(55);
                series = new LineGraphSeries<>();
                //ArrayList used to take out each temperature out of tempHash to then calculate the minimum and maximum
                ArrayList<Integer>integerArrayList= new ArrayList<>();
                try{
                    System.out.println("Today's tempHash:" + tempHash.getOrDefault(formattedDate,""));
                    double i =0.000;
                    for (Map.Entry<Date, String> e : tempHash.entrySet()) {
                        //TODO: Test this further,
                        //Both dates need to be the same format!
                        //day-month-year
                        System.out.println("Comparing :" + e.getKey().toString()+" ");
                        System.out.println(date.toString());
                        i++;
                        System.out.println("DoubleT" + i);
                        //If xx-xx-xxxx == yy-yy-yyyy
                        int fileDay = e.getKey().getDate();
                        int fileMonth = e.getKey().getMonth();
                        int fileYear = e.getKey().getYear();
                        int nowMonth = date.getMonth();
                        int nowYear = date.getYear();
                        System.out.println("fileday"+ fileDay);

                        if(fileMonth==nowMonth&&fileYear==nowYear){
                            integerArrayList.add(Integer.parseInt(e.getValue()));
                            System.out.println("LinkedHashMap read: " + e.getKey() + " Temperature" + e.getValue());
                            int temp = Integer.parseInt(e.getValue());
                            series.appendData(new DataPoint(fileDay+i/(30*24),temp),false,25000);
                        }
                    }
                    int min = Collections.min(integerArrayList);
                    int max = Collections.max(integerArrayList);
                    TextView maxTemp = rootView.findViewById(R.id.tempMax);
                    TextView minTemp = rootView.findViewById(R.id.tempMin);
                    maxTemp.setText(String.valueOf(max));
                    minTemp.setText(String.valueOf(min));


                }catch (NullPointerException e){
                    Toast.makeText(getActivity().getApplicationContext(),"No dates!",Toast.LENGTH_SHORT).show();
                }
                GridLabelRenderer glr = graph.getGridLabelRenderer();
                glr.setTextSize(44);
                glr.setHighlightZeroLines(true);
                
                series.setAnimated(true);
                series.setColor(Color.WHITE);
                glr.setGridColor(Color.WHITE);
                glr.setVerticalLabelsColor(Color.WHITE);
                glr.setHorizontalLabelsColor(Color.WHITE);
                graph.addSeries(series);


            }
            if(getArguments().getInt(ARG_SECTION_NUMBER)==3){
                final SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("prefs",Context.MODE_PRIVATE);
                sharedPref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        tempNow.setText(sharedPref.getString("tempNow","0"));
                        humNow.setText(sharedPref.getString("humNow","0"));
                        tempLast.setText(sharedPref.getString("tempLast","0"));
                        humLast.setText(sharedPref.getString("humLast","0"));
                    }
                });

                System.out.println("TEMP NOW IN SECTION 2" + tempNow.getText());
                data = new LinkedHashMap<>();
                LinkedHashMap<Date,String>tempHash;
                tempHash = getData();
                graph = rootView.findViewById(R.id.graph);
                graph.setBackgroundColor(Color.TRANSPARENT);
                graph.setTitleColor(Color.WHITE);
                Date date = Calendar.getInstance().getTime();

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                graph.setTitle("" + year);
                System.out.println("CALLING INIT");

                graph.setTitleTextSize(55);
                series = new LineGraphSeries<>();
                //ArrayList used to take out each temperature out of tempHash to then calculate the minimum and maximum
                ArrayList<Integer>integerArrayList= new ArrayList<>();
                try{
                    double i = 0.0000;
                    for (Map.Entry<Date, String> e : tempHash.entrySet()) {
                        //TODO: Test this further,
                        //TODO: Make the yearly graph show which month the temperatures are at
                        //Both dates need to be the same format!
                        //day-month-year
                        //If xx-xx-xxxx == yy-yy-yyyy
                        i++;
                        System.out.println("Dddd" + i);
                        int fileYear = e.getKey().getYear();
                        int fileMonth = e.getKey().getMonth();
                        int nowYear = date.getYear();
                        if(fileYear==nowYear){
                            integerArrayList.add(Integer.parseInt(e.getValue()));
                            System.out.println("LinkedHashMap read: " + e.getKey() + " Temperature" + e.getValue());
                            int temp = Integer.parseInt(e.getValue());
                            series.appendData(new DataPoint(fileMonth+1+i/(24*12),temp),false,25000);
                        }
                    }
                    int min = Collections.min(integerArrayList);
                    int max = Collections.max(integerArrayList);
                    TextView maxTemp = rootView.findViewById(R.id.tempMax);
                    TextView minTemp = rootView.findViewById(R.id.tempMin);
                    maxTemp.setText(String.valueOf(max));
                    minTemp.setText(String.valueOf(min));


                }catch (NullPointerException e){
                    Toast.makeText(getActivity().getApplicationContext(),"No dates!",Toast.LENGTH_SHORT).show();
                }
                GridLabelRenderer glr = graph.getGridLabelRenderer();
                glr.setTextSize(44);
                series.setAnimated(true);
                series.setColor(Color.WHITE);
                glr.setGridColor(Color.WHITE);
                glr.setVerticalLabelsColor(Color.WHITE);
                glr.setHorizontalLabelsColor(Color.WHITE);
                graph.addSeries(series);
            }



            FloatingActionButton fab = rootView.findViewById(R.id.floatingActionButton);
            //This desyncs previous and current temperature and
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
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
                    //Move to LinkedLinkedHashMap
                    //LinkedHashMap has the form of (Date,String)
                    //Date is the date of the measurement, and String is the temperature


                    Log.w("Debug", mqttMessage.toString());
                    Log.w("Topic",topic);
                    SharedPreferences sharedpreferences =getActivity().getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    if(topic.equals("sensor/hum")){
                        Log.w("Hum","Humidity received ");
                        humLast.setText(humNow.getText());
                        humNow.setText(mqttMessage.toString());

                        editor.putString("humNow", humNow.getText().toString());
                        editor.putString("humLast", humLast.getText().toString());

                        editor.apply();
                    }
                    else {
                        //TODO: If temp new is higher than the current highest temp set it
                        tempLast.setText(tempNow.getText());
                        editor.putString("tempNow", mqttMessage.toString()+"°C");
                        editor.putString("tempLast", tempLast.getText().toString());
                        editor.apply();
                        int temp = Integer.parseInt(mqttMessage.toString());
                        series.appendData(new DataPoint(series.getHighestValueX()+1,temp), false, 25000);
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
                        //Only store 1 value per hour into the file!!!
                        SimpleDateFormat df = new SimpleDateFormat("hh-dd-MMM-yyyy");

                        String dateString = df.format(date);
                        try {
                            formattedDate = df.parse(dateString);
                            data.put(formattedDate,mqttMessage.toString());
                            System.out.println("Data"  + data);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        //append to the temp.mqtt file
                        System.out.println("DATA SIZE" + data.size());
                        tempNow.setText(mqttMessage.toString()+"°C");
                        LinkedHashMap<Date,String>dateStringLinkedHashMap = new LinkedHashMap<>();
                        dateStringLinkedHashMap.put(formattedDate,mqttMessage.toString());
                        System.out.println(dateStringLinkedHashMap.size());
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
            //Put data into treemap
            //Hope treemap sorts it out
            //Hope there are no errors and that LinkedHashMap puts in new elements to the end!
            try
            {
                LinkedHashMap<Date,String> tempMap=getData();
                if(tempMap==null){
                    tempMap = new LinkedHashMap<>();
                }
                //tempMap.clear();
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
        private LinkedHashMap<Date,String> getData(){
            LinkedHashMap<Date,String> myLinkedHashMap=null;
            try
            {
                FileInputStream fileInputStream = new FileInputStream(getActivity().getApplicationContext().getFilesDir()+"/map.ser");
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                myLinkedHashMap = (LinkedHashMap<Date, String>) objectInputStream.readObject();
            }
            catch(ClassNotFoundException | IOException | ClassCastException e) {
                e.printStackTrace();
            }
            return myLinkedHashMap;
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




