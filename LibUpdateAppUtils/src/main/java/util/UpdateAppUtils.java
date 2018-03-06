package util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import customview.ConfirmDialog;
import feature.Callback;


/**
 * Created by Teprinciple on 2016/11/15.
 */
public class UpdateAppUtils {
    public static final int CHECK_BY_VERSION_NAME = 1001;
    public static final int CHECK_BY_VERSION_CODE = 1002;
    public static final int DOWNLOAD_BY_APP = 1003;
    public static final int DOWNLOAD_BY_BROWSER = 1004;
    public static boolean needFitAndroidN = true; //提供给 整个工程不需要适配到7.0的项目 置为false
    public static boolean showNotification = true;
    private final String TAG = "UpdateAppUtils";
    private Activity activity;
    private int checkBy = CHECK_BY_VERSION_CODE;
    private int downloadBy = DOWNLOAD_BY_APP;
    private int serverVersionCode = 0;
    private String apkPath = "";
    private String serverVersionName = "";
    private boolean isForce = false; //是否强制更新
    private int localVersionCode = 0;
    private String localVersionName = "";
    private String updateInfo = "";

    public Boolean showFlag = false;  //是否弹出框

    private UpdateCallback mUpdateCallback;
    public interface UpdateCallback{
        void dialogCancel();
        void downloadByApp(Activity activity, String apkPath, String serverVersionName);
        void downloadByBrowser(Activity activity, String apkPath);
    }

    private UpdateAppUtils(Activity activity,UpdateCallback mUpdateCallback) {
        this.activity = activity;
        this.mUpdateCallback = mUpdateCallback;
        getAPPLocalVersion(activity);
    }
    public UpdateAppUtils(Activity activity) {
        this.activity = activity;
        getAPPLocalVersion(activity);
    }

    public static UpdateAppUtils from(Activity activity,UpdateCallback mCancelCallback) {
        return new UpdateAppUtils(activity,mCancelCallback);
    }

    /**
     * 检测wifi是否连接
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

    public UpdateAppUtils needFitAndroidN(boolean needFitAndroidN) {
        UpdateAppUtils.needFitAndroidN = needFitAndroidN;
        return this;
    }

    public UpdateAppUtils checkBy(int checkBy) {
        this.checkBy = checkBy;
        return this;
    }

    public UpdateAppUtils apkPath(String apkPath) {
        this.apkPath = apkPath;
        return this;
    }

    public UpdateAppUtils downloadBy(int downloadBy) {
        this.downloadBy = downloadBy;
        return this;
    }

    public UpdateAppUtils showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    public UpdateAppUtils updateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
        return this;
    }

    public UpdateAppUtils serverVersionCode(int serverVersionCode) {
        this.serverVersionCode = serverVersionCode;
        return this;
    }

    public UpdateAppUtils serverVersionName(String serverVersionName) {
        this.serverVersionName = serverVersionName;
        return this;
    }

    public UpdateAppUtils isForce(boolean isForce) {
        this.isForce = isForce;
        return this;
    }

    //获取apk的版本号 currentVersionCode
    private void getAPPLocalVersion(Context ctx) {
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            localVersionName = info.versionName; // 版本名
            localVersionCode = info.versionCode; // 版本号
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void update() {

        switch (checkBy) {
            case CHECK_BY_VERSION_CODE:
                if (serverVersionCode > localVersionCode) {
                    toUpdate();
                } else {
                    Log.i(TAG, "当前版本是最新版本" + serverVersionCode + "/" + serverVersionName);
                }
                break;

            case CHECK_BY_VERSION_NAME:
                if (!serverVersionName.equals(localVersionName)) {
                    toUpdate();
                } else {
                    Log.i(TAG, "当前版本是最新版本" + serverVersionName);
                }
                break;
        }

    }

    private void toUpdate() {
        realUpdate();
    }
    public ConfirmDialog dialog;
    private void realUpdate() {
        dialog = new ConfirmDialog(activity, new Callback() {
            @Override
            public void callback(int position) {

                switch (position) {
                    case 0:  //cancle
                        if (isForce) {
                            System.exit(0);
                        }else{
                            mUpdateCallback.dialogCancel();
                        }

                        showFlag = true;
                        break;

                    case 1:  //sure
                        if (downloadBy == DOWNLOAD_BY_APP) {
                            if (isWifiConnected(activity)) {
                                mUpdateCallback.downloadByApp(activity, apkPath, serverVersionName);
                            } else {
                                new ConfirmDialog(activity, new Callback() {
                                    @Override
                                    public void callback(int position) {
                                        if (position == 1) {
                                            mUpdateCallback.downloadByApp(activity, apkPath, serverVersionName);
                                        } else {    //是否强制更新
                                            if (isForce) activity.finish();
                                        }
                                    }
                                }).setContent("手机不是WiFi状态\n是否继续下载更新？").show();
                            }

                        } else if (downloadBy == DOWNLOAD_BY_BROWSER) {
                            mUpdateCallback.downloadByBrowser(activity,apkPath);
                        }
                        showFlag = true;
                        break;
                }
            }
        });
        dialog.setContent(updateInfo);
        dialog.setCancelable(false);
        dialog.show();
    }

}
