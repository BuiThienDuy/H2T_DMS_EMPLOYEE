package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.InvoiceAdapter;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog.MyEditDatePicker;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Invoice;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.*;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.parse.*;

import java.text.SimpleDateFormat;
import java.util.*;

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
    EditText etFromDate;
    EditText etToDate;
    MultiSpinner spinnerStatus;
    BootstrapButton btnSearch;

    ArrayList<String> statusSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_management);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.invoiceManagementTitle));

        if(ConnectUtils.hasConnectToInternet(InvoiceManagementActivity.this)) {
            DownloadUtils.DownloadParseInvoice(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    tvEmptyInvoice.setVisibility(View.VISIBLE);
                    invoiceAdapter.loadObjects();
                }
            });
        }

        InitializeComponent();
        SetUpListView();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String sFromDate = etFromDate.getText().toString();
                final String sToDate = etToDate.getText().toString();

                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date fromDate = null;
                    Date toDate = null;
                    fromDate = dateFormat.parse(etFromDate.getText().toString());
                    toDate = dateFormat.parse(etToDate.getText().toString());

                    final Date finalFromDate = fromDate;
                    final Date finalToDate = toDate;

                    String sToSearch = "";
                    for(int i = 0 ; i < statusSelected.size();i++) {
                        if(i == statusSelected.size() -1) {
                            sToSearch += "("+ statusSelected.get(i) +")";
                        } else {
                            sToSearch += "("+ statusSelected.get(i) +")|";
                        }
                    }

                    Log.e("He he he", sToSearch);
                    final String finalSToSearch = sToSearch;
                    ParseQueryAdapter.QueryFactory<Invoice> factory = new ParseQueryAdapter.QueryFactory<Invoice>() {
                        @Override
                        public ParseQuery<Invoice> create() {
                            ParseQuery<Invoice> query = Invoice.getQuery();
                            query.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
                            query.whereGreaterThan("createdAt", finalFromDate);
                            query.whereLessThan("createdAt", finalToDate);
                            query.orderByDescending("updatedAt");
                            query.whereMatches("invoice_status", finalSToSearch);
                            query.fromPin(DownloadUtils.PIN_INVOICE);
                            return query;
                        }
                    };
                    invoiceAdapter = new InvoiceAdapter(InvoiceManagementActivity.this,factory);
                    lvInvoice.setAdapter(invoiceAdapter);
                } catch(Exception ex) {
                    ex.printStackTrace();
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

        etFromDate = (EditText) findViewById(R.id.activity_invoice_management_et_from_date);
        etToDate = (EditText) findViewById(R.id.activity_invoice_management_et_to_date);
        spinnerStatus = (MultiSpinner) findViewById(R.id.activity_invoice_management_spinner_status);

        btnSearch = (BootstrapButton) findViewById(R.id.activity_invoice_management_btn_search);

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


        List<String> items = Arrays.asList(getString(R.string.MOI_TAO),getString(R.string.DANG_XU_LY),getString(R
                .string.DA_THANH_TOAN));
        final List<String> stringArrayList = Arrays.asList(Invoice.MOI_TAO,Invoice.DANG_XU_LY,Invoice.DA_THANH_TOAN);

        statusSelected=  new ArrayList<String>();
        statusSelected.add(Invoice.MOI_TAO);
        statusSelected.add(Invoice.DANG_XU_LY);
        statusSelected.add(Invoice.DA_THANH_TOAN);
        spinnerStatus.setItems(items, getString(R.string.for_all), new MultiSpinner.MultiSpinnerListener() {
            @Override
            public void onItemsSelected(boolean[] selected) {
                statusSelected.clear();
                for (int i = 0; i < selected.length; i++) {
                    if (selected[i]) {
                        String status = null;
                        switch (i) {
                            case 0:
                                status = Invoice.MOI_TAO;
                                break;
                            case 1:
                                status = Invoice.DANG_XU_LY;
                                break;
                            case 2:
                                status = Invoice.DA_THANH_TOAN;
                                break;
                        }
                        Toast.makeText(InvoiceManagementActivity.this,"Adding " + status,Toast.LENGTH_SHORT).show();
                        statusSelected.add(status);
                    }
                }
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