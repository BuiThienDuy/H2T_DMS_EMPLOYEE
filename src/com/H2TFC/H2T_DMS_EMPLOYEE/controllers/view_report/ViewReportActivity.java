package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.view_report;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.BarChartItem;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.ChartItem;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.PieChartItem;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog.MyEditDatePicker;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Invoice;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Product;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.ProductPurchase;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.*;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class ViewReportActivity extends Activity {
    ListView lvChart;
    BootstrapEditText etFromDate,etToDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        setTitle(getString(R.string.reportTitle));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        InitializeComponent();
        LoadReport();
        SetupEvent();
    }

    public void SetupEvent() {
        etFromDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LoadReport();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etToDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LoadReport();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void InitializeComponent() {
        lvChart = (ListView) findViewById(R.id.activity_view_report_lv_chart);
        etFromDate = (BootstrapEditText) findViewById(R.id.activity_view_report_et_from_date);
        etToDate = (BootstrapEditText) findViewById(R.id.activity_view_report_et_to_date);

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        MyEditDatePicker edpFromDate = new MyEditDatePicker(ViewReportActivity.this, R.id
                .activity_view_report_et_from_date,day,month,
                year);

        c.add(Calendar.DATE,1);
        day = c.get(Calendar.DATE);
        month = c.get(Calendar.MONTH);
        year = c.get(Calendar.YEAR);
        MyEditDatePicker edpToDate =new MyEditDatePicker(ViewReportActivity.this, R.id
                .activity_view_report_et_to_date,day,month,
                year);

        edpFromDate.updateDisplay();
        edpToDate.updateDisplay();
    }

    private void LoadReport() {
        ArrayList<ChartItem> list = new ArrayList<ChartItem>();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = new Date();
        Date toDate = new Date();

        try {
            fromDate = df.parse(etFromDate.getText().toString());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        try {
            toDate = df.parse(etToDate.getText().toString());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }


        list.add(new BarChartItem(generateDataBar_DoanhThu_ThucThu(fromDate,toDate), getApplicationContext(),getString(R
                .string
                .doanhThuThucThuChart)));
        list.add(new PieChartItem(generateDataPie_CuaHang_TiemNang(fromDate,toDate), getApplicationContext(),getString(R.string
                .diemCuaHangChart)));
        list.add(new PieChartItem(generateDataPie_TinhTrang_DonHang(fromDate,toDate), getApplicationContext(),getString(R.string
                .donHangChart)));
        list.add(new BarChartItem(generateDataBar_SoLuong_HangBan(fromDate,toDate), getApplicationContext(),getString(R.string.soLuongHangBanChart)));

        ChartDataAdapter cda = new ChartDataAdapter(getApplicationContext(),list);
        lvChart.setAdapter(cda);
    }

    /** adapter that supports 3 different item types */
    private class ChartDataAdapter extends ArrayAdapter<ChartItem> {

        public ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(position == 0) {

            }
            return getItem(position).getView(position, convertView, getContext());
        }

        @Override
        public int getItemViewType(int position) {
            // return the views type
            return getItem(position).getItemType();
        }

        @Override
        public int getViewTypeCount() {
            return 3; // we have 3 different item-types
        }
    }

    // 1. Generate Doanh Thu/ Thuc Thu chart
    private BarData generateDataBar_DoanhThu_ThucThu(Date fromDate, Date toDate) {
        // Get data first
        ParseQuery<Store> storeParseQuery = Store.getQuery();
        storeParseQuery.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
        storeParseQuery.whereEqualTo("status", Store.StoreStatus.BAN_HANG.name());
        storeParseQuery.fromPin(DownloadUtils.PIN_STORE);
        List<Store> storeList = null;
        ArrayList<String> storeNameList = new ArrayList<String>();
        try {
            storeList = storeParseQuery.find();
            for(Store store : storeList) {
                storeNameList.add(store.getStoreType() + " " + store.getName());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Generate chart
        ArrayList<BarEntry> doanhthu_entries = new ArrayList<BarEntry>();

        assert storeList != null;
        for (int i = 0; i < storeList.size(); i++) {
            double doanhThu = 0;
            ParseQuery<Invoice> invoiceParseQuery = Invoice.getQuery();
            invoiceParseQuery.whereEqualTo("storeId", storeList.get(i).getObjectId());
            //invoiceParseQuery.whereNotEqualTo("invoice_status",Invoice.DA_THANH_TOAN);
            invoiceParseQuery.fromPin(DownloadUtils.PIN_INVOICE);
            try {
                List<Invoice> invoiceList = invoiceParseQuery.find();
                for(Invoice invoice : invoiceList) {
                    doanhThu += invoice.getInvoicePrice();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }


            doanhthu_entries.add(new BarEntry((int)doanhThu, i));
        }

        BarDataSet d = new BarDataSet(doanhthu_entries, getString(R.string.income));
        d.setBarSpacePercent(20f);
        d.setColor(Color.CYAN);
        //d.setColors(ColorTemplate.VORDIPLOM_COLORS);
        d.setHighLightAlpha(255);

        ArrayList<BarEntry> thucthu_entries = new ArrayList<BarEntry>();

        for (int i = 0; i < storeList.size(); i++) {
            double thucThu = 0;
            ParseQuery<Invoice> invoiceParseQuery = Invoice.getQuery();
            invoiceParseQuery.whereEqualTo("storeId", storeList.get(i).getObjectId());
            invoiceParseQuery.whereEqualTo("invoice_status", Invoice.DA_THANH_TOAN);
            invoiceParseQuery.fromPin(DownloadUtils.PIN_INVOICE);
            try {
                List<Invoice> invoiceList = invoiceParseQuery.find();
                for(Invoice invoice : invoiceList) {
                    thucThu += invoice.getInvoicePrice();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            thucthu_entries.add(new BarEntry((int) thucThu, i));
        }

        BarDataSet t = new BarDataSet(thucthu_entries, getString(R.string.realIncome));
        t.setBarSpacePercent(20f);
        t.setColor(Color.YELLOW);
        //t.setColors(ColorTemplate.LIBERTY_COLORS);
        t.setHighLightAlpha(255);

        ArrayList<BarDataSet> sets = new ArrayList<BarDataSet>();
        sets.add(d);
        sets.add(t);



        BarData cd = new BarData(storeNameList, sets);
        return cd;
    }

    // 2. Generate bieu do cua hang tiem nang
    private PieData generateDataPie_CuaHang_TiemNang(Date fromDate, Date toDate) {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        // Get data first
        ParseQuery<Store> storeParseQuery = Store.getQuery();
        storeParseQuery.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
        storeParseQuery.whereGreaterThan("createdAt", fromDate);
        storeParseQuery.whereLessThan("createdAt",toDate);
        storeParseQuery.fromPin(DownloadUtils.PIN_STORE);
        List<Store> storeList = null;
        int count_ban_hang = 0;
        int count_tiem_nang = 0;
        int count_khao_sat = 0;
        int count_khong_du_tieu_chuan = 0;
        int count_cho_cap_tren = 0;
        int count_dang_thoa_thuan = 0;

        try {
            storeList = storeParseQuery.find();
            for(Store store : storeList) {
                //storeNameList.add(store.getStoreType() + " " + store.getName());
                String status = store.getStatus();
                if(status.equals(Store.StoreStatus.BAN_HANG.name())) {
                    count_ban_hang++;
                }
                if(status.equals(Store.StoreStatus.TIEM_NANG.name())) {
                    count_tiem_nang++;
                }
                if(status.equals(Store.StoreStatus.KHAO_SAT.name())) {
                    count_khao_sat++;
                }
                if(status.equals(Store.StoreStatus.KHONG_DU_TIEU_CHUAN.name())) {
                    count_khong_du_tieu_chuan++;
                }
                if(status.equals(Store.StoreStatus.CHO_CAP_TREN.name())) {
                    count_cho_cap_tren++;
                }
                if(status.equals(Store.StoreStatus.DANG_THOA_THUAN.name())) {
                    count_dang_thoa_thuan++;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ArrayList<String> q = new ArrayList<String>();
        if(count_ban_hang > 0) {
            entries.add(new Entry(count_ban_hang, 0));
            q.add(getString(R.string.BAN_HANG));
        }
        if(count_tiem_nang > 0) {
            entries.add(new Entry(count_tiem_nang, 1));
            q.add(getString(R.string.TIEM_NANG));
        }
        if(count_khao_sat > 0) {
            entries.add(new Entry(count_khao_sat, 2));
            q.add(getString(R.string.KHAO_SAT));
        }
        if(count_khong_du_tieu_chuan > 0) {
            entries.add(new Entry(count_khong_du_tieu_chuan, 3));
            q.add(getString(R.string.KHONG_DU_TIEU_CHUAN));
        }
        if(count_cho_cap_tren > 0) {
            entries.add(new Entry(count_cho_cap_tren, 4));
            q.add(getString(R.string.CHO_CAP_TREN));
        }
        if(count_dang_thoa_thuan > 0) {
            entries.add(new Entry(count_dang_thoa_thuan, 5));
            q.add(getString(R.string.DANG_THOA_THUAN));
        }

        PieDataSet d = new PieDataSet(entries, "");

        // space between slices
        d.setSliceSpace(2f);
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);

        PieData cd = new PieData(q, d);
        return cd;
    }

    // 3.
    private PieData generateDataPie_TinhTrang_DonHang(Date fromDate, Date toDate) {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        // Get data first
        ParseQuery<Invoice> invoiceParseQuery = Invoice.getQuery();
        invoiceParseQuery.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
        invoiceParseQuery.whereGreaterThan("createdAt",fromDate);
        invoiceParseQuery.whereLessThan("createdAt", toDate);
        invoiceParseQuery.fromPin(DownloadUtils.PIN_INVOICE);
        List<Invoice> invoiceList = null;
        int count_moi_tao = 0;
        int count_dang_xu_ly = 0;
        int count_hoan_thanh = 0;

        try {
            invoiceList = invoiceParseQuery.find();
            for(Invoice invoice : invoiceList) {
                //storeNameList.add(store.getStoreType() + " " + store.getName());
                String status = invoice.getInvoiceStatus();
                if(status.equals(Invoice.MOI_TAO)) {
                    count_moi_tao++;
                }
                if(status.equals(Store.StoreStatus.TIEM_NANG.name())) {
                    count_dang_xu_ly++;
                }
                if(status.equals(Store.StoreStatus.KHAO_SAT.name())) {
                    count_hoan_thanh++;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ArrayList<String> q = new ArrayList<String>();
        if(count_moi_tao > 0) {
            entries.add(new Entry(count_moi_tao, 0));
            q.add(getString(R.string.MOI_TAO));
        }
        if(count_dang_xu_ly > 0) {
            entries.add(new Entry(count_dang_xu_ly, 1));
            q.add(getString(R.string.DANG_XU_LY));
        }
        if(count_hoan_thanh > 0) {
            entries.add(new Entry(count_hoan_thanh, 2));
            q.add(getString(R.string.DA_THANH_TOAN));
        }


        PieDataSet d = new PieDataSet(entries, "");

        // space between slices
        d.setSliceSpace(2f);
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);

        PieData cd = new PieData(q, d);
        return cd;
    }

    // 4. Generate
    private BarData generateDataBar_SoLuong_HangBan(Date fromDate, Date toDate) {
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

        ParseQuery<Product> productParseQuery = Product.getQuery();
        productParseQuery.fromPin(DownloadUtils.PIN_PRODUCT);
        List<Product> productList;
        HashSet<String> productNameList = new HashSet<String>();
        try {
            productList = productParseQuery.find();
            for (Product product : productList) {
                productNameList.add(product.getProductName());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Map<String, Integer> productPurchaseQuantity = new HashMap<String, Integer>();
        for (String productName : productNameList) {
            productPurchaseQuantity.put(productName, 0);
        }

        ParseQuery<Invoice> invoiceParseQuery = Invoice.getQuery();
        invoiceParseQuery.whereEqualTo("employee_id", ParseUser.getCurrentUser().getObjectId());
        invoiceParseQuery.whereGreaterThan("createdAt",fromDate);
        invoiceParseQuery.whereLessThan("createdAt", toDate);
        invoiceParseQuery.fromPin(DownloadUtils.PIN_INVOICE);
        List<Invoice> invoiceList;
        ArrayList<String> productNameArray = null;
        try {
            invoiceList = invoiceParseQuery.find();
            for (Invoice invoice : invoiceList) {
                List<ProductPurchase> productPurchaseList = invoice.getProductPurchases();
                for (ProductPurchase productPurchase : productPurchaseList) {
                    for (String name : productPurchaseQuantity.keySet()) {
                        if (name.equals(productPurchase.getProductName())) {
                            int currentQuantity = productPurchaseQuantity.get(name);
                            int newQuantity = productPurchase.getQuantity();

                            productPurchaseQuantity.put(name, currentQuantity + newQuantity);
                        }
                    }
                }
            }

            productNameArray = new ArrayList<String>();
            for (String productName : productNameList) {
                productNameArray.add(productName);
            }

            for (int i = 0; i < productNameArray.size(); i++) {
                entries.add(new BarEntry(productPurchaseQuantity.get(productNameArray.get(i)), i));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        BarDataSet d = new BarDataSet(entries, getString(R.string.quantity));
        d.setBarSpacePercent(20f);
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);
        d.setHighLightAlpha(255);

        BarData cd = new BarData(productNameArray, d);
        return cd;
    }



    private ArrayList<String> getMonths() {

        ArrayList<String> m = new ArrayList<String>();
        m.add("Jan");
        m.add("Feb");
        m.add("Mar");
        m.add("Apr");
        m.add("May");
        m.add("Jun");
        m.add("Jul");
        m.add("Aug");
        m.add("Sep");
        m.add("Okt");
        m.add("Nov");
        m.add("Dec");

        return m;
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