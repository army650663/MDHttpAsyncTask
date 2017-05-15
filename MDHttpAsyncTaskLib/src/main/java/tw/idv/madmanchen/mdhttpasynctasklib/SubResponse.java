package tw.idv.madmanchen.mdhttpasynctasklib;

/**
 * Author:      chenshaowei
 * Version      V1.0
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2017/5/12      chenshaowei         V1.0.0          Create
 * What is modified:
 */

public interface SubResponse {
    void onSuccess(Object data);
    void onError(String error);
}
