package com.example.lib_network.response;

import android.os.Handler;
import android.os.Looper;

import com.example.lib_network.exception.OkHttpException;
import com.example.lib_network.listener.DisposeDataHandle;
import com.example.lib_network.listener.DisposeDataListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 专门处理JSON的回调
 */
public class CommonJsonCallback implements Callback {
    /**
     * the logic layer exception, may alter in different app
     */
    protected final String RESULT_CODE = "ecode"; // 有返回则对于http请求来说是成功的，但还有可能是业务逻辑上的错误
    protected final int RESULT_CODE_VALUE = 0;
    protected final String ERROR_MSG = "emsg";
    protected final String EMPTY_MSG = "";

    /**
     * the java layer exception, do not same to the logic error
     */
    protected final int NETWORK_ERROR = -1; // the network relative error
    protected final int JSON_ERROR = -2; // the JSON relative error
    protected final int OTHER_ERROR = -3; // the unknow error

    private DisposeDataListener mListener;
    private Class<?> mClass;
    /**
     * 将其它线程的数据转发到UI线程
     */
    private Handler mDeliveryHandler;

    public CommonJsonCallback(DisposeDataHandle handle) {
        this.mListener=handle.mListener;
        this.mClass=handle.mClass;
        this.mDeliveryHandler=new Handler(Looper.getMainLooper());
    }

    @Override
    public void onFailure(Call call, final IOException ioexception) {
        /**
         * 此时还在非UI线程，因此要转发
         */
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onFailure(new OkHttpException(NETWORK_ERROR,ioexception));
            }
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        final String result = response.body().string();
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                handleResponse(result);
            }
        });
    }

    private void handleResponse(Object responseObj){
        if(responseObj==null||responseObj.toString().trim().equals("")){
            mListener.onFailure(new OkHttpException(NETWORK_ERROR,EMPTY_MSG));
            return;
        }

        try {
            /**
             * 协议确定后看这里如何修改
             * gson fastjson
             */
            JSONObject result=new JSONObject(responseObj.toString());
            //业务层不需要框架解析
            if(mClass==null){
                mListener.onSuccess(result);
            }else {
                Object obj = new Gson().fromJson(responseObj.toString(), mClass);
                if(obj!=null){
                    mListener.onSuccess(obj);
                }else {
                    mListener.onFailure(new OkHttpException(JSON_ERROR,EMPTY_MSG));
                }
            }

        }catch (Exception e){
            mListener.onFailure(new OkHttpException(OTHER_ERROR,e.getMessage()));
            e.printStackTrace();
        }

    }
}
