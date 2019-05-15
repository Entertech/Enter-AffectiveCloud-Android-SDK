package cn.entertech.biomoduledemo.test;

import android.os.Build;
import androidx.annotation.RequiresApi;
import cn.entertech.biomoduledemo.entity.DataEntity;
import cn.entertech.biomoduledemo.websocket.WebSocketManager;
import com.google.gson.Gson;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.java_websocket.handshake.ServerHandshake;

import java.util.Arrays;
import java.util.List;

public class TestCase {
    public static void main(String[] args) {
        String testFile = "/Users/Enter/Code/BiomoduleDemo/app/src/main/java/cn/entertech/biomoduledemo/test/flowtime_eegdata.txt";
        final WebSocketManager webSocketManager = WebSocketManager.Companion.getInstance();
        webSocketManager.addBrainDataListener(new Function1<String, Unit>() {
            @Override
            public Unit invoke(String s) {
                return null;
            }
        });
        webSocketManager.connect(new Function1<ServerHandshake, Unit>() {
            @Override
            public Unit invoke(ServerHandshake serverHandshake) {
                Runnable runnable = new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        String eegData = FileUtil.readFile(testFile);
                        String eegData1 = eegData.replace("[", "").replace("]", "").replace(" ", "");
                        String[] eegDataArray = eegData1.split(",");
                        Integer[] eegInts = new Integer[eegDataArray.length];
                        String fileDebugData = "";
                        for (int i = 0; i < eegDataArray.length; i++) {
                            if (i % 600 == 0) {
                                fileDebugData = fileDebugData + eegDataArray[i] + ",";
                            }
                            eegInts[i] = Integer.parseInt(eegDataArray[i]);
                        }
                        List<Integer> eegList = Arrays.asList(eegInts);
                        DataEntity startData = new DataEntity();
                        startData.setRequest_id(System.currentTimeMillis() + "");
                        startData.setCommand("start");
                        startData.setDevice_id("A0");
                        String startJson = new Gson().toJson(startData);
                        webSocketManager.sendMessage(startJson);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        int startIndex = 0;
                        int step = 600;
                        String rawData = "";
                        while (startIndex + step < eegList.size()) {
                            List<Integer> eegSub = eegList.subList(startIndex, startIndex + step);
                            int[] eegArray = eegSub.stream().mapToInt(i -> i).toArray();
                            rawData = rawData + eegList.get(startIndex) + ",";
                            DataEntity processData = new DataEntity();
                            processData.setCommand("process");
                            processData.setRequest_id(System.currentTimeMillis() + "");
                            processData.setData(eegArray);
                            String processJson1 = new Gson().toJson(processData);
                            webSocketManager.sendMessage(processJson1);
                            startIndex = startIndex + step;
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        List<Integer> eegSub = eegList.subList(startIndex, eegList.size());
                        int[] eegArray1 = eegSub.stream().mapToInt(i -> i).toArray();
                        DataEntity processData = new DataEntity();
                        processData.setCommand("process");
                        processData.setRequest_id(System.currentTimeMillis() + "");
                        processData.setData(eegArray1);
                        String processJson1 = new Gson().toJson(processData);
                        webSocketManager.sendMessage(processJson1);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        DataEntity finishData = new DataEntity();
                        finishData.setCommand("finish");
                        finishData.setRequest_id(System.currentTimeMillis() + "");
                        String json2 = new Gson().toJson(finishData);
                        webSocketManager.sendMessage(json2);
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                return null;
            }
        });


    }

}
