package com.example.lib_network.request;

import java.io.File;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 构建request请求
 * build the request
 */
public class CommonRequest {
    /**
     * create the key-value Request
     *
     * @param url
     * @param params
     * @return
     */
    public static Request createPostRequest(String url, RequestParams params) {
        return createPostRequest(url, params, null);
    }

    /**
     * 可以带请求头的Post请求
     * @param url
     * @param params
     * @param headers
     * @return
     */
    public static Request createPostRequest(String url,RequestParams params,RequestParams headers){
        //添加FormBody请求参数
        FormBody.Builder mFromBodyBuild =new FormBody.Builder();
        if(params!=null){
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                mFromBodyBuild.add(entry.getKey(),entry.getValue());
            }
        }

        //添加请求头
        Headers.Builder mHeaderBuild =new Headers.Builder();
        if(headers!=null){
            for (Map.Entry<String, String> entry : headers.urlParams.entrySet()) {
                mHeaderBuild.add(entry.getKey(),entry.getValue());
            }
        }

        FormBody mFormBody=mFromBodyBuild.build();
        Headers mHeader=mHeaderBuild.build();
        Request request=new Request.Builder().url(url)
                .post(mFormBody)
                .headers(mHeader)
                .build();
        return request;
    }

    /**
     * ressemble the params to the url
     *
     * @param url
     * @param params
     * @return
     */
    public static Request createGetRequest(String url, RequestParams params) {
        return createGetRequest(url, params, null);
    }

    /**
     * 可以带请求头的Get请求
     * @param url
     * @param params
     * @param headers
     * @return
     */
    public static Request createGetRequest(String url,RequestParams params,RequestParams headers){
        //添加urlBuilder请求参数
        StringBuilder urlBuilder =new StringBuilder(url).append("?");
        if(params!=null){
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        //添加请求头
        Headers.Builder mHeaderBuild =new Headers.Builder();
        if(headers!=null){
            for (Map.Entry<String, String> entry : headers.urlParams.entrySet()) {
                mHeaderBuild.add(entry.getKey(),entry.getValue());
            }
        }
        Headers mHeader=mHeaderBuild.build();
        return new Request.Builder()
                .url(urlBuilder.substring(0,urlBuilder.length()-1))
                .get()
                .headers(mHeader)
                .build();
    }

    private static final MediaType FILE_TYPE=MediaType.parse("application/octet-stream");

    /**
     * 文件上传请求
     * @param url
     * @param params
     * @return
     */
    public static Request createMultiPostRequest(String url,RequestParams params){
        MultipartBody.Builder requestBody = new MultipartBody.Builder();
        requestBody.setType(MultipartBody.FORM);//指定文件上传表单提交
        if(params!=null){
            for (Map.Entry<String, Object> entry : params.fileParams.entrySet()) {
                if(entry.getValue() instanceof File){
                    requestBody.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                            RequestBody.create(FILE_TYPE,(File)entry.getValue()));
                }else if(entry.getValue() instanceof String){
                    requestBody.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                            RequestBody.create(null,(String)entry.getValue()));
                }
            }
        }
        return new Request.Builder().url(url).post(requestBody.build()).build();
    }
}
