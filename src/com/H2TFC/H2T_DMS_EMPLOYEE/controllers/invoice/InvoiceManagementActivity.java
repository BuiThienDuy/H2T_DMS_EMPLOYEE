package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.InvoiceAdapter;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog.MyEditDatePicker;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Invoice;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.*;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class InvoiceManagementActivity extends Activity {
    InvoiceAdapter invoiceAdapter;
    ListView lvInvoice;

    TextView tvEmptyInvoice;
    EditText etName;
    EditText etFromDate;
    EditText etToDate;
    MultiSpinner spinnerStatus;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_management);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if(ConnectUtils.hasConnectToInternet(InvoiceManagementActivity.this)) {
            DownloadUtils.DownloadParseProduct(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    tvEmptyInvoice.setVisibility(View.VISIBLE);
                }
            });
        }

        InitializeComponent();
        SetUpListView();
    }

    private void SetUpListView() {
        ParseQueryAdapter.QueryFactory<Invoice> factory = new ParseQueryAdapter.QueryFactory<Invoice>() {
            @Override
            public ParseQuery<Invoice> create() {
                ParseQuery<Invoice> query = Invoice.getQuery();
                query.orderByDescending("updatedAt");
                query.fromPin(DownloadUtils.PIN_PRODUCT);
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

        etName = (EditText) findViewById(R.id.activity_invoice_management_et_name);
        etFromDate = (EditText) findViewById(R.id.activity_invoice_management_et_from_date);
        etToDate = (EditText) findViewById(R.id.activity_invoice_management_et_to_date);
        spinnerStatus = (MultiSpinner) findViewById(R.id.activity_invoice_management_spinner_status);

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        MyEditDatePicker edpFromDate = new MyEditDatePicker(InvoiceManagementActivity.this, R.id
                .activity_invoice_management_et_from_date,day,month,
                year);
        MyEditDatePicker edpToDate =new MyEditDatePicker(InvoiceManagementActivity.this, R.id
                .activity_invoice_management_et_to_date,day,month,
                year);
        edpFromDate.updateDisplay();
        edpToDate.updateDisplay();


        List<String> items = Arrays.asList(Invoice.MOI_TAO, Invoice.DANG_XU_LY,
                Invoice.DA_THANH_TOAN);
        spinnerStatus.setItems(items, getString(R.string.for_all), new MultiSpinner.MultiSpinnerListener() {
            @Override
            public void onItemsSelected(boolean[] selected) {

            }
        });
        // Default setting
        tvEmptyInvoice.setVisibility(View.INVISIBLE);
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