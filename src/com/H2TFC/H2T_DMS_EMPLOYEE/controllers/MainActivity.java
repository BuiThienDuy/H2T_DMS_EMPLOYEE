package com.H2TFC.H2T_DMS_EMPLOYEE.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.visit_store_point.VisitStorePointDashboardActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class MainActivity extends Activity {
    public static final String LOGIN_KHAO_SAT = "EXTRAS_KHAOSAT";
    BootstrapButton btnViengTham,btnKhaoSat;

    LinearLayout llStatus;
    TextView tvLogout;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(getString(R.string.mainTitle));

        int state = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (state == ConnectionResult.SUCCESS) {

        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(state, this, -1);
            dialog.show();
        }

            DownloadUtils.DownloadParseStore(MainActivity.this, new SaveCallback() {
                @Override
                public void done(ParseException e) {

                }
            });
            DownloadUtils.DownloadParseStoreType(MainActivity.this, new SaveCallback() {
                @Override
                public void done(ParseException e) {

                }
            });

            DownloadUtils.DownloadParseProductPurchase(MainActivity.this, new SaveCallback() {
                @Override
                public void done(ParseException e) {

                }
            });


        btnViengTham = (BootstrapButton) findViewById(R.id.activity_main_btn_viengtham);
        btnKhaoSat = (BootstrapButton) findViewById(R.id.activity_main_btn_khaosat);
        tvLogout = (TextView) findViewById(R.id.main_tv_log_out);
        llStatus = (LinearLayout) findViewById(R.id.main_ll_status);

        if(ParseUser.getCurrentUser() == null) {
            llStatus.setVisibility(View.GONE);
        } else {
            llStatus.setVisibility(View.VISIBLE);
        }

        btnViengTham.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,VisitStorePointDashboardActivity.class);
                startActivity(intent);
            }
        });

        btnKhaoSat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.putExtra(LOGIN_KHAO_SAT,true);
                startActivity(intent);
            }
        });

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle(getString(R.string.logOut));
                dialog.setMessage(getString(R.string.confirmLogOut));

                dialog.setPositiveButton(getString(R.string.approve), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseUser.logOut();
                        if(ParseUser.getCurrentUser() == null) {
                            llStatus.setVisibility(View.GONE);
                        } else {
                            llStatus.setVisibility(View.VISIBLE);
                        }
                    }
                });
                dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ParseUser.getCurrentUser() == null) {
            llStatus.setVisibility(View.GONE);
        } else {
            llStatus.setVisibility(View.VISIBLE);
        }
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
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
