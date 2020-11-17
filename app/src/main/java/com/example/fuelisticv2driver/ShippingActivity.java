package com.example.fuelisticv2driver;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fuelisticv2driver.Common.Common;
import com.example.fuelisticv2driver.Common.LatLngInterpolator;
import com.example.fuelisticv2driver.Common.MarkerAnimation;
import com.example.fuelisticv2driver.Model.DriverUserModel;
import com.example.fuelisticv2driver.Model.ShippingOrderModel;
import com.example.fuelisticv2driver.Remote.IGoogleAPI;
import com.example.fuelisticv2driver.Remote.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class ShippingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Marker driverMarker;
    private ShippingOrderModel shippingOrderModel;

    // Animation
    private Handler handler;
    private int index, next,  time;
    private LatLng start, end;
    private float v;
    private double lat, lng;
    private Polyline blackPolyline,greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;
    private IGoogleAPI iGoogleAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private boolean isInit = false;
    private Location prevLocation;

    @BindView(R.id.txt_order_number)
    TextView txt_order_number;
    @BindView(R.id.txt_user_name)
    TextView txt_user_name;
    @BindView(R.id.txt_address)
    TextView txt_address;
    @BindView(R.id.txt_delivery_date)
    TextView txt_delivery_date;

    @BindView(R.id.btn_start_trip)
    MaterialButton btn_start_trip;
    @BindView(R.id.btn_call)
    MaterialButton btn_call;
    @BindView(R.id.btn_done)
    MaterialButton btn_done;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);

        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);

        ButterKnife.bind(this);
        buildLocationRequest();
        buildLocationCallback();

        setShippingOrder();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(ShippingActivity.this::onMapReady);

                        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(ShippingActivity.this);
                        fusedLocationProviderClient.requestLocationUpdates( locationRequest, locationCallback, Looper.myLooper());

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ShippingActivity.this, "You must enable this location permission to use Directions", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();


    }

    private void setShippingOrder() {
        Paper.init(this);
        String data = Paper.book().read(Common.SHIPPING_ORDER_DATA);

        if(!TextUtils.isEmpty(data))
        {
                shippingOrderModel = new Gson()
                        .fromJson(data, new TypeToken<ShippingOrderModel>(){}.getType());

                if(shippingOrderModel != null)
                {
                    Common.setSpanStringColor("Username: ", shippingOrderModel.getOrderModel().getUserName()
                    , txt_user_name, Color.parseColor("#333639"));

                    Common.setSpanString("Delivery Date: ", shippingOrderModel.getOrderModel().getDeliveryDate()
                            , txt_delivery_date );

                    Common.setSpanStringColor("No. : ", shippingOrderModel.getOrderModel().getKey()
                            , txt_order_number, Color.parseColor("#673ab7"));
                    Common.setSpanStringColor("Address: ", shippingOrderModel.getOrderModel().getShippingAddress()
                            , txt_address, Color.parseColor("#795548"));


                }
                else {
                    Toast.makeText(this, "Shipping order is null!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // Add a marker in Sydney and move the camera
                LatLng locationDriver = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());

                if(driverMarker == null){
                    // Inflate drawable
                    int height , width;
                    height = width = 80;
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat
                            .getDrawable(ShippingActivity.this, R.drawable.driver);
                    Bitmap resized = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width,height,false);

                    driverMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resized))
                            .position(locationDriver)
                            .title("You"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationDriver, 18));

                }

                if(isInit && prevLocation != null)
                {
                   String from = new StringBuilder()
                           .append(prevLocation.getLatitude())
                           .append(",")
                           .append(prevLocation.getLongitude())
                           .toString();

                    String to = new StringBuilder()
                            .append(locationDriver.latitude)
                            .append(",")
                            .append(locationDriver.longitude)
                            .toString();


                    moveMarkerAnimation(driverMarker, from, to);

                    prevLocation = locationResult.getLastLocation();


                }

                if(!isInit){
                    isInit = true;
                    prevLocation = locationResult.getLastLocation();
                }

            }
        };

    }

    private void moveMarkerAnimation(Marker marker, String from, String to) {
        // Request directions API to get data
        compositeDisposable.add(iGoogleAPI.getDirections("driving", "less_driving",
                from, to, getString(R.string.google_maps_key))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(returnResult  -> {

            try{
                //Parse json
                JSONObject jsonObject = new JSONObject(returnResult);
                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                for(int i=0; i<jsonArray.length(); i++)
                {
                    JSONObject route = jsonArray.getJSONObject(i);
                    JSONObject poly = route.getJSONObject("overview_polyline");
                    String polyLine = poly.getString("points");
                    polylineList = Common.decodePoly(polyLine);

                }
                polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.GRAY);
                polylineOptions.width(5);
                polylineOptions.startCap(new SquareCap());
                polylineOptions.jointType(JointType.ROUND);
                polylineOptions.addAll(polylineList);
                greyPolyline = mMap.addPolyline(polylineOptions);

                blackPolylineOptions = new PolylineOptions();
                blackPolylineOptions.color(Color.BLACK);
                blackPolylineOptions.width(5);
                blackPolylineOptions.startCap(new SquareCap());
                blackPolylineOptions.jointType(JointType.ROUND);
                blackPolylineOptions.addAll(polylineList);
                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                //Animator
                ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
                polylineAnimator.setDuration(2000);
                polylineAnimator.setInterpolator(new LinearInterpolator());
                polylineAnimator.addUpdateListener(valueAnimator -> {
                    List<LatLng> points = greyPolyline.getPoints();
                    int percentValue = (int)valueAnimator.getAnimatedValue();
                    int size = points.size();
                    int newPoints = (int) (size*(percentValue/100.0f));
                    List<LatLng> p = points.subList(0, newPoints);
                    blackPolyline.setPoints(p);
                });
                polylineAnimator.start();

                //Truck Moving
                handler = new Handler();
                index = -1;
                next = 1;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(index < polylineList.size()- 1)
                        {
                            index++;
                            next = index+1;
                            start = polylineList.get(index);
                            end = polylineList.get(next);
                        }

                        //Animator
                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                        valueAnimator.setDuration(1500);
                        valueAnimator.setInterpolator(new LinearInterpolator());
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                v = valueAnimator.getAnimatedFraction();
                                lng = v*end.longitude+(1-v)
                                        *start.longitude;
                                lat = v*end.latitude+(1-v)
                                        *start.latitude;
                                LatLng newPos = new LatLng(lat, lng);
                                marker.setPosition(newPos);
                                marker.setAnchor(0.5f, 0.5f);
                                marker.setRotation(Common.getBearing(start, newPos));

                                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                            }
                        });

                        valueAnimator.start();
                        if(index< polylineList.size() -2 )  // Reach destination
                            handler.postDelayed(this, 1500);

                    }
                } , 1500);


            }
            catch (Exception e){
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }, throwable -> {
            if(throwable!= null)
                 Toast.makeText(ShippingActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(15000); //15 sec
        locationRequest.setFastestInterval(10000); // 10 sec
        locationRequest.setSmallestDisplacement(20f); // 10 m
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onDestroy();

    }
}