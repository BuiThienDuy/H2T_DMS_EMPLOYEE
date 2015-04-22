package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.InvoiceNewAdapter;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.*;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.parse.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class InvoiceNewActivity extends Activity {
    String storeId;

    InvoiceNewAdapter productListAdapter;

    ListView lvProduct;
    TextView tvEmptyProduct;

    EditText editTextSearch;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_new);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.invoiceNewTitle));

        if(getIntent().hasExtra("EXTRAS_STORE_ID")) {
            storeId = getIntent().getStringExtra("EXTRAS_STORE_ID");
        }

        if (ConnectUtils.hasConnectToInternet(InvoiceNewActivity.this)) {
            DownloadUtils.DownloadParseProduct(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    tvEmptyProduct.setVisibility(View.VISIBLE);
                    productListAdapter.loadObjects();
                }
            });

            DownloadUtils.DownloadParsePromotion(new SaveCallback() {
                @Override
                public void done(ParseException e) {
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
                if (editTextSearch.getText().toString().length() != 0) {
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
                    productListAdapter = new InvoiceNewAdapter(InvoiceNewActivity.this, factory);
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
                    productListAdapter = new InvoiceNewAdapter(InvoiceNewActivity.this, factory);
                    lvProduct.setAdapter(productListAdapter);
                    productListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    double totalPrice = 0;

    public String generateResult() {
        StringBuilder result = new StringBuilder(getString(R.string.haveOrdered));
        StringBuilder result_promotion = new StringBuilder(getString(R.string.promotionS));
        double totalResult = 0;
        int count = 0;
        StringBuilder sGifted = new StringBuilder("");
        for(int i = 0 ; i < productListAdapter.getCount() ; i++) {
            View view = getViewByPosition(i,lvProduct);
            EditText etAmount = (EditText) view.findViewById(R.id.list_product_et_amount);
            String amount = etAmount.getText().toString();
            try {
                int i_amount = Integer.parseInt(amount);
                if(i_amount > 0) {
                    Product product = productListAdapter.getItem(i);

                    // Product promotion
                    ParseQuery<Product> queryProduct = Product.getQuery();
                    queryProduct.whereEqualTo("objectId",product.getObjectId());
                    queryProduct.fromPin(DownloadUtils.PIN_PRODUCT);

                    ParseQuery<Promotion> queryPromotion = Promotion.getQuery();
                    queryPromotion.whereMatchesQuery("promotion_product_gift", queryProduct);
                    queryPromotion.fromPin(DownloadUtils.PIN_PROMOTION);
                    List<Promotion> promotionList = queryPromotion.find();

                    HashMap<Integer,String> listOfPromo = new HashMap<Integer, String>();
                    // Promotion type 1 = <quantity_gift><quantity_gifted||product_gifted>
                    // Promotion type 2 = <quantity_gift><discount>


                    int discount = 0;
                    for(Promotion promotion : promotionList) {
                        int quantity_gift = promotion.getQuantityGift();
                        int quantity_gifted = promotion.getQuantityGifted();
                        int discount_gift = promotion.getDiscount();
                        if(discount_gift == 0) {
                            if(i_amount >= quantity_gift) {
                                int m = i_amount/quantity_gift;
                                quantity_gifted *= m;

                                sGifted.append("-" + getString(R.string.sGift) + " " + quantity_gifted + " " + promotion
                                        .getProductGifted().getUnit()
                                        + " " + promotion.getProductGifted().getProductName() +".\n");
                            }
                        } else {
                            if(i_amount > quantity_gift && discount < discount_gift) { // get the max discount
                                discount = discount_gift;
                            }
                        }
                    }

                    // Product purchase
                    double price = product.getPrice();
                    double total = price * i_amount;
                    double discount_price = (discount*10/100);
                    total = total - discount_price;
                    totalResult += total;
                    result.append(count + ". " + product.getProductName() + "(" + i_amount + " " + product.getUnit
                            () + ")" +
                            "=" +
                            " ");

                    if(discount_price > 0) {
                        result.append(String.format(Locale
                                        .CHINESE,
                                "%1$,.0f",
                                price) + " " + getString(R.string.VND) + " x " + i_amount + " - " +
                                discount_price + " = " +
                                String
                                        .format(Locale.CHINESE, "%1$,.0f",
                                                total) + " " +
                                getString(R.string.VND) +
                                "\n");
                    } else {
                        result.append(String.format(Locale
                                        .CHINESE,
                                "%1$,.0f",
                                price) + " " + getString(R.string.VND) + " x " + i_amount  + " = " +
                                String
                                        .format(Locale.CHINESE, "%1$,.0f",
                                                total) + " " +
                                getString(R.string.VND) +
                                "\n");
                    }



                    count++;
                }
            } catch(Exception ex) {

            }
        }
        totalPrice = totalResult;
        result.append("\n" + sGifted);

        result.append("\n" + getString(R.string.resultTotalPayEqual) + String.format(Locale.CHINESE, "%1$,.0f",
                totalResult) + " " + getString(R.string.VND));

        return result.toString();
    }

    public List<ProductPurchase> getProductPurchaseList() {
        final ArrayList<ProductPurchase> list = new ArrayList<ProductPurchase>();

        for(int i = 0 ; i < productListAdapter.getCount() ; i++) {
            View view = getViewByPosition(i,lvProduct);
            EditText etAmount = (EditText) view.findViewById(R.id.list_product_et_amount);
            String amount = etAmount.getText().toString();
            try {
                int i_amount = Integer.parseInt(amount);
                if(i_amount > 0) {
                    Product product = productListAdapter.getItem(i);
                    final ProductPurchase productPurchase = new ProductPurchase();
                    productPurchase.setName(product.getProductName());
                    productPurchase.setQuantity(i_amount);
                    productPurchase.setPrice(product.getPrice());
                    productPurchase.setProductRelate(product);
                    productPurchase.setUnit(product.getUnit());

                    productPurchase.saveEventually();
                    productPurchase.pinInBackground(DownloadUtils.PIN_PRODUCT_PURCHASE, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                list.add(productPurchase);
                            }
                        }
                    });

                }
            } catch(Exception ex) {

            }
        }
        return list;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_invoice_new,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }

            case R.id.action_bar_invoice_new_create: {
                CreateANewInvoice();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void CreateANewInvoice() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(InvoiceNewActivity.this);
        dialog.setTitle(getString(R.string.createNewInvoice));

        String message = generateResult() + "\n\n" + getString(R.string.confirmCreateNewInvoice);

        dialog.setMessage(message);

        dialog.setPositiveButton(getString(R.string.approve), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Invoice invoice = new Invoice();
                invoice.setStoreId(storeId);
                invoice.setEmployee(ParseUser.getCurrentUser());
                invoice.setInvoiceStatus(Invoice.MOI_TAO);
                invoice.setEmployeeId(ParseUser.getCurrentUser().getObjectId());
                invoice.setManagerId(ParseUser.getCurrentUser().getString("manager_id"));

                ArrayList<ProductPurchase> productPurchaseList = (ArrayList<ProductPurchase>) getProductPurchaseList();
                invoice.setProductPurchases(productPurchaseList);

                invoice.setInvoicePrice(totalPrice);

                invoice.saveEventually();

                invoice.pinInBackground(DownloadUtils.PIN_INVOICE, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(InvoiceNewActivity.this, getString(R.string.createInvoiceSuccess), Toast
                                    .LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });

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
}