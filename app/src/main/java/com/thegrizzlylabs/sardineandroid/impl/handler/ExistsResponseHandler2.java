package com.thegrizzlylabs.sardineandroid.impl.handler;

import com.thegrizzlylabs.sardineandroid.impl.SardineException;

import okhttp3.Response;

/**
 * Created by guillaume on 20/11/2017.
 */

public class ExistsResponseHandler2 extends ValidatingResponseHandler<Boolean>
{
    @Override
    public Boolean handleResponse(Response response) throws SardineException {
        if (!response.isSuccessful() && response.code() >= 400&&response.code()<500) {
            return false;
        }
        validateResponse(response);
        return true;
    }
}
