package com.example.rishab.pebble;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    public static final UUID  PEBBLE_ID = UUID.fromString("4522eded-9321-4e6c-b527-760e6b6bb8c9");
    private PebbleKit.PebbleDataReceiver dataReceiver;
    private double lat, lng;
    private String userName, userNumber;
    private Firebase pebble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Firebase fbref = new Firebase("https://glowing-inferno-197.firebaseio.com/");

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("PEBBLE_PREFS", Context.MODE_PRIVATE);
        userName = sharedPref.getString("name", "John Doe");
        userNumber = sharedPref.getString("number", "+91-9999999999");

        pebble = fbref.child("Pebble");
        lat = lng = 0;

        pebble.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String name = (String) dataSnapshot.child("name").getValue();
                if(name.equals(userName)) {
                    return;
                }
                String number = (String) dataSnapshot.child("number").getValue();
//                Toast.makeText(MainActivity.this, "Got callback", Toast.LENGTH_SHORT).show();
//                float rcdLat = Float.valueOf((String) dataSnapshot.child("lat").getValue());
//                float rcdLng = Float.valueOf((String) dataSnapshot.child("lng").getValue());
//                if(Math.abs(rcdLat - lat) <= 1 && Math.abs(rcdLng - lng) <= 1) {
                    notifyContactSave(name, number);
//                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // Do nothing
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Do nothing
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_ID);

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

    @Override
    public void onPause() {
        super.onPause();

//        if (dataReceiver != null) {
//            unregisterReceiver(dataReceiver);
//            dataReceiver = null;
//        }
//        PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_ID) {

            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary dict) {

                PebbleKit.sendAckToPebble(context, transactionId);

                lat = 0;
                lng = 0;

//                Toast.makeText(MainActivity.this, "KUCHHH AAYAA", Toast.LENGTH_SHORT).show();

//                if(dict.getString(1)!=null) {
//                    String msg = dict.getString(0);
//                    if(!msg.equals("KUCH AAYA")) {
//                        return;
//                    }
//                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                }
//                if(dict.getString(1)!=null) {
//                    lng = Float.valueOf(dict.getString(1))/10000;
//                    Toast.makeText(MainActivity.this, dict.getString(1), Toast.LENGTH_SHORT).show();
//                }

                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = null;
                try {
                    location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    lat = location.getLongitude();
                    lng = location.getLatitude();
                } catch(SecurityException e){
                    //Do nothing
                } catch(NullPointerException e) {
                    // Do nothing
                }

                pingFirebase();
//
//                if(lat>0 && lng>0) {
//                    pingFirebase();
//                }

            }
        };

        PebbleKit.registerReceivedDataHandler(this, dataReceiver);

    }

    public void saveContact(View view) {
        EditText nameBox = (EditText) findViewById(R.id.name);
        EditText numberBox = (EditText) findViewById(R.id.number);
        userName = nameBox.getText().toString();
        userNumber = numberBox.getText().toString();
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("PEBBLE_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("name", userName);
        editor.putString("number", userNumber);
        editor.apply();
        this.moveTaskToBack(true);
//        Toast.makeText(MainActivity.this, userName, Toast.LENGTH_SHORT).show();
//        pingFirebase();
    }

    private void notifyContactSave(String name, String number) {

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);

        NotificationCompat.Builder notif = new NotificationCompat.Builder(this);
        notif.setContentTitle("New Contact Received!");
        notif.setContentText("Touch to save...");
        notif.setSmallIcon(R.mipmap.ic_launcher);
        notif.setPriority(2);
        notif.setAutoCancel(true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notif.setContentIntent(pendingIntent);

        Notification notification = notif.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(999, notification);

    }

    private void pingFirebase() {

        FBObject obj = new FBObject(userName, userNumber, lat, lng);
        pebble.push().setValue(obj);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("PEBBLE_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("name", userName);
        editor.putString("number", userNumber);
        editor.apply();

    }

    public void simulateHandshake(View view) {
        pingFirebase();
    }

    private class FBObject {
        private String name;
        private String number;
        Double lat;
        Double lng;

        public FBObject(String name, String number, Double lat, Double lng) {
            this.name = name;
            this.number = number;
            this.lat = lat;
            this.lng = lng;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }
    }
}
