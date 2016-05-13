package com.example.yanhoor.flickrgallery.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.yanhoor.flickrgallery.R;

/**
 * Created by yanhoor on 2016/3/10.
 */
public class WebViewFragment extends Fragment {
    private static final String TAG="WebViewFragment";

    private WebView mWebView;

    public static final String EXTRA_URL="url";
    private String url;

    public static WebViewFragment newInstance(String url){
        Bundle args=new Bundle();
        args.putString(EXTRA_URL,url);
        WebViewFragment fragment=new WebViewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        url=getArguments().getString(EXTRA_URL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_web_view,container,false);

        mWebView=(WebView) v.findViewById(R.id.webView);
        final ProgressBar mProgressBar=(ProgressBar) v.findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress==100){
                    mProgressBar.setVisibility(View.INVISIBLE);
                }else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }
        });
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN){
                    if (keyCode==KeyEvent.KEYCODE_BACK&&mWebView.canGoBack()){
                        mWebView.goBack();
                        return true;
                    }
                }
                return false;
            }
        });
        mWebView.loadUrl(url);

        FloatingActionButton mFAB=(FloatingActionButton)v.findViewById(R.id.float_button);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        return v;
    }
}
