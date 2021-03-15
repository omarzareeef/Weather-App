package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements CityNameDialog.CityNameDialogListener {

    RelativeLayout mainLayout;
    ProgressBar loading;
    TextView errorMessage, address, updatedAt, status, temp, tempMin, tempMax, sunrise, sunset, wind, pressure, humidity;
    ImageView icon;

    private static final String TAG = "MainActivity";

    String cityName = "Minya";
    String apiKey = "4273578d5cb1a54777b34ef01a0a7cfc";

    @Override
    public void getCityName(String cityName) {
        mainLayout.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);

        apiFunction(cityName);
    }

    public class DownloadIcon extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {

            Bitmap bitmap = null;
            URL url;
            HttpURLConnection httpURLConnection;
            InputStream inputStream;

            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                inputStream = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        apiFunction(cityName);
    }

    private void initView() {
        mainLayout = findViewById(R.id.mainContainer);
        loading = findViewById(R.id.loader);
        errorMessage = findViewById(R.id.errortext);

        mainLayout.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);

        icon = findViewById(R.id.weather_icon);
        address = findViewById(R.id.address);
        updatedAt = findViewById(R.id.updated_at);
        status = findViewById(R.id.status);
        temp = findViewById(R.id.temp);
        tempMin = findViewById(R.id.temp_min);
        tempMax = findViewById(R.id.temp_max);
        sunrise = findViewById(R.id.sunrise);
        sunset = findViewById(R.id.sunset);
        wind = findViewById(R.id.wind);
        pressure = findViewById(R.id.pressure);
        humidity = findViewById(R.id.humidity);
    }

    private void openDialog() {
        CityNameDialog cityNameDialog = new CityNameDialog();
        cityNameDialog.show(getSupportFragmentManager(), "City Name");
    }

    private void apiFunction(String city) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherApiInterface weatherApiInterface = retrofit.create(WeatherApiInterface.class);

        Call<WeatherMap> call = weatherApiInterface.getWeatherMap(city, apiKey);

        call.enqueue(new Callback<WeatherMap>() {
            @Override
            public void onResponse(Call<WeatherMap> call, Response<WeatherMap> response) {


                String urlIcon = "http://openweathermap.org/img/wn/"+response.body().getWeather().get(0).getIcon()+"@2x.png";
                DownloadIcon downloadIcon = new DownloadIcon();
                Bitmap bitmap = null;
                try {
                    bitmap = downloadIcon.execute(urlIcon).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                icon.setImageBitmap(bitmap);

                address.setText(response.body().getName()+", "+response.body().getSys().getCountry());
                updatedAt.setText("Updated at: "+new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
                        .format(response.body().getDt()*1000));
                status.setText(response.body().getWeather().get(0).getDescription());
                temp.setText(fahrenheitToCelsius((double) response.body().getMain().getTemp())+"C");
                tempMin.setText("Min Temp: "+fahrenheitToCelsius((double) response.body().getMain().getTempMin())+"C");
                tempMax.setText("Max Temp: "+fahrenheitToCelsius((double) response.body().getMain().getTempMax())+"C");
                sunrise.setText(new SimpleDateFormat("hh:mm a",Locale.ENGLISH)
                    .format(response.body().getSys().getSunrise()*1000));
                sunset.setText(new SimpleDateFormat("hh:mm a",Locale.ENGLISH)
                        .format(response.body().getSys().getSunset()*1000));
                wind.setText(response.body().getWind().getSpeed().toString());
                pressure.setText(response.body().getMain().getPressure()+"");
                humidity.setText(response.body().getMain().getHumidity()+"");

                loading.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<WeatherMap> call, Throwable t) {
                loading.setVisibility(View.GONE);
                errorMessage.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "onFailure: "+t.getMessage());
            }
        });
    }

    public int fahrenheitToCelsius(double degree) {
        return (int) ((degree-32)/1.8);
    }
}