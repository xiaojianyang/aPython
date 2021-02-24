package com.joey.apython;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.srplab.www.starcore.StarCoreFactory;
import com.srplab.www.starcore.StarCoreFactoryPath;
import com.srplab.www.starcore.StarObjectClass;
import com.srplab.www.starcore.StarServiceClass;
import com.srplab.www.starcore.StarSrvGroupClass;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

// https://www.programmersought.com/article/51306677043/

public class MainActivity extends Activity {
    public StarSrvGroupClass SrvGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final File appFile = getFilesDir();  /*-- /data/data/packageName/files --*/
        final String appLib = getApplicationInfo().nativeLibraryDir;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                loadPy(appFile, appLib);
            }
        });
    }

    void loadPy(File appFile, String appLib) {
        // Copy Python related environment
        File pythonLibFile = new File(appFile, "python3.4.zip");
        if (!pythonLibFile.exists()) {
            copyFile(this, "python3.4.zip");
            copyFile(this, "_struct.cpython-34m.so");
            copyFile(this, "binascii.cpython-34m.so");
            copyFile(this, "time.cpython-34m.so");
            copyFile(this, "zlib.cpython-34m.so");
        }

        // Copy Python code
        copyFile(this, "calljava.py");
        copyFile(this, "test.py");

        try {
            // Load the Python interpreter
            System.load(appLib + File.separator + "libpython3.4m.so");

            // In addition to copying the code directly, it also supports compressing the code into a zip package and decompressing it to the specified path through the Install method
            InputStream dataSource = getAssets().open("py_code.zip");
            StarCoreFactoryPath.Install(dataSource, appFile.getPath(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*----init starcore----*/
        StarCoreFactoryPath.StarCoreCoreLibraryPath = appLib;
        StarCoreFactoryPath.StarCoreShareLibraryPath = appLib;
        StarCoreFactoryPath.StarCoreOperationPath = appFile.getPath();

        StarCoreFactory starcore = StarCoreFactory.GetFactory();
        StarServiceClass Service = starcore._InitSimple("test", "123", 0, 0);
        SrvGroup = (StarSrvGroupClass) Service._Get("_ServiceGroup");
        Service._CheckPassword(false);

        /*----run python code----*/
        SrvGroup._InitRaw("python34", Service);
        StarObjectClass python = Service._ImportRawContext("python", "", false, "");
        // Set the Python module loading path
        python._Call("import", "sys");
        StarObjectClass pythonSys = python._GetObject("sys");
        StarObjectClass pythonPath = (StarObjectClass) pythonSys._Get("path");
        pythonPath._Call("insert", 0, appFile.getPath() + File.separator + "python3.4.zip");
        pythonPath._Call("insert", 0, appLib);
        pythonPath._Call("insert", 0, appFile.getPath());

        //Call Python code
        Service._DoFile("python", appFile.getPath() + "/py_code.py", "");
        long time = python._Calllong("get_time");
        Log.d("yangxj", "form python time=" + time);

        Service._DoFile("python", appFile.getPath() + "/test.py", "");
        long result = python._Calllong("add", 5, 2);
        Log.d("yangxj", "add result = " + result);

        result = python._Calllong("mul", 5, 2);
        Log.d("yangxj", "mul result = " + result);

        python._Set("JavaClass", Log.class);
        Service._DoFile("python", appFile.getPath() + "/calljava.py", "");
    }

    private void copyFile(Context c, String Name) {
        File outfile = new File(c.getFilesDir(), Name);
        BufferedOutputStream outStream = null;
        BufferedInputStream inStream = null;

        try {
            outStream = new BufferedOutputStream(new FileOutputStream(outfile));
            inStream = new BufferedInputStream(c.getAssets().open(Name));

            byte[] buffer = new byte[1024 * 10];
            int readLen = 0;
            while ((readLen = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, readLen);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
