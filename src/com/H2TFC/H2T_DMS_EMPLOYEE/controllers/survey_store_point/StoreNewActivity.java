package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.MyApplication;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.StoreImage;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.StoreType;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.CustomPushUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ImageUtils;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.google.android.gms.maps.model.LatLng;
import com.parse.*;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class StoreNewActivity extends Activity {
    BootstrapButton btnDongY,btnHuy;
    BootstrapButton btnTrungBay;
    BootstrapEditText etTenCuaHang,etTenChuCuaHang,etDiaChi,etSDT,etDoanhThu,etMatHangDoiThu;
    Spinner spnLoaiCuaHang;
    TextView tvNgayTao,tvBucAnhDaChup;
    LatLng storeLocation;
    List<StoreType> storeTypeList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_new);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.createSurveyStorePointTitle));

        if(getIntent().hasExtra("Lat") && getIntent().hasExtra("Lng")) {
            storeLocation = new LatLng(getIntent().getDoubleExtra("Lat",0),getIntent().getDoubleExtra("Lng",0));
        }

        storeTypeList = new ArrayList<StoreType>();

        InitializeComponent();
        SetupEvent();
    }

    private void SetupEvent() {
        btnDongY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenCuaHang = etTenCuaHang.getText().toString();
                String tenChuCuaHang = etTenChuCuaHang.getText().toString();
                String diaChi = etDiaChi.getText().toString();
                String sdt = etSDT.getText().toString();
                String matHangDoiThuCanhTranh = etMatHangDoiThu.getText().toString();

                String error_msg = ValidateInput();
                boolean error_existed = !error_msg.trim().equals("") || error_msg.trim().length() != 0;

                if(error_existed) {
                    Log.d("error_msg",error_msg);
                    Toast.makeText(StoreNewActivity.this,error_msg,Toast.LENGTH_LONG).show();
                } else {
                    double doanhThu = Double.parseDouble(etDoanhThu.getText().toString());
                    final Store store = new Store();
                    store.setName(tenCuaHang);
                    store.setStoreOwner(tenChuCuaHang);
                    store.setAddress(diaChi);
                    store.setIncome(doanhThu);

                    store.setPhoneNumber(sdt);
                    store.setCompetitor(matHangDoiThuCanhTranh);
                    store.setStatus(Store.StoreStatus.KHAO_SAT.name());
                    store.setStoreType(spnLoaiCuaHang.getSelectedItem().toString());

                    for(StoreType storeType : storeTypeList) {
                        if(storeType.getStoreTypeName().equals(spnLoaiCuaHang.getSelectedItem().toString())) {
                            store.setMaxDebt(storeType.getDefaultDebt());
                        }
                    }


                    final String uuid = UUID.randomUUID().toString();
                    store.setStoreImageId(uuid);
                    if (storeLocation != null) {
                        store.setLocationPoint(new ParseGeoPoint(storeLocation.latitude, storeLocation.longitude));
                    }

                    store.setEmployeeId(ParseUser.getCurrentUser().getObjectId());

                    // Get store image from pin
                    // save
                    store.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            ParseQuery<StoreImage> queryStore = StoreImage.getQuery();
                            queryStore.whereEqualTo("photo_synched",false);
                            queryStore.fromPin("PIN_DRAFT_PHOTO");
                            queryStore.findInBackground(new FindCallback<StoreImage>() {
                                @Override
                                public void done(List<StoreImage> list, ParseException e) {
                                    if (e == null) {
                                        Log.e("storeImage","Save eventually size = " + list.size());
                                        for (final StoreImage storeImage : list) {
                                            Bitmap photoOnSdCard = ImageUtils.getPhotoSaved(storeImage.getPhotoTitle
                                                    ());
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            photoOnSdCard.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                            byte[] bitmapdata = stream.toByteArray();

                                            storeImage.setStoreId(store.getStoreImageId());
                                            storeImage.setEmployeeId(ParseUser.getCurrentUser().getObjectId());

                                            final ParseFile photo = new ParseFile("parse_photo.png", bitmapdata);
                                            photo.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        storeImage.setPhoto(photo);
                                                        storeImage.setPhotoSynched(true);
                                                        storeImage.saveEventually();
                                                        storeImage.pinInBackground(DownloadUtils.PIN_STORE_IMAGE);
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        Log.d("StoreNewActivity", getString(R.string.errorAddStorePoint));
                                    }
                                }
                            });
                        }
                    });

                    store.pinInBackground(DownloadUtils.PIN_STORE, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            // Send push notification to NVQL
                            String employeeName = (String) ParseUser.getCurrentUser().get("name");
                            CustomPushUtils.sendMessageToEmployee(ParseUser.getCurrentUser().getString("manager_id"),
                                    employeeName + getString(R.string.haveCreateSurveyPoint));
                            Toast.makeText(StoreNewActivity.this, getString(R.string.addStorePointSuccess), Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    });

                }
            }
        });

        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnTrungBay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreNewActivity.this,TrungBayActivity.class);
                startActivityForResult(intent, MyApplication.REQUEST_ADD_NEW);
            }
        });
    }

    private String ValidateInput() {
        String error_msg = "";
        String tenCuaHang = etTenCuaHang.getText().toString();
        String tenChuCuaHang = etTenChuCuaHang.getText().toString();
        String diaChi = etDiaChi.getText().toString();
        String sdt = etSDT.getText().toString();
        String matHangDoiThuCanhTranh = etMatHangDoiThu.getText().toString();
        String soBucAnhCup = tvBucAnhDaChup.getText().toString();

        String sDoanhThu = etDoanhThu.getText().toString();
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

    private void InitializeComponent() {
        btnDongY = (BootstrapButton) findViewById(R.id.activity_store_new_btn_dong_y);
        btnHuy = (BootstrapButton) findViewById(R.id.activity_store_new_btn_huy_bo);

        btnTrungBay = (BootstrapButton) findViewById(R.id.activity_store_new_btn_trung_bay_quay_ke);

        etTenCuaHang = (BootstrapEditText) findViewById(R.id.activity_store_new_et_ten);
        etTenChuCuaHang = (BootstrapEditText) findViewById(R.id.activity_store_new_et_ten_chu_cua_hang);
        etDiaChi = (BootstrapEditText) findViewById(R.id.activity_store_new_et_dia_chi);
        etDoanhThu = (BootstrapEditText) findViewById(R.id.activity_store_new_et_doanh_thu);
        etSDT = (BootstrapEditText) findViewById(R.id.activity_store_new_et_sdt);
        etMatHangDoiThu = (BootstrapEditText) findViewById(R.id.activity_store_new_et_mat_hang_doi_thu_canh_tranh);

        spnLoaiCuaHang = (Spinner) findViewById(R.id.activity_store_new_spn_loai_cua_hang);

        tvBucAnhDaChup = (TextView) findViewById(R.id.activity_store_new_tv_total_image);

        ParseQuery<StoreType> storeTypeParseQuery = StoreType.getQuery();
        storeTypeParseQuery.fromPin(DownloadUtils.PIN_STORE_TYPE);
        try {
            List<StoreType> storeTypeList = storeTypeParseQuery.find();
            this.storeTypeList = storeTypeList;
            String[] items = new String[storeTypeList.size()];
            for(int i = 0 ; i < storeTypeList.size(); i++) {
                items[i] = storeTypeList.get(i).getStoreTypeName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
            spnLoaiCuaHang.setAdapter(adapter);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvNgayTao = (TextView) findViewById(R.id.activity_store_new_tv_ngaytao);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        String currentDate = sdf.format(new Date());
        tvNgayTao.setText(tvNgayTao.getText() + currentDate);


        ParseQuery<StoreImage> queryStore = StoreImage.getQuery();
        queryStore.fromPin("PIN_DRAFT_PHOTO");
        try {
            tvBucAnhDaChup.setText(queryStore.count() + getString(R.string.captureImage));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == MyApplication.REQUEST_ADD_NEW) {
                ParseQuery<StoreImage> queryStore = StoreImage.getQuery();
                queryStore.whereEqualTo("photo_synched", false);
                queryStore.fromPin("PIN_DRAFT_PHOTO");
                try {
                    tvBucAnhDaChup.setText(queryStore.count() + getString(R.string.captureImage));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if(resultCode == RESULT_CANCELED) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ParseQuery<StoreImage> queryStore = StoreImage.getQuery();
        queryStore.fromPin("PIN_DRAFT_PHOTO");
        try {
            tvBucAnhDaChup.setText(queryStore.count() + getString(R.string.captureImage));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ParseQuery<StoreImage> query = StoreImage.getQuery();
        query.whereEqualTo("photo_synched", true);
        query.fromPin("PIN_DRAFT_PHOTO");
        query.findInBackground(new FindCallback<StoreImage>() {
            @Override
            public void done(List<StoreImage> list, ParseException e) {
                for (StoreImage storeImage : list) {
                    storeImage.unpinInBackground("PIN_DRAFT_PHOTO");
                }
            }
        });

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