package com.example.geek.udaanv20;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Beacon;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
/**
 * Created by Geek on 3/23/2017.
 */



public class MyApplication extends Application  {
    private BeaconManager beaconManager;
    public static boolean flag;

    //public static final String EXTRA_MESSAGE ="com.example.udaanv20.MESSAGE";
    private static final String THINGSPEAK_READ_GATE_CHANGE = "http://api.thingspeak.com/channels/251864/feeds/last.json?api_key=8HY7EHNB0CWGFZW2";
    //private String myVal;
    private int toGate;
    private int fromGate;
    private int flightNo;

    @Override
    public void onCreate(){
        super.onCreate();

        beaconManager = new BeaconManager(getApplicationContext());


        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                runApplication();
            }
            @Override
            public void onExitedRegion(Region region) {
                // could add an "exit" notification too if you want (-:
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback(){
            @Override
            public void onServiceReady(){
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        49478,42872));
            }
        });
    }

    public void runApplication(){
        //final Global globalVariable = (Global) getApplicationContext();
        //globalVariable.setName(true);
        /*showNotification(
                "Your gate closes in 47 minutes.",
                "Current security wait time is 15 minutes, "
                        + "and it's a 5 minute walk from security to the gate. "
                        + "Looks like you've got plenty of time!");*/
        //Global.flag=1;
        //setFlag(true);
        /*if(flag==false)
            myVal="false";
        else
            myVal="true";

        showNotification("",myVal);*/

            String To_Gate=Integer.toString(toGate);
            String From_Gate=Integer.toString(fromGate);
            String Flight_No=Integer.toString(flightNo);

            showNotification(
                    "Flight Gate Change: "+Flight_No,
                    "You have go to "+To_Gate+" from "+From_Gate
                            + " "
                            + " ");

    }

    public void setGateTo(int toGate){
        this.toGate=toGate;
    }
    public int getGateTo(){
        return toGate;
    }

    public void setGateFrom(int fromGate){
        this.fromGate=fromGate;
    }
    public int getGateFrom(){
        return fromGate;
    }

    public void setFlightNo(int flightNo){
        this.flightNo=flightNo;
    }
    public int getFlightName(){
        return flightNo;
    }

    public void setFlag(boolean flag) {
        MyApplication.flag = flag;
    }
    public boolean getFlag() {
        return MyApplication.flag;
    }

    public void showNotification(String title,String message){
        Intent notifyIntent= new Intent(this,MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivities(this,0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification= new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager=
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification);
    }

}
