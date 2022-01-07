package cn.entertech.biomoduledemo.utils;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileHelper {

    public static FileHelper mInstance = null;

    private Handler mHandler;
    private HandlerThread handlerThread;

    private FileHelper() {
        handlerThread = new HandlerThread("write_file_thread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public static FileHelper getInstance() {
        if (mInstance == null) {
            synchronized (FileHelper.class) {
                if (mInstance == null) {
                    mInstance = new FileHelper();
                }
            }
        }
        return mInstance;
    }

    private PrintWriter eegPw;
    private PrintWriter hrPw;
    private PrintWriter realtimeDataPw;
    private PrintWriter reportDataPw;
    private boolean isFirstWriteEEG = true;
    private boolean isFirstWriteHR = true;
    private boolean isFirstWriteRealtime = true;
    private boolean isFirstWriteReport = true;

    public void setEEGPath(String filePath) {
        try {
            eegPw = new PrintWriter(new FileWriter(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHRPath(String filePath) {
        try {
            hrPw = new PrintWriter(new FileWriter(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setRealtimeDataPath(String filePath) {
        try {
            realtimeDataPw = new PrintWriter(new FileWriter(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setReportDataPath(String filePath) {
        try {
            reportDataPw = new PrintWriter(new FileWriter(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeEEG(String data) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isFirstWriteEEG) {
                    eegPw.print(data);
                    isFirstWriteEEG = false;
                } else {
                    eegPw.append(data);
                }
                eegPw.flush();
            }
        });
    }
    public void writeRealtimeData(String data) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isFirstWriteRealtime) {
                    realtimeDataPw.print(data);
                    isFirstWriteRealtime = false;
                } else {
                    realtimeDataPw.append(data);
                }
                realtimeDataPw.flush();
            }
        });
    }
    public void writeReportData(String data) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isFirstWriteReport) {
                    reportDataPw.print(data);
                    isFirstWriteReport = false;
                } else {
                    reportDataPw.append(data);
                }
                reportDataPw.flush();
            }
        });
    }

    public void writeHr(String data) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isFirstWriteHR) {
                    hrPw.print(data);
                    isFirstWriteHR = false;
                } else {
                    hrPw.append(data);
                }
                hrPw.flush();
            }
        });
    }
}
