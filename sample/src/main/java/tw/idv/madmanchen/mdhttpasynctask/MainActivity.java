package tw.idv.madmanchen.mdhttpasynctask;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import tw.idv.madmanchen.library.MDHttpAsyncTask;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        new MDHttpAsyncTask.Builder()
                .load("http://pub.mysoqi.com/ht_agent/060/")
                .addPostData("acc", "T221142968")
                .addPostData("psd", "1123")
                .build()
                .startAll(new MDHttpAsyncTask.SubResponse() {
                    @Override
                    public void onResponse(Object data) {
                        Log.e("data", data.toString());
                    }
                });
    }
}
