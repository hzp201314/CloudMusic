package com.example.lib_network.listener;

/**
 * 包装类
 */
public class DisposeDataHandle {
    //成功失败回调接口监听器
    public DisposeDataListener mListener = null;
    //要解析成的对象
    public Class<?> mClass = null;
    //文件保存路径
    public String mSource = null;

    public DisposeDataHandle(DisposeDataListener listener) {
        this.mListener = listener;
    }

    public DisposeDataHandle(DisposeDataListener listener, Class<?> clazz) {
        this.mListener = listener;
        this.mClass = clazz;
    }

    public DisposeDataHandle(DisposeDataListener listener, String source) {
        this.mListener = listener;
        this.mSource = source;
    }
}
