package com.H2TFC.H2T_DMS_EMPLOYEE.utils;

import com.H2TFC.H2T_DMS_EMPLOYEE.models.*;
import com.parse.*;

import java.util.List;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
public class DownloadUtils {
    public static final String PIN_AREA = "PIN_AREA_DOWNLOAD";
    public static final String PIN_EMPLOYEE = "PIN_EMPLOYEE_DOWNLOAD";
    public static final String PIN_STORE = "PIN_STORE_DOWNLOAD";
    public static final String PIN_STORE_IMAGE = "PIN_STORE_IMAGE_DOWNLOAD";
    public static final String PIN_STORE_TYPE = "PIN_STORE_TYPE_DOWNLOAD";
    public static final String PIN_PRODUCT = "PIN_PRODUCT_DOWNLOAD";
    public static final String PIN_INVOICE = "PIN_INVOICE_DOWNLOAD";
    public static final String PIN_ATTENDANCE = "PIN_ATTENDANCE_DOWNLOAD";
    public static final String PIN_FEEDBACK = "PIN_FEEDBACK_DOWNLOAD";
    public static final String PIN_PROMOTION = "PIN_PROMOTION_DOWNLOAD";
    public static final String PIN_PRODUCT_PURCHASE = "PIN_PRODUCT_PURCHASE_DOWNLOAD";

    public static void DownloadParseArea(final SaveCallback saveCallback) {

        ParseQuery<Area> query = Area.getQuery();
        query.whereEqualTo("employee_id",ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<Area>() {
            @Override
            public void done(List<Area> list, ParseException e) {
                if(e == null) {
                    ParseObject.unpinAllInBackground(PIN_AREA);
                    ParseObject.pinAllInBackground(PIN_AREA, list, saveCallback);
                }
                }
        });
    }

    public static void DownloadParseEmployee(final SaveCallback saveCallback) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if(e==null) {
                    ParseObject.unpinAllInBackground(PIN_EMPLOYEE);
                    ParseObject.pinAllInBackground(PIN_EMPLOYEE, list, saveCallback);
                }
                }
        });
    }

    public static void DownloadParseStore(final SaveCallback saveCallback) {
        ParseQuery<Store> query = Store.getQuery();
        //query.whereEqualTo("employee_id",ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<Store>() {
            @Override
            public void done(List<Store> list, ParseException e) {
                if(e == null) {
                    ParseObject.unpinAllInBackground(PIN_STORE);
                    ParseObject.pinAllInBackground(PIN_STORE, list, saveCallback);
                }
            }
        });
    }

    public static void DownloadParseStoreImage(final SaveCallback saveCallback) {
        ParseQuery<StoreImage> query = StoreImage.getQuery();
        query.whereEqualTo("employee_id",ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<StoreImage>() {
            @Override
            public void done(List<StoreImage> list, ParseException e) {
                if(e == null) {
                    ParseObject.unpinAllInBackground(PIN_STORE_IMAGE);
                    ParseObject.pinAllInBackground(PIN_STORE_IMAGE, list, saveCallback);
                }
                }
        });
    }

    public static void DownloadParseStoreType(final SaveCallback saveCallback) {
        ParseQuery<StoreType> query = StoreType.getQuery();
        query.findInBackground(new FindCallback<StoreType>() {
            @Override
            public void done(List<StoreType> list, ParseException e) {
                if(e == null) {
                    ParseObject.unpinAllInBackground(PIN_STORE_TYPE);
                    ParseObject.pinAllInBackground(PIN_STORE_TYPE, list, saveCallback);
                }
            }
        });
    }

    public static void DownloadParseProduct(final SaveCallback saveCallback) {
        ParseQuery<Product> query = Product.getQuery();
        query.findInBackground(new FindCallback<Product>() {
            @Override
            public void done(List<Product> list, ParseException e) {
                if(e == null) {
                    ParseObject.unpinAllInBackground(PIN_PRODUCT);
                    ParseObject.pinAllInBackground(PIN_PRODUCT, list, saveCallback);
                }
            }
        });
    }

    public static void DownloadParseInvoice(final SaveCallback saveCallback) {
        ParseQuery<Invoice> query = Invoice.getQuery();
        query.findInBackground(new FindCallback<Invoice>() {
            @Override
            public void done(List<Invoice> list, ParseException e) {
                if(e == null) {
                    ParseObject.unpinAllInBackground(PIN_INVOICE);
                    ParseObject.pinAllInBackground(PIN_INVOICE, list, saveCallback);
                }
                }
        });
    }

    public static void DownloadParseAttendance(final SaveCallback saveCallback) {
        ParseQuery<Attendance> query = Attendance.getQuery();
        query.findInBackground(new FindCallback<Attendance>() {
            @Override
            public void done(List<Attendance> list, ParseException e) {
                if(e==null) {
                    ParseObject.unpinAllInBackground(PIN_ATTENDANCE);
                    ParseObject.pinAllInBackground(PIN_ATTENDANCE, list, saveCallback);
                }
            }
        });
    }

    public static void DownloadParseFeedback(final SaveCallback saveCallback) {
        ParseQuery<Feedback> query = Feedback.getQuery();
        query.findInBackground(new FindCallback<Feedback>() {
            @Override
            public void done(List<Feedback> list, ParseException e) {
                if(e==null) {
                    ParseObject.unpinAllInBackground(PIN_FEEDBACK);
                    ParseObject.pinAllInBackground(PIN_FEEDBACK, list, saveCallback);
                }
            }
        });
    }

    public static void DownloadParsePromotion(final SaveCallback saveCallback) {
        ParseQuery<Promotion> query = Promotion.getQuery();
        query.findInBackground(new FindCallback<Promotion>() {
            @Override
            public void done(List<Promotion> list, ParseException e) {
                if(e==null) {
                    ParseObject.unpinAllInBackground(PIN_PROMOTION);
                    ParseObject.pinAllInBackground(PIN_PROMOTION, list, saveCallback);
                }
            }
        });
    }


    public static void DownloadParseProductPurchase(final SaveCallback saveCallback) {
        ParseQuery<ProductPurchase> query = ProductPurchase.getQuery();
        query.findInBackground(new FindCallback<ProductPurchase>() {
            @Override
            public void done(List<ProductPurchase> list, ParseException e) {
                if(e==null) {
                    ParseObject.unpinAllInBackground(PIN_PRODUCT_PURCHASE);
                    ParseObject.pinAllInBackground(PIN_PRODUCT_PURCHASE, list, saveCallback);
                }
            }
        });
    }

}
