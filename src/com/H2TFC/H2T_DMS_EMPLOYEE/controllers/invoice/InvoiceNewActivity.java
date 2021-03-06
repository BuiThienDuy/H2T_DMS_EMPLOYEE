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
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Invoice;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Product;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.ProductPurchase;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Promotion;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.parse.*;

import java.util.ArrayList;
import java.util.Date;
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

        if (getIntent().hasExtra("EXTRAS_STORE_ID")) {
            storeId = getIntent().getStringExtra("EXTRAS_STORE_ID");
        }

        if (ConnectUtils.hasConnectToInternet(InvoiceNewActivity.this)) {
            DownloadUtils.DownloadParseProduct(InvoiceNewActivity.this,new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    tvEmptyProduct.setVisibility(View.VISIBLE);
                    productListAdapter.loadObjects();
                }
            });

            DownloadUtils.DownloadParsePromotion(InvoiceNewActivity.this,new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    productListAdapter.loadObjects();
                }
            });
        }

        InitializeComponent();
        SetupEvent();
        SetupListView();
    }

    private void SetupListView() {
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
        lvProduct.setEmptyView(tvEmptyProduct);
        lvProduct.setAdapter(productListAdapter);
    }

    private void SetupEvent() {
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

    private void InitializeComponent() {
        lvProduct = (ListView) findViewById(R.id.activity_invoice_new_listview);
        tvEmptyProduct = (TextView) findViewById(R.id.activity_invoice_new_tv_empty_product);
        tvEmptyProduct.setVisibility(View.INVISIBLE);

        editTextSearch = (EditText) findViewById(R.id.activity_invoice_new_search);
    }

    double totalPrice = 0;

    public String generateResult() {
        StringBuilder result = new StringBuilder(getString(R.string.haveOrdered));
        StringBuilder result_promotion = new StringBuilder(getString(R.string.promotionS));
        double totalResult = 0;
        int count = 0;
        StringBuilder sGifted = new StringBuilder("");
        for (int i = 0; i < productListAdapter.getCount(); i++) {
            View view = getViewByPosition(i, lvProduct);
            EditText etAmount = (EditText) view.findViewById(R.id.list_product_et_amount);
            String amount = etAmount.getText().toString();
            try {
                int i_amount = Integer.parseInt(amount);
                if (i_amount > 0) {
                    Product product = productListAdapter.getItem(i);

                    // Product promotion
                    ParseQuery<Product> queryProduct = Product.getQuery();
                    queryProduct.whereEqualTo("objectId", product.getObjectId());
                    queryProduct.fromPin(DownloadUtils.PIN_PRODUCT);

                    ParseQuery<Promotion> queryPromotion = Promotion.getQuery();
//                    queryPromotion.whereGreaterThan("promotion_apply_from", new Date());
//                    queryPromotion.whereLessThan("promotion_apply_to", new Date());
                    queryPromotion.whereMatchesQuery("promotion_product_gift", queryProduct);
                    queryPromotion.fromPin(DownloadUtils.PIN_PROMOTION);
                    List<Promotion> promotionList = queryPromotion.find();
                    Date currentDate = new Date();

                    double discount = 0.0;
                    for (Promotion promotion : promotionList) {
                        if (currentDate.after(promotion.getPromotionApplyFrom()) && currentDate.before(promotion
                                .getPromotionApplyTo())) {

                            int quantity_gift = promotion.getQuantityGift();
                            int quantity_gifted = promotion.getQuantityGifted();
                            int discount_gift = promotion.getDiscount();
                            if (discount_gift == 0) {
                                if (i_amount >= quantity_gift) {
                                    int m = i_amount / quantity_gift;
                                    quantity_gifted *= m;

                                    sGifted.append("-" + getString(R.string.sGift) + " " + quantity_gifted + " " + promotion
                                            .getProductGifted().getUnit()
                                            + " " + promotion.getProductGifted().getProductName() + ".\n");
                                }
                            } else {
                                if (i_amount > quantity_gift && discount < discount_gift) { // get the max discount
                                    discount = discount_gift;
                                }
                            }
                        }
                    }

                    // Product purchase
                    double price = product.getPrice();
                    double total = price * i_amount;
                    double discount_price = total * (discount / 100);
                    total = total - discount_price;
                    totalResult += total;
                    result.append(count + ". " + product.getProductName() + "(" + i_amount + " " + product.getUnit
                            () + ")" +
                            "=" +
                            " ");

                    if (discount_price > 0) {
                        result.append(String.format(Locale
                                        .CHINESE,
                                "%1$,.0f",
                                price) + " " + getString(R.string.VND) + " x " + i_amount + " - " +
                                String.format(Locale
                                                .CHINESE,
                                        "%1$,.0f",
                                        discount_price) + " " + getString(R.string.VND) + " = " +
                                String
                                        .format(Locale.CHINESE, "%1$,.0f",
                                                total) + " " +
                                getString(R.string.VND) +
                                "\n");
                    } else {
                        result.append(String.format(Locale
                                        .CHINESE,
                                "%1$,.0f",
                                price) + " " + getString(R.string.VND) + " x " + i_amount + " = " +
                                String
                                        .format(Locale.CHINESE, "%1$,.0f",
                                                total) + " " +
                                getString(R.string.VND) +
                                "\n");
                    }


                    count++;
                }
            } catch (Exception ex) {

            }
        }
        totalPrice = totalResult;
        if (sGifted.toString().length() > 0) {
            result.append("\n" + sGifted.toString());
        }

        result.append("\n" + getString(R.string.resultTotalPayEqual) + String.format(Locale.CHINESE, "%1$,.0f",
                totalResult) + " " + getString(R.string.VND));

        return result.toString();
    }

    public List<ProductPurchase> getProductPurchaseList() {
        final ArrayList<ProductPurchase> list = new ArrayList<ProductPurchase>();

        for (int i = 0; i < productListAdapter.getCount(); i++) {
            View view = getViewByPosition(i, lvProduct);
            EditText etAmount = (EditText) view.findViewById(R.id.list_product_et_amount);
            String amount = etAmount.getText().toString();
            try {
                int i_amount = Integer.parseInt(amount);
                if (i_amount > 0) {
                    Product product = productListAdapter.getItem(i);

                    // Product promotion
                    ParseQuery<Product> queryProduct = Product.getQuery();
                    queryProduct.whereEqualTo("objectId", product.getObjectId());
                    queryProduct.fromPin(DownloadUtils.PIN_PRODUCT);

                    ParseQuery<Promotion> queryPromotion = Promotion.getQuery();
                    queryPromotion.whereMatchesQuery("promotion_product_gift", queryProduct);
                    queryPromotion.fromPin(DownloadUtils.PIN_PROMOTION);
                    List<Promotion> promotionList = queryPromotion.find();
                    List<Promotion> realPromotionList = new ArrayList<Promotion>();
                    Date currentDate = new Date();

                    for (Promotion promotion : promotionList) {
                        if (currentDate.after(promotion.getPromotionApplyFrom()) && currentDate.before(promotion
                                .getPromotionApplyTo())) {
                            realPromotionList.add(promotion);
                        }
                    }

                    final ProductPurchase productPurchase = new ProductPurchase();
                    productPurchase.setName(product.getProductName());
                    productPurchase.setQuantity(i_amount);
                    productPurchase.setPrice(product.getPrice());
                    productPurchase.setProductRelate(product);
                    productPurchase.setUnit(product.getUnit());
                    productPurchase.setPromotionRelate(realPromotionList);

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
            } catch (Exception ex) {

            }
        }
        return list;
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_invoice_new, menu);
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
        if (totalPrice != 0) {
            dialog.show();
        }
    }


}