package com.example.fuelisticv2driver;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fuelisticv2driver.Adapter.MyShippingOrderAdapter;
import com.example.fuelisticv2driver.Common.Common;
import com.example.fuelisticv2driver.Common.LatLngInterpolator;
import com.example.fuelisticv2driver.Common.MarkerAnimation;
import com.example.fuelisticv2driver.Model.DriverUserModel;
import com.example.fuelisticv2driver.Model.Eventbus.UpdateShippingOrderEvent;
import com.example.fuelisticv2driver.Model.FCMSendData;
import com.example.fuelisticv2driver.Model.ShippingOrderModel;
import com.example.fuelisticv2driver.Model.TokenModel;
import com.example.fuelisticv2driver.Remote.IFCMService;
import com.example.fuelisticv2driver.Remote.IGoogleAPI;
import com.example.fuelisticv2driver.Remote.RetrofitClient;
import com.example.fuelisticv2driver.Remote.RetrofitFCMClient;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
    private int index, next, time;
    private LatLng start, end;
    private float v;
    private double lat, lng;
    private Polyline blackPolyline, greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;
    private IGoogleAPI iGoogleAPI;
    private IFCMService ifcmService;
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

    private Polyline redPolyline;
//    MyShippingOrderAdapter adapter;

    @OnClick(R.id.btn_done)
    void onDoneClick() {
        if (shippingOrderModel != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Order Completed")
                    .setMessage("Is this order delivered?")
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setPositiveButton("Yes", (dialogInterface, i) -> {

                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setMessage("Waiting...")
                                .create();

                        //Update Order
                        Map<String, Object> update_data = new HashMap<>();
                        update_data.put("orderStatus", 2);
//                            update_data.put("phoneNo", Common.currentDriverUser.getPhoneNo());  // ,MAYBE driverPhone

                        FirebaseDatabase.getInstance()
                                .getReference(Common.ORDER_REF)
                                .child(shippingOrderModel.getOrderModel().getKey())
                                .updateChildren(update_data)
                                .addOnFailureListener(e -> Toast.makeText(ShippingActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                                .addOnSuccessListener(aVoid -> {

                                    // Delete shipping order information
                                    FirebaseDatabase.getInstance()
                                            .getReference(Common.SHIPPING_ORDER_REF)
                                            .child(shippingOrderModel.getOrderModel().getKey())
                                            .removeValue()
                                            .addOnFailureListener(e -> Toast.makeText(ShippingActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                                            .addOnSuccessListener(aVoid1 -> {
                                                // Delete done
                                                FirebaseDatabase.getInstance()
                                                        .getReference(Common.TOKEN_REF)
                                                        .child(shippingOrderModel.getOrderModel().getUserPhone())
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()) {
                                                                    TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                                                                    Map<String, String> notiData = new HashMap<>();
                                                                    notiData.put(Common.NOTI_TITLE, "Your Order has been shipped!!");
                                                                    notiData.put(Common.NOTI_CONTENT, new StringBuilder("Your Order ")
                                                                            .append(shippingOrderModel.getOrderModel().getKey())
                                                                            .append(" just got delivered by ")
                                                                            .append(Common.currentDriverUser.getFullName()).toString());

                                                                    FCMSendData sendData = new FCMSendData(tokenModel.getToken(), notiData);


                                                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                                                            .subscribeOn(Schedulers.io())
                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                            .subscribe(fcmResponse -> {
                                                                                dialog.dismiss();
                                                                                if (fcmResponse.getSuccess() == 1) {
                                                                                    Toast.makeText(ShippingActivity.this,
                                                                                            "Order Completed Successfully!", Toast.LENGTH_SHORT).show();
                                                                                } else {
                                                                                    Toast.makeText(ShippingActivity.this, "Order Completed Successfully but " +
                                                                                            "failed to send notification!", Toast.LENGTH_SHORT).show();
                                                                                }

                                                                                if(!TextUtils.isEmpty(Paper.book().read(Common.TRIP_START)));
                                                                                    Paper.book().delete(Common.TRIP_START);

                                                                                EventBus.getDefault().postSticky(new UpdateShippingOrderEvent());
                                                                                finish();

                                                                            }, throwable -> {
                                                                                dialog.dismiss();
                                                                                Toast.makeText(ShippingActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }));
                                                                } else {
                                                                    dialog.dismiss();
                                                                    Toast.makeText(ShippingActivity.this, "Token not found!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                dialog.dismiss();
                                                                Toast.makeText(ShippingActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });


                                            });
                                });

                    });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    @OnClick(R.id.btn_call)
    void onCallClick() {
        Paper.init(this);
        String data = Paper.book().read(Common.SHIPPING_ORDER_DATA);

        shippingOrderModel = new Gson()
                .fromJson(data, new TypeToken<ShippingOrderModel>() {
                }.getType());

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(new StringBuilder("tel: ")
                                .append(shippingOrderModel.getOrderModel().getUserPhone()).toString()));
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ShippingActivity.this, "You must accept" + response.getPermissionName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @SuppressLint("MissingPermission")
    @OnClick(R.id.btn_start_trip)
    void onStartTripClicked() {
        String data = Paper.book().read(Common.SHIPPING_ORDER_DATA);
        Paper.book().write(Common.TRIP_START, data);
//        btn_start_trip.setEnabled(false);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    Map<String, Object> update_data = new HashMap<>();
                    update_data.put("currentLat", location.getLatitude());
                    update_data.put("currentLng", location.getLongitude());

                    FirebaseDatabase.getInstance()
                            .getReference(Common.SHIPPING_ORDER_REF)
                            .child(shippingOrderModel.getKey())
                            .updateChildren(update_data)
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(aVoid -> {
                                drawRoutes(data);
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(ShippingActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);

        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        ButterKnife.bind(this);
        buildLocationRequest();
        buildLocationCallback();


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

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ShippingActivity.this);
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

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

        if (!TextUtils.isEmpty(data)) {
            drawRoutes(data);
            shippingOrderModel = new Gson()
                    .fromJson(data, new TypeToken<ShippingOrderModel>() {
                    }.getType());

            if (shippingOrderModel != null) {
                Common.setSpanStringColor("Username: ", shippingOrderModel.getOrderModel().getUserName()
                        , txt_user_name, Color.parseColor("#333639"));

                Common.setSpanString("Delivery Date: ", shippingOrderModel.getOrderModel().getDeliveryDate()
                        , txt_delivery_date);

                Common.setSpanStringColor("No. : ", shippingOrderModel.getOrderModel().getKey()
                        , txt_order_number, Color.parseColor("#673ab7"));
                Common.setSpanStringColor("Address: ", shippingOrderModel.getOrderModel().getShippingAddress()
                        , txt_address, Color.parseColor("#795548"));


            } else {
                Toast.makeText(this, "Shipping order is null!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void drawRoutes(String data) {
        ShippingOrderModel shippingOrderModel = new Gson()
                .fromJson(data, new TypeToken<ShippingOrderModel>() {
                }.getType());

        //Add box
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.box))
                .title(shippingOrderModel.getOrderModel().getUserName())
                .snippet(shippingOrderModel.getOrderModel().getShippingAddress())
                .position(new LatLng(shippingOrderModel.getOrderModel().getLat(), shippingOrderModel.getOrderModel().getLng())));


        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Toast.makeText(ShippingActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {
                    String to = new StringBuilder()
                            .append(shippingOrderModel.getOrderModel().getLat())
                            .append(",")
                            .append(shippingOrderModel.getOrderModel().getLng())
                            .toString();

                    String from = new StringBuilder()
                            .append(location.getLatitude())
                            .append(",")
                            .append(location.getLongitude())
                            .toString();


                    compositeDisposable.add(iGoogleAPI.getDirections("driving"
                            , "less_driving",
                            from, to, getString(R.string.google_maps_key))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s -> {

                                try {
                                    //Parse json
                                    JSONObject jsonObject = new JSONObject(s);
                                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject route = jsonArray.getJSONObject(i);
                                        JSONObject poly = route.getJSONObject("overview_polyline");
                                        String polyLine = poly.getString("points");
                                        polylineList = Common.decodePoly(polyLine);

                                    }

                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(Color.RED);
                                    polylineOptions.width(12);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polylineList);
                                    redPolyline = mMap.addPolyline(polylineOptions);
                                } catch (Exception e) {
                                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }, throwable -> Toast.makeText(ShippingActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));

                    String uri = "http://maps.google.com/maps?saddr=" + location.getLatitude() + "," + location.getLongitude() +
                            "&daddr=" + shippingOrderModel.getOrderModel().getLat() + "," + shippingOrderModel.getOrderModel().getLng();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);


                });


    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // Add a marker in Sydney and move the camera
                LatLng locationDriver = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());

                if (driverMarker == null) {
                    // Inflate drawable
                    int height, width;
                    height = width = 80;
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat
                            .getDrawable(ShippingActivity.this, R.drawable.driver);
                    Bitmap resized = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height, false);

                    driverMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resized))
                            .position(locationDriver)
                            .title("You"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationDriver, 18));

                }

                if (isInit && prevLocation != null) {
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

                if (!isInit) {
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
                .subscribe(returnResult -> {

                    try {
                        //Parse json
                        JSONObject jsonObject = new JSONObject(returnResult);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
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
                            int percentValue = (int) valueAnimator.getAnimatedValue();
                            int size = points.size();
                            int newPoints = (int) (size * (percentValue / 100.0f));
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
                                if (index < polylineList.size() - 1) {
                                    index++;
                                    next = index + 1;
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
                                        lng = v * end.longitude + (1 - v)
                                                * start.longitude;
                                        lat = v * end.latitude + (1 - v)
                                                * start.latitude;
                                        LatLng newPos = new LatLng(lat, lng);
                                        marker.setPosition(newPos);
                                        marker.setAnchor(0.5f, 0.5f);
                                        marker.setRotation(Common.getBearing(start, newPos));

                                        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                                    }
                                });

                                valueAnimator.start();
                                if (index < polylineList.size() - 2)  // Reach destination
                                    handler.postDelayed(this, 1500);

                            }
                        }, 1500);


                    } catch (Exception e) {
                        Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }, throwable -> {
                    if (throwable != null)
                        Toast.makeText(ShippingActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
        setShippingOrder();
    }

    @Override
    protected void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onDestroy();

    }
}