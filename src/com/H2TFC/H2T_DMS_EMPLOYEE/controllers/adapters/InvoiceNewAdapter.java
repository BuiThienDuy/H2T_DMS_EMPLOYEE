package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.controllers.invoice.InvoiceNewActivity;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Product;
import com.H2TFC.H2T_DMS_EMPLOYEE.utils.DownloadUtils;
import com.parse.*;

import java.util.Locale;

/**
 * Created by c4sau on 10/04/2015.
 */
public class InvoiceNewAdapter extends ParseQueryAdapter<Product> {


    public InvoiceNewAdapter(Context context, QueryFactory<Product> queryFactory) {
        super(context, queryFactory);
    }

    @Override
    public View getItemView(final Product object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_product, null);
        }
        super.getItemView(object, v, parent);
        TextView tvName = (TextView) v.findViewById(R.id.list_product_tv_name);
        tvName.setText(object.getProductName());
        TextView tvUnit = (TextView) v.findViewById(R.id.list_product_tv_unit);
        tvUnit.setText(object.getUnit());
        TextView tvPrice = (TextView) v.findViewById(R.id.list_product_tv_price);
        tvPrice.setText(String.format(Locale.CHINESE,"%1$,.0f", object.getPrice()) + " " + v.getContext().getString(R.string.VND));


        ParseImageView productPhoto = (ParseImageView) v.findViewById(R.id.list_product_iv_product);
        ParseFile photoFile = object.getParseFile("photo");

        productPhoto.setPlaceholder(getContext().getResources().getDrawable(R.drawable.ic_contact_empty));

        if (photoFile != null) {
            productPhoto.setParseFile(photoFile);
            productPhoto.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    // nothing to do
                }
            });
        }

        final EditText etAmount = (EditText) v.findViewById(R.id.list_product_et_amount);
        final View finalV = v;
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.equals("")) {
                    etAmount.setText("0");
                }

                InvoiceNewActivity invoiceNewActivity = (InvoiceNewActivity) finalV.getContext();
                invoiceNewActivity.generateResult();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return v;
    }


}
