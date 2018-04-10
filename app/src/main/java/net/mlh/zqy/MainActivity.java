package net.mlh.zqy;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.ChromeClientCallbackManager;
import com.tsy.sdk.pay.alipay.Alipay;
import com.tsy.sdk.pay.weixin.WXPay;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.UpdateCallback;
import com.vector.update_app.utils.AppUpdateUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import net.mlh.zqy.bean.share;
import net.mlh.zqy.http.UpdateAppHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.HashMap;
import java.util.Map;

import me.shaohui.shareutil.ShareUtil;
import me.shaohui.shareutil.share.ShareListener;
import me.shaohui.shareutil.share.SharePlatform;
import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements CustomPopupWindow.OnItemClickListener{
    AgentWeb mAgentWeb;
    LinearLayout ll;
    private CustomPopupWindow mPop;

  // String URL = "http://www.mlhkj.net/webapp/index/loadIndex.do";
    private long mExitTime;//退出程序的时间
    private Toast toast = null;
    public String  GitUrl="https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/json/json.txt?appKey=ab55ce55Ac4bcP408cPb8c1Aaeac179c5f6f&version=0.1.0";
    public  String SHARE_URL="";
    public String UpdateUrl="http://www.mlhkj.net/zqy.apk";

   // public String BASE_URL="http://www.mlhkj.net";
    public String BASE_URL="http://13156152063.tunnel.qydev.com";

    public String URL = BASE_URL+"/webapp/index/loadIndex.do";
    public String TO_URL=BASE_URL+"/webapp/order/saveAlipay.do";//通知服务端支付成功的接口
    public String GET_ALIPAY=BASE_URL+"/webapp/order/payAlipay.do";//获取加密支付订单

    String appVersion;
    String appContent;
    ShareListener shareListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /* BarUtils.setStatusBarColor(this, Color.parseColor("#C0C0C0"));
        BarUtils.setStatusBarVisibility(this,true);*/
         ll = findViewById(R.id.ll);
        // 注册对象
        EventBus.getDefault().register(this);
        mPop=new CustomPopupWindow(this);
        mPop.setOnItemClickListener(this);
        mAgentWeb = AgentWeb.with(this)//传入Activity or Fragment
                .setAgentWebParent(ll, new LinearLayout.LayoutParams(-1, -1))//传入AgentWeb 的父控件 ，如果父控件为 RelativeLayout ， 那么第二参数需要传入 RelativeLayout.LayoutParams ,第一个参数和第二个参数应该对应。
                .useDefaultIndicator()// 使用默认进度条
                .defaultProgressBarColor() // 使用默认进度条颜色
                .setReceivedTitleCallback(new ChromeClientCallbackManager.ReceivedTitleCallback() {
                    @Override
                    public void onReceivedTitle(WebView view, String title) {

                    }
                }) //设置 Web 页面的 title 回调
                .createAgentWeb()//
                .ready()
                .go(URL);
        if (toast == null) {
            toast = Toast.makeText(this, "再次点击退出程序", Toast.LENGTH_SHORT);
        }
        setListener();
        if(mAgentWeb!=null){
            //注入对象
            mAgentWeb.getJsInterfaceHolder().addJavaObject("android",new net.mlh.zqy.AndroidInterface(mAgentWeb,this));
        }


    }

    //对返回键进行监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //调JS的方法
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (System.currentTimeMillis() - mExitTime > 2000) {
            mAgentWeb.getJsEntraceAccess().quickCallJs("historyBack");
           // toast.show();
            mExitTime = System.currentTimeMillis();
        } else {
        //    toast.cancel();
            finish();
            //  System.exit(0);
        }
    }
    private void setListener() {
        shareListener = new ShareListener() {
            @Override
            public void shareSuccess() {
                Toast.makeText(MainActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void shareFailure(Exception e) {
                Toast.makeText(MainActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void shareCancel() {
                Toast.makeText(MainActivity.this, "取消分享", Toast.LENGTH_SHORT).show();
            }
        };
    }


    /***
     * JS调用分享弹窗
     * @param share
     */
    @Subscriber(tag = "url")
    private void updateUserWithTag(share share) {
        SHARE_URL = share.url;
        //设置PopupWindow中的位置
         mPop.showAtLocation(ll, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    /**
     * JS调用升级弹窗
     * @param share
     */
    @Subscriber(tag = "update")
    private void updateVersion(share share) {
        appVersion = share.version;
        appContent = share.content;
        //如果JS返回的版本号和本地版本号不相等，开始更新
       if (!appVersion.equals(AppUpdateUtils.getVersionName(this))){
           startUpdateVersion(appVersion,appContent);
       }
    }

    /**
     * JS调用支付宝支付
     * @param share
     */
    @Subscriber(tag = "zfb")
    private void callZFBPay(share share) {
        //1.创建支付宝支付订单的信息
        //服务器产生的订单信息
        Map<String, String> map = new HashMap<>();
        map.put("orderNo",share.orderNo);
        map.put("typeIds", share.typeIds);
        map.put("userId",share.userid);
        OkHttpUtils.post()
                .url(GET_ALIPAY)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Response response, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (!response.isEmpty()){
                            goAliPay(response);
                        }

                    }
                });

    }

    /**
     * JS调用微信支付
     * @param share
     */
    @Subscriber(tag = "wx")
    private void callWXPay(share share) {
        String wx_appid = "wxXXXXXXX";     //替换为自己的appid
        WXPay.init(getApplicationContext(), wx_appid);      //要在支付前调用
     //   goWxPay();
    }

    private void goWxPay(String s) {
        WXPay.getInstance().doPay(s, new WXPay.WXPayResultCallBack() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplication(), "支付成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error_code) {
                switch (error_code) {
                    case WXPay.NO_OR_LOW_WX:
                        Toast.makeText(getApplication(), "未安装微信或微信版本过低", Toast.LENGTH_SHORT).show();
                        break;

                    case WXPay.ERROR_PAY_PARAM:
                        Toast.makeText(getApplication(), "参数错误", Toast.LENGTH_SHORT).show();
                        break;

                    case WXPay.ERROR_PAY:
                        Toast.makeText(getApplication(), "支付失败", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplication(), "支付取消", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void goAliPay(String s) {
        final String[] strings = s.split(",");
        new Alipay(this,strings[3], new Alipay.AlipayResultCallBack() {
            @Override
            public void onSuccess() {
                Map<String, String> map = new HashMap<>();
                map.put("userId", strings[2]);
                map.put("orderNo", strings[0]);
                map.put("body", strings[1]);
                sendUrlToServer(map);
            }

            @Override
            public void onDealing() {
                Toast.makeText(getApplication(), "支付处理中...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error_code) {
                switch (error_code) {
                    case Alipay.ERROR_RESULT:
                        Toast.makeText(getApplication(), "支付失败:支付结果解析错误", Toast.LENGTH_SHORT).show();
                        break;

                    case Alipay.ERROR_NETWORK:
                        Toast.makeText(getApplication(), "支付失败:网络连接错误", Toast.LENGTH_SHORT).show();
                        break;

                    case Alipay.ERROR_PAY:
                        Toast.makeText(getApplication(), "支付错误:支付码支付失败", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(getApplication(), "支付错误", Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplication(), "支付取消", Toast.LENGTH_SHORT).show();
            }
        }).doPay();
    }

    private void sendUrlToServer(Map<String,String> params) {
        OkHttpUtils.post()
                .url(TO_URL)
                .params(params)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Response response, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String ok = jsonObject.optString("ok");
                            if (ok.equals("true")){
                                callJs();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }


    /***
     * 通知服务端支付成功
     */
    private void callJs() {
        mAgentWeb.getJsEntraceAccess().quickCallJs("paySuccess");
    }


    private void startUpdateVersion(final String v, final String c) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Map<String, String> params = new HashMap<String, String>();
        params.put("appKey", "ab55ce55Ac4bcP408cPb8c1Aaeac179c5f6f");
        params.put("appVersion", appVersion);
        params.put("key1", "value2");
        params.put("key2", "value3");

        new UpdateAppManager
                .Builder()
                //必须设置，当前Activity
                .setActivity(this)
                //必须设置，实现httpManager接口的对象
                .setHttpManager(new UpdateAppHttpUtil())
                //必须设置，更新地址
                .setUpdateUrl(GitUrl)
                //以下设置，都是可选
                //设置请求方式，默认get
                .setPost(false)
                //添加自定义参数，默认version=1.0.0（app的versionName）；apkKey=唯一表示（在AndroidManifest.xml配置）
                .setParams(params)
                //设置点击升级后，消失对话框，默认点击升级后，对话框显示下载进度
                .hideDialogOnDownloading(false)
                //设置头部，不设置显示默认的图片，设置图片后自动识别主色调，然后为按钮，进度条设置颜色
                .setTopPic(R.mipmap.top_8)
                //为按钮，进度条设置颜色，默认从顶部图片自动识别。
                //.setThemeColor(ColorUtil.getRandomColor())
                //设置apk下砸路径，默认是在下载到sd卡下/Download/1.0.0/test.apk
                .setTargetPath(path)
                //设置appKey，默认从AndroidManifest.xml获取，如果，使用自定义参数，则此项无效
                //.setAppKey("ab55ce55Ac4bcP408cPb8c1Aaeac179c5f6f")
                //不显示通知栏进度条
                .dismissNotificationProgress()
                //是否忽略版本
                //.showIgnoreVersion()
                .build()
                //检测是否有新版本
                .checkNewApp(new UpdateCallback() {
                    /**
                     * 解析json,自定义协议
                     *
                     * @param json 服务器返回的json
                     * @return UpdateAppBean
                     */
                    @Override
                    protected UpdateAppBean parseJson(String json) {
                        UpdateAppBean updateAppBean = new UpdateAppBean();
                        updateAppBean
                                //（必须）是否更新Yes,No
                                .setUpdate("Yes")
                                //（必须）新版本号，
                                .setNewVersion(v)
                                //（必须）下载地址
                                .setApkFileUrl(UpdateUrl)
                                //（必须）更新内容
                                .setUpdateLog(c)
                                  /*  //大小，不设置不显示大小，可以不设置
                                    .setTargetSize(jsonObject.optString("target_size"))*/
                                //是否强制更新，可以不设置
                                .setConstraint(true);
                        //设置md5，可以不设置
                                 /*   .setNewMd5(jsonObject.optString("new_md51"));*/

                        return updateAppBean;
                    }

                    /**
                     * 网络请求之前
                     */
                    @Override
                    public void onBefore() {
                      //  CProgressDialogUtils.showProgressDialog(MainActivity.this);
                    }

                    /**
                     * 网路请求之后
                     */
                    @Override
                    public void onAfter() {
                     //   CProgressDialogUtils.cancelProgressDialog(MainActivity.this);
                    }

                    /**
                     * 没有新版本
                     */
                    @Override
                    public void noNewApp() {
                    }
                });



    }


    @Override
    public void setOnItemClick(View v) {
        // TODO Auto-generated method stub mRlWechat, mRlWeixinCircle, mRlQQ,mRlQzone;

        switch(v.getId()){
            case R.id.mRlWechat:
                ShareUtil.shareMedia(this, SharePlatform.WX, "众情易,开启创业新奇迹", "众情易，引领分配新纪元", SHARE_URL,"http://www.mlhkj.net/images/logo.png", shareListener);
                break;
            case R.id.mRlWeixinCircle:
                ShareUtil.shareMedia(this, SharePlatform.WX_TIMELINE, "众情易,开启创业新奇迹", "众情易，引领分配新纪元", SHARE_URL,"http://www.mlhkj.net/images/logo.png", shareListener);
                break;
            case R.id.mRlQQ:
                ShareUtil.shareMedia(this, SharePlatform.QQ, "众情易,开启创业新奇迹", "众情易，引领分配新纪元", SHARE_URL, "http://www.mlhkj.net/images/logo.png", shareListener);
                break;
            case R.id.mRlQzone:
                ShareUtil.shareMedia(this, SharePlatform.QZONE, "众情易,开启创业新奇迹", "众情易，引领分配新纪元",SHARE_URL , "http://www.mlhkj.net/images/logo.png", shareListener);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销
        EventBus.getDefault().unregister(this);
    }
}
