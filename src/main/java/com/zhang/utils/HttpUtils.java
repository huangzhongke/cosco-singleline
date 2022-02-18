package com.zhang.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * @author zzh</ n>
 * 编写日期   2021-12-30下午5:13:37</n>
 * 邮箱  1821851614@qq.com</n>
 * TODO</n>
 */
public class HttpUtils {

    public static final String TAG = "HttpUtils";
    public static int putCode = 0;
    public static int arraysCode = 0;
    public static int getCode = 0;
    public static int postCode = 0;
    public static String host = "tps371.kdlapi.com";
    public static int port = 15818;

    private HttpUtils() {

    }

    /*
     * 获取Cookie信息
     */
    public static String getCookie() {
        CloseableHttpClient Client = HttpClients.createDefault();
        HttpClientContext httpClientContext = new HttpClientContext();
        CloseableHttpResponse response = null;
        String Cookie = "";
        String url = "https://synconhub.coscoshipping.com/manifest.json";
        try {
            HttpGet get = new HttpGet(url);
            HttpHost httpHost = new HttpHost(host, port);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(100000)
                    .setSocketTimeout(100000)
//                    .setProxy(httpHost)
                    .build();
            get.setConfig(requestConfig);
            get.addHeader("User-Agent", "[{\"key\":\"User-Agent\",\"value\":\"Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10\",\"description\":\"\",\"type\":\"text\",\"enabled\":false}]");
            get.addHeader("Referer", " https://synconhub.coscoshipping.com/");
            response = Client.execute(get, httpClientContext);
            System.out.println(response);
            Header[] headers = response.getHeaders("Set-Cookie");
            for (Header header : headers) {
                Cookie = StringUtils.substringBefore(header.getValue(), ";");
                return Cookie + ";";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (Client != null) {
                try {
                    Client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /*
     * 获取token值
     */
    public static String getToken(String Cookie, List<BasicNameValuePair> paramMap) {
        CloseableHttpClient Client = HttpClients.createDefault();
        HttpClientContext httpClientContext = new HttpClientContext();
        String capUrl = "https://synconhub.coscoshipping.com/api/common/captcha/image";
        String loginUrl = "https://synconhub.coscoshipping.com/api/admin/user/login";
        String content = "";
        CloseableHttpResponse response = null;
        try {
            HttpGet get = new HttpGet(capUrl);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(100000)
                    .setSocketTimeout(100000)
                    .build();
            get.setConfig(requestConfig);
            get.addHeader("User-Agent", "[{\"key\":\"User-Agent\",\"value\":\"Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10\",\"description\":\"\",\"type\":\"text\",\"enabled\":false}]");
            get.addHeader("Referer", " https://synconhub.coscoshipping.com/");
            response = Client.execute(get, httpClientContext);
            Header[] headers = response.getHeaders("Set-Cookie");
            for (Header header : headers) {
                for (HeaderElement element : header.getElements()) {
                    if (element.getName().equals("ECTIMGCAPTCHA")) {
//                        System.out.println(element.getValue());
                        content = element.getValue();
//                        get.releaseConnection();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        try {
            HttpPost post = new HttpPost(loginUrl);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(100000)
                    .setSocketTimeout(100000)
                    .build();
            post.setConfig(requestConfig);
            post.addHeader("User-Agent", "[{\"key\":\"User-Agent\",\"value\":\"Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10\",\"description\":\"\",\"type\":\"text\",\"enabled\":false}]");
            post.addHeader("Referer", "https://synconhub.coscoshipping.com/");
            post.addHeader("Cookie", Cookie);
            post.addHeader("ECTIMGCAPTCHA", content);
            post.addHeader("Content-Type", "application/x-www-form-urlencoded");
            post.addHeader("Accept", "*/*");
            if (null != paramMap) {
                // 为httpPost设置封装好的请求参数
                try {
                    post.setEntity(new UrlEncodedFormEntity(paramMap, "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 执行请求用execute方法，context用来帮我们附带上额外信息
            response = Client.execute(post, httpClientContext);
            Header[] headers = response.getAllHeaders();
//            System.out.println(response);
            for (Header header : headers) {
                if (header.getName().equals("X-Auth-Token")) {
                    return header.getValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (Client != null) {
                try {
                    Client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static String httpHost() {
        String url1 = "http://ip.yqie.com/ipproxy.htm";

        return myHttp.doGet(url1);
    }

    /**
     * get请求
     *
     * @param url
     * @param params
     * @return
     */
    public static String sendGet(String url, Map<String, String> params) {
        CloseableHttpClient Client = HttpClients.createDefault();
        HttpClientContext httpClientContext = new HttpClientContext();
        CloseableHttpResponse response = null;
        String content = null;
        try {
//			String result = httpHost();
            HttpGet get = new HttpGet(url);
            //代理IP设置，代理 ip查询地址：https://www.xicidaili.com/
//			if (result !=null){
//				String[] arr = result.split(":");
//				String host = arr[0];
//				int port = Integer.parseInt(arr[1]);
//				HttpHost httpHost = new HttpHost(host,port);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(100000)//设置连接超时时间,单位毫秒
                    .setSocketTimeout(100000)//设置读取超时时间,单位毫秒
//						.setProxy(httpHost)//设置代理
                    .build();
            get.setConfig(requestConfig);
//				System.out.println("HttpGet----host:"+host+"port:"+port);
//			}
            //			添加头
            for (String key : params.keySet()) {
//				System.out.println("key= "+ key + " and value= " + params.get(key));
                get.addHeader(key, params.get(key));
            }
            get.addHeader("User-Agent", "[{\"key\":\"User-Agent\",\"value\":\"Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10\",\"description\":\"\",\"type\":\"text\",\"enabled\":false}]");
            get.addHeader("Content-Type", "application/json; charset=UTF-8");
            get.addHeader("Accept", "application/json");

            response = Client.execute(get, httpClientContext);
            getCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            content = EntityUtils.toString(entity);
            // System.out.println(TAG + "GET:" + content);
            EntityUtils.consume(entity);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (Client != null) {
                try {
                    Client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    public static String searchPost(String url, Map<String, Object> paramMap, Map<String, String> params, JSONObject hostObject) {
        CloseableHttpClient Client = HttpClients.createDefault();
        HttpClientContext httpClientContext = new HttpClientContext();
        CloseableHttpResponse response = null;
        String content = null;
        try {
//			String result = httpHost();
            // 　HttpClient中的post请求包装类
            HttpPost post = new HttpPost(url);

            //代理IP设置，代理 ip查询地址：https://www.xicidaili.com/
//			if (result !=null){
//				String[] arr = result.split(":");
//				String host = arr[0];
//				int port = Integer.parseInt(arr[1]);
            HttpHost httpHost = new HttpHost(hostObject.getString("IP"), hostObject.getInteger("PORT"));
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(1000)//设置连接超时时间,单位毫秒
                    .setSocketTimeout(1000)//设置读取超时时间,单位毫秒
                    .setProxy(httpHost)//设置代理
                    .build();
            post.setConfig(requestConfig);
//				System.out.println("HttpPost----host:"+host+"port:"+port);
//			}

//			添加头
            for (String key : params.keySet()) {
//				System.out.println("key= "+ key + " and value= " + params.get(key));
                post.addHeader(key, params.get(key));
            }
            post.addHeader("User-Agent", "[{\"key\":\"User-Agent\",\"value\":\"Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10\",\"description\":\"\",\"type\":\"text\",\"enabled\":false}]");
            post.addHeader("Content-Type", "application/json; charset=UTF-8");
            post.addHeader("Accept", "application/json, text/plain, */*");

            // 封装post请求参数
            if (null != paramMap && paramMap.size() > 0) {
                JSONObject jsonObject = new JSONObject();
                // 通过map集成entrySet方法获取entity
                Set<Map.Entry<String, Object>> entrySet = paramMap.entrySet();

                // 循环遍历，获取迭代器
                for (Map.Entry<String, Object> mapEntry : entrySet) {
                    jsonObject.put(mapEntry.getKey(), mapEntry.getValue());
//					System.out.println(mapEntry.getValue());
                }

                // 为httpPost设置封装好的请求参数
                try {
                    post.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 执行请求用execute方法，context用来帮我们附带上额外信息
            response = Client.execute(post, httpClientContext);
            // 得到相应实体、包括响应头以及相应内容
            HttpEntity entity = response.getEntity();
            // 得到response的内容
            content = EntityUtils.toString(entity);
            // System.out.println(TAG + "POST:" + content);
            // 　关闭输入流
            EntityUtils.consume(entity);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (Client != null) {
                try {
                    Client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    /**
     * post请求
     *
     * @param url
     * @param paramMap
     * @param params
     * @return
     */
    public static String sendPost(String url, Map<String, Object> paramMap, Map<String, String> params) {
        CloseableHttpClient Client = HttpClients.createDefault();
        HttpClientContext httpClientContext = new HttpClientContext();
        CloseableHttpResponse response = null;
        String content = null;
        try {
//			String result = httpHost();
            // 　HttpClient中的post请求包装类
            HttpPost post = new HttpPost(url);

            //代理IP设置，代理 ip查询地址：https://www.xicidaili.com/
//			if (result !=null){
//				String[] arr = result.split(":");
//				String host = arr[0];
//				int port = Integer.parseInt(arr[1]);
//				HttpHost httpHost = new HttpHost(host,port);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(2000)//设置连接超时时间,单位毫秒
                    .setSocketTimeout(2000)//设置读取超时时间,单位毫秒
//						.setProxy(httpHost)//设置代理
                    .build();
            post.setConfig(requestConfig);
//				System.out.println("HttpPost----host:"+host+"port:"+port);
//			}

//			添加头
            for (String key : params.keySet()) {
//				System.out.println("key= "+ key + " and value= " + params.get(key));
                post.addHeader(key, params.get(key));
            }
            post.addHeader("User-Agent", "[{\"key\":\"User-Agent\",\"value\":\"Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10\",\"description\":\"\",\"type\":\"text\",\"enabled\":false}]");
            post.addHeader("Content-Type", "application/json; charset=UTF-8");
            post.addHeader("Accept", "application/json, text/plain, */*");

            // 封装post请求参数
            if (null != paramMap && paramMap.size() > 0) {
                JSONObject jsonObject = new JSONObject();
                // 通过map集成entrySet方法获取entity
                Set<Map.Entry<String, Object>> entrySet = paramMap.entrySet();

                // 循环遍历，获取迭代器
                for (Map.Entry<String, Object> mapEntry : entrySet) {
                    jsonObject.put(mapEntry.getKey(), mapEntry.getValue());
//					System.out.println(mapEntry.getValue());
                }

                // 为httpPost设置封装好的请求参数
                try {
                    post.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 执行请求用execute方法，context用来帮我们附带上额外信息
            response = Client.execute(post, httpClientContext);
            postCode = response.getStatusLine().getStatusCode();
            // 得到相应实体、包括响应头以及相应内容
            HttpEntity entity = response.getEntity();
            // 得到response的内容
            content = EntityUtils.toString(entity);
            // System.out.println(TAG + "POST:" + content);
            // 　关闭输入流
            EntityUtils.consume(entity);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (Client != null) {
                try {
                    Client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    /**
     * post请求
     *
     * @param url
     * @param paramArr
     * @param params
     * @return
     */
    public static String sendPostArray(String url, JSONArray paramArr, Map<String, String> params) {
        CloseableHttpClient Client = HttpClients.createDefault();
        HttpClientContext httpClientContext = new HttpClientContext();
        CloseableHttpResponse response = null;
        String content = null;
        try {
//			String result = httpHost();
            // 　HttpClient中的post请求包装类
            HttpPost post = new HttpPost(url);

            //代理IP设置，代理 ip查询地址：https://www.xicidaili.com/
//			if (result !=null){
//				String[] arr = result.split(":");
//				String host = arr[0];
//				int port = Integer.parseInt(arr[1]);
//				HttpHost httpHost = new HttpHost(host,port);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(100000)//设置连接超时时间,单位毫秒
                    .setSocketTimeout(100000)//设置读取超时时间,单位毫秒
//						.setProxy(httpHost)//设置代理
                    .build();
            post.setConfig(requestConfig);
//				System.out.println("HttpPost----host:"+host+"port:"+port);
//			}

//			添加头
            for (String key : params.keySet()) {
//				System.out.println("key= "+ key + " and value= " + params.get(key));
                post.addHeader(key, params.get(key));
            }
            post.addHeader("User-Agent", "[{\"key\":\"User-Agent\",\"value\":\"Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10\",\"description\":\"\",\"type\":\"text\",\"enabled\":false}]");
            post.addHeader("Content-Type", "application/json; charset=UTF-8");
            post.addHeader("Accept", "application/json");

            // 封装post请求参数
            if (null != paramArr && paramArr.size() > 0) {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();                // 通过map集成entrySet方法获取entity
                Map<String, Object> m1 = new HashMap<String, Object>();
//				Set<Map.Entry<String, Object>> entrySet = m1.entrySet();
//				System.out.println(entrySet);
//				// 循环遍历，获取迭代器
//				for (Map<String, Object> map : list) {
//					for (Map.Entry<String, Object> m : map.entrySet()) {
//						System.out.print(m.getKey() + ":");
//						System.out.println(m.getValue());
//						m1.put(m.getKey(), m.getValue());
//					}
//				}

                // 为httpPost设置封装好的请求参数
                try {
                    post.setEntity(new StringEntity(paramArr.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            // 执行请求用execute方法，content用来帮我们附带上额外信息
            response = Client.execute(post, httpClientContext);
            arraysCode = response.getStatusLine().getStatusCode();
            // 得到相应实体、包括响应头以及相应内容
            HttpEntity entity = response.getEntity();
            // 得到response的内容
            content = EntityUtils.toString(entity);
            // System.out.println(TAG + "POST:" + content);
            // 　关闭输入流
            EntityUtils.consume(entity);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (Client != null) {
                try {
                    Client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    /**
     * put请求
     *
     * @param url
     * @param paramMap
     * @param params
     * @return
     */

    public static String sendPut(String url, Map<String, Object> paramMap, Map<String, String> params) {
        CloseableHttpClient Client = HttpClients.createDefault();
        HttpClientContext httpClientContext = new HttpClientContext();
        CloseableHttpResponse response = null;
        String content = null;
        try {
//			String result = httpHost();
            // 　HttpClient中的post请求包装类
            HttpPut put = new HttpPut(url);

            //代理IP设置，代理 ip查询地址：https://www.xicidaili.com/
//			if (result !=null){
//				String[] arr = result.split(":");
//				String host = arr[0];
//				int port = Integer.parseInt(arr[1]);
//				HttpHost httpHost = new HttpHost(host,port);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(100000)//设置连接超时时间,单位毫秒
                    .setSocketTimeout(100000)//设置读取超时时间,单位毫秒
//						.setProxy(httpHost)//设置代理
                    .build();
            put.setConfig(requestConfig);
//				System.out.println("HttpPost----host:"+host+"port:"+port);
//			}

//			添加头
            for (String key : params.keySet()) {
//				System.out.println("key= "+ key + " and value= " + params.get(key));
                put.addHeader(key, params.get(key));
            }
            put.addHeader("User-Agent", "[{\"key\":\"User-Agent\",\"value\":\"Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10\",\"description\":\"\",\"type\":\"text\",\"enabled\":false}]");
            put.addHeader("Content-Type", "application/json; charset=UTF-8");
            put.addHeader("Accept", "application/json, text/plain, */*");

            // 封装post请求参数
            if (null != paramMap && paramMap.size() > 0) {
                JSONObject jsonObject = new JSONObject();
                // 通过map集成entrySet方法获取entity
                Set<Map.Entry<String, Object>> entrySet = paramMap.entrySet();
                // 循环遍历，获取迭代器
                for (Map.Entry<String, Object> mapEntry : entrySet) {
                    jsonObject.put(mapEntry.getKey(), mapEntry.getValue());
                }

                // 为httpPost设置封装好的请求参数
                try {
                    put.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 执行请求用execute方法，content用来帮我们附带上额外信息
            response = Client.execute(put, httpClientContext);
            putCode = response.getStatusLine().getStatusCode();
            // 得到相应实体、包括响应头以及相应内容
            HttpEntity entity = response.getEntity();
            // 得到response的内容
            content = EntityUtils.toString(entity);
            // System.out.println(TAG + "POST:" + content);
            // 　关闭输入流
            EntityUtils.consume(entity);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (Client != null) {
                try {
                    Client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    //       String bookId = submitBookings();
//       if (bookId!=null){
////           确定提交，修改状态
//           updateStatus(bookId);
//       }
//        TestJob01Scheduler testJob01Scheduler = new TestJob01Scheduler();
//        testJob01Scheduler.run();


//        for(int i = 0; i <= 10; i++) {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//            System.out.println("任务打印之前："+sdf.format(new Date()));
//            getFclScheduleWithRates();
//            try {
//                sleep(180000); //暂停，每三分钟输出一次
//                System.out.println("任务打印之后："+sdf.format(new Date()));
//            }catch (InterruptedException e) {
//                e.printStackTrace();
//                return;
//            }
//        }
}
