package com.appinforium.newthinktanktutorials.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.appinforium.newthinktanktutorials.adapter.NewThinkTankSyncAdapter;

public class NewThinkTankSyncService extends Service {

    private static final Object syncAdapterLock = new Object();
    private static NewThinkTankSyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new NewThinkTankSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
