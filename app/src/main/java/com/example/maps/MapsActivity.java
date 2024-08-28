package com.example.maps;

import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.Duration;
import com.google.maps.model.TravelMode;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private ImageView btn_lieux;
    private PlacesClient placesClient;
    private Marker userMarker;
    private com.google.maps.model.LatLng currentLocation;
    private String selectedPlaceType;
    private Place.Type typeChoosedPlace, placeType;
    private List<Marker> mapMarkers = new ArrayList<>();
    private  ImageView share_btn;
    private  ImageView weather_btn;





    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    PlacesService apiService = retrofit.create(PlacesService.class);
    //création de GeoApiContext pour l'utilisation de l'api Directions:
    GeoApiContext mGeoApiContext = new GeoApiContext.Builder()
            .apiKey("AIzaSyD-ewzqSNRxN8FP2cvKAHIBbPbCncPmvWs")
            .build();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Récupérer la latitude et la longitude :
        latitude = getIntent().getDoubleExtra("LATITUDE", 0.0);
        longitude = getIntent().getDoubleExtra("LONGITUDE", 0.0);
        // créer l'objet LatLng avec les coordonnées de la position actuelle :
        currentLocation = new com.google.maps.model.LatLng(latitude, longitude);
        LatLng currentLoc = new LatLng(latitude, longitude);

        // Initialisation de Places API pour la recherche des endroits :
        Places.initialize(getApplicationContext(), "AIzaSyD-ewzqSNRxN8FP2cvKAHIBbPbCncPmvWs");
        placesClient = Places.createClient(MapsActivity.this);


