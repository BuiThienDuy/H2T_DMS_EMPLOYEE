package com.H2TFC.H2T_DMS_EMPLOYEE.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.MyApplication;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point.SurveyStorePointActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.visit_store_point.VisitStorePointActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Attendance;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.GPSTracker;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ImageUtils;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.parse.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class LoginActivity extends Activity {
    boolean isKhaoSat = false, hasImage = false;
    BootstrapButton btnLogin;
    BootstrapEditText etUsername, etPassword;
    String employeeStoreId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.signIn));

        DownloadUtils.DownloadParseEmployee(LoginActivity.this, new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });


        InitializeComponent();

        if (getIntent().hasExtra(MainActivity.LOGIN_KHAO_SAT)) {
            isKhaoSat = getIntent().getBooleanExtra(MainActivity.LOGIN_KHAO_SAT, false);
        } else {
            //finish();
        }

        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().get("role_name").equals("NVKD")) {
            if (isKhaoSat) {
                Intent intent = new Intent(LoginActivity.this, SurveyStorePointActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

        if (!isKhaoSat && !hasImage) {
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
                if (username.trim().equals("")) {
                    error_exist = true;
                    error_msg.append(getString(R.string.errorUsername));
                }

                // blank password
                if (password.trim().equals("")) {
                    if (error_exist) {
                        error_msg.append(getString(R.string.errorJoin));
                    }
                    error_exist = true;
                    error_msg.append(getString(R.string.errorPassword));
                }

                if (error_exist) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, error_msg.toString(), Toast.LENGTH_LONG).show();
                } else if (!ConnectUtils.hasConnectToInternet(LoginActivity.this)) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, getString(R.string.needInternetAccessToLogin), Toast.LENGTH_LONG)
                            .show();
                } else {
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
                                        intent = new Intent(LoginActivity.this, VisitStorePointActivity.class);
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
                                Toast.makeText(LoginActivity.this, getString(R.string.userOrPasswordIncorrect), Toast
                                        .LENGTH_LONG).show();
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
        if (requestCode == MyApplication.REQUEST_TAKE_PHOTO) {
            isKhaoSat = false;
            hasImage = true;
            if (data == null || !data.hasExtra("data")) {
                finish();
            } else {
                // Save the image to external store card
                Bitmap bm = (Bitmap) data.getExtras().get("data");
                String uuid = UUID.randomUUID().toString();
                ImageUtils.SaveImage(bm, uuid);


                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bitmapdata = stream.toByteArray();

                // check if GPS enabled
                GPSTracker gpsTracker = new GPSTracker(this);

                if (gpsTracker.canGetLocation()) {
                    final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setTitle(getString(R.string.pleaseWaitTitle));
                    progressDialog.setMessage(getString(R.string.loadingPleaseWait));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    double Latitude = gpsTracker.getLatitude();
                    double Longitude = gpsTracker.getLongitude();
                    final Attendance attendance = new Attendance();

                    attendance.setLocation(new ParseGeoPoint(Latitude, Longitude));
                    attendance.setPhotoTitle(uuid);
                    attendance.setPhotoSynched(false);

                    if (ParseUser.getCurrentUser() != null) {
                        attendance.setEmployeeId(ParseUser.getCurrentUser().getObjectId());
                        attendance.setManagerId(ParseUser.getCurrentUser().getString("manager_id"));
                    }

                    ParseQuery<Store> storeParseQuery = Store.getQuery();
                    if (ParseUser.getCurrentUser() != null) {
                        storeParseQuery.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
                    }
                    storeParseQuery.whereEqualTo("status", Store.StoreStatus.BAN_HANG.name());
                    storeParseQuery.whereWithinKilometers("location_point", attendance.getLocation(), 0.3); // 300 met
                    storeParseQuery.fromPin(DownloadUtils.PIN_STORE);

                    storeParseQuery.findInBackground(new FindCallback<Store>() {
                        @Override
                        public void done(List<Store> list, ParseException e) {
                            if (e == null) {
                                //
                                if (list.size() == 1) {
                                    attendance.setStoreId(list.get(0).getObjectId());
                                    employeeStoreId = list.get(0).getObjectId();
                                    attendance.pinInBackground(DownloadUtils.PIN_ATTENDANCE + "_DRAFT", new SaveCallback() {
                                        @Override
                                        public void done(ParseException ex) {
                                            if (ex == null) {
                                                progressDialog.dismiss();
                                                if (ParseUser.getCurrentUser() != null) {
                                                    Intent intent = new Intent(LoginActivity.this, VisitStorePointActivity.class);
                                                    intent.putExtra("EXTRAS_STORE_ID", employeeStoreId);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }

                                            } else {
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                                    return;
                                }
                                //
                                if (list.size() > 1) {
                                    final CharSequence[] items = new CharSequence[list.size()];
                                    final CharSequence[] items_id = new CharSequence[list.size()];
                                    for (int i = 0; i < items.length; i++) {
                                        items[i] = list.get(i).getName() + " - " + list.get(i).getAddress();
                                        items_id[i] = list.get(i).getObjectId();
                                    }

                                    progressDialog.dismiss();

                                    AlertDialog.Builder builder = new AlertDialog.Builder
                                            (LoginActivity.this);
                                    builder.setTitle(getString(R.string.pleaseSelectAtLeastOneStoreBelow));
                                    builder.setItems(items, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int item) {
                                            // Do something with the selection
                                            String storeIdSelected = items_id[item].toString();
                                            attendance.setStoreId(storeIdSelected);
                                            employeeStoreId = storeIdSelected;
                                            attendance.pinInBackground(DownloadUtils.PIN_ATTENDANCE + "_DRAFT", new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        progressDialog.dismiss();
                                                        if (ParseUser.getCurrentUser() != null) {
                                                            Intent intent = new Intent(LoginActivity.this, VisitStorePointActivity.class);
                                                            intent.putExtra("EXTRAS_STORE_ID", employeeStoreId);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                        }

                                                    } else {
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                    return;
                                }
                                //
                                if (list.size() == 0) {
                                    progressDialog.dismiss();
                                    AlertDialog.Builder dialog = new AlertDialog.Builder
                                            (LoginActivity.this);

                                    dialog.setTitle(getString(R.string.errorThereAreNoStoreNear));
                                    dialog.setCancelable(false);
                                    dialog.setPositiveButton(getString(R.string.approve), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ParseObject.unpinAllInBackground(DownloadUtils
                                                    .PIN_ATTENDANCE + "_DRAFT", new DeleteCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    finish();
                                                }
                                            });

                                        }
                                    });
                                    dialog.show();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "1111." + e.getMessage(), Toast
                                        .LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });


                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    finish();
                    gpsTracker.showSettingsAlert();
                }

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