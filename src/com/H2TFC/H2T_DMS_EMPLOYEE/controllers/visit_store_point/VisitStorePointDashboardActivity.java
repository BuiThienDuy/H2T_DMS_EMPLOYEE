package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.visit_store_point;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.LoginActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.MainActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice.InvoiceManagementActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.user_information_management.UserInformationManagementActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.view_report.ViewReportActivity;
import com.parse.ParseUser;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class VisitStorePointDashboardActivity extends Activity {
    Button btnInvoiceManagement,btnVisitStore,btnViewReport,btnLogOut,btnUserInformation;
    String employeeStoreId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viengtham_dashboard);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.visitStorePointTitle));

        btnInvoiceManagement = (Button) findViewById(R.id.activity_viengtham_dashboard_btn_invoice_management);
        btnVisitStore = (Button) findViewById(R.id.dashboard_btn_visit_store);
        btnViewReport = (Button) findViewById(R.id.dashboard_btn_view_report);
        btnLogOut = (Button) findViewById(R.id.activity_viengtham_dashboard_btn_log_out);
        btnUserInformation = (Button) findViewById(R.id.activity_viengtham_dashboard_btn_user_information);

        if(ParseUser.getCurrentUser() == null) {
            btnLogOut.setVisibility(View.INVISIBLE);
        } else {
            btnLogOut.setVisibility(View.VISIBLE);
        }

        btnInvoiceManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VisitStorePointDashboardActivity.this, InvoiceManagementActivity.class);
                startActivity(intent);
            }
        });

        btnVisitStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VisitStorePointDashboardActivity.this, LoginActivity.class);
                intent.putExtra("EXTRAS_KHAOSAT",false);
                startActivity(intent);
            }
        });

        btnUserInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VisitStorePointDashboardActivity.this, UserInformationManagementActivity.class);
                startActivity(intent);
            }
        });

        btnViewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VisitStorePointDashboardActivity.this, ViewReportActivity.class);
                startActivity(intent);
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(VisitStorePointDashboardActivity.this);
                dialog.setTitle(getString(R.string.logOut));
                dialog.setMessage(getString(R.string.confirmLogOut));

                dialog.setPositiveButton(getString(R.string.approve), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseUser.logOut();
                        Intent intent = new Intent(VisitStorePointDashboardActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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
            btnLogOut.setVisibility(View.INVISIBLE);
        } else {
            btnLogOut.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}