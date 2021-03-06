# MDHttpAsyncTask
## 使用
**1. Gradle dependency** (recommended)

  -  Add the following to your project level `build.gradle`:
 
``` gradle
allprojects {
	repositories {
		maven { url "https://jitpack.io" }
	}
}
```
  -  Add this to your app `build.gradle`:
 
``` gradle
dependencies {
	compile 'com.github.army650663:MDHttpAsyncTask:v1.0.2'
}
```
#### 範例
##### 取得字串回傳
 
 ``` java
 new MDHttpAsyncTask.Builder()
                 .load(url)
                 .setRequestType(MDHttpAsyncTask.TEXT)
                 .setLoadingView(mContext, "", "Loading")
                 .build()
                 .startAll(new MDHttpAsyncTask.SubResponse() {
                     @Override
                     public void onResponse(Object data) {
                         if (data != null) {
                             
                         }
                     }
                 });
 ``` 
##### 下載檔案
  ``` java
  new MDHttpAsyncTask.Builder()
                  .load(url)
                  .setRequestType(MDHttpAsyncTask.FILE)
                  .setLoadingView(mContext, "", "")
                  .setDownloadPath(PATH)
                  .build()
                  .startAll(new MDHttpAsyncTask.SubResponse() {
                      @Override
                      public void onResponse(Object data) {
                          if (data != null) {
                              
                          }
                      }
                  });
  ```
  
#####  - 上傳檔案
  ``` java
  new MDHttpAsyncTask.Builder()
                  .load(url)
                  .setLoadingView(mContext, "", "")
                  .setRequestType(MDHttpAsyncTask.UPLOAD_FILE)
                  .addUploadFile(Files)
                  .build()
                  .startAll(new MDHttpAsyncTask.SubResponse() {
                      @Override
                      public void onResponse(Object data) {
                          if (data != null) {
                              
                          }
                      }
                  });
  ```

