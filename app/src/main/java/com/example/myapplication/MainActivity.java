package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);

        }
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);


        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter city Name", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults [0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted..", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getCityName(double longitude, double latitude) {
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for (Address adr : addresses) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                    } else {
                        Log.d("TAG", "CITY NOT FOUND");
                        Toast.makeText(this, "User City Not Found..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch(IOException e){
                e.printStackTrace();
            }
            return cityName;
        }
    private void getWeatherInfo (String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=3f5447b99490492ea6b145644232402&q="+cityName+"&days=1&aqi=yes&alerts=yes";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();
                try {
                    String temperature = response.getJSONObject("current").getString("temp_f");
                    temperatureTV.setText(temperature+"°f");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if(isDay==1){
//morn
                        Picasso.get().load("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAoHCBUWFRgVFRUYGBgYEhgYGBoYEhgYGBgYGBgZGRgYGBgcIS4lHB4rIRgYJjgmKy8xNTU1GiQ7QDs0Py40NTEBDAwMEA8QHhISHjQkISE0NDQ0NDQ0NDQ0NDQ0NDQ0NDQxNDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NP/AABEIAUsAmAMBIgACEQEDEQH/xAAbAAACAwEBAQAAAAAAAAAAAAACAwEEBQAGB//EADcQAAICAQMDAgQEBAYCAwAAAAECABEDBBIhBTFBUWEGInGREzKBoRQVQvBSscHR4fEjkmJygv/EABkBAAMBAQEAAAAAAAAAAAAAAAABAgMEBf/EACARAAMBAQEAAwEAAwAAAAAAAAABEQIhEgMxQRMEImH/2gAMAwEAAhEDEQA/AMFst3BSoNzlWelDyaNdfSVqj1YiL2wQPokzqj9sFljCCisEAx+yQFgEE3Ho1cyRjhMniJjSY1ORO/hrjtNh9ZaWhMWdGVwzDp/acNPNBquAVjHDPfHKzpNTIkrukES0ZjJBZJcyLFLz4lJEPRV2To9xU6OEejR2w0SWHwwQkqi8wAJ6QSsvfgUPrEPjMlarLeGkVysWRHkQCsshijIhkQCIASpqPxJfIiVxEy2jBVqTpwvKo8cCI3kwXzboeJOZmak7qjgsj8Eee0kXVRMFReRpVfmWMhlRmNysonTEuk5FriMJiczyyHELzidEMTIjhl6PVKnrIdBfaGy8wynExp1eUArGScckNFnKY0mD0hWXB7RTac+JZDmRdSlSGssznSQuO5bzKLkInmXeGfnoz8PxEZUqXF7ReXHcybOhIzl7y5iaK/BNx2zj3ghNB5CZG81F/iG5KOTx5uVCaMZOORKmZObE1AnyylkUk14kplazwpERGXHNEoB4isyjxKpDzzpllJ0tpj5nSvRl4PQFOYbofEagnAzOnTCo+MiLIl5llZklJkazBNSWMZtg7ZRIhluNwYzJEs4iJL+h5XSRjoSAohPfrKoyV3kLNNHpINqMQwEF3N8QauUskPdIyESvjb5gTHOPEUyS0jJvpsplVgBfNROTDRuVunVvozafGDMdLyzqxr0umDmUxDLNjLpeYg6SJaHrJmqJ003wgVxOlUjyXROMYFklYUIIIglY8pFkSkxNCtkEJ6xxEAiMkW6CSgklYBWMRLmotgDCIkQgUUyQGWWCIJEZMK7CDslkiRsjoeSuoqPXVPY57TikErE+jVRpJqww5gNmHrM+4NzPwjT+jLruLFc3JlTHmoyIeRe0bhWQRHFYDLEWJMiowrBIjExZEErG1BIjFBZEErGkSCsdJgkrBKR+2RshRQr7ZG2WCkErHQgnbIIjds4pABFQGWPKSNsYoI2wSI8rBKwCCNs6MKzoCh6XbBZI2p1TCnVCuyRRSWykBklUloqlZ22WCkErCkwRtkFY7bIKQoQTtnVG7JGyFCCysEpGlZ1QoQQUkFJYKwSsdCFdlgESwVglI6TCuywCJYZIBSVRQQVnRpSdAR6Kp1RmydUwp0iqgkRxSDtjooJIgFZYKSCkdCFcrJ2xxWQFhQgnbIKR5SDthRQQUglJYKQCkdFBG2Qyy2unYqXA4UgH6m6/yMSwhRQRtkERlQajAUUgnHHSKhRQQQJMPIlidFQh6Gp22C1iEpMyp0wgidthkSIEgFYNRlTqgIWRAKR5kEygEbZFRxIgNUBAgCSUUwZBaADFBAKhuG7j1rtEZdKRz3EXmzhZKauhfb6w6g4/sS61Fkx+XXB/lC2fWQUlJktL8K9TqjSIJEZMAAnQp0Bm+RICxhWRUyNwKgkRhkEQEBIIhVIqAgDAIjSIJEoUFGA0Ywg1AQuQYZEioAV/wATZi8ye30luQ4viAFbAnHPeGwjagkRiKbqx8wkWhzDcfeFs4jCCnFzo4LIhRQ3d19pBjvw67QCJjTeAGIfMF7mDrnIHfv24mQuBmNuTUoTNF9cl0D+0amYHt/lK+m0i9xz95c2VxARANyGnbYpgTACSwgmCEnBDHRQFmPiQBxHbYNGIDK1Op2jiU16i/JJ+8ta3TNZ4mRm0RvzGmTpMcOrvfcd/SbmHOGQMfT95iY+kt3M1NLoq7x0STLG4dwYS5FPFzmwRJwVCjLG2dBVfl7zoAbm+xdyjq9UUbhgPryJl6nVsosWL8eJkanUO5v0MxSprpw9in/kF8fWGdFzZnmNBrcikV2Heeq0GtDgA9/Mb4GWmEMQkFJo/hiA2EReivJmMkHbLz4Ih8cpaJeSsVg1HskWVlEwWRJqSRIgIBsYMT/DLd1LJnVCBRRSDsEawgwEA0o6nXKviX3P6zC6oL7AQbGlwRn6ufYTpkvpWJ49Z0rhHT6Fr+nb2FSv/ACUf2JrYnBPBuBl1yKaZuZidDKeHpaoOw+0cmJU+axz3/SOy5xtBHkXMZtzWPfj0gH0badQSuWA/WWMepVuxueW0+kG7k37XNHT6Mghgx47RQFpm0ziKZovfBLRpA2c0Uwh3IMpCFMsArHEQSJSZME1OjCsErAmAVIIhGQTAIJyg7TXpPM6w1dGeh1uoAUqCCT+08zrG7w/Q/BWjdi3cces6UkJDTpUM6fScGBl5B57dvEYdIHHzC/euZqMF9It1AnOmdbRWbDS0K7VKv8tU8m/vNG5BAjFCmmkRewjQsYUgERkkEQDDIkGAAQahyCIwBqdthQWMYAtQ5JqZmp6gwakUEDuSa+0sOjXR5Ja/YTP1+RcSkkgs3Yeh9ajRLLTaxTR9v0H1mZk1TOTRG0ekwMWd3Y1dnivaaenTILCrfFDiufUyoRaUdZ1IDgTPx6os0tv0fK7kbbAaiR2s+k1k6EqY7CneTQvm4+ImaZhBW71xOnoT09VG1u+3z4J7mdFQ8s+kF181KWbX4h78+BI3QSiHuoP6Tlh2UZhyB13L29DJKj0nJkA7ACNTJfpDocEUPcQDUfvUk8ixwfaA4jTE0JaooxziKYS0yGgCZETm1aL+ZwK78zL1HxAgNIN3ueI0I2YNytodWMgsCpcTGWYKO5g2kqwgjIhIpeD6zO1HRw/5iSfUmeiOBEBDGz63X2EzMmqUGhdeo5/WZ4/yM6fC38OpYUV6ZiSvWgOPMbkfGgpRz5v6RubEmRSQboeDz9pgat+9N2mv2Zrhp5esBRWxfaBg6wADvXn+nj9p5t9T7xY1hqjyCfMpZJejU1uvLksf+hOmHqM3E6OCp9OwZtyF9woHvzzK56ti8P8AptP+c80vxB8m1TQHiqHvxMnLrdxPcD6zlznXp36OzXlZ59nvcvVMaLbN9hM1viTcGGNCTfBPb61MJNTiyYfwxuVibLE383t7T0nSNEi40A5JXk33msSMXaZ2ZMjuSj+hYgUL8/Qzd0GQonzXyeATfHeNxYlQUoAnPjB7xPoLhk6n4jVXrbxZlDW/EW5SACtnweam1qemo60a9uJi9R6AqrYb9o1CXTzep1JJsMSP3lcZrMjU4drEAyqMu2aqGXb09JotQ6qNu4fNffipq4uqOLINk+tTx2HqJPBPEt4+obfNzPebw6fh0kb2r1mZ+b4PfnzKf8xZRXG7tYN8TJy9WZhtviUW11eJOMckNN7VqZtP1Qjz+sqP1D37zCyaoseJYAVe/wAx8+n2nQso4t6bZcLg8xOfV+BKr564uVnf3jhFLeTUmuZ0zMuo950cF6LuPXEmaOiO9toNX3J7D6z1vTvgrEhP4ilhRAO4iiPN9uTxLvRPh3Diy7w4qyFBNfMK4vz5nO95OpLRn9H+Glb5nY8H6D/eem0nTijWGLCq5P8AlNbYvpCoTJ7pfgq7DI2SyxqYnUPiBEcoF3EXuPgH0gm39A0l9jNdlGJC12bIAvz7zF1fWy67ABz395iZOo05LHcDfynnk+Znazqagmgf9ZtnJjrZoa7Cv5j3PoeJ5/WIQTxIfqbt+UftBXUMT89zRKGb1SrZhpkIjswBHERn4AEbDLZH8RA/G9YraPJg/iAe8SRT0WVeu0DJmlN9TEPqJZm2XPxYrJkHrKLagxLZbhSY2W8jL6yZQJnReivJ93+JesIdPQba5/LtNHxYoetzw+p605CIqldpBLf1bh3YEduK+01Om6VnNOB24vj/ALl/UdDJ4UWQvcChZ8AnxMF5zw3frXUbGk+IV/BVi+9rCGh/VV+fpNrDqQwBBBsX38TxOH4Y2cs241dDgE+hnodIExgFQbK0bMy0s/hrh6/RvxB1BsWMsO54Ht7j3niMelfLt27hf+I1uvyP+Z6zWIMvD2VsEDt2H/chcKKtVSj8vt54lZaSFrL0/wDhjv0IYxQot7i6P1+0811PCUb5iCT4Anp+oa8jdbeDPPPqyfmA9prlv9MdpfhUxaV2Fqv+kYiKl76J/YS2+qCLwe47/wC0wNRqrPJmi6Z64aGcDuFmRqsh8zT02TepIvjjt5mXq8LHyPvAV4ZzZoo5DLH8H6sI0Ig95UIejPO70MlcLHgKftNTDl9BNHDlrk94eQWzCXpGY0dnf1IH1kDpOT2/9p6s3l+VVLEDwO32lfL0fJuRbsuSFrk2O912iiK9a/Dzf8uYHaxA/WRPZaf4PyP+dgtDkt249xOiuRf7n0LTJiJDC+OQCO0uu1+ZlaLFt+UA0O5o9/a5ZzuRwo+5nA309JLgx2A94svfZZVwswveyn0oVX6+YxMleSfrGBY/DuZWv0+Q8IQeCe8vjPF6ld6kDvX9+ZWXCdKo8dnxu5prX5iDfp61EZ0QfLuAAH1MDqeLaT81/sJ53V5SPM6so49aaNfXZFKhVIAF1z6zN/hlu2ex6DvMt9UfBimzN6zRcM3Wehza4BdifKo8CZr5rma2SCMkVSB5bLrN7wgo9ZRGSNx5JSaJeWXSK/LLmmXd+a/0lJDu5PbwJfwKOApJPoBz+kZKXT03wz1MYmI/AYqw5Ycnj/4zR1PWMY+ZMD714B/Dog36m+PWY/TunZ95IXYyBT83m/btN7NpdS73uVEFcI1G/LE9/aY6lOjNkFdaxanIm5CQrqpfHuNggC6J8e06N6jp9WybEC9vz7iDZ+p+s6GXz7ROvv6Zv6TWDLfj0Ht635h5UA8z5hi65mFBPkodxwfrc9f0DqL5qRvmavzVXYefUzm38Lz1fR1/H8y1x/Zp5aiSa7cS5k0LwH6cyqWc8CZrSNWikcxidXr9iHmjXEDrOoRFX8PIik+XDftQnncuqRh/5Dua73AkKZvnN6Yb3OIy+q6iySD9p5zLZM9WcmLn5R+ogfxeFf6F/VQZ0I5tGDpeku9EDgnvN7Q/Bu/l8wVR3pbb7XI/ng7DgfaKTrlcAxNa/AzpJ96W8/wbjBG3Px/UStfbmZ+X4XA/K4PPfsJOfqhPmKHUWrvGsv8AQ18ivFDh8NEfmdB/+v8AiKy9I2mh830NiJz6tj3Yn9Ypc7f4j95SRD0aun6JkchUomr7/wB3PadE6U2Adl3VRauf38zxPS+oujq6tRHYn9wfae2X4lxjGru4dj3CCivHkfXzM/k9fSL+J5+39mxvUcsbPqZOPUr3Uzwuq+K8pY8gLfAA/u5W/nrvYJ4Juv8Ab0kr42W/lyfQn14/xDtZ57Tp81y9RYc7p0f8xf1FaTCpYfOBZHLDge5E9dpNa2mr8J0eyLdex9QARYr1954DFLuJztuz39faVpX7IxqfR9f0/VXYUy03r4N88D7Sv1De61urv9DYqjMHpmZl0+IqaJuzQN8n1lnp2sc7rbyfA9/acbxOo7s79RP9PI67oWqLG1NAWPmBH6GZWZHT5Wr7z3+u1TsrbmJ48/pPE9V/N+onT8enpdOb5cLL4ZZym4jLkudn7mVjNjD7DLTgYkxmncjzFSmuB7oQeJc8yFhReSyqlu0tYenuSPAIu6MLpiAsOJ9A6dhVlBKg3V8d4tag8Zp851OIo2277GOTTNs3kn1oA9vU+093rtFjV7VFB/8AqI0YloChXpXEXvgfz6fOsWPe1A19ZAxNVizRo8dp63rONU/IoWx4UTEx6p1b5WI+kpOqkNRwzDiyHsh+06ex0vzUW57d50n0X5P/2Q==").into (backIV);
                    }else {
                        Picasso.get().load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSBlnbsNm1vxIHFgTn8pCJoVfxIGM-kbVTKnw&usqp=CAU").into(backIV);
                    }
                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forcastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forcastO.getJSONArray("hour");


                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString( "temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getJSONObject("condition").getString("icon");
                        hourObj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal (time, temper, img, wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please enter Valid city name",Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }
}