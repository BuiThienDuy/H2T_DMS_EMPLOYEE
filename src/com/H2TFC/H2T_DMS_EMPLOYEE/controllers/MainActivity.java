package com.H2TFC.H2T_DMS_EMPLOYEE.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.parse.ParseException;
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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(ConnectUtils.hasConnectToInternet(MainActivity.this)) {
            DownloadUtils.DownloadParseStore(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                }
            });
            DownloadUtils.DownloadParseStoreType(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                }
            });

            DownloadUtils.DownloadParseProductPurchase(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                }
            });
        }

        btnViengTham = (BootstrapButton) findViewById(R.id.activity_main_btn_viengtham);
        btnKhaoSat = (BootstrapButton) findViewById(R.id.activity_main_btn_khaosat);

        btnViengTham.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.putExtra(LOGIN_KHAO_SAT,false);
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
