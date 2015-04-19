package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.visit_store_point;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.MainActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice.InvoiceManagementActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Attendance;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.maps.android.geometry.Point;
import com.parse.*;

import java.util.List;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class VisitStorePointDashboardActivity extends Activity {
    Button btnInvoiceManagement,btnVisitStore;
    String employeeStoreId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viengtham_dashboard);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.visitStorePointTitle));

        btnInvoiceManagement = (Button) findViewById(R.id.activity_viengtham_dashboard_btn_invoice_management);
        btnVisitStore = (Button) findViewById(R.id.dashboard_btn_visit_store);
        btnInvoiceManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VisitStorePointDashboardActivity.this, InvoiceManagementActivity.class);
                intent.putExtra("EXTRAS_STORE_ID", employeeStoreId);
                startActivity(intent);
            }
        });

        if(getIntent().hasExtra("EXTRAS_STORE_ID")) {
            employeeStoreId = getIntent().getStringExtra("EXTRAS_STORE_ID");
        }
        btnVisitStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VisitStorePointDashboardActivity.this, VisitStorePointActivity.class);
                intent.putExtra("EXTRAS_STORE_ID", employeeStoreId);
                startActivity(intent);
            }
        });

        // Sync Attendance to online
        ParseQuery<Store> storeParseQuery = Store.getQuery();
        storeParseQuery.whereEqualTo("objectId",employeeStoreId);
        storeParseQuery.fromPin(DownloadUtils.PIN_STORE);
        storeParseQuery.getFirstInBackground(new GetCallback<Store>() {
            @Override
            public void done(Store store, ParseException e) {
                if (e == null) {
                    if (store.getEmployeeId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        ParseQuery<Attendance> query = Attendance.getQuery();
                        query.whereEqualTo("store_id", employeeStoreId);
                        query.fromPin(DownloadUtils.PIN_ATTENDANCE + "_DRAFT");
                        query.findInBackground(new FindCallback<Attendance>() {
                            @Override
                            public void done(List<Attendance> listAttendance, ParseException e) {
                                for (final Attendance attendance : listAttendance) {
                                    attendance.setEmployeeId(ParseUser.getCurrentUser().getObjectId());
                                    attendance.setManagerId(ParseUser.getCurrentUser().getString("manager_id"));
                                    attendance.saveEventually();
                                    attendance.unpinInBackground(DownloadUtils.PIN_ATTENDANCE + "_DRAFT");
                                }
                            }
                        });
                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder
                                (VisitStorePointDashboardActivity.this);

                        dialog.setTitle(getString(R.string.errorThereAreNoStoreNear));
                        dialog.setCancelable(false);
                        dialog.setPositiveButton(getString(R.string.approve), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ParseObject.unpinAllInBackground(DownloadUtils
                                        .PIN_ATTENDANCE + "_DRAFT", new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Intent intent = new Intent(VisitStorePointDashboardActivity.this,MainActivity
                                                .class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent
                                                .FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                });

                            }
                        });
                    }
                }
            }
        });


    }

    private void NavigateIntent(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavigateIntent(MainActivity.class);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}