package pl.romanov.s14048.studentstalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SplashTheme);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isOnline()){
            startMainActivity();
            //checkWifi();
        }else{
            Toast.makeText(this, "Please check your internet connection and try again",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void checkWifi() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String wifiSSID = wifiInfo.getSSID();
            Toast.makeText(this, "Connected to" + wifiSSID, Toast.LENGTH_SHORT).show();
            if("\"eduroam\"".equals(wifiSSID) || "\"PJWSTK\"".equals(wifiSSID) || "\"Tech_D0044190\"".equals(wifiSSID)) {
                startMainActivity();
            }else{
                Toast.makeText(this, "You are not in Academy. Please connect to eduoram or PJWSTK (WI-FI)", Toast.LENGTH_SHORT).show();
            }
        }

//    private boolean checkPermissions() {
//        if (ContextCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//           ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
//           return false;
//        }
//        return true;
//    }
//
//    @SuppressLint("MissingPermission")
//    private void checkLocation() {
//        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//        if (checkPermissions() && locationManager != null) {
//            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    //TODO check our location
//                    location.getLongitude();
//                    location.getLatitude();
//                    Toast.makeText(WelcomeActivity.this,  location.getLongitude() + ", " + location.getLatitude(), Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onStatusChanged(String s, int i, Bundle bundle) { }
//
//                @Override
//                public void onProviderEnabled(String s) {}
//
//                @Override
//                public void onProviderDisabled(String s) {
//                    Toast.makeText(WelcomeActivity.this, "Please enable location", Toast.LENGTH_LONG).show();
//                }
//            }, null);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            checkLocation();
//        } else {
//            Toast.makeText(this, "Plese grant permissions!", Toast.LENGTH_LONG).show();
//        }
//    }



}
