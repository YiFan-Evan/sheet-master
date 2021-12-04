package com.sid.soundrecorderutils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.BosObject;
import com.baidubce.services.bos.model.PutObjectResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {

    private static final String URL = "http://1.116.1.85:5050";

    public static Bitmap load(final String tag) throws IOException {
        final Bitmap[] res = {null};
        Thread t =new Thread(new Runnable() {
            @Override
            public void run() {
                String ACCESS_KEY_ID = "67a3432ef0cf4df6ab78bf82ad3fde88";             // 用户的Access Key ID
                String SECRET_ACCESS_KEY = "8acdbb27f09449c5ae79148cba4a779a";         // 用户的Secret Access Key
                String ENDPOINT = "bj.bcebos.com";                                     // 用户自己指定的域名，参考说明文档
                // 初始化一个BosClient
                BosClientConfiguration config = new BosClientConfiguration();
                config.setCredentials(new DefaultBceCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY));
                config.setEndpoint(ENDPOINT);
                BosClient client = new BosClient(config);
                System.out.println(tag);
                BosObject object = client.getObject("file-bed", tag);
                InputStream objectContent = object.getObjectContent();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while(true){
                    try {
                        if ((len = objectContent.read(buffer)) == -1) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    outStream.write(buffer, 0, len);
                }
                try {
                    outStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] data = outStream.toByteArray();
                res[0] = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res[0];
    }

    public static String store(String path) {
        File file = new File(path);
        String ACCESS_KEY_ID = "67a3432ef0cf4df6ab78bf82ad3fde88";             // 用户的Access Key ID
        String SECRET_ACCESS_KEY = "8acdbb27f09449c5ae79148cba4a779a";         // 用户的Secret Access Key
        String ENDPOINT = "bj.bcebos.com";                                     // 用户自己指定的域名，参考说明文档
        // 初始化一个BosClient
        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY));
        config.setEndpoint(ENDPOINT);
        BosClient client = new BosClient(config);
        // 获取指定文件
        String objectKey = file.getName();
        // 以文件形式上传Object
        PutObjectResponse putObjectFromFileResponse =
                client.putObject("file-bed", objectKey, file);
        // 打印ETag
        URL url = client.generatePresignedUrl("file-bed", objectKey, -1);
        return objectKey;
    }

    public static Map<String, String> json2map(String str) {
        return (Map<String, String>) JSON.parse(str);
    }

    public static List<Map<String, String>> json2list(String str) {
        return (List<Map<String, String>>) JSONArray.parse(str);
    }

    public static String map2json(Map<String, String> map) {
        return JSON.toJSONString(map);
    }

    public static String list2json(List<Map<String, String>> list) {
        return JSON.toJSONString(list);
    }

    public static Pair<String,Integer> send(String str, String api) {
        String url = URL + api;
        String body = str;
        int code = 0;
        String mesg = null;
        HttpClient httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(60000);
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(60000);
        PostMethod postMethod = new PostMethod(url);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
        Map<String, Object> map = JSONObject.parseObject(body, Map.class);
        Set<String> set = map.keySet();
        for (String s : set) {
            postMethod.addParameter(s, map.get(s).toString());
        }
        try {
            httpClient.executeMethod(postMethod);
            mesg = postMethod.getResponseBodyAsString();
            code = postMethod.getStatusCode();
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            postMethod.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
        return new Pair<String,Integer>(mesg,code);
    }

    public static Pair<Bitmap,Integer> sended(String str, String api) throws FileNotFoundException {
        String url = URL + api;
        String body = str;
        int code = 0;
        InputStream mesg = null;
        HttpClient httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(60000);
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(60000);
        PostMethod postMethod = new PostMethod(url);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "MPEG4");

//        Part[] parts = {new FilePart("pic", new File("1.jpg"))};
//        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

        Map<String, Object> map = JSONObject.parseObject(body, Map.class);
        Set<String> set = map.keySet();
        for (String s : set) {
            postMethod.addParameter(s, map.get(s).toString());
        }
        Bitmap bitmap=null;
        try {
            httpClient.executeMethod(postMethod);
            mesg = postMethod.getResponseBodyAsStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8000];
            int n = 0;
            while (-1 != (n = mesg.read(buffer))) {
                output.write(buffer, 0, n);
            }
            byte[] bytes= output.toByteArray();
            bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            code = postMethod.getStatusCode();
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            postMethod.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
        return new Pair<Bitmap,Integer>(bitmap,code);
    }

    public static Pair<String,Integer> sendFile(String para, File file, String api) throws FileNotFoundException {
        String url = URL + api;
        int code = 0;
        String mesg = "";
        HttpClient httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(60000);
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(60000);
        PostMethod postMethod = new PostMethod(url);
//        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
//        Log.d("==================",file.getPath());
        Part[] parts = {new FilePart(para, file)};
        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

//        Map<String, Object> map = JSONObject.parseObject(body, Map.class);
//        Set<String> set = map.keySet();
//        for (String s : set) {
//            postMethod.addParameter(s, map.get(s).toString());
//        }
//        Bitmap bitmap=null;
        try {
            httpClient.executeMethod(postMethod);
            mesg = postMethod.getResponseBodyAsString();
            code = postMethod.getStatusCode();
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            postMethod.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
        return new Pair<String, Integer>(mesg,code);
    }

}

