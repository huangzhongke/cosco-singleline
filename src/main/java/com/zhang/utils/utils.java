package com.zhang.utils;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.core.io.ClassPathResource;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;

public class utils {
    public static List<Map<String, String>> createCookieList(String url) {
        JSONArray jsonArray = JSONArray.parseArray(readJsonFile(url));
        List<BasicNameValuePair> resultObj = new ArrayList<>();
        List<List<BasicNameValuePair>> ListPairs = new ArrayList<>();
        List<Map<String, String>> CookieList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            BasicNameValuePair pair = new BasicNameValuePair("username", jsonArray.getJSONObject(i).getString("username"));
            BasicNameValuePair pair1 = new BasicNameValuePair("password", jsonArray.getJSONObject(i).getString("password"));
            resultObj.add(pair);
            resultObj.add(pair1);
            ListPairs.add(resultObj);
        }
        for (int i = 0; i < ListPairs.size(); i++) {
            String Cookie = HttpUtils.getCookie();
            String XAuthToken = HttpUtils.getToken(Cookie, ListPairs.get(i));
            Map<String, String> object = new HashMap<>();
            object.put("Cookie", Cookie);
            object.put("X-Auth-Token", XAuthToken);
            CookieList.add(object);
        }
        System.out.println(CookieList);
        return CookieList;
    }

    /**
     * @param date1 日期1
     * @param date2 日期2
     * @return
     * @title: dateCompare
     * @description: 比较日期大小, 1:保留不变，0：新的比旧的大
     */
    public static Boolean dateCompare(Date date1, Date date2) {
        long dateFirst = date1.getTime();
        long dateLast = date2.getTime();
        return dateFirst < dateLast;
    }

    /**
     * 读取txt文件的内容
     *
     * @param file 想要读取的文件对象
     * @return 返回文件内容
     */
    public static String txt2String(File file) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(String.valueOf(file)));//构造一个BufferedReader类来读取文件
            String s = null;
            while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                result.append(System.lineSeparator()).append(s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String getNowDate(int amount, String format) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Calendar ca = Calendar.getInstance();//得到一个Calendar的实例
        ca.setTime(new Date()); //设置时间为当前时间
        ca.add(Calendar.DATE, amount); //几天之后

        return dateFormat.format(ca.getTime());
    }

    /**
     * 读取json文件，返回json串
     */
    public static String readJsonFile(String url) {
        BufferedReader reader = null;
        String json = "";
        String context = null;
        try {
            ClassPathResource resource = new ClassPathResource(url);
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            //json内容转化为Map集合通过遍历集合来进行封装
            while ((context = reader.readLine()) != null) {
                //Context就是读到的json数据
                json += context;
            }
        } catch (Exception e) {
            System.out.println("接口【DictionaryService getUnitMapping】异常参数:" + e);
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("接口【DictionaryService getUnitMapping】异常参数:" + e);
            }
        }

        return json;
    }

    public static String formatUTC(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat utcSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        Calendar cal = Calendar.getInstance();
        // 取得时间偏移量：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        // 取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        try {
            Date dateValue = sdf.parse(date);
            long longDate = dateValue.getTime();
            longDate = longDate;
            Date UTCDate = new Date(longDate);
            return utcSdf.format(UTCDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void mail(String content) throws Exception {
        // 发件人的 邮箱 和 密码（替换为自己的邮箱和密码）
        // PS: 某些邮箱服务器为了增加邮箱本身密码的安全性，给 SMTP 客户端设置了独立密码（有的邮箱称为“授权码”）,
        //     对于开启了独立密码的邮箱, 这里的邮箱密码必需使用这个独立密码（授权码）。
        String myEmailAccount = "1821851614@qq.com";
        String myEmailPassword = "xpzdfjoclqvkbgab";

        // 发件人邮箱的 SMTP 服务器地址, 必须准确, 不同邮件服务器地址不同, 一般(只是一般, 绝非绝对)格式为: smtp.xxx.com
        // 网易126邮箱的 SMTP 服务器地址为: smtp.126.com
        String myEmailSMTPHost = "smtp.qq.com";

        // 收件人邮箱（替换为自己知道的有效邮箱）
        String receiveMailAccount = "13362993760@163.com";
        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        Properties props = new Properties();                    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", myEmailSMTPHost);   // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证

        // PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接, 也可以自己开启),
        //     如果无法连接邮件服务器, 仔细查看控制台打印的 log, 如果有有类似 “连接失败, 要求 SSL 安全连接” 等错误,
        //     取消下面 /* ... */ 之间的注释代码, 开启 SSL 安全连接。
        // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,
        //                  需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助,
        //                  QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看)
        final String smtpPort = "465";
        props.setProperty("mail.smtp.port", smtpPort);
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.socketFactory.port", smtpPort);

        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getInstance(props);
        // 设置为debug模式, 可以查看详细的发送 log
        session.setDebug(true);

        // 3. 创建一封邮件
        MimeMessage message = createMimeMessage(session, myEmailAccount, receiveMailAccount, content);

        // 4. 根据 Session 获取邮件传输对象
        Transport transport = session.getTransport();

        // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
        //
        //    PS_01: 如果连接服务器失败, 都会在控制台输出相应失败原因的log。
        //    仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接,
        //    根据给出的错误类型到对应邮件服务器的帮助网站上查看具体失败原因。
        //
        //    PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
        //           (1) 邮箱没有开启 SMTP 服务;
        //           (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
        //           (3) 邮箱服务器要求必须要使用 SSL 安全连接;
        //           (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
        //           (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
        //
        transport.connect(myEmailAccount, myEmailPassword);

        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());

        // 7. 关闭连接
        transport.close();
    }

    public static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail, String content) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人
        message.setFrom(new InternetAddress(sendMail, "张子涵", "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "备用", "UTF-8"));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress("Krirto@outlook.com", "张子涵", "UTF-8"));

        // 4. Subject: 邮件主题
        message.setSubject("订单结果", "UTF-8");

        // 5. Content: 邮件正文（可以使用html标签）
        message.setContent(content, "text/html;charset=UTF-8");
        // 6. 设置发件时间
        message.setSentDate(new Date());

        // 7. 保存设置
        message.saveChanges();

        return message;
    }

    public static int caculateTotal(JSONObject jsonObject) {
        int i = 0;
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            switch (entry.getKey()) {
                case "20GP":
                    i = i + jsonObject.getJSONObject("20GP").getInteger("eachNum");
                    break;
                case "40GP":
                    i = i + 2 * jsonObject.getJSONObject("40GP").getInteger("eachNum");
                    break;
                case "40HQ":
                    i = i + 2 * jsonObject.getJSONObject("40HQ").getInteger("eachNum");
                    break;
            }
        }
        return i;
    }

    public static int caculateInfo(JSONObject jsonObject) {
        int i = 0;
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            switch (entry.getKey()) {
                case "20GP":
                    i = i + 1;
                    break;
                case "40GP":
                    i = i + 1;
                    break;
                case "40HQ":
                    i = i + 1;
                    break;
            }
        }
        return i;
    }
}
