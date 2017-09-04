package org.apache.cordova.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.IBinder;

import cn.bluemobi.dylan.step.step.service.StepService;
import cn.bluemobi.dylan.step.step.UpdateUiCallBack;

public class StepCount extends CordovaPlugin{

    private CallbackContext context;
    private Activity activity;
    private boolean isBind = false;

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        activity = this.cordova.getActivity();
        context = callbackContext;
        if (action.equals("start")) {
            Intent intent = new Intent(activity, StepService.class);
            isBind = cordova.getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
            cordova.getActivity().startService(intent);
            callbackContext.success("正在读取计步");
        }
        return false;
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    ServiceConnection conn = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService stepService = ((StepService.StepBinder) service).getService();
            //设置步数监听回调
            stepService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int stepCount) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "stepCount");
                    result.setKeepCallback(true);
                    context.sendPluginResult(result);
                }
            });
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBind) {
            cordova.getActivity().unbindService(conn);
        }
    }

}
