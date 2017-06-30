package com.lefu.miniapps;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainAty extends Activity implements EasyPermissions.PermissionCallbacks {

    public final static String TAG = "MainAty";
    public static MainAty context;
    EditText editText;
    public WebView webView;
    //Choose Pic
    String mCameraPhotoPath;
    final int RESULT_CODE = 1;
    //ValueCallback
    ValueCallback<Uri> mUploadMessage;
    ValueCallback<Uri[]> mUploadCallbackAboveL;
    //二维码
    static final int REQUEST_CODE_QRCODE_PERMISSIONS = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL)
                return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                if (data != null && result != null)
                    mUploadMessage.onReceiveValue(result);
                else {
                    result = Uri.parse(mCameraPhotoPath);
                    mUploadMessage.onReceiveValue(result);
                }
                mUploadMessage = null;
            }
        }

        //二维码
        if (requestCode == 2) {
            if (data != null) {
                editText.setText(data.getStringExtra("url"));
                webView.loadUrl(String.valueOf(data.getStringExtra("url")));
            }
        }
    }

    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != RESULT_CODE
                || mUploadCallbackAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            if (data != null) {
                String dataString = data.getDataString();
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            } else {
                results = new Uri[]{Uri.parse(mCameraPhotoPath)};
            }
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        AndroidBug5497Workaround.assistActivity(this);
        MainAty.context = this;
        editText = (EditText) findViewById(R.id.et_txt);
        //
        webView = (WebView) findViewById(R.id.wv);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            // For Android 4.1
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                this.openFileChooser(uploadMsg);
            }

            // For Android 3.0+
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                this.openFileChooser(uploadMsg);
            }

            //For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                chooserPicIntent();
            }

            //For Android > 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                chooserPicIntent();
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        });
        //设置支持JavaScript脚本
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCacheMaxSize(1048576);// 10M
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        webSettings.setGeolocationEnabled(true);
        webSettings.setGeolocationDatabasePath(dir);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.contains("talkingdata"))
                    view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

        });
//        webView.addJavascriptInterface(new UpChat(), "upChat");
        //扫描
        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainAty.this, TestScanActivity.class), 2);
            }
        });
        //走你
        findViewById(R.id.zouni).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(String.valueOf(editText.getText()));
            }
        });
        //删除
        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
        //清楚缓存
        findViewById(R.id.deleteCach).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDatabase("WebView.db");
                deleteDatabase("WebViewCache.db");
                webView.clearCache(true);
                webView.clearHistory();
                webView.clearFormData();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                Toast.makeText(MainAty.this, "清除缓存成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }


    private void chooserPicIntent() {
        //photoIntent
        Intent photoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoIntent.setType("image/*");
        //cameraIntent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File saveBigPic = new File(getAlbumDir().getPath() + "/ocr_temp_" + System.currentTimeMillis() + ".jpg");
        mCameraPhotoPath = "file://" + saveBigPic.getAbsolutePath();
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(saveBigPic));
        //chooserIntent
        Intent chooserIntent = chooserIntent(cameraIntent);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, photoIntent);
        startActivityForResult(chooserIntent, RESULT_CODE);
    }

    private Intent chooserIntent(Intent... intents) {
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        chooser.putExtra(Intent.EXTRA_TITLE, "操作选择");
        return chooser;
    }

    /**
     * 获取保存图片的目录
     */
    public static File getAlbumDir() {
        //        File dir = new File(context.getExternalCacheDir(), getAlbumName());
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getAlbumName());
        if (!dir.exists()) {
            dir.mkdirs();
            File f = new File(dir, ".nomedia");
            try {
                f.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dir;
    }

    /**
     * 获取保存 隐患检查的图片文件夹名称
     */
    public static String getAlbumName() {
        return "test_wv";
    }


    @Override
    protected void onStart() {
        super.onStart();
        requestCodeQRCodePermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }
}
