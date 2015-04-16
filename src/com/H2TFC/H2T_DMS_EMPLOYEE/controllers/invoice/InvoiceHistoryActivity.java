package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.InvoiceAdapter;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog.MyEditDatePicker;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Invoice;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.MultiSpinner;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

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
public class InvoiceHistoryActivity extends Activity {
    InvoiceAdapter invoiceAdapter;
    ListView lvInvoice;
    TextView tvEmptyInvoice;

    String storeId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_history);

        if(getIntent().hasExtra("EXTRAS_STORE_ID")) {
            storeId = getIntent().getStringExtra("EXTRAS_STORE_ID");
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
                query.whereEqualTo("storeId", storeId);
                query.fromPin(DownloadUtils.PIN_INVOICE);
                return query;
            }
        };

        invoiceAdapter = new InvoiceAdapter(this,factory);
        lvInvoice.setAdapter(invoiceAdapter);
        lvInvoice.setEmptyView(tvEmptyInvoice);
    }

    private void InitializeComponent() {
        tvEmptyInvoice = (TextView) findViewById(R.id.activity_invoice_history_tv_empty);
        lvInvoice = (ListView) findViewById(R.id.activity_invoice_history_lv_invoice);

        // Default setting
        tvEmptyInvoice.setVisibility(View.INVISIBLE);
    }
}