package com.easy.database;

import android.text.TextUtils;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Log工具，类似android.util.Log。
 * tag自动产生，格式: customTagPrefix:className.methodName(L:lineNumber),
 * customTagPrefix为空时只输出：className.methodName(L:lineNumber)。
 */
public class LogUtils {
    public static String customTagPrefix = "easy_database";
    private static final int LOG_MAX_LENGTH = 3500;
    private static String generateTag() {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
        return tag;
    }

    public static void d(String content) {
        String tag = generateTag();
        for(String str:strSplit(content)){
            Log.d(tag, str);
        }
    }

    public static void e(String content) {
        String tag = generateTag();
        for(String str:strSplit(content))
            Log.e(tag, str);
    }

    public static void e(String msg, Throwable tr){
        String tag = generateTag();
        Log.e(tag, msg, tr);
    }

    public static void e(Throwable tr) {
        String tag = generateTag();
        Log.e(tag, getMessage(tr));
    }

    public static void i(String content) {
        String tag = generateTag();
        for(String str:strSplit(content))
            Log.i(tag, str);
    }

    public static void i(String content, Throwable tr) {
        String tag = generateTag();
        Log.i(tag, content, tr);
    }

    public static void v(String content) {
        String tag = generateTag();
        Log.v(tag, content);
    }

    public static void v(String content, Throwable tr) {
        String tag = generateTag();

        Log.v(tag, content, tr);
    }

    public static void w(String content) {
        String tag = generateTag();
        Log.w(tag, content);
    }

    public static void w(String content, Throwable tr) {
        String tag = generateTag();
        Log.w(tag, content, tr);
    }

    public static void w(Throwable tr) {
        String tag = generateTag();
        Log.w(tag, tr);
    }


    public static void wtf(String content) {
        String tag = generateTag();
        Log.wtf(tag, content);
    }

    public static void wtf(String content, Throwable tr) {
        String tag = generateTag();
        Log.wtf(tag, content, tr);
    }

    public static void wtf(Throwable tr) {
        String tag = generateTag();
        Log.wtf(tag, tr);
    }

    private static ArrayList<String> strSplit(String log){
        ArrayList<String> logs = new ArrayList<>();
        if(log == null || log.length() == 0 || log.length() < LOG_MAX_LENGTH){
            logs.add(log);
        }else{
            int sum = log.length()/LOG_MAX_LENGTH;
            if(sum*LOG_MAX_LENGTH<log.length()){
                sum+=1;
            }
            for(int i=0;i<sum;i++){
                int startIndex = i*LOG_MAX_LENGTH;
                int endIndex = (i+1)*LOG_MAX_LENGTH;
                if(endIndex > log.length()) endIndex = log.length();
                logs.add(log.substring(startIndex,endIndex));
            }
        }
        return logs;
    }

    public static String getMessage(Throwable paramThrowable){
        StringWriter localStringWriter = new StringWriter();
        PrintWriter localPrintWriter = new PrintWriter(localStringWriter);
        paramThrowable.printStackTrace(localPrintWriter);
        for (Throwable localThrowable = paramThrowable.getCause(); localThrowable != null; localThrowable = localThrowable.getCause())
            localThrowable.printStackTrace(localPrintWriter);
        String str = localStringWriter.toString();
        localPrintWriter.close();
        return str;
    }
}
