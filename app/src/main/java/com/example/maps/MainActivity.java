package com.example.maps;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {

    private Button btnGrant;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // recuperation d'id du bouton grant permission
        btnGrant = findViewById(R.id.btn_grant);

        /* la manipulation des autorisations a l'aide de dexter :

        // instanciation de l'objet fusedLocationProvider Client: */
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        btnGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Dexter.withContext(MainActivity.this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //Permission accordée :

                        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                            if (location != null) {
                                // recuperation de la latitude et longitude a partir de l'objet location retourné par la methode fusedLocationprovider.getLastLocation :
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                    // envoyer latitude et longitude vers mapsActivity pour que ca soit utilisé pour afficher la localisation dans la map :
                                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                intent.putExtra("LATITUDE", latitude);
                                intent.putExtra("LONGITUDE", longitude);
                                startActivity(intent);
                                finish();
                            } else {
                                // La localisation est indisponible ou n'a pas été trouvée
                                Toast.makeText(MainActivity.this, "La localisation est indisponible ou n'a pas été trouvée.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if (permissionDeniedResponse.isPermanentlyDenied()) {
                            // L'utilisateur a refusé la permission de localisation de manière permanente, ouvrir les paramètres de l'application
                            showSettingsDialog();
                        } else {
                            // L'utilisateur a refusé la permission de localisation
                            Toast.makeText(MainActivity.this, "Permission refusée", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        // Afficher la demande de justification si nécessaire
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
            }
        });
    }








    // Afficher une boîte de dialogue pour inviter l'utilisateur à ouvrir les paramètres de l'application et accorder la permission
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Permission refusée")
                .setMessage("La permission de localisation est refusée de manière permanente. Vous devez aller dans les paramètres de l'application pour autoriser la permission.")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Paramètres", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                       // Uri uri = Uri.fromParts("package", getPackageName(), null);
                        //intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .show();
    }
}
