package util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.beiing.flikerprogressbar.FlikerProgressBar;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;


/**
 * Created by SoMustYY on 2018/01/13.
 */
public class DownloadAppUtils {
    private static final String TAG = DownloadAppUtils.class.getSimpleName();
    public static long downloadUpdateApkId = -1;//下载更新Apk 下载任务对应的Id
    public static String downloadUpdateApkFilePath;//下载更新Apk 文件路径


    /**
     * 通过浏览器下载APK包
     *
     * @param context
     * @param url
     */
    public static void downloadForWebView(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static void download(final Context context, String url, final String serverVersionName, final FlikerProgressBar mDownloadProgressBar) {
        mDownloadProgressBar.setVisibility(View.VISIBLE);
//        Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
        String packageName = context.getPackageName();
        String filePath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//是否存在外部存储卡
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/profession/myFolder/apk";
        } else {
            Log.i(TAG, "没有SD卡");
            return;
        }

        final String apkLocalPath = filePath + File.separator + packageName + "_" + serverVersionName + "_" + new Date().getTime() + ".apk";  //安装包保存位置+下载的安装包命名

        downloadUpdateApkFilePath = apkLocalPath;
        FileDownloader.setup(context);
        FileDownloader.getImpl().create(url)
                .setPath(apkLocalPath)//set_address
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        send(context, (int) (soFarBytes * 100.0 / totalBytes), serverVersionName);
                        mDownloadProgressBar.setProgress(soFarBytes * 100 / totalBytes);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
//                        Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show();
                        //根据文件是否存在来回调是否下载成功的
                        send(context, 100, serverVersionName);
                        mDownloadProgressBar.finishLoad();
                        mDownloadProgressBar.setVisibility(View.GONE);

//                        Toast.makeText(context, "安装包位置：" + apkLocalPath, Toast.LENGTH_LONG).show();
                        Log.e("apk下载完成，文件地址：", apkLocalPath);

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Toast.makeText(context, "下载APP出错,请联系管理员", Toast.LENGTH_SHORT).show();
                        System.out.println("下载报错信息：" + e.getMessage());
                       // mDownloadProgressBar.finishLoad();
                        mDownloadProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, long soFarBytes, long totalBytes) {
                        mDownloadProgressBar.reset();
                    }
                }).start();
    }


    /**
     * 更新进度条UI
     *
     * @param context
     * @param progress
     * @param serverVersionName
     */
    private static void send(Context context, int progress, String serverVersionName) {
        Intent intent = new Intent("teprinciple.update");
        intent.putExtra("progress", progress);
        intent.putExtra("title", serverVersionName);
        context.sendBroadcast(intent);
    }







}
