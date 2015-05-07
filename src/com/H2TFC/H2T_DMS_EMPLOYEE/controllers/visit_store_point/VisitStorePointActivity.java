package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.visit_store_point;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import com.H2TFC.H2T_DMS_EMPLOYEE.MyApplication;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.PopupAdapter;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point.StoreDetailActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Area;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.GPSTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.ui.IconGenerator;
import com.parse.*;

import java.util.HashMap;
import java.util.List;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class VisitStorePointActivity extends Activity {
    private GoogleMap map;
    private HashMap<Marker, Store> myMapMarker;

    String currentStoreId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.visitStorePointActivityTitle));
        setContentView(R.layout.activity_visit_store_point);
        InitializeComponent();
        SetupMap();

        if(getIntent().hasExtra("EXTRAS_STORE_ID")) {
            currentStoreId = getIntent().getStringExtra("EXTRAS_STORE_ID");
        }

            DownloadUtils.DownloadParseStore(VisitStorePointActivity.this,new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    DrawStorePoint();
                }
            });

        DrawStorePoint();

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Store store = myMapMarker.get(marker);
                if(store.getObjectId().equals(currentStoreId)) {
                    Intent intent = new Intent(VisitStorePointActivity.this,StoreDetailActivity.class);
                    intent.putExtra("EXTRAS_STORE_ID", store.getObjectId());
                    intent.putExtra("EXTRAS_STORE_IMAGE_ID", store.getStoreImageId());
                    intent.putExtra("EXTRAS_VIENG_THAM", true);
                    //intent.putExtra("EXTRAS_READ_ONLY", true);

                    startActivityForResult(intent, MyApplication.REQUEST_EDIT);
                } else {
                    Intent intent = new Intent(VisitStorePointActivity.this,StoreDetailActivity.class);
                    intent.putExtra("EXTRAS_STORE_ID", store.getObjectId());
                    intent.putExtra("EXTRAS_STORE_IMAGE_ID", store.getStoreImageId());
                    startActivityForResult(intent, MyApplication.REQUEST_EDIT);
                }
            }
        });
    }

    public void DrawStorePoint() {
        // clear marker first
        for(Marker marker : myMapMarker.keySet()) {
            marker.remove();
        }

        // Get store point position
        ParseQuery<Store> storeParseQuery = Store.getQuery()
                                            .whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId())
                                            .whereEqualTo("status",Store.StoreStatus.BAN_HANG.name())
                                            .fromPin(DownloadUtils.PIN_STORE);
        storeParseQuery.findInBackground(new FindCallback<Store>() {
            @Override
            public void done(List<Store> list, ParseException e) {
                if(e == null) {
                    for(Store store : list) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                                        .position(new LatLng(store.getLocationPoint().getLatitude(),store
                                                                .getLocationPoint().getLongitude()))
                                                        .title(store.getStoreType() + " " + store.getName())
                                                        .snippet(store.getAddress() +
                                                                "\n" + getString(R.string.descriptionStoreOwner) + store.getStoreOwner());
                        IconGenerator iconGenerator = new IconGenerator(VisitStorePointActivity.this);
                        iconGenerator.setColor(Store.getStatusColor(Store.StoreStatus.valueOf(store
                                .getStatus())));

                        String displayName = store.getStoreType() + " " + store.getName();
                        if(store.getObjectId().equals(currentStoreId)) {
                            iconGenerator.setColor(Color.YELLOW);
                            displayName += " - " + getString(R.string.visiting);
                        }
                        Bitmap bitmap = iconGenerator.makeIcon(displayName);
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));

                        myMapMarker.put(map.addMarker(markerOptions), store);
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void InitializeComponent() {
        // Map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.activity_visit_store_point_map)).getMap();

        // Other
        myMapMarker = new HashMap<Marker, Store>();
    }


    public void SetupMap() {
        // Initialize The map
        try {
            MapsInitializer.initialize(VisitStorePointActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setting up the button that show on the map
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMyLocationEnabled(true);

        // Zooming camera to position user
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        GPSTracker gpsTracker = new GPSTracker(this);

        if (gpsTracker.canGetLocation())
        {
            Location location = gpsTracker.getLocation();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                                                                 // Sets the zoom
                        .build();                                                                 // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            gpsTracker.showSettingsAlert();
        }
        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        DrawStorePoint();
    }
}