package com.example.agm_3_000.myapplication;
//jull Quintero Daza #Coby01
package com.dam.profesor.webservice;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener{

    Button dato;
    EditText latitud;
    EditText longitud;
    TextView resultado;
    ObtenWeb conexion;
	Location location;
    LocationManager locationManager;
    LocationListener locationListener;
    AlertDialog alert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dato = (Button)findViewById(R.id.dato);
        latitud = (EditText)findViewById(R.id.latitud0);
        longitud = (EditText)findViewById(R.id.longitud0);
        resultado = (TextView)findViewById(R.id.resultado);
        dato.setOnClickListener(this);
		
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        /****Mejora****/
        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertNoGps();
        }
       /********/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        MostrarLocalizacion(location);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                MostrarLocalizacion(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

    }

    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(alert != null)
        {
            alert.dismiss ();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.removeUpdates(locationListener);
            }
        } else {
            locationManager.removeUpdates(locationListener);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public void MostrarLocalizacion(Location loc){

        if (loc!=null){
            hiloconexion = new ObtenerWebService();
            hiloconexion.execute(String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()));
        }

}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dato:
                conexion = new ObtenWeb();
                conexion.execute(latitud.getText().toString(),longitud.getText().toString());
                break;
            default:
                break;
        }

    }
    public class ObtenWeb extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) { //http://maps.googleapis.com/maps/api/geocode/json?latlng=40.7127837,-74.00594130000002&sensor=false
            String rama = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";

            rama = rama + params[0];
            rama = rama + ",";
            rama = rama + params[1];
            rama = rama + "&sensor=false";
            URL url = null;// donde quiero to get la Information
            String d ="";
            try {
                url = new URL(rama);
                HttpURLConnection online = (HttpURLConnection) url.openConnection(); // Para abrir la conexion
                online.setRequestProperty("User-Agent", "Mozilla/5.0" +
                        " (Linux; Android 1.5; es-ES) Ejemplo HTTP");
                int respuesta = online.getResponseCode();
                StringBuilder resultado = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK){
                    InputStream in = new BufferedInputStream(online.getInputStream());  // preparo la cadena de entrada

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));  // la introduzco en un BufferedReader

                    // JSONOBject necesita un String y tengo que tranformar el BufferedReader a String. Esto lo hago a traves de StringBuilder.

                    String line;
                    while ((line = reader.readLine()) != null) {
                        resultado.append(line);        // Paso toda la entrada al StringBuilder
                    }


                    JSONObject respuestaJSON = new JSONObject(resultado.toString());
                    JSONArray resultJSON = respuestaJSON.getJSONArray("results");
                    //Vamos obteniendo todos los campos que nos interesen.
                    //En este caso obtenemos la primera dirección de los resultados.
                    String direccion="SIN DATOS PARA ESA LONGITUD Y LATITUD";
                    if (resultJSON.length()>0){
                        direccion = resultJSON.getJSONObject(0).getString("formatted_address");
                    }
                    d = "Dirección: " + direccion;

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return d;
        }

        @Override
        protected void onCancelled(String aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected void onPostExecute(String aVoid) {
            resultado.setText(aVoid);

            //super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

}
