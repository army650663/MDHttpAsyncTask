package tw.idv.madmanchen.mdhttpasynctasklib;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author:      chenshaowei
 * Version      V1.0
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2017/3/28      chenshaowei         V1.0.0          Create
 * Why & What is modified:
 */

public final class MDHttpAsyncTaskU extends AsyncTask<String, Number, Object> {
    // 用於 UI Thread
    private Handler mHandler = new Handler();
    // 請求的 URL 陣列
    private String[] mUrls;
    // URL 連線
    private HttpURLConnection mURLConnection;
    // Http 請求的 Method
    private String mMethod = "GET";
    // 讀取逾時時間
    private int mReadTimeout = 10_000;
    // 連線逾時時間
    private int mConnTimeout = 10_000;
    // 所要回傳的
    private SubResponse mSubResponse;
    // Http POST 的資料 Map
    private Map<String, String> mPostData = new HashMap<>();
    // 所要回傳的類型
    private int mType = TEXT;
    // 下載檔案的路徑
    private String mDownloadPath;
    // 讀取視窗
    private ProgressDialog mLoadingView;
    // 是否顯示讀取視窗
    private boolean mIsShowLoadingView;
    private static AtomicBoolean mIsShowingLoadingView = new AtomicBoolean(false);
    // 是否可取消下載
    private boolean mCancelable;
    // 是否覆寫存在的檔案
    private boolean mOverWrite;

    public static final int TEXT = 0;
    public static final int TEXT_ARRAY = 1;
    public static final int FILE = 2;
    public static final int FILE_ARRAY = 3;
    public static final int UPLOAD_FILE = 4;

