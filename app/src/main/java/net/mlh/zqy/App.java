package net.mlh.zqy;


import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.zhy.http.okhttp.OkHttpUtils;

import me.shaohui.shareutil.ShareConfig;
import me.shaohui.shareutil.ShareManager;

/**
 * Created by 1227228155@qq.com on 2018/1/15.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // init
        ShareConfig config = ShareConfig.instance()
                .qqId("1227228155")
                .wxId("wxc4e1039e65142340")
                .weiboId("1234555");
        ShareManager.init(config);
// init it in the function of onCreate in ur Application
        Utils.init(this);
        OkHttpUtils.getInstance()
                .init(this)
                .debug(true, "okHttp")
                .timeout(10* 1000);
       LogUtils.Config mConfig = LogUtils.getConfig();
        mConfig.setBorderSwitch(false);
    }
}
