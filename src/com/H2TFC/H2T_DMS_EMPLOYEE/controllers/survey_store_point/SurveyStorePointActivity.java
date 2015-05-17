package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.MyApplication;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.MainActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Area;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.StoreImage;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.CustomPushUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.GPSTracker;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.mobisys.android.autocompletetextviewcomponent.ClearableAutoTextView;
import com.mobisys.android.autocompletetextviewcomponent.SelectionListener;
import com.parse.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.H2TFC.H2T_DMS_EMPLOYEE.utils.SizeUtils.distance;


/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class SurveyStorePointActivity extends Activity {
    private GoogleMap map;

    HashMap<Marker, Store> myMapMarker;

    Marker currentMarker;
    ImageView ivCrosshair;

    ClearableAutoTextView tvSearchMap;

    HashMap<Polygon, Area> mapPolygon;
    HashMap<Polyline, Area> mapPolyline;
    Area currentAreaSelected;

    private long pressStartTime;
    private float pressedX;
    private float pressedY;

    /**
     * Max allowed duration for a "click", in milliseconds.
     */
    private static final int MAX_CLICK_DURATION = 150;

    /**
     * Max allowed distance to move during a "click", in DP.
     */
    private static final int MAX_CLICK_DISTANCE = 15;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.surveyStorePointTitle));

            setContentView(R.layout.activity_khaosat);
            InitializeComponent();
            SetupMap();
            SetupEvent();

                DownloadUtils.DownloadParseArea(SurveyStorePointActivity.this,new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        DrawAreaCurrentManage();
                    }
                });
                DownloadUtils.DownloadParseStore(SurveyStorePointActivity.this,new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        DrawStorePoint();
                    }
                });
                DownloadUtils.DownloadParseStoreImage(SurveyStorePointActivity.this,new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                    }
                });
                DownloadUtils.DownloadParseStoreType(SurveyStorePointActivity.this,new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                    }
                });
                DrawStorePoint();
                DrawAreaCurrentManage();



    }

    public boolean isGoogleMapsInstalled()
    {
        try
        {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
            return true;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Unpinning the photo draft file the have cache from previous working session
        ParseQuery<StoreImage> query = StoreImage.getQuery();
        query.whereEqualTo("photo_synched", true);
        query.fromPin("PIN_DRAFT_PHOTO");
        query.findInBackground(new FindCallback<StoreImage>() {
            @Override
            public void done(List<StoreImage> list, ParseException e) {
                for (StoreImage storeImage : list) {
                    storeImage.unpinInBackground("PIN_DRAFT_PHOTO");
                }
            }
        });
    }

    public void InitializeComponent() {
        // Map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.activity_khaosat_map)).getMap();
        // Textview
        tvSearchMap = (ClearableAutoTextView) findViewById(R.id.activity_khaosat_tv_search);

        // Image view
        ivCrosshair = (ImageView) findViewById(R.id.activity_khaosat_iv_crosshair);

        // Other
        myMapMarker = new HashMap<Marker, Store>();
        mapPolygon = new HashMap<Polygon, Area>();
        mapPolyline = new HashMap<Polyline, Area>();
    }

    public void SetupMap() {
        // Initialize The map
        try {
            MapsInitializer.initialize(SurveyStorePointActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Zooming camera to position user
            GPSTracker gpsTracker = new GPSTracker(this);

            if (gpsTracker.canGetLocation() && map != null) {
                Location location = gpsTracker.getLocation();


                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                                                                 // Sets the zoom
                        .build();                                                                 // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // Setting up the button that show on the map
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
                map.setMyLocationEnabled(true);
            } else {
                gpsTracker.showSettingsAlert();
            }

    }

    public void SetupEvent() {
        // Crosshair that help employee add new survey store point
        ivCrosshair.setOnTouchListener(new View.OnTouchListener() {
            public float offsetX;
            public float offsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int theAction = event.getAction();
                switch (theAction) {
                    case MotionEvent.ACTION_DOWN:
                        // Button down
                        offsetX = ivCrosshair.getX() - event.getRawX();
                        offsetY = ivCrosshair.getY() - event.getRawY();

                        pressStartTime = System.currentTimeMillis();
                        pressedX = event.getX();
                        pressedY = event.getY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Button moved
                        float newX = event.getRawX() + offsetX;
                        float newY = event.getRawY() + offsetY;
                        v.setX(newX);
                        v.setY(newY);

                        break;
                    case MotionEvent.ACTION_UP:
                        long pressDuration = System.currentTimeMillis() - pressStartTime;
                        if (pressDuration < MAX_CLICK_DURATION && distance(getApplicationContext(), pressedX, pressedY, event.getX(),
                                event.getY()) < MAX_CLICK_DISTANCE) {
                            // Click event has occurred
                            Projection projection = map.getProjection();
                            int centerX = (int) (ivCrosshair.getX() + ivCrosshair.getWidth() / 2);
                            int centerY = (int) (ivCrosshair.getY() + ivCrosshair.getHeight() / 2);
                            LatLng centerImagePoint = projection.fromScreenLocation(new Point(centerX, centerY));
                            boolean isMarkerAllowed = false;
                            // Check if touch on Polygon
                            for (Polygon polygon : mapPolygon.keySet()) {
                                if (pointInPolygon(centerImagePoint, polygon)) {
                                    // get current area selected
                                    currentAreaSelected = mapPolygon.get(polygon);
                                    polygon.setStrokeColor(Color.BLUE);
                                    polygon.setStrokeWidth(4);
                                    isMarkerAllowed = true;
                                }else {
                                    polygon.setStrokeColor(Area.getStatusColor(Area.AreaStatus.valueOf(mapPolygon
                                            .get(polygon)
                                            .getStatus())));

                                    polygon.setStrokeWidth(2);
                                }
                            }
                            // Check if touch on Polyline
                            for (Polyline polyline : mapPolyline.keySet()) {
                                if (PolyUtil.isLocationOnPath(centerImagePoint,polyline.getPoints(),true,10)) {
                                    // get current area selected
                                    currentAreaSelected = mapPolyline.get(polyline);
                                    polyline.setColor(Color.BLUE);
                                    isMarkerAllowed = true;
                                }else {
                                    polyline.setColor(Area.getStatusColor(Area.AreaStatus.valueOf(mapPolyline
                                            .get(polyline)
                                            .getStatus())));
                                    polyline.setWidth(10);
                                }
                            }
                            // Button up
                            if (isMarkerAllowed) {
                                if (currentMarker != null) {
                                    currentMarker.remove();
                                }
                                MarkerOptions markerOptions = new MarkerOptions();

                                markerOptions.position(centerImagePoint);
                                markerOptions.title(centerImagePoint.toString());
                                markerOptions.draggable(true);
                                currentMarker = map.addMarker(markerOptions);
                            } else {
                                if (currentMarker != null) {
                                    currentMarker.remove();
                                }
                                currentAreaSelected = null;
                                Toast.makeText(SurveyStorePointActivity.this, getString(R.string
                                        .errorStorePointOutOfPolygon), Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                final Store store = myMapMarker.get(marker);
                if (store.getStatus().equals(Store.StoreStatus.TIEM_NANG.name())) {
                    marker.setSnippet(getString(R.string.clickToChangeToStorePoint));
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SurveyStorePointActivity.this);
                    alertDialog.setMessage(getString(R.string.confirmChangeToStorePoint));
                    alertDialog.setTitle(getString(R.string.confirmChangeStoreStatusTitle));
                    alertDialog.setPositiveButton(getString(R.string.approve), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            store.setStatus(Store.StoreStatus.BAN_HANG.name());
                            store.saveEventually();
                            store.pinInBackground(DownloadUtils.PIN_STORE, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    // Send push notification to NVQL
                                    String employeeName = (String) ParseUser.getCurrentUser().get("name");
                                    CustomPushUtils.sendMessageToEmployee(ParseUser.getCurrentUser().getString("manager_id"),
                                            employeeName + getString(R.string.signContractWith) + store.getName() +
                                                    getString(R.string.success));
                                    DrawStorePoint();
                                }
                            });
                        }
                    });
                    alertDialog.setNeutralButton(getString(R.string.signContractFailed), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            store.setStatus(Store.StoreStatus.CHO_CAP_TREN.name());
                            store.saveEventually();
                            store.pinInBackground(DownloadUtils.PIN_STORE, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    // Send push notification to NVQL
                                    String employeeName = (String) ParseUser.getCurrentUser().get("name");
                                    CustomPushUtils.sendMessageToEmployee(ParseUser.getCurrentUser().getString("manager_id"),
                                            employeeName + getString(R.string.signContractWith) + store.getName() + getString(R.string.failed));
                                    DrawStorePoint();
                                }
                            });
                        }
                    });
                    alertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                } else if (store.getStatus().equals(Store.StoreStatus.BAN_HANG.name())) {
                    Intent intent = new Intent(SurveyStorePointActivity.this, StoreDetailActivity.class);
                    intent.putExtra("EXTRAS_STORE_ID", store.getObjectId());
                    intent.putExtra("EXTRAS_STORE_IMAGE_ID", store.getStoreImageId());
                    intent.putExtra("EXTRAS_READ_ONLY", true);

                    startActivityForResult(intent, MyApplication.REQUEST_EDIT);
                } else {
                    Intent intent = new Intent(SurveyStorePointActivity.this, StoreDetailActivity.class);
                    intent.putExtra("EXTRAS_STORE_ID", store.getObjectId());
                    intent.putExtra("EXTRAS_STORE_IMAGE_ID", store.getStoreImageId());
                    startActivityForResult(intent,  MyApplication.REQUEST_ADD_NEW);
                }
            }
        });

        tvSearchMap.setSelectionListener(new SelectionListener() {
            @Override
            public void onItemSelection(ClearableAutoTextView.DisplayStringInterface selectedItem) {

            }

            @Override
            public void onReceiveLocationInformation(double lat, double lng) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lat, lng), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(lat, lng))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                                //.bearing(90)                // Sets the orientation of the camera to east
                                //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    public boolean pointInPolygon(LatLng point, Polygon polygon) {
        // ray casting alogrithm http://rosettacode.org/wiki/Ray-casting_algorithm
        int crossings = 0;
        List<LatLng> path = polygon.getPoints();
        path.remove(path.size()-1); //remove the last point that is added automatically by getPoints()

        // for each edge
        for (int i=0; i < path.size(); i++) {
            LatLng a = path.get(i);
            int j = i + 1;
            //to close the last edge, you have to take the first point of your polygon
            if (j >= path.size()) {
                j = 0;
            }
            LatLng b = path.get(j);
            if (rayCrossesSegment(point, a, b)) {
                crossings++;
            }
        }

        // odd number of crossings?
        return (crossings % 2 == 1);
    }

    public boolean rayCrossesSegment(LatLng point, LatLng a,LatLng b) {
        // Ray Casting algorithm checks, for each segment, if the point is 1) to the left of the segment and 2) not above nor below the segment. If these two conditions are met, it returns true
        double px = point.longitude,
                py = point.latitude,
                ax = a.longitude,
                ay = a.latitude,
                bx = b.longitude,
                by = b.latitude;
        if (ay > by) {
            ax = b.longitude;
            ay = b.latitude;
            bx = a.longitude;
            by = a.latitude;
        }
        // alter longitude to cater for 180 degree crossings
        if (px < 0 || ax <0 || bx <0) { px += 360; ax+=360; bx+=360; }
        // if the point has the same latitude as a or b, increase slightly py
        if (py == ay || py == by) py += 0.00000001;


        // if the point is above, below or to the right of the segment, it returns false
        if ((py > by || py < ay) || (px > Math.max(ax, bx))){
            return false;
        }
        // if the point is not above, below or to the right and is to the left, return true
        else if (px < Math.min(ax, bx)){
            return true;
        }
        // if the two above conditions are not met, you have to compare the slope of segment [a,b] (the red one here) and segment [a,p] (the blue one here) to see if your point is to the left of segment [a,b] or not
        else {
            double red = (ax != bx) ? ((by - ay) / (bx - ax)) : Double.POSITIVE_INFINITY;
            double blue = (ax != px) ? ((py - ay) / (px - ax)) : Double.POSITIVE_INFINITY;
            return (blue >= red);
        }

    }

    public void DrawAreaCurrentManage() {
        // Clear all polygon
        for(Polygon polygon : mapPolygon.keySet()) {
            polygon.remove();
        }
        for(Polyline polyline : mapPolyline.keySet()) {
            polyline.remove();
        }

        // Query and draw polygon
        ParseQuery<Area> areaQuery = Area.getQuery();
        areaQuery.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
        areaQuery.fromPin(DownloadUtils.PIN_AREA);
        areaQuery.findInBackground(new FindCallback<Area>() {
            @Override
            public void done(List<Area> list, ParseException e) {
                for (Area employeeArea : list) {
                    // get list of Parse geopoint then make a polygon
                    ArrayList<ParseGeoPoint> listGeoPoint = (ArrayList<ParseGeoPoint>) employeeArea.getNodeList();
                    if(listGeoPoint.size() > 0) {
                        if(listGeoPoint.size() > 2) {
                            PolygonOptions polygonOptions = new PolygonOptions();
                            for (ParseGeoPoint geoPoint : listGeoPoint) {
                                polygonOptions.add(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                            }

                            // polygon setting
                            polygonOptions.fillColor(Area.getStatusColor(Area.AreaStatus.valueOf(employeeArea
                                    .getStatus())));


                            polygonOptions.strokeColor(Area.getStatusColor(Area.AreaStatus.valueOf(employeeArea
                                    .getStatus())));

                            polygonOptions.strokeWidth(2);
                            mapPolygon.put(map.addPolygon(polygonOptions), employeeArea);
                        } else {
                            PolylineOptions polylineOptions = new PolylineOptions();
                            for (ParseGeoPoint geoPoint : listGeoPoint) {
                                polylineOptions.add(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                            }

                            // polygon setting
                            polylineOptions.color(Area.getStatusColor(Area.AreaStatus.valueOf(employeeArea
                                    .getStatus())));
                            polylineOptions.width(10);

                            mapPolyline.put(map.addPolyline(polylineOptions), employeeArea);
                        }
                    }
                }
            }
        });
    }

    public void DrawStorePoint() {
        // clear map marker
        for(Marker marker : myMapMarker.keySet()) {
            marker.remove();
        }

        ParseQuery<Store> queryStore = Store.getQuery();
        queryStore.whereEqualTo("employee_id",ParseUser.getCurrentUser().getObjectId());
        queryStore.fromPin(DownloadUtils.PIN_STORE);
        queryStore.findInBackground(new FindCallback<Store>() {
            @Override
            public void done(List<Store> list, ParseException e) {
                for(final Store store : list) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(store.getLocationPoint().getLatitude(), store.getLocationPoint
                            ().getLongitude()));
                    markerOptions.title(getString(R.string.cua_hang) + store.getName());
                    markerOptions.snippet(getString(R.string.nhan_de_xem_chi_tiet_cua_hang));
                    IconGenerator iconGenerator = new IconGenerator(SurveyStorePointActivity.this);
                    iconGenerator.setColor(Store.getStatusColor(Store.StoreStatus.valueOf(store
                            .getStatus())));
                    if(store.getStatus().equals(Store.StoreStatus.TIEM_NANG.name()) ||
                            store.getStatus().equals(Store.StoreStatus.KHONG_DU_TIEU_CHUAN.name()) ||
                            store.getStatus().equals(Store.StoreStatus.CHO_CAP_TREN.name())){
                        iconGenerator.setTextAppearance(R.style.iconGenText_WHITE);
                    }
                    Bitmap bitmap = iconGenerator.makeIcon((String) ParseUser.getCurrentUser().get("name"));
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));

                    Marker marker = map.addMarker(markerOptions);
                    myMapMarker.put(marker, store);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        DrawStorePoint();
        DrawAreaCurrentManage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_survey_store_point, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(SurveyStorePointActivity.this);
                confirmDialog.setMessage(getString(R.string.areYouSureYouWantToEndSession));
                confirmDialog.setPositiveButton(getString(R.string.approve), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(SurveyStorePointActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
                confirmDialog.setNegativeButton(getString(R.string.cancel),null);

                confirmDialog.show();
                break;
            }

            case R.id.action_bar_survey_store_point_add: {
                if(currentMarker != null) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SurveyStorePointActivity.this);
                    alertDialog.setMessage(getString(R.string.ban_co_chac_muon_tao_diem_khao_sat));
                    alertDialog.setPositiveButton(getString(R.string.xac_nhan), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SurveyStorePointActivity.this,StoreNewActivity.class);
                            intent.putExtra("Lat",currentMarker.getPosition().latitude);
                            intent.putExtra("Lng",currentMarker.getPosition().longitude);
                            startActivityForResult(intent, MyApplication.REQUEST_ADD_NEW);
                        }
                    });

                    alertDialog.setNegativeButton(getString(R.string.huy_bo), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                } else {
                    Toast.makeText(SurveyStorePointActivity.this,getString(R.string.vui_long_tao_marker),Toast.LENGTH_LONG).show();
                }
                break;
            }

            case R.id.action_bar_survey_store_point_update_area: {
                if(currentAreaSelected != null) {
                    final CharSequence[] items = {
                            getString(R.string.areaOnCreated), getString(R.string.areaOnSurvey), getString(R.string
                            .areaOnSuccess)
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.updateAreaStatus));
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            // Do something with the selection
                            switch(item) {
                                case 0: {
                                    currentAreaSelected.setStatus(Area.AreaStatus.MOI_TAO.name());
                                    currentAreaSelected.pinInBackground(DownloadUtils.PIN_AREA, new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(currentAreaSelected.getNodeList().size() == 2) {
                                                Polyline currentPolyline = getKeyByValue(mapPolyline,
                                                        currentAreaSelected);
                                                currentPolyline.setColor(Area.getStatusColor(Area.AreaStatus
                                                        .MOI_TAO));
                                            } else {
                                                Polygon currentPolygon = getKeyByValue(mapPolygon, currentAreaSelected);
                                                currentPolygon.setFillColor(Area.getStatusColor(Area.AreaStatus.MOI_TAO));
                                            }
                                            Toast.makeText(SurveyStorePointActivity.this,getString(R.string
                                                    .updateAreaStatusSuccess),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    currentAreaSelected.saveEventually();
                                    break;
                                }
                                case 1: {
                                    currentAreaSelected.setStatus(Area.AreaStatus.DANG_KHAO_SAT.name());
                                    currentAreaSelected.pinInBackground(DownloadUtils.PIN_AREA, new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(currentAreaSelected.getNodeList().size() == 2) {
                                                Polyline currentPolyline = getKeyByValue(mapPolyline,
                                                        currentAreaSelected);
                                                currentPolyline.setColor(Area.getStatusColor(Area.AreaStatus.DANG_KHAO_SAT));
                                            } else {
                                                Polygon currentPolygon = getKeyByValue(mapPolygon, currentAreaSelected);
                                                currentPolygon.setFillColor(Area.getStatusColor(Area.AreaStatus.DANG_KHAO_SAT));
                                            }
                                            Toast.makeText(SurveyStorePointActivity.this, getString(R.string
                                                    .updateAreaStatusSuccess), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    currentAreaSelected.saveEventually();
                                    break;
                                }
                                case 2: {
                                    currentAreaSelected.setStatus(Area.AreaStatus.HOAN_THANH.name());
                                    currentAreaSelected.pinInBackground(DownloadUtils.PIN_AREA, new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(currentAreaSelected.getNodeList().size() == 2) {
                                                Polyline currentPolyline = getKeyByValue(mapPolyline,
                                                        currentAreaSelected);
                                                currentPolyline.setColor(Area.getStatusColor(Area.AreaStatus.HOAN_THANH));
                                            } else {
                                                Polygon currentPolygon = getKeyByValue(mapPolygon, currentAreaSelected);
                                                currentPolygon.setFillColor(Area.getStatusColor(Area.AreaStatus.HOAN_THANH));
                                            }
                                            Toast.makeText(SurveyStorePointActivity.this,getString(R.string
                                                    .updateAreaStatusSuccess),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    currentAreaSelected.saveEventually();
                                    break;
                                }
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                } else{
                    Toast.makeText(SurveyStorePointActivity.this,getString(R.string.errorStreetOrAreaNotFoundForUpdate),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.action_bar_survey_store_point_log_out: {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(SurveyStorePointActivity.this);
                confirmDialog.setMessage(getString(R.string.confirmLogOut));
                confirmDialog.setPositiveButton(getString(R.string.approve), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseUser.logOut();
                        Intent intent = new Intent(SurveyStorePointActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
                confirmDialog.setNegativeButton(getString(R.string.cancel),null);

                confirmDialog.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private LatLng getPolygonCenterPoint(ArrayList<LatLng> polygonPointsList){
        LatLng centerLatLng = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng aPolygonPointsList : polygonPointsList) {
            builder.include(aPolygonPointsList);
        }
        LatLngBounds bounds = builder.build();
        centerLatLng =  bounds.getCenter();

        return centerLatLng;
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.confirmExit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }


}