package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.dialog;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.MainActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.survey_store_point.SurveyStorePointActivity;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;
import com.parse.PushService;

/**
 * Created by c4sau on 06/04/2015.
 */
public class Receiver extends ParsePushBroadcastReceiver {

    @Override
    public void onPushOpen(Context context, Intent intent) {
        Intent i;
        if(ParseUser.getCurrentUser() != null) {
            i = new Intent(context, SurveyStorePointActivity.class);
        } else{
            i = new Intent(context, MainActivity.class);
        }
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

    }
}
