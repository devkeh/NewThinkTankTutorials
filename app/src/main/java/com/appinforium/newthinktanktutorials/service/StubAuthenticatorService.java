package com.appinforium.newthinktanktutorials.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.appinforium.newthinktanktutorials.StubAuthenticator;


public class StubAuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private StubAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new StubAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
