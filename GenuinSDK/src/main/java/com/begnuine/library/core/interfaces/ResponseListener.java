package com.begnuine.library.core.interfaces;

/* * ****************************************************************************
 * Author: Iconflux Technologies
 *
 * Created: 2/1/2017
 * Purpose: For the handling of event which contains two options success and failure, mainly used for api response handling.
 *
 * Change Log:
 * ===========
 * Name                          Change Date            Purpose
 * Vishal Nirmal               2/1/2017              Created.
 * ***************************************************************************** */
public interface ResponseListener {
    void onSuccess(String response);

    void onFailure(String error);
}
