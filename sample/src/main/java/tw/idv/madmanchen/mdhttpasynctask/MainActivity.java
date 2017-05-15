package tw.idv.madmanchen.mdhttpasynctask;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import tw.idv.madmanchen.mdhttpasynctasklib.MDHttpAsyncTaskU;
import tw.idv.madmanchen.mdhttpasynctasklib.SubResponse;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        new MDHttpAsyncTaskU()
                .load("https://developer.android.com/studio/images/studio-icon_2x.png")
                .setLoadingView(mContext, "", "")
                .setRequestType(MDHttpAsyncTaskU.FILE)
                .start(new SubResponse() {
                    @Override
                    public void onSuccess(Object data) {
                        Log.i(TAG, "onSuccess: " + data.toString());
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, error);
                    }
                }, true);
    }
}