//*********************************************************************************************************************************************************************************************************************************************************************************************
//******************************************************* Search Bar + Itineraire  ******************************************************************************************************************************************************************************************************************
        // Initialisation de l'AutocompleteSupportFragment :
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Spécifier le champ de recherche :
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(31.7003057, 12.452307),
                new LatLng(31.7003057, 12.452307)
        ));
        autocompleteFragment.setCountries("MAR");
        // Spécifier ce qu'on doit récupérer de la recherche :
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG));

        // Lorsque l'utilisateur choisit l'une des suggestions :
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String placeName = place.getName();
                LatLng suggLatLng = place.getLatLng();
                String address = place.getId();
                //afficher le lieu dans la map avec un marqueur :
                clearMapMarkers();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentLoc).title("Position actuelle").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                mapMarkers.add(mMap.addMarker(new MarkerOptions().position(suggLatLng).title(placeName)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(suggLatLng, 17f));
                // demande d'avoir un itinéraire vers le lieu recherché:
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Itinéraire")
                        .setMessage("Voulez vous un itinéraire a partir de votre localisation à cette addresse ?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // envoyer la requete à l'api directions :
                                DirectionsApiRequest directions = DirectionsApi.newRequest(mGeoApiContext);
                                // definir les points de départ et d'arrivée
                                com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(latitude, longitude);
                                com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(suggLatLng.latitude, suggLatLng.longitude);
                                // Demandez l'itinéraire en specifiant l'origine, destination, mode de traverse vers la destination et optimiser le chemin (le chemin le plus court)
                                directions.origin(origin);
                                directions.destination(destination);
                                directions.mode(TravelMode.DRIVING);
                                directions.optimizeWaypoints(true);
                                List<LatLng> coordinates = new ArrayList<>();
                                // initialiser le polylineOptions ( les traits qui sont affichés pour lier l'itineraire
                                PolylineOptions polylineOptions = new PolylineOptions();
                                try {
                                    // recuperer les resultats a partir de l'api
                                    DirectionsResult result = directions.await();
                                    if (result != null && result.routes.length > 0) {
                                        // Sélectionnez le premier itinéraire
                                        DirectionsRoute route = result.routes[0];
                                        // Créez un constructeur de LatLngBounds pour créer une zone englobante autour de l'itinéraire pour le zoom :
                                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                                        for (DirectionsLeg leg : route.legs) {
                                            for (DirectionsStep step : leg.steps) {
                                                String polyline = step.polyline.getEncodedPath();
                                                List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(polyline);
                                                // Créez une Polyline pour chaque segment de l'étape:
                                                List<LatLng> segmentCoordinates = new ArrayList<>();
                                                for (com.google.maps.model.LatLng latLng : decodedPath) {
                                                    boundsBuilder.include(new LatLng(latLng.lat, latLng.lng));
                                                    segmentCoordinates.add(new LatLng(latLng.lat, latLng.lng));
                                                }
                                                // Ajoutez la Polyline du segment à la carte
                                                PolylineOptions segmentPolylineOptions = new PolylineOptions()
                                                        .addAll(segmentCoordinates)
                                                        .width(10)// Définissez la largeur de la ligne
                                                        .color(Color.RED); // Définissez la couleur de la ligne
                                                mMap.addPolyline(segmentPolylineOptions);
                                            }
                                            Duration duration = leg.duration; // Obtenez la durée estimée du tronçon
                                            // Affichez la durée estimée dans un TextView (remplacez "textViewDuration" par l'ID de votre TextView)
                                            Toast.makeText(MapsActivity.this, "Durée estimée : " + duration, Toast.LENGTH_SHORT).show();
                                        }
                                        // Construisez la zone englobante
                                        LatLngBounds bounds = boundsBuilder.build();

                                        // Ajustez le niveau de zoom pour afficher toute la zone englobante
                                        int padding = 50; // Marge en pixels autour de la zone englobante
                                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                                        // Appliquez le zoom à la carte
                                        mMap.animateCamera(cu);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            @Override
            public void onError(Status status) {

            }
        });


//*********************************************************************************************************************************************************************************************************************************************************************************************
//******************************************************* Lieux a proximite  ******************************************************************************************************************************************************************************************************************
        // Récupérer le bouton :
        btn_lieux = findViewById(R.id.btn_lieux);
        // Ajouter l'événement onClick :
        btn_lieux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Afficher une boîte de dialogue qui permet à l'utilisateur de choisir les lieux à chercher :
                final String[] placeTypes = {"atm", "bank", "cafe", "bus_station", "doctor", "gym", "hospital","university","train_station","school","restaurant","pharmacy","mosque"};
                // Créer un AlertDialog.Builder :
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Choisir un type de lieu");

                builder.setItems(placeTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Récupérer le type de lieu sélectionné par l'utilisateur :
                        selectedPlaceType = placeTypes[which];
                        // Définissez les paramètres de l'appel à l'API (latitude, longitude, rayon, type, clé d'API)
                        String location = latitude+","+longitude; // Remplacez par les coordonnées souhaitées
                        int radius = 2000; // Rayon en mètres
                        String type = selectedPlaceType; // Type de lieu souhaité
                        String apiKey = "AIzaSyD-ewzqSNRxN8FP2cvKAHIBbPbCncPmvWs";
                        // Effectuez l'appel à l'API pour obtenir les lieux à proximité
                        Call<PlacesResponse> call = apiService.getNearbyPlaces(location, radius, type, apiKey);
                        Log.e("call" , ""+call);
                        // Exécutez l'appel de manière asynchrone
                        call.enqueue(new Callback<PlacesResponse>() {
                            @Override
                            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                                if (response.isSuccessful()) {
                                    //clear la map :
                                    clearMapMarkers();
                                    // Récupérez les lieux à partir de la réponse
                                    List<com.example.maps.Place> places = response.body().getPlaces();
                                    // Maintenant, vous pouvez utiliser ces données pour afficher les lieux sur la carte
                                    for (com.example.maps.Place place : places) {
                                        // Obtenez les coordonnées du lieu
                                        double placeLatitude = place.getGeometry().getLocation().getLatitude();
                                        double placeLongitude = place.getGeometry().getLocation().getLongitude();
                                        // Obtenez le nom et l'adresse du lieu
                                        String placeName = place.getName();
                                        String placeAddress = place.getVicinity();
                                        // Créez un marqueur sur la carte pour le lieu
                                        MarkerOptions markerOptions = new MarkerOptions()
                                                .position(new LatLng(placeLatitude, placeLongitude))
                                                .title(placeName)
                                                .snippet(placeAddress);
                                        mapMarkers.add( mMap.addMarker(markerOptions));// Ajoutez le marqueur à la carte
                                    }
                                } else {
                                    Log.e("Retrofit", "Échec de la requête. Code de statut HTTP : " + response.code());
                                    // Vous pouvez également enregistrer le corps de la réponse (si disponible) pour plus d'informations
                                    try {
                                        Log.e("Retrofit", "Corps de la réponse (erreur) : " + response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    // Gérez les erreurs en cas de réponse non réussie
                                    // Par exemple, vous pouvez afficher un message d'erreur à l'utilisateur
                                }
                            }
                            @Override
                            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                                // Gérez les erreurs en cas d'échec de l'appel
                                // Par exemple, vous pouvez afficher un message d'erreur à l'utilisateur
                            }
                        });
                    }
                });
                builder.show();
            }
            });

