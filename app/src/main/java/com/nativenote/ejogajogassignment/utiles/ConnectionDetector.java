package com.nativenote.ejogajogassignment.utiles;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {

    /**
     * Checking for all possible internet providers
     **/
    public static boolean isNetworkPresent(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }

		/*ToastMsg.Toast(context,
				context.getString(R.string.tst_connection_lost));*/
        return false;
    }
}
