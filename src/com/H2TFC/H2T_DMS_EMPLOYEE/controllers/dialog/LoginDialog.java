package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point.SurveyStorePointActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.visit_store_point.VisitStorePointActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.ConnectUtils;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.parse.*;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class LoginDialog extends Dialog {
    BootstrapEditText etName;
    BootstrapEditText etPassword;
    BootstrapButton btnDone;
    Context context;

    public LoginDialog(Context context) {
        super(context);
        this.context = context;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login);
        setTitle(context.getString(R.string.loginTitle));

        etName = (BootstrapEditText) findViewById(R.id.dialog_login_et_tendangnhap);
        etPassword = (BootstrapEditText) findViewById(R.id.dialog_login_et_matkhau);
        btnDone = (BootstrapButton) findViewById(R.id.dialog_login_btn_dangnhap);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etName.getText().toString();
                String password = etPassword.getText().toString();

                boolean error_exist = false;
                StringBuilder error_msg = new StringBuilder(context.getString(R.string.errorPrefix));

                // blank username
                if (username.trim().equals("")) {
                    error_exist = true;
                    error_msg.append(context.getString(R.string.errorUsername));
                }

                // blank password
                if (password.trim().equals("")) {
                    if (error_exist) {
                        error_msg.append(context.getString(R.string.errorJoin));
                    }
                    error_exist = true;
                    error_msg.append(context.getString(R.string.errorPassword));
                }

                if (error_exist) {
                    Toast.makeText(context, error_msg.toString(), Toast.LENGTH_LONG).show();
                } else if (!ConnectUtils.hasConnectToInternet(context)) {
                    Toast.makeText(context, context.getString(R.string.needInternetAccessToLogin), Toast.LENGTH_LONG)
                            .show();
                } else {
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (e == null) {
                                if (parseUser.get("role_name").toString().equals("NVKD")) {
                                    ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
                                    parseInstallation.put("username", parseUser.getUsername());
                                    parseInstallation.put("userId", parseUser.getObjectId());
                                    parseInstallation.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            dismiss();
                                        }
                                    });
                                } else {
                                    Toast.makeText(context, context.getString(R.string.errorSignIn), Toast
                                            .LENGTH_LONG)
                                            .show();
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.userOrPasswordIncorrect), Toast
                                        .LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}