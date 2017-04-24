package tw.idv.madmanchen.mdhttpasynctask;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import tw.idv.madmanchen.library.MDHttpAsyncTask;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        new MDHttpAsyncTask.Builder()
                .load("http://pub.mysoqi.com/isoqi_us/?acc=A161779&psd=08278023&usr_type=agent")
                .setRequestType(MDHttpAsyncTask.TEXT)
                .build()
                .startAll(new MDHttpAsyncTask.SubResponse() {
                    @Override
                    public void onResponse(Object data) {
                        Toast.makeText(mContext, data.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
