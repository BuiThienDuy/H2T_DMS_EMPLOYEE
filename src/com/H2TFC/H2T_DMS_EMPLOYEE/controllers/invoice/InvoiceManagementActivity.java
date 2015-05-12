package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.H2TFC.H2T_DMS_EMPLOYEE.MyApplication;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.InvoiceAdapter;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Invoice;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.*;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.parse.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class InvoiceManagementActivity extends Activity {
    public InvoiceAdapter invoiceAdapter;
    public ListView lvInvoice;
    BootstrapEditText etName;
    BootstrapEditText etPassword;
    BootstrapButton btnDone,btnCancel;

    TextView tvEmptyInvoice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_management);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.invoiceManagementTitle));

        if(ParseUser.getCurrentUser() == null) {
            final Dialog login = new Dialog(this);
            login.setContentView(R.layout.dialog_login);
            login.setTitle(getString(R.string.loginTitle));
            login.setCancelable(false);

            etName = (BootstrapEditText) login.findViewById(R.id.dialog_login_et_tendangnhap);
            etPassword = (BootstrapEditText) login.findViewById(R.id.dialog_login_et_matkhau);
            btnDone = (BootstrapButton) login.findViewById(R.id.dialog_login_btn_dangnhap);
            btnCancel = (BootstrapButton) login.findViewById(R.id.dialog_login_btn_huy);

            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = etName.getText().toString();
                    String password = etPassword.getText().toString();

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
                        Toast.makeText(InvoiceManagementActivity.this, error_msg.toString(), Toast.LENGTH_LONG).show();
                    } else if (!ConnectUtils.hasConnectToInternet(InvoiceManagementActivity.this)) {
                        Toast.makeText(InvoiceManagementActivity.this, getString(R.string.needInternetAccessToLogin),
                                Toast.LENGTH_LONG)
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
                                                login.dismiss();
                                                InitializeComponent();
                                                SetUpListView();
                                                SetupEvent();
                                                DownloadUtils.DownloadParseInvoice(InvoiceManagementActivity.this, new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        tvEmptyInvoice.setVisibility(View.VISIBLE);
                                                        invoiceAdapter.loadObjects();
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        Toast.makeText(InvoiceManagementActivity.this, getString(R.string.errorSignIn), Toast
                                                .LENGTH_LONG)
                                                .show();
                                    }
                                } else {
                                    Toast.makeText(InvoiceManagementActivity.this, getString(R.string.userOrPasswordIncorrect), Toast
                                            .LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            login.show();
        } else {
            InitializeComponent();
            SetUpListView();
            SetupEvent();
            DownloadUtils.DownloadParseInvoice(InvoiceManagementActivity.this, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    tvEmptyInvoice.setVisibility(View.VISIBLE);
                    invoiceAdapter.loadObjects();
                }
            });
        }


    }

    private void SetupEvent() {
        lvInvoice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    Invoice invoice = invoiceAdapter.getItem(position);

                    Intent intent = new Intent(InvoiceManagementActivity.this, InvoiceDetailActivity.class);
                    intent.putExtra("EXTRAS_INVOICE_ID", invoice.getObjectId());
                    startActivity(intent);
                }
            }
        });


    }

    private void SetUpListView() {
        ParseQueryAdapter.QueryFactory<Invoice> factory = new ParseQueryAdapter.QueryFactory<Invoice>() {
            @Override
            public ParseQuery<Invoice> create() {
                ParseQuery<Invoice> query = Invoice.getQuery();
                query.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
                query.orderByDescending("updatedAt");
                query.fromPin(DownloadUtils.PIN_INVOICE);
                return query;
            }
        };

        invoiceAdapter = new InvoiceAdapter(this,factory);
        lvInvoice.setAdapter(invoiceAdapter);
        lvInvoice.setEmptyView(tvEmptyInvoice);
    }

    private void InitializeComponent() {
        tvEmptyInvoice = (TextView) findViewById(R.id.activity_invoice_tv_empty);
        lvInvoice = (ListView) findViewById(R.id.activity_invoice_management_lv_invoice);
        // Default setting
        tvEmptyInvoice.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_invoice_management,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_bar_invoice_management_search: {
                Intent intent = new Intent(InvoiceManagementActivity.this,InvoiceSearchActivity.class);
                startActivityForResult(intent, MyApplication.REQUEST_SEARCH);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == MyApplication.REQUEST_SEARCH) {
                String sFromDate = data.getStringExtra("EXTRAS_RETURN_FROM_DATE");
                String sToDate = data.getStringExtra("EXTRAS_RETURN_TO_DATE");
                final String sToSearch = data.getStringExtra("EXTRAS_RETURN_STATUS");
                Date fromDate = null;
                Date toDate = null;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

                try {
                    fromDate = simpleDateFormat.parse(sFromDate);
                    toDate = simpleDateFormat.parse(sToDate);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }

                final Date finalFromDate = fromDate;
                final Date finalToDate = toDate;
                ParseQueryAdapter.QueryFactory<Invoice> factory = new ParseQueryAdapter.QueryFactory<Invoice>() {
                    @Override
                    public ParseQuery<Invoice> create() {
                        ParseQuery<Invoice> query = Invoice.getQuery();
                        query.whereGreaterThan("createdAt", finalFromDate);
                        query.whereLessThanOrEqualTo("createdAt", finalToDate);
                        query.whereMatches("invoice_status",sToSearch);
                        query.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
                        query.orderByDescending("updatedAt");
                        query.fromPin(DownloadUtils.PIN_INVOICE);
                        return query;
                    }
                };

                invoiceAdapter = new InvoiceAdapter(this,factory);
                lvInvoice.setAdapter(invoiceAdapter);
                lvInvoice.setEmptyView(tvEmptyInvoice);
            }
        }
    }
}