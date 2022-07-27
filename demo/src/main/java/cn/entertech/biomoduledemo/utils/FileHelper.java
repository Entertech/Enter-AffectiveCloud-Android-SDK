package cn.entertech.biomoduledemo.utils;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class FileHelper {

    public static FileHelper mInstance = null;

    private Handler mHandler;
    private HandlerThread handlerThread;
    private String filePath;

    public FileHelper() {
        handlerThread = new HandlerThread("write_file_thread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

//    public static FileHelper getInstance() {
//        if (mInstance == null) {
//            synchronized (FileHelper.class) {
//                if (mInstance == null) {
//                    mInstance = new FileHelper();
//                }
//            }
//        }
//        return mInstance;
//    }

    private PrintWriter currentDataPw;
    private boolean isFirstWriteEEG = true;

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        try {
            currentDataPw = new PrintWriter(new FileWriter(filePath));
            isFirstWriteEEG = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeData(final String data) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isFirstWriteEEG) {
                    currentDataPw.print(data);
                    isFirstWriteEEG = false;
                } else {
                    currentDataPw.append(data);
                }
                currentDataPw.flush();
            }
        });
    }

    public interface OnFileReadListener {
        void onRead(String result);
    }

    public void overrideWrite(final String data) {
        try {
            currentDataPw = new PrintWriter(new FileWriter(filePath));
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    currentDataPw.print(data);
                    currentDataPw.flush();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readFile(final OnFileReadListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                FileInputStream fis = null;
                try {
                    if (!file.exists()) listener.onRead("");
                    fis = new FileInputStream(file);
                    int length = fis.available();
                    byte[] buffer = new byte[length];
                    fis.read(buffer);
                    String res = new String(buffer, "UTF-8");
                    fis.close();
                    listener.onRead(res);
                } catch (IOException e) {
                    listener.onRead("error");
                    e.printStackTrace();
                }
            }
        });
    }

    public static String readFile(String fileName) {
        File file = new File(fileName);
        FileInputStream fis = null;
        try {
            if (!file.exists()) return null;
            String result = "";
            String line;
            fis = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
            line = bufferedReader.readLine();
            while (line != null) {
                result = result + line+",";
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            fis.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
