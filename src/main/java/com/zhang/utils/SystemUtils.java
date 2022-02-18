/**
 * Copyright (c) 2018 新耶科技 All rights reserved.
 *
 * https://www.newjelu.io
 *
 * 版权所有，侵权必究！
 */

package com.zhang.utils;


import javax.servlet.http.HttpServletRequest;

/**
 * 系统工具类
 *
 * @author chenqi
 */
public class SystemUtils {

    public static Boolean isWin()
    {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static Boolean isLinux()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;
    }

    /**
     * 判断是否是手机端访问
     * @param request
     * @return
     */
     public static boolean isMobileDevice(HttpServletRequest request) {
            String requestHeader = request.getHeader("user-agent");
            String[] deviceArray = new String[] {"android","windows phone"};
            if(requestHeader==null) {
                return false;
            }
            requestHeader = requestHeader.toLowerCase();
            for (String device:deviceArray) {
                if(requestHeader.indexOf(device)!=-1) {
                    return true;
                }
            }
            return false;
        }

}