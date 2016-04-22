package com.example.yanhoor.flickrgallery.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.yanhoor.flickrgallery.util.DiskLRUCacheUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yanhoor on 2016/3/3.
 */
//Handy class for starting a new thread that has a looper.
// The looper can then be used to create handler classes. Note that start() must still be called.

public class ThumbnaiDownloader<Token> extends HandlerThread {
    private static final String TAG="ThumbnaiDownloader";
    private static final int MESSAGE_DOWNLOAD=0;

    Handler mHandler;
    Context mContext;

    //创建键为Token类型，值为string类型的同步hashMap
    //Collections.synchronizedMap允许需要同步的用户可以拥有同步，而不需要同步的用户则不必为同步付出代价。
    Map<Token,String> requestMap= Collections.synchronizedMap(new HashMap<Token, String>());
    Handler mResponseHandler;
    Listener<Token> mListener;

    public  interface Listener<Token>{
        void onThumbnailDownloaded(Token token,Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener){
        mListener=listener;//主线程的listener实例
    }

    public ThumbnaiDownloader(Context context,Handler responseHandler){
        super(TAG);
        mResponseHandler=responseHandler;//主线程Handler
        mContext=context;
    }

    //该方法的调用发生在Looper第一次检查消息队列之前
    @Override
    protected void onLooperPrepared() {
        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==MESSAGE_DOWNLOAD){
                    @SuppressWarnings("unchecked")
                    Token token=(Token)msg.obj;
                    Log.d(TAG,"Got a request for url: "+requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public void queueThumbnail(Token token, String url){
        Log.d(TAG,"Got an URL: "+url);
        requestMap.put(token,url);//将token-url键值对存放到requestMap

        //获取一条消息并发送出去存放到消息队列，以token为obj，常量作为what属性
        mHandler.obtainMessage(MESSAGE_DOWNLOAD,token)
                .sendToTarget();//发送message给handler，handler将message放在looper消息队列尾部
    }

    private void handleRequest(final Token token){
        final String urlString=requestMap.get(token);
        Log.d(TAG,"urlString is "+urlString);
        if (urlString==null)return;

        final Bitmap bitmap=DiskLRUCacheUtil.get(mContext).getBitmapFromCache(urlString);
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (requestMap.get(token)!=urlString)
                    return;

                requestMap.remove(token);//删除键对应的条目
                mListener.onThumbnailDownloaded(token,bitmap);
            }
        });
    }

    //清除队列外的所有请求
    public void clearQueue(){
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();//删除所有条目
    }

}
