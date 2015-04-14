package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.InvoiceNewAdapter;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point.TrungBayActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Product;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.SaveCallback;

import java.util.Locale;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class InvoiceNewActivity extends Activity {
    InvoiceNewAdapter productListAdapter;

    ListView lvProduct;
    TextView tvEmptyProduct;
    public TextView tvResult;

    EditText editTextSearch;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_new);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (ConnectUtils.hasConnectToInternet(InvoiceNewActivity.this)) {
            DownloadUtils.DownloadParseProduct(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    tvEmptyProduct.setVisibility(View.VISIBLE);
                    productListAdapter.loadObjects();
                }
            });
        }

        ParseQueryAdapter.QueryFactory<Product> factory = new ParseQueryAdapter.QueryFactory<Product>() {
            @Override
            public ParseQuery<Product> create() {
                ParseQuery<Product> query = Product.getQuery();
                query.whereNotEqualTo("status", Product.ProductStatus.KHOA.name());
                query.orderByDescending("createdAt");
                query.fromPin(DownloadUtils.PIN_PRODUCT);
                return query;
            }
        };



        productListAdapter = new InvoiceNewAdapter(this, factory);

        lvProduct = (ListView) findViewById(R.id.activity_invoice_new_listview);
        tvEmptyProduct = (TextView) findViewById(R.id.activity_invoice_new_tv_empty_product);
        tvEmptyProduct.setVisibility(View.INVISIBLE);
        lvProduct.setEmptyView(tvEmptyProduct);
        lvProduct.setAdapter(productListAdapter);
        editTextSearch = (EditText) findViewById(R.id.activity_invoice_new_search);
        tvResult = (TextView) findViewById(R.id.activity_invoice_new_tv_result);

        lvProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {


            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editTextSearch.getText().toString().length() != 0) {
                    ParseQueryAdapter.QueryFactory<Product> factory = new ParseQueryAdapter.QueryFactory<Product>() {
                        @Override
                        public ParseQuery<Product> create() {
                            ParseQuery<Product> query = Product.getQuery();
                            query.whereNotEqualTo("status", Product.ProductStatus.KHOA.name());
                            query.whereContains("name", editTextSearch.getText().toString());
                            query.orderByDescending("createdAt");
                            query.fromPin(DownloadUtils.PIN_PRODUCT);
                            return query;
                        }
                    };
                    productListAdapter = new InvoiceNewAdapter(InvoiceNewActivity.this,factory);
                    lvProduct.setAdapter(productListAdapter);
                    productListAdapter.notifyDataSetChanged();
                } else {
                    ParseQueryAdapter.QueryFactory<Product> factory = new ParseQueryAdapter.QueryFactory<Product>() {
                        @Override
                        public ParseQuery<Product> create() {
                            ParseQuery<Product> query = Product.getQuery();
                            query.whereNotEqualTo("status", Product.ProductStatus.KHOA.name());
                            query.orderByDescending("createdAt");
                            query.fromPin(DownloadUtils.PIN_PRODUCT);
                            return query;
                        }
                    };
                    productListAdapter = new InvoiceNewAdapter(InvoiceNewActivity.this,factory);
                    lvProduct.setAdapter(productListAdapter);
                    productListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void generateResult() {
        tvResult.setText(getString(R.string.haveOrdered));
        double totalResult = 0;
        int count = 0;
        for(int i = 0 ; i < productListAdapter.getCount() ; i++) {
            View view = getViewByPosition(i,lvProduct);
            EditText etAmount = (EditText) view.findViewById(R.id.list_product_et_amount);
            String amount = etAmount.getText().toString();
            try {
                int i_amount = Integer.parseInt(amount);
                if(i_amount > 0) {
                    Product product = productListAdapter.getItem(i);
                    double price = product.getPrice();
                    double total = price * i_amount;
                    totalResult += total;
                    tvResult.append(count + ". " +  product.getProductName() + "(" + i_amount + " " + product.getUnit
                            ()+")" +
                            ": \n=" +
                            " " );
                    tvResult.append(String.format(Locale
                                    .CHINESE,
                            "%1$,.0f",
                            price) + " " + getString(R.string.VND) + " x " + i_amount + " = " + String.format(Locale.CHINESE,"%1$,.0f",
                            total) + " " +
                            getString(R.string.VND) +
                            "\n");
                    count++;
                }
            } catch(Exception ex) {

            }
        }
        tvResult.append(getString(R.string.resultTotalPayEqual) + String.format(Locale.CHINESE,"%1$,.0f",
                totalResult) + " " + getString(R.string.VND)  );
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
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