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

/**
 * Created by c4sau on 11/04/2015.
 */
public class InvoiceHistoryActivity extends Activity {
    InvoiceAdapter invoiceAdapter;
    ListView lvInvoice;
    TextView tvEmptyInvoice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_history);

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
        tvEmptyInvoice = (TextView) findViewById(R.id.activity_invoice_history_tv_empty);
        lvInvoice = (ListView) findViewById(R.id.activity_invoice_history_lv_invoice);

        // Default setting
        tvEmptyInvoice.setVisibility(View.INVISIBLE);
    }
}