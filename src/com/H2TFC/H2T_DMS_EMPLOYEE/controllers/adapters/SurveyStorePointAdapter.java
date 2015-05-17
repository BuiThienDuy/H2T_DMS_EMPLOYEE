package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Area;
import com.parse.ParseQueryAdapter;

/**
 * Created by c4sau on 27/03/2015.
 */
public class SurveyStorePointAdapter extends ParseQueryAdapter<Area> {
    public SurveyStorePointAdapter(Context context, ParseQueryAdapter.QueryFactory<Area> queryFactory) {
        super(context, queryFactory);
    }

    @Override
    public View getItemView(Area object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_survey_store_point_area, null);
        }
        super.getItemView(object, v, parent);

        TextView tvName = (TextView) v.findViewById(R.id.list_survery_store_point_area_name);
        tvName.setText("");

        return v;
    }
}
