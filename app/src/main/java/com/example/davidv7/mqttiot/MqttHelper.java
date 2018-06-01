package com.example.davidv7.mqttiot;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttToken;

import java.util.Arrays;

public class MqttHelper extends Activity {
    public MqttAndroidClient mqttAndroidClient;
    //TODO: Make server URI dynamic
    String serverUri = "tcp://164.8.161.30:1883";

    final String clientId = "ExampleAndroidClient";
    final String subscriptionTopic = "sensor/#";

    final String username = "user1";
    final String password = "plaintextpass";

    public MqttHelper(Activity activity){

        mqttAndroidClient = new MqttAndroidClient(activity.getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Connection is lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect(activity);
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect(final Activity activity){
         final MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Success!");
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());

                    //In case there is no connection(no input is coming in)
                    //Show a pop up an edit text where user inputs a new IP of the server
                    //TODO: dynamic port
                    //TODO: dynamic topic
                    System.out.println("Connection lost");
                    LayoutInflater li = activity.getLayoutInflater();
                    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                    final View buidView = li.inflate(R.layout.alert_dialog,null);
                    alert.setTitle("No connection found");
                    alert.setMessage("Input the correct IP");

                    // Set an EditText view to get user input
                    alert.setView(buidView);

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                EditText ip = buidView.findViewById(R.id.IP);
                                String inputIP = ip.getText().toString();
                                EditText port = buidView.findViewById(R.id.port);
                                String inputPort = port.getText().toString();
                                String[] newUri = new String[]{"tcp://" + inputIP + ":" + inputPort};

                                EditText username = buidView.findViewById(R.id.username);
                                String inputUsername = username.getText().toString();
                                EditText password = buidView.findViewById(R.id.password);
                                String inputPassword = password.getText().toString();

                                System.out.println("Array[0] : " + newUri[0]);
                                mqttConnectOptions.setServerURIs(newUri);
                                mqttConnectOptions.setUserName(inputUsername);
                                mqttConnectOptions.setPassword(inputPassword.toCharArray());

                            } catch (NullPointerException err) {
                                System.out.println("URI: "+  mqttConnectOptions.getServerURIs());

                                err.printStackTrace();
                                return;
                            }
                            try {
                                System.out.println("Reconnecting");
                                connect(activity);
                            } catch (ActivityNotFoundException e) {
                                System.out.println("Reconnect failed");
                                e.printStackTrace();
                            }
                            //Restart the whole thing with a new server uri

                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    alert.show();
                    }


            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }


    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exceptionst subscribing");
            ex.printStackTrace();
        }
    }
}
