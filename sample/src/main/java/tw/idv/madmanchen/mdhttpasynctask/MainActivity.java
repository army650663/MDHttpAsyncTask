package tw.idv.madmanchen.mdhttpasynctask;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import tw.idv.madmanchen.mdhttpasynctasklib.MDHttpAsyncTask;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        syncNum();

    }

    private void syncNum() {
        new MDHttpAsyncTask.Builder()
                .load(urls)
                .setLoadingView(mContext, "", "Loading")
                .setRequestType(MDHttpAsyncTask.UPLOAD_FILE)
                .addUploadFile(Files)
                .build()
                .startAll(new MDHttpAsyncTask.SubResponse() {
                    @Override
                    public void onResponse(Object data) {
                        if (data != null) {
                            String[] dataArr = (String[]) data;
                            try {
                                if (dataArr[0] != null) {
                                    JSONObject hmJObj = new JSONObject(dataArr[0]);
                                    int hmNum = hmJObj.optInt("totalnum", 0);
                                    Log.e("hmNum", hmNum + "");

                                }

                                if (dataArr[1] != null) {
                                    JSONObject docJObj = new JSONObject(dataArr[1]);
                                    int docNum = docJObj.optInt("totalnum", 0);
                                    Log.e("docNum", docNum + "");
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        syncNum();
                                    }
                                }, 6000);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
}
