package com.example.yanhoor.flickrgallery.controller;

import android.net.Uri;
import android.util.Log;

import com.example.yanhoor.flickrgallery.model.GalleryItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by yanhoor on 2016/3/3.
 */
public class FlickrFetchr {
    public static final String TAG="FlickrFetchr";

    public static final String PREF_LAST_RESULT_ID="lastResultId";
    public static int page=1;
    public static int per_page=25;
    public static String fromWhere="1";

    private static final String ENDPOINT="https://api.flickr.com/services/rest/";
    private static final String API_KEY="0964378968b9ce3044e29838e2fc0cd8";
    private static final String METHOD_INTERESTINGNESS ="flickr.interestingness.getList";
    private static final String PARAM_EXTRAS="extras";
    private static final String EXTRA_SMALL_URL="url_s";

    private static final String XML_PHOTO="photo";

    //从指定url获取原始数据并返回一个字节流数组
    byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url=new URL(urlSpec);
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream in=connection.getInputStream();

            if (connection.getResponseCode() !=HttpURLConnection.HTTP_OK){
                return null;
            }

            int bytesRead;
            byte[] buffer=new byte[1024];
            while((bytesRead=in.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    //将getUrlBytes方法返回的结果转换为string
    public String getUrl(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    //构建适当的url并获取xml文件
    public ArrayList<GalleryItem> downloadGalleryItems(String url){
        ArrayList<GalleryItem> items=new ArrayList<>();
        try {
            Log.d(TAG,"Query url is "+url);
            String xmlString=getUrl(url);
            Log.d(TAG,"Received xml: "+xmlString);

            //将返回的xmlstring创建为xmlpullparser实例
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser parser=factory.newPullParser();
            parser.setInput(new StringReader(xmlString));//事件类型初始化为START_DOCUMENT
            parseItems(items,parser);//解析资源
        }catch (IOException ioe){
            Log.e(TAG,"Failed to fetch items",ioe);
        }catch (XmlPullParserException xppe){
            Log.e(TAG,"Failed to parse items",xppe);
        }
        return items;
    }

    public ArrayList<GalleryItem> fetchItems(){
        Log.d(TAG,"Loding page "+page);
        String url= Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_INTERESTINGNESS)//自动转义查询字符串
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("page",String.valueOf(page))
                .appendQueryParameter("per_page",String.valueOf(per_page))
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL)
                .build().toString();
        return downloadGalleryItems(url);
    }

    //解析xml文件
    public void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException,IOException{
        int eventType=parser.next();

        while (eventType!=XmlPullParser.END_DOCUMENT){
            if (eventType==XmlPullParser.START_TAG&&"photos".equals(parser.getName())){
                String pages=parser.getAttributeValue(null,"pages");

                if (fromWhere.equals("interesting")){
                    PhotoInterestingFragment.totalPages=Integer.parseInt(pages);
                }
                if (fromWhere.equals("contacts")){
                    PhotoContactsFragment.totalPages=Integer.parseInt(pages);
                }
                if (fromWhere.equals("latest")){
                    PhotoLatestFragment.totalPages=Integer.parseInt(pages);
                }
            }

            if (eventType==XmlPullParser.START_TAG&&XML_PHOTO.equals(parser.getName())){
                String id=parser.getAttributeValue(null,"id");
                String caption=parser.getAttributeValue(null,"title");
                String smallUrl=parser.getAttributeValue(null,EXTRA_SMALL_URL);
                String owner=parser.getAttributeValue(null,"owner");
                String secret=parser.getAttributeValue(null,"secret");
                String server=parser.getAttributeValue(null,"server");
                String farm=parser.getAttributeValue(null,"farm");

                GalleryItem item=new GalleryItem();
                item.setId(id);
                item.setTitle(caption);
                item.setUrl(smallUrl);
                item.setSecret(secret);
                item.setServer(server);
                item.setFarm(farm);
                items.add(item);
            }
            eventType=parser.next();
        }
        Log.d(TAG,"Update GalleryItem");
    }

}
