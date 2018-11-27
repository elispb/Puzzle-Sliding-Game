package mobile.labs.acw;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ErrorChecking {
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void LaunchErrorScreen(Context context, String errorMsg){
        Intent intent = new Intent(context, ErrorScreen.class);
        intent.putExtra("ErrorMsg",errorMsg);
        context.startActivity(intent);
    }
}
