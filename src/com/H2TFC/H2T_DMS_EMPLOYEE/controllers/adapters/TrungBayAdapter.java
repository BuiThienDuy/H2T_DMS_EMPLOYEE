package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.H2TFC.H2T_DMS_EMPLOYEE.MyApplication;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog.ViewImageFullScreen;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.StoreImage;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ImageUtils;
import com.parse.*;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class TrungBayAdapter extends ParseQueryAdapter<StoreImage> {
    private Context context;

    public TrungBayAdapter(Context context, QueryFactory<StoreImage> queryFactory) {
        super(context, queryFactory);
        this.context = context;
    }

    @Override
    public View getItemView(final StoreImage object, View v, final ViewGroup parent) {
        if (v == null) {
            //v = View.inflate(getContext(), R.layout.list_trungbay, null);
            v = LayoutInflater.from(context).inflate(R.layout.list_trungbay, null);
        }
        super.getItemView(object, v, parent);

        final ParseImageView storePhoto = (ParseImageView) v.findViewById(R.id.item_list_trungbay_iv_photo);
        ParseFile photoFile = object.getPhoto();
        String photoTitle = object.getPhotoTitle();

        if(photoTitle == null) {
            photoTitle = "";
        }

        storePhoto.setPlaceholder(v.getContext().getResources().getDrawable(R.drawable.ic_action_picture) );

        if(photoFile != null) {
            storePhoto.setParseFile(photoFile);
            storePhoto.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    storePhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ViewImageFullScreen.class);
                            intent.putExtra("EXTRA_IMAGE",((BitmapDrawable)storePhoto.getDrawable()).getBitmap());
                            intent.putExtra("EXTRA_IMAGE_ID",object.getObjectId());
                            ((Activity) context).startActivityForResult(intent, MyApplication.REQUEST_DELETE);
                        }
                    });
                }
            });
        } else {
            Log.e("photoTitle",photoTitle);
            if(photoTitle.trim().length() > 0) {
                Bitmap bm = ImageUtils.getPhotoSaved(photoTitle);
                storePhoto.setImageBitmap(bm);
            }
        }

        return v;
    }

}