//**************************************************Share Location

        // Récupérer le bouton :
        share_btn = findViewById(R.id.share_btn);
        // Ajouter l'événement onClick :
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Créez une intention pour ouvrir l'application de partage de localisation
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "https://maps.google.com/?q="+latitude+","+longitude);

                // Démarrez l'activité de partage de localisation
                startActivity(Intent.createChooser(intent, "Partager ma localisation via..."));

            }
        });

//***********************************************************************
        OkHttpClient client = new OkHttpClient();
        String apiKey = "8043d4a224d56b85c7ae0488a1c0dc4f";
        String baseUrl = "https://api.openweathermap.org/data/2.5/";
        String apiUrl = baseUrl + "weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey;



// Récupérer le bouton :
        weather_btn = findViewById(R.id.weather_btn);

        // Ajouter l'événement onClick :
        weather_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Retrofit retrofit2 = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                WeatherApi weatherApi = retrofit2.create(WeatherApi.class);

                Call<WeatherResponse> call = weatherApi.getWeather(latitude, longitude, apiKey);

                call.enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {

                        if (!response.isSuccessful()) {
                            // Gérez les erreurs ici

                            return;
                        }

                        WeatherResponse weatherResponse = response.body();
                        if (weatherResponse != null) {
                            double temperature = weatherResponse.getMain().getTemperature() - 273.15; // Convert to Celsius
                            String weatherCondition = weatherResponse.getWeatherConditions()[0].getMain();
                            Toast.makeText(MapsActivity.this, "Température : " + String.format("%.1f", temperature) + " °C", Toast.LENGTH_SHORT).show();
                            Toast.makeText(MapsActivity.this, "Condition : " + weatherCondition + String.format("%.1f", temperature) + " °C", Toast.LENGTH_SHORT).show();

                            AlertDialog.Builder weather_builder = new AlertDialog.Builder(MapsActivity.this);
                            weather_builder.setTitle("Météo :");
                            if( weatherCondition == "Rain" ){
                                weather_builder.setMessage("le ciel va peut etre pleuvoir aujourd'hui ! avec une temperature de : "+String.format("%f",temperature+"°C") );
                                weather_builder.show();

                            }
                            else {
                                weather_builder.setMessage("Température :"+String.format("%.1f",temperature)+"°C" );
                                weather_builder.show();

                            }
                            //temperatureTextView.setText("Température : " + String.format("%.1f", temperature) + " °C");
                            //weatherConditionTextView.setText("Condition : " + weatherCondition);
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        // Gérez les erreurs de réseau ici

                    }
                });
            }

        });





        // Récupérer le SupportMapFragment et être notifié lorsque la carte est prête à être utilisée :
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(MapsActivity.this);
            }

    private void clearMapMarkers() {
        for (Marker marker : mapMarkers) {
            marker.remove();
        }
        mapMarkers.clear();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Ajouter un marqueur à l'emplacement de l'utilisateur et déplacer la caméra :
        LatLng userLatLng = new LatLng(latitude, longitude);
        userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title("Position actuelle").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f));

        // Activer les contrôles de zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Activer les gestes de zoom (pincer pour zoomer) et de rotation de la carte
        mMap.getUiSettings().setZoomGesturesEnabled(true);

    }



}