    @IntDef({TEXT, TEXT_ARRAY, FILE, FILE_ARRAY, UPLOAD_FILE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    // Http form data 表單格式
    private static final String BOUNDARY = "==================================";
    private static final String HYPHENS = "--";
    private static final String CRLF = "\r\n";

    // 上傳檔案用
    private List<File> mUploadFileList = new ArrayList<>();

    // 自定義線程池
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    // 記錄任務時間
    private long startTime;

    /**
     * 所要連線的 URL 陣列
     *
     * @return Builder
     */
    public
    @NonNull
    MDHttpAsyncTaskU load(String... url) {
        this.mUrls = url;
        return this;
    }

    /**
     * 加入 Http post 參數
     *
     * @param postData 以 HashMap 傳入
     * @return Builder
     */
    public MDHttpAsyncTaskU addPostData(HashMap<String, String> postData) {
        mMethod = "POST";
        mPostData.putAll(postData);
        return this;
    }

    /**
     * 加入 Http post 參數
     *
     * @param key   參數名稱
     * @param value 參數內容
     * @return Builder
     */
    public MDHttpAsyncTaskU addPostData(String key, String value) {
        mMethod = "POST";
        mPostData.put(key, value);
        return this;
    }

    /**
     * 設定 Http 連線方式
     * 預設為 GET
     *
     * @param method GET, POST
     * @return Builder
     */
    public MDHttpAsyncTaskU setMethod(String method) {
        this.mMethod = method.toUpperCase();
        return this;
    }

    /**
     * 設定 Http 讀取逾時時間
     *
     * @param timeout 單位為微秒
     * @return Builder
     */
    public MDHttpAsyncTaskU setReadTimeout(int timeout) {
        mReadTimeout = timeout;
        return this;
    }

    /**
     * 設定 Http 連線逾時時間
     *
     * @param timeout 單位為微秒
     * @return Builder
     */
    public MDHttpAsyncTaskU setConnectTimeout(int timeout) {
        mConnTimeout = timeout;
        return this;
    }


    /**
     * 設定請求回傳類型
     *
     * @param type 回傳類型 Enum
     * @return Builder
     */
    public MDHttpAsyncTaskU setRequestType(@Type int type) {
        mType = type;
        return this;
    }

    /**
     * 設定下載路徑
     *
     * @param path 下載路徑
     * @return Builder
     */
    public MDHttpAsyncTaskU setDownloadPath(String path) {
        mDownloadPath = path;
        return this;
    }

    /**
     * 設定讀取視窗
     *
     * @param loadingView 傳入 ProgressDialog
     * @return Builder
     */
    public MDHttpAsyncTaskU setLoadingView(ProgressDialog loadingView) {
        mIsShowLoadingView = true;
        mLoadingView = loadingView;

        return this;
    }

    /**
     * 設定讀取視窗
     *
     * @param context 場景物件
     * @param title   標題
     * @param msg     訊息
     * @return Builder
     */
    public MDHttpAsyncTaskU setLoadingView(Context context, String title, String msg) {
        mIsShowLoadingView = true;
        mLoadingView = new ProgressDialog(context);
        mLoadingView.setCancelable(false);
        mLoadingView.setTitle(title);
        mLoadingView.setMessage(msg);
        return this;
    }

    /**
     * 是否可取消任務
     *
     * @param cancelable 是否可取消
     * @return Builder
     */
    public MDHttpAsyncTaskU cancelable(boolean cancelable) {
        mCancelable = cancelable;
        return this;
    }

    /**
     * 是否覆寫已存在的檔案
     *
     * @param overWrite 是否可覆寫
     * @return Builder
     */
    public MDHttpAsyncTaskU overWriteFile(boolean overWrite) {
        mOverWrite = overWrite;
        return this;
    }

    /**
     * 加入上傳檔案
     *
     * @param files 上傳檔案陣列
     * @return Builder
     */
    public MDHttpAsyncTaskU addUploadFile(File... files) {
        mMethod = "POST";
        Collections.addAll(mUploadFileList, files);
        return this;
    }

    /**
     * 加入上傳檔案
     *
     * @param fileList 上傳檔案 List
     * @return Builder
     */
    public MDHttpAsyncTaskU addUploadFile(List<File> fileList) {
        mMethod = "POST";
        for (File file : fileList) {
            mUploadFileList.add(file);
        }
        return this;
    }

    /**
     * 初始化連線
     *
     * @param url 連線網址
     */
    private void initConnect(String url) {
        // 檢查是否為 Http 連線
        if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
            try {
                URL fUrl = new URL(url);
                mURLConnection = (HttpURLConnection) fUrl.openConnection();
                // 設定連線逾時時間
                mURLConnection.setConnectTimeout(mConnTimeout);
                // 設定讀取逾時時間
                mURLConnection.setReadTimeout(mReadTimeout);
                // 判斷 Http 所設定的 Method
                switch (mMethod) {
                    case "GET":
                        mURLConnection.setDoInput(true);
                        mURLConnection.setUseCaches(false);
                        break;

                    case "POST":
                        mURLConnection.setDoInput(true);
                        mURLConnection.setDoOutput(true);
                        mURLConnection.setChunkedStreamingMode(0);
                        if (mType == UPLOAD_FILE) {
                            mURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                            String fContentDisposition = "Content-Disposition: form-data; name=\"%s\"";
                            String fContentType = "Content-Type: %s";
                            try {
                                DataOutputStream dos = new DataOutputStream(mURLConnection.getOutputStream());

                                // 將檔案寫入 Http form data
                                for (int i = 0; i < mUploadFileList.size(); i++) {
                                    File file = mUploadFileList.get(i);
                                    String fileName = file.getName();
                                    String mimeType = HttpURLConnection.guessContentTypeFromName(fileName);
                                    String contentDisposition = String.format(fContentDisposition, "file" + i) + "; filename=\"" + fileName + "\"";
                                    String contentType = String.format(fContentType, mimeType);
                                    dos.writeBytes(HYPHENS + BOUNDARY + CRLF);
                                    dos.writeBytes(contentDisposition + CRLF);
                                    dos.writeBytes(contentType + CRLF);
                                    dos.writeBytes(CRLF);

                                    FileInputStream fis = new FileInputStream(file);
                                    byte[] buffer = new byte[2048];
                                    int bufferLength;
                                    while ((bufferLength = fis.read(buffer)) > 0) {
                                        dos.write(buffer, 0, bufferLength);
                                    }
                                    fis.close();
                                    dos.writeBytes(CRLF);
                                }

                                // 將參數寫入 Http form data
                                if (mPostData != null) {
                                    for (Map.Entry<String, String> entry : mPostData.entrySet()) {
                                        String contentDisposition = String.format(fContentDisposition, entry.getKey());
                                        dos.writeBytes(HYPHENS + BOUNDARY + CRLF);
                                        dos.writeBytes(contentDisposition + CRLF);
                                        dos.writeBytes("Content-Type: text/plain" + CRLF);
                                        dos.writeBytes(CRLF);
                                        dos.writeBytes(entry.getValue());
                                        dos.writeBytes(CRLF);
                                    }
                                }
                                dos.writeBytes(HYPHENS + BOUNDARY + HYPHENS);
                                dos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (mPostData != null) {
                                DataOutputStream dos = new DataOutputStream(mURLConnection.getOutputStream());
                                dos.write(mapToPostData(mPostData).getBytes());
                                dos.close();
                            }
                        }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 依序啟動 Task
     *
     * @param subResponse 結果回傳介面
     */
    public void start(SubResponse subResponse, boolean inThreadPool) {
        mSubResponse = subResponse;
        if (inThreadPool) {
            executeOnExecutor(EXECUTOR_SERVICE, mUrls);
        } else {
            execute(mUrls);
        }
    }

    public Object getResult(boolean inThreadPool) {
        try {
            if (inThreadPool) {
                return executeOnExecutor(EXECUTOR_SERVICE, mUrls).get();
            } else {
                return execute(mUrls).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 顯示讀取視窗
     *
     * @param task 用於取消任務
     */
    private void showLoadingView(final MDHttpAsyncTaskU task) {
        if (!mIsShowingLoadingView.get()) {
            if (mIsShowLoadingView && mLoadingView != null && !mLoadingView.isShowing()) {
                if (mType == FILE || mType == FILE_ARRAY) {
                    mLoadingView.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mLoadingView.setProgressNumberFormat("%dKB/%dKB");
                    if (mCancelable) {
                        mLoadingView.setButton(ProgressDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                task.cancel(true);
                                hideLoadingView();
                            }
                        });
                    }
                }
                mLoadingView.show();
                mIsShowingLoadingView.set(true);
            }
        }
    }

    /**
     * 結束讀取視窗
     */
    private void hideLoadingView() {
        if (mIsShowingLoadingView.get()) {
            if (mIsShowLoadingView && mLoadingView != null && mLoadingView.isShowing()) {
                mLoadingView.hide();
                mLoadingView.dismiss();
                mLoadingView = null;
                mIsShowingLoadingView.set(false);
            }
        }
    }

    /**
     * 請求文字
     *
     * @param url 連結網址
     * @return 將任務轉給 requestTextArr 回傳 String
     */
    private String requestText(String url) {
        return requestTextArr(url)[0];
    }

    /**
     * 請求文字 List
     *
     * @param urls 連結網址陣列
     * @return String list
     */
    private String[] requestTextArr(String... urls) {
        String[] textArr = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            if (isCancelled()) {
                break;
            }
            String url = urls[i];
            initConnect(url);
            String text;
            try {
                if (mURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedInputStream bufferedInputStream = new BufferedInputStream(mURLConnection.getInputStream());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    byte[] buffer = new byte[2048];
                    int bufferLength;

                    while ((bufferLength = bufferedInputStream.read(buffer)) > 0) {
                        byteArrayOutputStream.write(buffer, 0, bufferLength);
                    }
                    text = byteArrayOutputStream.toString("UTF-8");
                    textArr[i] = text;

                    bufferedInputStream.close();
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (mSubResponse != null) {
                    mSubResponse.onError(e.getMessage());
                }
            } finally {
                mURLConnection.disconnect();
            }
        }
        Log.i(Thread.currentThread().getName(), "Spend time : " + (System.currentTimeMillis() - startTime));
        return textArr;
    }

    /**
     * 請求檔案
     *
     * @param url 連結網址
     * @return 將任務轉給 requestFileArr 回傳 File
     */
    private File requestFile(String url) {
        return requestFileArr(url)[0];
    }

    /**
     * 請求檔案 List
     *
     * @param urls 連結網址陣列
     * @return File list
     */
    private File[] requestFileArr(String... urls) {
        File[] files = new File[urls.length];
        for (int i = 0; i < urls.length; i++) {
            // 檢查是否取消 Task
            if (isCancelled()) {
                break;
            }
            String url = urls[i];
            initConnect(url);
            String fileName = URLUtil.guessFileName(url, null, null);

            final String msg = fileName + "\n" + (i + 1) + "/" + urls.length;
            if (mIsShowingLoadingView.get() && mIsShowLoadingView && mLoadingView != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLoadingView != null) {
                            mLoadingView.setMessage(msg);
                        }
                    }
                });
            }
            File file = new File(mDownloadPath, fileName);

            // 檢查是否覆寫檔案
            if (file.exists() && !mOverWrite) {
                files[i] = file;
                continue;
            }
            try {
                if (mURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(mURLConnection.getInputStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    int fileLength = mURLConnection.getContentLength();
                    int bufferedLength = 0;
                    byte[] buffer = new byte[2048];
                    int bufferLength;
                    while ((bufferLength = bufferedInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, bufferLength);
                        bufferedLength += bufferLength;
                        if (mIsShowingLoadingView.get() && mIsShowLoadingView && mLoadingView != null) {
                            onProgressUpdate(bufferedLength / Math.pow(1024, 1), fileLength / Math.pow(1024, 1));
                        }
                    }
                    files[i] = file;

                    bufferedInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (mSubResponse != null) {
                    mSubResponse.onError(e.getMessage());
                }
            } finally {
                mURLConnection.disconnect();
            }
        }
        Log.i(Thread.currentThread().getName(), "Spend time : " + (System.currentTimeMillis() - startTime));
        return files;
    }

    /**
     * 上傳檔案
     * 使用 Http multipart/form-data
     *
     * @param url 上傳檔案網址
     * @return 伺服器所回傳的字串
     */
    private String uploadFiles(String url) {
        initConnect(url);
        String result = null;
        try {
            if (mURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                BufferedInputStream bufferedInputStream = new BufferedInputStream(mURLConnection.getInputStream());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int bufferLength;

                while ((bufferLength = bufferedInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, bufferLength);
                }
                result = byteArrayOutputStream.toString("UTF-8");

                bufferedInputStream.close();
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mSubResponse != null) {
                mSubResponse.onError(e.getMessage());
            }
        } finally {
            mURLConnection.disconnect();
        }
        return result;
    }

    /**
     * AsyncTask 執行前
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showLoadingView(this);
    }

    /**
     * AsyncTask 進度更新
     */
    @Override
    protected void onProgressUpdate(Number... values) {
        super.onProgressUpdate(values);
        if (mLoadingView != null) {
            mLoadingView.setProgress(values[0].intValue());
            mLoadingView.setMax(values[1].intValue());
        }
    }

    /**
     * AsyncTask 背景執行任務
     */
    @Override
    protected Object doInBackground(String... urls) {
        startTime = System.currentTimeMillis();
        switch (mType) {
            case TEXT:
                return requestText(urls[0]);
            case TEXT_ARRAY:
                return requestTextArr(urls);
            case FILE:
                return requestFile(urls[0]);
            case FILE_ARRAY:
                return requestFileArr(urls);
            case UPLOAD_FILE:
                return uploadFiles(urls[0]);
        }
        return null;
    }

    /**
     * AsyncTask 任務完成
     */
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        hideLoadingView();
        if (mSubResponse != null) {
            if (o != null) {
                mSubResponse.onSuccess(o);
            } else {
                mSubResponse.onError("data is null");
            }
        }
    }

    /**
     * AsyncTask 任務取消
     */
    @Override
    protected void onCancelled() {
        super.onCancelled();
        hideLoadingView();
    }

    private String mapToPostData(Map<String, String> postData) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : postData.entrySet()) {
            stringBuilder.append(entry.getKey());
            stringBuilder.append("=");
            stringBuilder.append(entry.getValue());
            stringBuilder.append("&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

}


