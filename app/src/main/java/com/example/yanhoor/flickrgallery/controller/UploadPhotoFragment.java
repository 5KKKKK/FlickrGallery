package com.example.yanhoor.flickrgallery.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.yanhoor.flickrgallery.MyApplication;
import com.example.yanhoor.flickrgallery.R;
import com.example.yanhoor.flickrgallery.util.StaticMethodUtil;
import com.squareup.leakcanary.RefWatcher;

import org.kymjs.kjframe.http.HttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by yanhoor on 2016/3/31.
 */
public class UploadPhotoFragment extends Fragment {
    private static final String TAG="UploadPhotoFragment";

    private static final int REQUEST_CODE_PICK_PICTURE=1;
    private static final String BOUNDARYSTR="---------------------------7d44e178b0434";
    private static final String BOUNDARY="--" + BOUNDARYSTR + "\r\n";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";
    private static final String PUBLIC_CODE="a0e8c8d18675b5e2";

    private EditText editTitle;
    private EditText editDescription;
    private GridView newPhotoGridView;
    private Button cancelButton;
    private Button postButton;

    private ArrayList<HashMap<String,Object>> imageItems=new ArrayList<>();
    private SimpleAdapter mSimpleAdapter;
    private String imagePath;
    private String title;
    private String description;
    private ArrayList<byte[]>bitmapByteArrays=new ArrayList<>();
    private ArrayList<String>photoPaths=new ArrayList<>();
    private Handler UIHandler;
    private int count;//用于计算上传照片数
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIHandler=new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_upload_photo,container,false);

        editTitle=(EditText)v.findViewById(R.id.edit_new_photo_title);
        editDescription=(EditText)v.findViewById(R.id.edit_new_photo_description);
        newPhotoGridView=(GridView)v.findViewById(R.id.add_photo_gridView);
        cancelButton=(Button)v.findViewById(R.id.cancel_post_new_photo_button);
        postButton=(Button)v.findViewById(R.id.post_new_photo_button);

        //添加点击图片
        Bitmap addRes= BitmapFactory.decodeResource(getResources(),R.drawable.add_photo);
        HashMap<String,Object> map=new HashMap<>();
        map.put("imageItem",addRes);
        imageItems.add(map);

        setupAdapter();

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmapByteArrays.size()!=0){
                    progressDialog=new ProgressDialog(getActivity());
                    progressDialog.setMessage(getResources().getString(R.string.uploading_message));
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i=0;i<bitmapByteArrays.size();i++){
                                byte[] b=bitmapByteArrays.get(i);
                                String path=photoPaths.get(i);
                                uploadPhoto(bitmapByteArrays.size(),b,path);
                            }
                        }
                    }).start();
                }else{
                    Toast.makeText(getActivity(),R.string.no_photo_added_upload_photo,Toast.LENGTH_SHORT).show();
                }
            }

        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.Cancel_msg_upload_photo)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
            }
        });

        newPhotoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (imageItems.size()==10){
                    Toast.makeText(getActivity(),R.string.photo_full_toast,Toast.LENGTH_SHORT).show();
                }else if (position==0){
                    Toast.makeText(getActivity(),R.string.add_photo_toast,Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i,REQUEST_CODE_PICK_PICTURE);
                }else {
                    buildAlertDialog(position);
                }
            }
        });

        return v;
    }

    private void setupAdapter(){
        mSimpleAdapter=new SimpleAdapter(getActivity(),imageItems,R.layout.item_image_view,
                new String[]{"imageItem"},new int[]{R.id.gallery_item_imageView});

        mSimpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView &&data instanceof Bitmap){
                    ImageView i=(ImageView)view;
                    i.setImageBitmap((Bitmap)data);
                    return true;
                }
                return false;
            }
        });
        newPhotoGridView.setAdapter(mSimpleAdapter);
    }

    private void buildAlertDialog(final int position){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_photo_confirm_dialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        imageItems.remove(position);
                        mSimpleAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==Activity.RESULT_OK&&requestCode==REQUEST_CODE_PICK_PICTURE){
            Uri uri=data.getData();
            if (!TextUtils.isEmpty(uri.getAuthority())){
                Cursor cursor=getActivity().getContentResolver().query(
                        uri, new String[] { MediaStore.Images.Media.DATA }, null, null, null);

                if (cursor==null)return;
                cursor.moveToFirst();
                imagePath=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                for (int i=0;i<photoPaths.size();i++){
                    if (imagePath.equals(photoPaths.get(i))){
                        Toast.makeText(getActivity(),R.string.same_photo_toast_upload_photo,Toast.LENGTH_SHORT).show();
                        imagePath=null;
                        break;
                    }
                }
                cursor.close();
            }
        }
    }

    //选择照片后刷新
    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(imagePath)){
            BitmapFactory.Options options;
            Bitmap newPic;

            try {
                newPic = BitmapFactory.decodeFile(imagePath);
            } catch (OutOfMemoryError e) {
                    options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    newPic = BitmapFactory.decodeFile(imagePath,options);
            }

            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            newPic.compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte[] bitmapByteArray=baos.toByteArray();
            bitmapByteArrays.add(bitmapByteArray);
            photoPaths.add(imagePath);

            //Bitmap newPic=BitmapFactory.decodeFile(imagePath);
            Bitmap pic=Bitmap.createScaledBitmap(newPic,40,40,false);
            HashMap<String,Object>map=new HashMap<>();
            map.put("imageItem",pic);
            imageItems.add(map);

            Log.d(TAG,"onResume");
            setupAdapter();
            mSimpleAdapter.notifyDataSetChanged();

//刷新后释放防止手机休眠后自动添加
            imagePath=null;

        }
    }

    private void uploadPhoto(int sum,byte[] photoBinary,String path){

        title=editTitle.getText().toString().trim();
        description=editDescription.getText().toString().trim();

        String[] mSignFullTokenStringArray = {"api_key" +API_KEY,
                "auth_token" + MainLayoutActivity.fullToken,
                PUBLIC_CODE, "title"+title, "description"+description};
        Arrays.sort(mSignFullTokenStringArray);
        StringBuilder mSB = new StringBuilder();
        for (String s : mSignFullTokenStringArray) {
            mSB.append(s);
        }
        String apiSig = StaticMethodUtil.countMD5OfString(mSB.toString());

        HttpParams params=new HttpParams();
        params.put("photo",photoBinary);

        String url = Uri.parse("https://up.flickr.com/services/upload/").buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("auth_token", MainLayoutActivity.fullToken)
                .appendQueryParameter("title",title)
                .appendQueryParameter("description",description)
                .appendQueryParameter("api_sig",apiSig)
                .build().toString();

        Log.d(TAG, "uploadPhoto: title is "+title);
        Log.d(TAG, "uploadPhoto: description is "+description);
        Log.d(TAG, "uploadPhoto: url is "+url+"?"+params);

        HttpURLConnection connection=null;
        String res=null;//服务器返回的上传结果
        try{
            URL uploadUrl=new URL(url+"?"+params);
            connection= (HttpURLConnection) uploadUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5*60*1000);
            connection.setReadTimeout(5*60*1000);
            connection.addRequestProperty("Content-Type","multipart/form-data; boundary="+BOUNDARYSTR);
            connection.connect();
            BufferedOutputStream out=new BufferedOutputStream(connection.getOutputStream());

            StringBuilder sb=new StringBuilder();
            sb.append(BOUNDARY);
            sb.append("Content-Disposition: form-data; name=\"api_key\"");
            sb.append("\r\n\r\n");
            sb.append(API_KEY);
            out.write(sb.toString().getBytes());

            StringBuilder tokenBuilder=new StringBuilder();
            tokenBuilder
                    .append("\r\n")
                    .append(BOUNDARY)
                    .append("Content-Disposition: form-data; name=\"auth_token\"")
                    .append("\r\n\r\n")
                    .append(MainLayoutActivity.fullToken);
            out.write(tokenBuilder.toString().getBytes());

            StringBuilder apisigBuilder=new StringBuilder();
            apisigBuilder
                    .append("\r\n")
                    .append(BOUNDARY)
                    .append("Content-Disposition: form-data; name=\"api_sig\"")
                    .append("\r\n\r\n")
                    .append(apiSig);
            out.write(apisigBuilder.toString().getBytes());

            StringBuilder titleBuilder=new StringBuilder();
            titleBuilder.append("\r\n")
                    .append(BOUNDARY)
                    .append("Content-Disposition: form-data; name=\"title\"")
                    .append("\r\n\r\n")
                    .append(title);
            out.write(titleBuilder.toString().getBytes());

            StringBuilder descriptionBuilder=new StringBuilder();
            descriptionBuilder.append("\r\n")
                    .append(BOUNDARY)
                    .append("Content-Disposition: form-data; name=\"description\"")
                    .append("\r\n\r\n")
                    .append(description);
            out.write(descriptionBuilder.toString().getBytes());

            StringBuilder photoBuilder=new StringBuilder();
            photoBuilder
                    .append("\r\n")
                    .append(BOUNDARY)
                    .append("Content-Disposition: form-data; name=\"photo\";filename=\""+path+"\'")
                    .append("\r\nContent-Type:image/jpeg")
                    .append("\r\n\r\n");
            out.write(photoBuilder.toString().getBytes());
            out.write(photoBinary);

            StringBuilder endBuilder=new StringBuilder();
            endBuilder.append("\r\n")
                    .append(BOUNDARY)
                    .append("--\r\n");
            out.write(endBuilder.toString().getBytes());
            out.flush();
            out.close();

            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res= strBuf.toString();
            Log.d(TAG, "uploadPhoto: respond result is "+res);
            reader.close();
            reader = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        }

        //解析返回的结果
        if (!TextUtils.isEmpty(res)){
            try {
                XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                XmlPullParser parser=factory.newPullParser();
                parser.setInput(new StringReader(res));

                int eventType=parser.getEventType();
                while (eventType!=XmlPullParser.END_DOCUMENT){
                    if (eventType==XmlPullParser.START_TAG&&"rsp".equals(parser.getName())){
                        String state=parser.getAttributeValue(null,"stat");
                        if (state.equals("ok")){
                            count++;
                            //返回主线程更新UI，
                            // 防止java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                            if (count==sum){
                                count=0;
                                UIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(),R.string.Upload_photo_successfully,Toast.LENGTH_SHORT).show();
                                        getActivity().finish();
                                    }
                                });
                            }
                        }
                    }

                    if (eventType==XmlPullParser.START_TAG&&"err".equals(parser.getName())){
                        final String errorMessage=parser.getAttributeValue(null,"msg");

                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity()!=null){
                                    Toast.makeText(getActivity(),"Error occur when upload No"+(count+1)+"photo"+errorMessage,Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }
                    eventType=parser.next();
                }
            }catch (XmlPullParserException xppe) {
                xppe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MyApplication.getRefWatcher(getActivity().getApplication());
        refWatcher.watch(this);
        UIHandler=null;
        System.gc();
    }
}
