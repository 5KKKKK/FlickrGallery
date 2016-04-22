package com.example.yanhoor.flickrgallery.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.util.DecodeXMLFile;
import com.example.yanhoor.flickrgallery.util.StaticMethodUtil;

import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

/**
 * Created by yanhoor on 2016/3/10.
 */
public class LogInFragment extends Fragment {
    private static final String TAG="LogInFragment";

    public static final String PREF_FULL_TOKEN="fullToken";
    public static final String PREF_USER_ID ="id";
    public static final String PREF_USER_NAME="username";
    public static final String PREF_FULL_NAME="fullName";

    public static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";
    public static final String PUBLIC_CODE="a0e8c8d18675b5e2";
    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String METHOD_GET_FULL_TOKEN="flickr.auth.getFullToken";
    private String mMiniToken;
    private String mApiSig;
    private String mFullToken;//即auth_token
    private String mSignString;

    private EditText mMiniTokenEditText;
    private Button mVerifyButton;
    private Button mConfirmButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_log_in,container,false);
        mMiniTokenEditText=(EditText)v.findViewById(R.id.mini_token_editText);
        mVerifyButton=(Button)v.findViewById(R.id.get_token_button);
        mConfirmButton=(Button)v.findViewById(R.id.confirm_button);

        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(),WebViewActivity.class);
                i.putExtra(WebViewFragment.EXTRA_URL," https://www.flickr.com/auth-72157664677091449");
                startActivity(i);
                /*
                Uri pageUri= Uri.parse("https://www.flickr.com/auth-72157664677091449");
                Intent i=new Intent(Intent.ACTION_VIEW,pageUri);
                startActivity(i);
                */
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMiniToken=mMiniTokenEditText.getText().toString();
                /*StringBuilder sb=new StringBuilder(mMiniToken);
                sb.insert(3,'-');
                sb.insert(7,'-');
                mMiniToken=sb.toString();//转换成XXX-XXX-XXX形式*/
                Log.d(TAG,"mini token is "+mMiniToken);
                String[] mSignFullTokenStringArray={PUBLIC_CODE,"api_key"+API_KEY,
                        "method"+METHOD_GET_FULL_TOKEN,"mini_token"+mMiniToken};
                Arrays.sort(mSignFullTokenStringArray);
                StringBuilder mSB=new StringBuilder();
                for (String s:mSignFullTokenStringArray) {
                    mSB.append(s);
                }
                mSignString=mSB.toString();
                Log.d(TAG,"Sign string  is "+mSignString);
                new GetFullTokenTask().execute();
            }
        });

        return v;
    }

    private class GetFullTokenTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            GetFullToken();
            return null;
        }
    }

    public String GetFullToken(){
        mApiSig= StaticMethodUtil.countMD5OfString(mSignString);
        Log.d(TAG,"api_sign is "+mApiSig);
        try {
            String url=Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method",METHOD_GET_FULL_TOKEN)
                    .appendQueryParameter("api_key",API_KEY)
                    .appendQueryParameter("mini_token",mMiniToken)
                    .appendQueryParameter("api_sig",mApiSig)
                    .build().toString();
            Log.d(TAG,"Get full token from "+url);
            String xmlResult=new FlickrFetchr().getUrl(url);//从url获取xml文件
            Log.d(TAG,"Get full token from "+xmlResult);
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser parser=factory.newPullParser();
            parser.setInput(new StringReader(xmlResult));
            mFullToken=new DecodeXMLFile().decodeFullTokenXml(parser);//解析xml文件并获得fulltoken
            Log.d(TAG,"Full token is "+mFullToken);
        }catch (IOException e){
            Log.e(TAG,"failed to get full token ",e);
        }catch (XmlPullParserException xppe){
            Log.e(TAG,"Failed to decode xml for full token",xppe);
        }
        checkToken();
        return mFullToken;
    }

    public void checkToken(){
        //为flickr.auth.checkToken方法获取api_sig
        String[] mSignFullTokenStringArray = {"method" + "flickr.auth.checkToken", "auth_token" + mFullToken,
                "api_key" + LogInFragment.API_KEY, LogInFragment.PUBLIC_CODE};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method","flickr.auth.checkToken")
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("auth_token", mFullToken)
                .appendQueryParameter("api_sig", apiSig)
                .build().toString();

        new KJHttp().get(url, new HttpCallBack() {
            @Override
            public void onFinish() {
                super.onFinish();
                if (mFullToken!=null){
                    Toast.makeText(getActivity(),R.string.toast_verify_token_success,Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }else{
                    Toast.makeText(getActivity(),R.string.toast_verify_token_failed,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d(TAG,"Checking full token from "+t);

                try {
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=factory.newPullParser();
                    parser.setInput(new StringReader(t));

                    int eventType=parser.getEventType();
                    while (eventType!=XmlPullParser.END_DOCUMENT){

                        if (eventType==XmlPullParser.START_TAG&&"user".equals(parser.getName())){
                            String id=parser.getAttributeValue(null,"nsid");
                            String userName=parser.getAttributeValue(null,"username");
                            String fullName=parser.getAttributeValue(null,"fullname");

                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                    .edit()
                                    .putString(PREF_FULL_TOKEN,mFullToken)
                                    .putString(PREF_USER_ID,id)
                                    .putString(PREF_USER_NAME,userName)
                                    .putString(PREF_FULL_NAME,fullName)
                                    .commit();
                        }
                        eventType=parser.next();
                    }
                }catch (XmlPullParserException xppe){
                    xppe.printStackTrace();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
        });

    }

}
