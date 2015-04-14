package com.H2TFC.H2T_DMS_EMPLOYEE.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.MyApplication;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.visit_store_point.VisitStorePointDashboardActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Attendance;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.GPSTracker;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ImageUtils;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point.SurveyStorePointActivity;
import com.parse.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class LoginActivity extends Activity {
    boolean isKhaoSat = false;
    BootstrapButton btnLogin;
    BootstrapEditText etUsername,etPassword;
    String employeeStoreId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.signIn));

        if(ConnectUtils.hasConnectToInternet(LoginActivity.this)) {
            DownloadUtils.DownloadParseEmployee(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                }
            });
        }

        InitializeComponent();

        if(getIntent().hasExtra(MainActivity.LOGIN_KHAO_SAT)) {
            isKhaoSat = getIntent().getBooleanExtra(MainActivity.LOGIN_KHAO_SAT,false);
        } else {
            //finish();
        }

        if(ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().get("role_name").equals("NVKD")) {
            if(isKhaoSat) {
                Intent intent = new Intent(LoginActivity.this, SurveyStorePointActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

        if(!isKhaoSat) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, MyApplication.REQUEST_TAKE_PHOTO);
                } catch (ActivityNotFoundException ex) {
                    //display an error message
                    String errorMessage = getString(R.string.yourDeviceDoesNotSupportCamera);
                    Toast toast = Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }

        }

        SetupEvent();


    }

    private void SetupEvent() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage(getString(R.string.pleaseWait));
                progressDialog.show();

                boolean error_exist = false;
                StringBuilder error_msg = new StringBuilder(getString(R.string.errorPrefix));

                // blank username
                if(username.trim().equals("")) {
                    error_exist = true;
                    error_msg.append(getString(R.string.errorUsername));
                }

                // blank password
                if(password.trim().equals("")) {
                    if(error_exist) {
                        error_msg.append(getString(R.string.errorJoin));
                    }
                    error_exist = true;
                    error_msg.append(getString(R.string.errorPassword));
                }

                if(error_exist) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, error_msg.toString(), Toast.LENGTH_LONG).show();
                }else if(!ConnectUtils.hasConnectToInternet(LoginActivity.this)) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, getString(R.string.needInternetAccessToLogin), Toast.LENGTH_LONG)
                            .show();
                }else {
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (e == null) {
                                if (parseUser.get("role_name").toString().equals("NVKD")) {
                                    ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
                                    parseInstallation.put("username", parseUser.getUsername());
                                    parseInstallation.put("userId", parseUser.getObjectId());
                                    parseInstallation.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {

                                            } else {
                                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                    progressDialog.dismiss();
                                    Intent intent;
                                    if (isKhaoSat) {
                                        intent = new Intent(LoginActivity.this, SurveyStorePointActivity.class);
                                    } else {
                                        intent = new Intent(LoginActivity.this, VisitStorePointDashboardActivity.class);
                                        intent.putExtra("EXTRAS_STORE_ID", employeeStoreId);
                                    }
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, getString(R.string.errorSignIn), Toast
                                            .LENGTH_LONG)
                                            .show();
                                }
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void InitializeComponent() {
        btnLogin = (BootstrapButton) findViewById(R.id.activity_login_btn_dangnhap);
        etUsername = (BootstrapEditText) findViewById(R.id.activity_login_et_tendangnhap);
        etPassword = (BootstrapEditText) findViewById(R.id.activity_login_et_matkhau);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MyApplication.REQUEST_TAKE_PHOTO) {
            isKhaoSat = false;
            try {
                if (data.getExtras().get("data") == null) {
                    finish();
                } else {
                    Bitmap bm = (Bitmap) data.getExtras().get("data");

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bitmapdata = stream.toByteArray();

                    // check if GPS enabled
                    GPSTracker gpsTracker = new GPSTracker(this);

                    if (gpsTracker.canGetLocation())
                    {
                        double Latitude = gpsTracker.getLatitude();
                        double Longitude = gpsTracker.getLongitude();
                        final Attendance attendance = new Attendance();
                        attendance.setLocation(new ParseGeoPoint(Latitude,Longitude));

                        final ParseFile photo = new ParseFile("parse_photo.png", bitmapdata);
                        photo.save();
                        attendance.setPhoto(photo);

                        if(ParseUser.getCurrentUser() != null) {
                            attendance.setEmployeeId(ParseUser.getCurrentUser().getObjectId());
                        }

                        // Get store id
                        ParseQuery<Store> storeParseQuery = Store.getQuery();
                        if(ParseUser.getCurrentUser() != null) {
                            storeParseQuery.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
                        }
                        storeParseQuery.whereEqualTo("status",Store.StoreStatus.BAN_HANG.name());
                        storeParseQuery.whereWithinKilometers("location_point", attendance.getLocation(), 0.3); // 300 met
                        storeParseQuery.fromPin(DownloadUtils.PIN_STORE);
                        try {
                            List<Store> list = storeParseQuery.find();
                            if(list.size() == 1) {
                                attendance.setStoreId(list.get(0).getObjectId());
                                employeeStoreId = list.get(0).getObjectId();
                                attendance.pinInBackground(DownloadUtils.PIN_ATTENDANCE + "_DRAFT", new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null && ParseUser.getCurrentUser()!= null) {
                                            Intent intent = new Intent(LoginActivity.this, VisitStorePointDashboardActivity.class);
                                            intent.putExtra("EXTRAS_STORE_ID", employeeStoreId);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            } else {
                                if(list.size() > 1) {
                                    final CharSequence[] items = new CharSequence[list.size()];
                                    final CharSequence[] items_id = new CharSequence[list.size()];
                                    for(int i = 0 ; i < items.length ; i++) {
                                        items[i] = list.get(i).getName() + " - " + list.get(i).getAddress();
                                        items_id[i] = list.get(i).getObjectId();
                                    }

                                    AlertDialog.Builder builder = new AlertDialog.Builder
                                            (LoginActivity.this);
                                    builder.setTitle(getString(R.string.pleaseSelectAtLeastOneStoreBelow));
                                    builder.setItems(items, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int item) {
                                            // Do something with the selection
                                            String storeIdSelected = items[item].toString();
                                            attendance.setStoreId(storeIdSelected);
                                            employeeStoreId = storeIdSelected;
                                            attendance.pinInBackground(DownloadUtils.PIN_ATTENDANCE + "_DRAFT", new
                                                    SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if(e == null && ParseUser.getCurrentUser()!= null) {
                                                        Intent intent = new Intent(LoginActivity.this, VisitStorePointDashboardActivity.class);
                                                        intent.putExtra("EXTRAS_STORE_ID", employeeStoreId);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                } else {
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                                    dialog.setTitle(getString(R.string.errorThereAreNoStoreNear));
                                    dialog.setCancelable(false);
                                    dialog.setPositiveButton(getString(R.string.approve), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    });
                                    dialog.show();
                                }
                            }
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    else
                    {
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings
                        gpsTracker.showSettingsAlert();
                    }

                }
            } catch(Exception ex) {
                ex.printStackTrace();
                finish();
            }
        }
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
}