package org.varunverma.androidtips;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuInstanceIDService;

public class AppInstanceIDService extends HanuInstanceIDService {

    @Override
    public void onTokenRefresh() {

        // Be Safe. Set Context.
        Application.getApplicationInstance().setContext(getApplicationContext());

        super.onTokenRefresh();

    }

}