package tw.idv.madmanchen.mdhttpasynctask;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.HashMap;

import tw.idv.madmanchen.mdhttpasynctasklib.MDHttpAsyncTask;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        HashMap<String, String> params = new HashMap<>();
        params.put("acc", "MADMANCHEN");
        params.put("psd", "Codeing");
        params.put("page", "searchagent");
        params.put("act", "searchagent_getdata");
        params.put("country", "TW");
        params.put("id", "張瑞蘭");
        params.put("addr", "");
        params.put("state", "");
        Log.e("search", params.toString());
        new MDHttpAsyncTask.Builder()
                .load("http://pub.mysoqi.com/ht_analy/0028/")
                .setLoadingView(mContext, "", "")
                .addPostData(params)
                .build()
                .startAll(new MDHttpAsyncTask.SubResponse() {
                    @Override
                    public void onResponse(Object data) {
                        if (data != null) {
                            Log.e("data", data.toString());
                        }
                    }
                });
    }
}
