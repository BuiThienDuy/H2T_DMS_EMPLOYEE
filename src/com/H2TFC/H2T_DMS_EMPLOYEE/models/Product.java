package com.H2TFC.H2T_DMS_EMPLOYEE.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/*
 * Copyright (C) 2015 H2TFC Team, LLC
 * thanhduongpham4293@gmail.com
 * nhatnang93@gmail.com
 * buithienduy93@gmail.com
 * All rights reserved
 */
@ParseClassName("Product")
public class Product extends ParseObject {
    public static enum ProductStatus {
        KHOA
    }

    // 1. Product name
    public String getProductName() {
        return getString("name");
    }

    public void setName(String productName) {
        put("name", productName);
    }

    // 2. Product unit
    public String getUnit() {
        return getString("unit");
    }

    public void setUnit(String unit) {
        put("unit", unit);
    }

    // 3. Product price
    public double getPrice() {
        return getDouble("price");
    }

    public void setPrice(double price) {
        put("price", price);
    }

    // 4. Product status
    public String getStatus() {
        return getString("status");
    }

    public void setStatus(String status) {
        put("status", status);
    }

    // 5. Product photo
    public ParseFile getPhoto() {
        return getParseFile("photo");
    }

    public void setPhoto(ParseFile photo) {
        put("photo", photo);
    }

    // 6. Query
    public static ParseQuery<Product> getQuery() {
        return ParseQuery.getQuery(Product.class);
    }
}
