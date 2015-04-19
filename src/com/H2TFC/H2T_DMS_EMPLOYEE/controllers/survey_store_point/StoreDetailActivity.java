package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point;

import android.app.Activity;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog.FeedBackDialog;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice.InvoiceHistoryActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice.InvoiceManagementActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice.InvoiceNewActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Invoice;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.StoreImage;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.StoreType;
import com.parse.*;

import java.util.List;
import java.util.Locale;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class StoreDetailActivity extends Activity {
    BootstrapButton btnTrungBay,btnCapNhat,btnQuayVe;

    BootstrapEditText etTenCuaHang,etTenChuCuaHang,etDiaChi,etSDT,etDoanhThu,etMatHangDoiThu;

    TextView tvBucAnhDaChup;

    Spinner spnLoaiCuaHang;

    public String storeID,store_image_id;

    ArrayAdapter<String> storeTypeAdapter;

    RelativeLayout dummy;

    // use for vieng tham
    BootstrapButton btnLichSu,btnDatHang,btnPhanHoi;
    BootstrapEditText etCongNo;
    ImageView ivCongNo;
    LinearLayout layoutTitleCongNo,layoutEditCongNo,layoutButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.updateSurveyPointTitle));

        if(getIntent().hasExtra("EXTRAS_STORE_ID")) {
            storeID = getIntent().getStringExtra("EXTRAS_STORE_ID");
        }
        if(getIntent().hasExtra("EXTRAS_STORE_IMAGE_ID")) {
            store_image_id = getIntent().getStringExtra("EXTRAS_STORE_IMAGE_ID");
        }

        if(ConnectUtils.hasConnectToInternet(StoreDetailActivity.this)) {

        }

        InitializeComponent();
        GetAndShowStoreDetail();
        SetupEvent();

        if(getIntent().hasExtra("EXTRAS_READ_ONLY")) {
            dummy.setFocusableInTouchMode(true);
            dummy.requestFocus();
            etTenCuaHang.setEnabled(false);
            etTenChuCuaHang.setEnabled(false);
            etDiaChi.setEnabled(false);
            etSDT.setEnabled(false);
            etDoanhThu.setEnabled(false);
            etMatHangDoiThu.setEnabled(false);
            spnLoaiCuaHang.setEnabled(false);
            btnCapNhat.setVisibility(View.GONE);
            btnQuayVe.setVisibility(View.GONE);
        }

        if(getIntent().hasExtra("EXTRAS_VIENG_THAM")){
            layoutButton.setVisibility(View.VISIBLE);
            layoutTitleCongNo.setVisibility(View.VISIBLE);
            layoutEditCongNo.setVisibility(View.VISIBLE);

            btnCapNhat.setVisibility(View.GONE);
            btnQuayVe.setVisibility(View.GONE);
        }
    }

    private void GetAndShowStoreDetail() {
        ParseQuery<StoreType> storeTypeParseQuery = StoreType.getQuery();
        storeTypeParseQuery.fromPin(DownloadUtils.PIN_STORE_TYPE);
        try {
            List<StoreType> storeTypeList = storeTypeParseQuery.find();
            String[] items = new String[storeTypeList.size()];
            for(int i = 0 ; i < storeTypeList.size(); i++) {
                items[i] = storeTypeList.get(i).getStoreTypeName();
            }
            storeTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
            spnLoaiCuaHang.setAdapter(storeTypeAdapter);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(storeID != null) {
            // Get the store image
            ParseQuery<StoreImage> imageQuery = StoreImage.getQuery();
            imageQuery.fromPin(DownloadUtils.PIN_STORE_IMAGE);
            imageQuery.whereEqualTo("store_id",storeID);

            ParseQuery<StoreImage> localImageQuery = StoreImage.getQuery();
            localImageQuery.fromPin("PIN_DRAFT_PHOTO");
            localImageQuery.whereEqualTo("store_id",storeID);
            try {
                int totalImage = imageQuery.count() + localImageQuery.count();
                tvBucAnhDaChup.setText(totalImage + getString(R.string.captureImage));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Get the store
            ParseQuery<Store> storeParseQuery = Store.getQuery();
            storeParseQuery.whereEqualTo("objectId", storeID);
            storeParseQuery.fromPin(DownloadUtils.PIN_STORE);
            storeParseQuery.getFirstInBackground(new GetCallback<Store>() {
                @Override
                public void done(Store store, ParseException e) {
                    if(e == null) {
                        try {
                            etTenCuaHang.setText(store.getName());
                            etTenChuCuaHang.setText(store.getStoreOwner());
                            etDiaChi.setText(store.getAddress());
                            etSDT.setText(store.getPhoneNumber());
                            etDoanhThu.setText(String.format(Locale.CHINESE,"%1$,.0f", store.getIncome()));
                            etMatHangDoiThu.setText(store.getCompetitor());

                            int itemPosition = -1;
                            for (int index = 0, count = storeTypeAdapter.getCount(); index < count; ++index)
                            {
                                if (storeTypeAdapter.getItem(index).equals(store.getStoreType()))
                                {
                                    itemPosition = index;
                                    break;
                                }
                            }
                            spnLoaiCuaHang.setSelection(itemPosition);
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {

                    }
                }
            });
        }
    }

    private void SetupEvent() {
        btnTrungBay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreDetailActivity.this,TrungBayActivity.class);
                intent.putExtra("EXTRAS_STORE_ID",storeID);
                intent.putExtra("EXTRAS_STORE_IMAGE_ID",store_image_id);
                if(getIntent().hasExtra("EXTRAS_READ_ONLY")) {
                    intent.putExtra("EXTRAS_READ_ONLY", true);
                }
                startActivity(intent);
            }
        });


        btnCapNhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<Store> storeParseQuery = Store.getQuery();
                storeParseQuery.fromPin(DownloadUtils.PIN_STORE);
                storeParseQuery.whereEqualTo("objectId",storeID);
                storeParseQuery.getFirstInBackground(new GetCallback<Store>() {
                    @Override
                    public void done(Store store, ParseException e) {
                         //
                        String tenCuaHang = etTenCuaHang.getText().toString();
                        String tenChuCuaHang = etTenChuCuaHang.getText().toString();
                        String diaChi = etDiaChi.getText().toString();
                        String sdt = etSDT.getText().toString();
                        String matHangDoiThuCanhTranh = etMatHangDoiThu.getText().toString();
                        String sDoanhThu = etDoanhThu.getText().toString().replace(",","").replace(".","");


                        String error_msg = ValidateInput();
                        boolean error_existed = !error_msg.equals("");

                        if(error_existed) {
                            Toast.makeText(StoreDetailActivity.this, error_msg, Toast.LENGTH_LONG).show();
                        } else {
                            double doanhThu = Double.parseDouble(sDoanhThu);
                            // Update store info
                            store.setName(tenCuaHang);
                            store.setStoreOwner(tenChuCuaHang);
                            store.setAddress(diaChi);
                            store.setIncome(doanhThu);
                            store.setPhoneNumber(sdt);
                            store.setCompetitor(matHangDoiThuCanhTranh);
                            store.setStoreType(spnLoaiCuaHang.getSelectedItem().toString());
                            // Pin in background
                            store.pinInBackground(DownloadUtils.PIN_STORE, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                }
                            });
                            // save
                            store.saveEventually();
                            Toast.makeText(StoreDetailActivity.this,getString(R.string.updateSuccess),Toast
                                    .LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
            }
        });

        btnPhanHoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedBackDialog dialog = new FeedBackDialog(StoreDetailActivity.this);
                dialog.show();
            }
        });

        btnLichSu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreDetailActivity.this, InvoiceHistoryActivity.class);
                intent.putExtra("EXTRAS_STORE_ID",storeID);
                startActivity(intent);
            }
        });

        btnDatHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreDetailActivity.this, InvoiceNewActivity.class);
                intent.putExtra("EXTRAS_STORE_ID",storeID);
                startActivity(intent);
            }
        });

        btnQuayVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void InitializeComponent() {
        etTenCuaHang = (BootstrapEditText) findViewById(R.id.activity_store_detail_et_ten);
        etTenChuCuaHang = (BootstrapEditText) findViewById(R.id.activity_store_detail_et_ten_chu_cua_hang);
        etDiaChi = (BootstrapEditText) findViewById(R.id.activity_store_detail_et_dia_chi);
        etSDT = (BootstrapEditText) findViewById(R.id.activity_store_detail_et_sdt);
        etDoanhThu = (BootstrapEditText) findViewById(R.id.activity_store_detail_et_doanh_thu);
        etMatHangDoiThu = (BootstrapEditText) findViewById(R.id.activity_store_detail_et_mat_hang_doi_thu_canh_tranh);

        btnTrungBay = (BootstrapButton) findViewById(R.id.activity_store_detail_btn_trung_bay_quay_ke);
        btnCapNhat = (BootstrapButton) findViewById(R.id.activity_store_detail_btn_cap_nhat);
        btnQuayVe = (BootstrapButton) findViewById(R.id.activity_store_detail_btn_quay_ve);

        tvBucAnhDaChup = (TextView) findViewById(R.id.activity_store_detail_tv_total_image);

        spnLoaiCuaHang = (Spinner) findViewById(R.id.activity_store_detail_spn_loai_cua_hang);

        dummy = (RelativeLayout) findViewById(R.id.activity_store_detail_dummy);

        // Invisible component
        btnDatHang = (BootstrapButton) findViewById(R.id.activity_store_detail_btn_invoice);
        btnLichSu = (BootstrapButton) findViewById(R.id.activity_store_detail_btn_invoice_history);
        btnPhanHoi = (BootstrapButton) findViewById(R.id.activity_store_detail_btn_feed_back);

        layoutButton = (LinearLayout) findViewById(R.id.activity_store_detail_layout_btn2);
        layoutEditCongNo = (LinearLayout) findViewById(R.id.activity_store_detail_layout_edit_cong_no);
        layoutTitleCongNo = (LinearLayout) findViewById(R.id.activity_store_detail_layout_title_cong_no);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ParseObject.unpinAllInBackground("PIN_DRAFT_PHOTO", new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {

                } else {

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ParseQuery<StoreImage> imageQuery = StoreImage.getQuery();
        imageQuery.fromPin(DownloadUtils.PIN_STORE_IMAGE);
        imageQuery.whereEqualTo("store_id",store_image_id);

        ParseQuery<StoreImage> localImageQuery = StoreImage.getQuery();
        localImageQuery.fromPin("PIN_DRAFT_PHOTO");
        localImageQuery.whereEqualTo("store_id",storeID);
        try {
            int totalImage = imageQuery.count() + localImageQuery.count();
            tvBucAnhDaChup.setText(totalImage + getString(R.string.captureImage));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String ValidateInput() {
        String error_msg = "";
        String tenCuaHang = etTenCuaHang.getText().toString();
        String tenChuCuaHang = etTenChuCuaHang.getText().toString();
        String diaChi = etDiaChi.getText().toString();
        String sdt = etSDT.getText().toString();
        String matHangDoiThuCanhTranh = etMatHangDoiThu.getText().toString();
        String soBucAnhCup = tvBucAnhDaChup.getText().toString();

        String sDoanhThu = etDoanhThu.getText().toString().replace(",", "").replace(".","");
        double doanhThu;
        if(tenCuaHang.trim().length() <= 0 || tenCuaHang.trim().length() > 100) {
            error_msg = getString(R.string.errorInputStoreName);
            return error_msg;
        }

        if(tenChuCuaHang.trim().length() <= 0 || tenChuCuaHang.trim().length() > 100) {
            error_msg = getString(R.string.errorInputStoreOwner);
            return error_msg;
        }

        if(diaChi.trim().length() <= 0) {
            error_msg = getString(R.string.errorInputAddress);
            return error_msg;
        }

        if(sdt.trim().length() > 11) {
            error_msg = getString(R.string.errorInputPhoneNumber);
            return error_msg;
        }

        try {
            doanhThu = Double.parseDouble(sDoanhThu);
        } catch(NumberFormatException ex) {
            error_msg = getString(R.string.errorInputIncome);
            return error_msg;
        } finally {
            if(sDoanhThu.trim().length() <= 0) {
                error_msg = getString(R.string.errorInputIncome);
                return error_msg;
            }
        }

        if(soBucAnhCup.equals("0" + getString(R.string.captureImage))) {
            error_msg = getString(R.string.errorInputImage);
            return error_msg;
        }

        if(matHangDoiThuCanhTranh.trim().length() <= 0) {
            error_msg = getString(R.string.errorInputCompetitiveProduct);
            return error_msg;
        }

        return error_msg;
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