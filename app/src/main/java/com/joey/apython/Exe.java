package com.joey.apython;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Random;

public class Exe {
    static String TAG = "ExeJoey";
    static Random random = new Random();
    static String[] TEXT = new String[]{
            "安卓加框是《一幅》吗",
            "大大安卓框用《一幅》?",
            "安卓是《一幅》软件加的框吗",
            "框安卓是用《一幅》软件？",
            "作者安卓是用《一幅》加框的？",
            "《一幅》加这种框吗，大大",
            "框是《一幅》做的吗作者",
            "大大是《一幅》做的框吗"};

    static int delay = 500;

    public static void discover() {
        Log.e(TAG, "discover");

    }

    public static String test() {
        return exe("adb devices");
    }

    public static void relative() {
        Log.e(TAG, "relative");

        exe("input tap 1013 1659");

        sleep();

        inputAndCommit();

        exe("input keyevent 4");
        exe("input keyevent 4");
    }

    private static void inputAndCommit() {
        exe("input tap 500 2000");
        sleep();

        inputText(TEXT[random.nextInt(TEXT.length)]);

        sleep();

        // 提交
        exe("input tap 970 1850");

        // sleep();
        // 收起输入法
        // exe("input keyevent 4")
        // 退出
        // exe("input keyevent 4")
    }

    private static void inputText(String raw) {
//        String charsb64 = base64.b64encode(str);
        exe("am broadcast -a ADB_INPUT_B64 --es msg " + raw);
    }

    private static void sleep() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
    }


    private static String exe(String cmd) {
        return shellExecSu(cmd);
    }

    /*
     root了的手机还是没权限，试试
     * adb root
     * adb remount
     * adb shell
     * chmod 6777 /system/xbin/su


     adb devices
     */
    private static String shellExecSu(String cmd) {
        Log.d(TAG, "shellExecSu cmd: " + cmd);
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream stream = new DataOutputStream(outputStream);
            stream.writeBytes(cmd);
            stream.flush();
            String ret = exe(process);

            stream.close();
            outputStream.close();

            Log.d(TAG, "shellExecSu ret: " + ret);
            return ret;

        } catch (Throwable t) {
            Log.e(TAG, "shellExecSu exception", t);
            return null;
        }
    }

    // "adb version"
    public static String shellExec(String cmd) {
        Log.d(TAG, "shellExec cmd: " + cmd);
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(cmd);
            String ret = exe(process);
            Log.d(TAG, "shellExec ret: " + ret);
            return ret;
        } catch (IOException e) {
            Log.e(TAG, "shellExec exception", e);
            return null;
        }
    }

    private static String exe(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder buffer = new StringBuilder();
        char[] buff = new char[1024];
        int ch;
        while ((ch = reader.read(buff)) != -1) {
            buffer.append(buff, 0, ch);
        }
        reader.close();
        return buffer.toString();
    }
}
