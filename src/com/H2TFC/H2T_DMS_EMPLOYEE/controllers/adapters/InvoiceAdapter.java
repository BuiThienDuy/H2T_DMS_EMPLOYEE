package com.H2TFC.H2T_DMS_EMPLOYEE.controllers.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.H2TFC.H2T_DMS_EMPLOYEE.R;
import com.H2TFC.H2T_DMS_EMPLOYEE.models.Invoice;
import com.parse.*;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class InvoiceAdapter extends ParseQueryAdapter<Invoice> {

    public InvoiceAdapter(Context context, QueryFactory<Invoice> queryFactory) {
        super(context, queryFactory);
    }

    @Override
    public View getItemView(Invoice object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_invoice, null);
        }
        super.getItemView(object, v, parent);

        TextView tvName = (TextView) v.findViewById(R.id.list_invoice_tv_name);
        tvName.setText(v.getContext().getString(R.string.invoice)+ " " +object.getObjectId());

        TextView tvEmployee = (TextView) v.findViewById(R.id.list_invoice_tv_employee);
        tvEmployee.setText(v.getContext().getString(R.string.invoiceOwner)+ ": " + object.getEmployee().getString
                ("name"));

        TextView tvStatus = (TextView) v.findViewById(R.id.list_invoice_tv_status);
        tvStatus.setText(v.getContext().getString(R.string.invoiceStatus)+ ": " + object.getInvoiceStatus());

        TextView tvDateOrder = (TextView) v.findViewById(R.id.list_invoice_tv_date_order);
        tvStatus.setText(v.getContext().getString(R.string.invoiceCreateDate)+ ": " + object.getCreatedAt());

        LinearLayout llProduct = (LinearLayout) v.findViewById(R.id.list_invoice_ll_product);
        llProduct.setBackgroundColor(Invoice.getStatusColor(object.getInvoiceStatus()));

        return v;
    }
}

