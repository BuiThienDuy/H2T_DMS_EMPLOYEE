package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point.StoreDetailActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Feedback;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Store;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.CustomPushUtils;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class FeedBackDialog extends Dialog {
    Context context;
    BootstrapEditText etTitle,etContent;
    BootstrapButton btnDone,btnCancel;

    public FeedBackDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(context.getString(R.string.titleFeedBackCustomer));
        this.setContentView(R.layout.dialog_feed_back);

        btnDone = (BootstrapButton) findViewById(R.id.dialog_feed_back_btn_done);
        btnCancel = (BootstrapButton) findViewById(R.id.dialog_feed_back_btn_cancel);
        etTitle = (BootstrapEditText) findViewById(R.id.dialog_feed_back_et_title);
        etContent = (BootstrapEditText) findViewById(R.id.dialog_feed_back_et_description);


        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString();
                String content = etContent.getText().toString();

                boolean errorExisted = false;
                String errorMessage = "";

                if(title.trim().equals("")) {
                    errorExisted = true;
                    errorMessage = "Vui lòng nhập tiêu đề phản hồi.";
                }
                if(content.trim().equals("")) {
                    errorExisted = true;
                    errorMessage = "Vui lòng nhập nội dung phản hồi.";
                }

                if(errorExisted) {
                    Toast.makeText(context,errorMessage,Toast.LENGTH_LONG).show();
                } else {
                    Feedback feedback = new Feedback();
                    feedback.setTitle(title);
                    feedback.setDescription(content);
                    feedback.setStatus(Feedback.MOI_TAO);
                    feedback.setManagerId(ParseUser.getCurrentUser().getString("manager_id"));
                    feedback.setEmployeeId(ParseUser.getCurrentUser().getObjectId());

                    final StoreDetailActivity activity = (StoreDetailActivity) context;
                    feedback.setStoreId(activity.storeID);

                    feedback.saveEventually();

                    feedback.pinInBackground(DownloadUtils.PIN_FEEDBACK, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                Toast.makeText(activity, activity.getString(R.string.sentFeedbackSuccess),
                                        Toast.LENGTH_SHORT).show();
                                // Send push notification to NVQL
                                try {
                                    String storeName = Store.getQuery().whereEqualTo("objectId",activity.storeID).fromPin
                                            (DownloadUtils.PIN_STORE).getFirst().getName();

                                CustomPushUtils.sendMessageToEmployee(ParseUser.getCurrentUser().getString
                                                ("manager_id"), storeName + activity.getString(R.string.haveSentFeedbackToYou)
                                );
                                dismiss();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
