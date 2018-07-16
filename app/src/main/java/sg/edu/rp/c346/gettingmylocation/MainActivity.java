package sg.edu.rp.c346.gettingmylocation;

import android.Manifest;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {
    TextView tvLat, tvLng;
    Button btnStart, btnStop, btnCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLat = findViewById(R.id.tvLat);
        tvLng = findViewById(R.id.tvLng);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnCheck = findViewById(R.id.btnCheck);

        //file create
        final String folderLocation = getFilesDir().getAbsolutePath() + "/Folder";
        final File targetFile = new File(folderLocation, "data.txt");

        File folder = new File(folderLocation);
        if (folder.exists() == false) {
            boolean result = folder.mkdir();
            if (result) {
                Log.d("File Read/Write", "Folder created");
            }
        }

        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(100);
        final LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();
                    String msg = "New location Lat: "+lat +
                            " Lng: "+lng;
                    Toast.makeText(getBaseContext(),msg,Toast.LENGTH_SHORT).show();
                    tvLat.setText("Latitude: "+lat);
                    tvLng.setText("Lngtitude: "+lng);
                    try {
                        FileWriter writer = new FileWriter(targetFile, true);
                        writer.write("Lat: "+lat+", lng: "+lng+"\n");
                        writer.flush();
                        writer.close();
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Failed to write", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    }
                }
            }
        };
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission()){
                    Task<Location> task = client.getLastLocation();
                    task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(checkPermission()) {
                                if (location != null) {
                                    client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                                }
                            }
                        }
                    });
                }else{
                    Toast.makeText(getBaseContext(),"Permission denied",Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }
            }
            });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.removeLocationUpdates(mLocationCallback);
            }
        });
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(targetFile.exists()){
                    try{
                        FileReader fileReader = new FileReader(targetFile);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String result = "";
                        String line = bufferedReader.readLine();
                        while (line != null){
                            result += line + "\n";
                            line = bufferedReader.readLine();
                        }
                        bufferedReader.close();
                        fileReader.close();
                        Toast.makeText(getBaseContext(),result,Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        Toast.makeText(getBaseContext(), "Failed to read",Toast.LENGTH_SHORT);
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(getBaseContext(),"Cannot find the file",Toast.LENGTH_SHORT).show();
                }
            }
        });
        }


    private boolean checkPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

}

