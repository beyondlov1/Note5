package com.beyond.note5.sync.webdav;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class BasicAuthenticator implements Authenticator {
    private String username;
    private String password;

    public BasicAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (response.request().header("Authorization") != null) {
       return null; // Give up, we've already failed to authenticate.
     }
 
     String credential = Credentials.basic(username,password);
     return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
    }
}
