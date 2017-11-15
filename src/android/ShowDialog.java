package com.example.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.OutputStream;
import android.util.Log;
import java.util.Enumeration;
import java.net.MalformedURLException;
import android.content.SharedPreferences;

/**
 * This class echoes a string called from JavaScript.
 */
public class ShowDialog extends CordovaPlugin {
    // 需要下载资源的url地址
    private String urlStr = "http://47.52.30.102:8080/app123/chcp.json";
    // 下载资源保存路径
    private String path;
    // 下载资源保存下来的文件名
    private String fileName = "downFile.zip";
    // 下载资源的大小
    private int downSize;
    // 下载的文件
    private File localFile;

    /**
     *APP发现版本更新之后，第一次不做任何处理，第二次进行版本更新和当前APP版本号的重新保存
     */

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        path = this.cordova.getActivity().getCacheDir().toString();
        if (action.equals("showDialog")) {
            //调用该方法获取当前手机APP版本号
            String releaseApp = getRelease();
            //第一次进入应用程序，执行setSharedPreference(...)方法;
            if (getSharedPreferenceInt() == 1) {
                Toast.makeText(this.cordova.getActivity(), "1111111", Toast.LENGTH_LONG).show();
                setSharedPreference(releaseApp, 2, false);
            }
            //Toast.makeText(this.cordova.getActivity(), "getSharedPreferenceString()>>>>"+getSharedPreferenceString(), Toast.LENGTH_LONG).show();
            String releaseAppVersion = getSharedPreferenceString();
            //调用该方法获取服务器上的版本号
            String releaseUpdate = getReleaseServe();
            // Toast.makeText(this.cordova.getActivity(), "update>>>" + releaseUpdate, Toast.LENGTH_LONG).show();
            //判断SharedPreference文件夹中数字
            //1的话，执行if()里面的语句，2的话执行else()中的语句
            if (getSharedPreferenceInt() == 2) {
                //Toast.makeText(this.cordova.getActivity(), "22222222", Toast.LENGTH_LONG).show();
                if (!releaseAppVersion.equals(releaseUpdate)) {
                    if (getSharedPreferenceBoolean()) {
                        showDialog();
                        setSharedPreference(releaseApp, 2, false);
                    } else {
                        setSharedPreferenceBoolean(true);
                    }
                } else {
                    //Toast.makeText(this.cordova.getActivity(), "版本号一样,不做更新。", Toast.LENGTH_LONG).show();
                }
            }

            callbackContext.success("success");
            return true;
        }
        callbackContext.error("error");
        return false;
    }

    /**
     * 显示弹窗，让用户知道应用程序已完成更新
     */
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.cordova.getActivity());
        builder.setTitle("APP更新");
        builder.setMessage("应用程序已完成更新，感谢您的使用。");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 获取版本号所在路径并调用stringPathGetRelease()方法获取当前APP的版本号
     */
    private String getRelease() {
        //该文件夹为APP /www文件夹存储位置。
        //String releasePath=getFilesDir().getPath()+"/cordova-hot-code-push-plugin";
        String releasePath = this.cordova.getActivity().getFilesDir().getPath() + "/cordova-hot-code-push-plugin";
        File filePath = new File(releasePath);
        //获取releasePath文件夹下的所有文件夹，要最后一个
        File[] files = filePath.listFiles();
        //为releasePath追加地址,改地址为APP版本号所在的文件夹
        releasePath = releasePath + "/" + files[files.length - 1].getName() + "/www/chcp.json";
        //APP安装以后版本号所存在的文件路径
        File fileEnd = new File(releasePath);
        //最终的版本号
        String s = readString(fileEnd);
        //  Toast.makeText(this.cordova.getActivity(), "release>>>" + s, Toast.LENGTH_LONG).show();
        return s;
    }

    /**
    * 从一个文件中读取字符串
    */
    private String readString(File path) {
        String release = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            //创建一个字符串，用来表示从文件中读取到的字符串
            StringBuffer sb = new StringBuffer();
            String rdLine;
            while ((rdLine = br.readLine()) != null) {
                sb.append(rdLine);
            }
            String[] splitstart = sb.toString().split(",");
            String[] splitcenter = splitstart[1].split(":");
            String[] splitend = splitcenter[1].split("\"");
            release = splitend[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return release;
    }

    /**
     * 该方法从网络中获去APP最新版本
     */
    private String getReleaseServe() {
        String release = "";
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置请求服务器请求超时时间
            conn.setConnectTimeout(14 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept",
                    "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Referer", urlStr);
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();
            //从url获取数据流
            InputStream inputStream = conn.getInputStream();
            if (conn.getResponseCode() == 200) {
                downSize = conn.getContentLength();
                Log.i("test", ",,,,,," + downSize);
                if (downSize <= 0) {
                    Log.i("Test", "文件为空");
                    throw new RuntimeException("the file that you download has a wrong size ... ");
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                //定义一个字符串用户存储文件中JSON字符串
                StringBuffer sbuffer = new StringBuffer();
                String len;
                while ((len = bufferedReader.readLine()) != null) {
                    sbuffer.append(len);
                }
                String[] splitstart = sbuffer.toString().split(",");
                String[] splitcenter = splitstart[1].split(":");
                String[] splitend = splitcenter[1].split("\"");
                release = splitend[1];

            } else {
                throw new RuntimeException("url that you conneted has error ...");
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return release;
    }

    /**
    * 将数据保存在sharedPreference
    */

    private void setSharedPreference(String name, int num, boolean flag) {
        SharedPreferences preferences = this.cordova.getActivity().getSharedPreferences("reseasetable",
                this.cordova.getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("reseaseVersion", name);
        editor.putInt("number", num);
        editor.putBoolean("flag", flag);
        editor.commit();
    }

    /**
    * 将数据保存在sharedPreference
    */

    private void setSharedPreferenceBoolean(boolean flag) {
        SharedPreferences preferences = this.cordova.getActivity().getSharedPreferences("reseasetable",
                this.cordova.getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("flag", flag);
        editor.commit();
    }

    /**
     * 从sharedPreference取String数据
     */
    private String getSharedPreferenceString() {
        SharedPreferences preferences = this.cordova.getActivity().getSharedPreferences("reseasetable",
                this.cordova.getActivity().MODE_PRIVATE);
        String reseaseVersion = preferences.getString("reseaseVersion", "2016");
        return reseaseVersion;
    }

    /**
     * 从sharedPreference取int数据
     */
    private int getSharedPreferenceInt() {
        SharedPreferences preferences = this.cordova.getActivity().getSharedPreferences("reseasetable",
                this.cordova.getActivity().MODE_PRIVATE);
        int num = preferences.getInt("number", 1);
        return num;
    }

    /**
     * 从sharedPreference取boolean数据
     */
    private boolean getSharedPreferenceBoolean() {
        SharedPreferences preferences = this.cordova.getActivity().getSharedPreferences("reseasetable",
                this.cordova.getActivity().MODE_PRIVATE);
        boolean flag = preferences.getBoolean("flag", false);
        return flag;
    }

}
