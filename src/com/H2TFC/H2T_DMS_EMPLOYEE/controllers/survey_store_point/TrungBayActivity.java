package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.H2TFC.H2T_DMS_EMPLOYEE.MyApplication;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters.TrungBayAdapter;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.StoreImage;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ImageUtils;
import com.parse.*;

import java.util.List;
import java.util.UUID;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class TrungBayActivity extends Activity {
    GridView gv_StoreImage;
    TrungBayAdapter storeImageAdapter;
    TextView tvEmpty;
    ProgressBar progressBarStoreImage;

    String store_id;
    String store_image_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trung_bay);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.trungBayTitle));

        if(getIntent().hasExtra("EXTRAS_STORE_IMAGE_ID")) {
            store_image_id = getIntent().getStringExtra("EXTRAS_STORE_IMAGE_ID");
        }

        if(getIntent().hasExtra("EXTRAS_STORE_ID")) {
            store_id = getIntent().getStringExtra("EXTRAS_STORE_ID");
        }
        InitializeComponent();
        SetUpListView();
    }

    private void SetUpListView() {
        // Query data from local data store
        ParseQueryAdapter.QueryFactory<StoreImage> factory = new ParseQueryAdapter.QueryFactory<StoreImage>() {
            @Override
            public ParseQuery<StoreImage> create() {
                ParseQuery<StoreImage> query = StoreImage.getQuery();
                if(store_id != null) {
                    query.fromPin(DownloadUtils.PIN_STORE_IMAGE);
                    query.whereEqualTo("store_id",store_image_id);
                } else {
                    query.fromPin("PIN_DRAFT_PHOTO");
                }
                query.orderByDescending("createdAt");
                return query;
            }
        };

        // Set list adapter
        storeImageAdapter = new TrungBayAdapter(TrungBayActivity.this, factory);

        storeImageAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<StoreImage>() {
            @Override
            public void onLoading() {
                progressBarStoreImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoaded(List<StoreImage> list, Exception e) {
                progressBarStoreImage.setVisibility(View.GONE);
            }
        });
        gv_StoreImage.setEmptyView(tvEmpty);
        gv_StoreImage.setAdapter(storeImageAdapter);
    }

    private void InitializeComponent() {
        gv_StoreImage = (GridView) findViewById(R.id.activity_trungbay_grid);
        tvEmpty = (TextView) findViewById(R.id.activity_trungbay_tv_empty);
        progressBarStoreImage = (ProgressBar) findViewById(R.id.activity_trungbay_management_progressbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!getIntent().hasExtra("EXTRAS_READ_ONLY")) {
            getMenuInflater().inflate(R.menu.action_bar_trungbay,menu);
        }


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_bar_trungbay_new: {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,  100);
                break;
            }
            case R.id.action_bar_trungbay_done: {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == MyApplication.REQUEST_ADD_NEW && data != null) {
                Bitmap bm = (Bitmap) data.getExtras().get("data");
                String uuid = UUID.randomUUID().toString();
                ImageUtils.SaveImage(bm,"storeimage-" + uuid);

                StoreImage storeImage = new StoreImage();
                storeImage.setName("");
                storeImage.setPhotoSynched(false);
                storeImage.setPhotoTitle("storeimage-" + uuid);

                if(store_id != null) {
                    storeImage.setEmployeeId(ParseUser.getCurrentUser().getObjectId());
                    storeImage.setStoreId(store_image_id);
                    storeImage.pinInBackground(DownloadUtils.PIN_STORE_IMAGE, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            storeImageAdapter.loadObjects();
                            storeImageAdapter.notifyDataSetChanged();
                            gv_StoreImage.invalidateViews();
                        }
                    });
                    storeImage.saveEventually();
                } else {
                    storeImage.pinInBackground("PIN_DRAFT_PHOTO", new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                storeImageAdapter.loadObjects();
                                storeImageAdapter.notifyDataSetChanged();
                                gv_StoreImage.invalidateViews();
                            } else {
                                Log.d("TrungBayActivity", e.getMessage());
                            }
                        }
                    });
                }



            }
            if(requestCode == MyApplication.REQUEST_DELETE) {
                storeImageAdapter.loadObjects();
                storeImageAdapter.notifyDataSetChanged();
                gv_StoreImage.invalidateViews();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        storeImageAdapter.loadObjects();
        storeImageAdapter.notifyDataSetChanged();
        gv_StoreImage.invalidateViews();
    }


}