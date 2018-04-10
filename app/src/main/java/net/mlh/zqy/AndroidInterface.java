package net.mlh.zqy;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.just.agentweb.AgentWeb;

import net.mlh.zqy.bean.share;

import org.simple.eventbus.EventBus;

import java.net.URLDecoder;

/**
 * Created by cenxiaozhong on 2017/5/14.
 *  source CODE  https://github.com/Justson/AgentWeb
 */

public class AndroidInterface {

    private Handler deliver = new Handler(Looper.getMainLooper());
    private AgentWeb agent;
    private Context context;
    public AndroidInterface(AgentWeb agent, Context context) {
        this.agent = agent;
        this.context = context;
    }



    @JavascriptInterface
    public void callShare(final String msg,final String content) {
        deliver.post(new Runnable() {
            @Override
            public void run() {
                String s = URLDecoder.decode(msg);
                EventBus.getDefault().post(new share(s,content), "url");
            }
        });
        Log.i("Info", "Thread:" + Thread.currentThread());

    }

    @JavascriptInterface
    public void updateVersion(final String version,final String content) {
        deliver.post(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new share(version,content), "update");
            }
        });
        Log.i("Info", "Thread:" + Thread.currentThread());
    }



    @JavascriptInterface
    public void callZFBPay(final String t1,final String t2,final String t3, final String t4) {
        deliver.post(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new share(t1,t2,t3,t4), "zfb");
            }
        });
    }

    @JavascriptInterface
    public void callWXPay(final String t1,final String t2,final String t3, final String t4) {
        deliver.post(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new share(t1,t2,t3,t4), "wx");
            }
        });
    }



}
