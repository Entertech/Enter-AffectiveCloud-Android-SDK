package cn.entertech.affectivecloudsdk;

import android.util.Log;
import cn.entertech.affectivecloudsdk.entity.Error;
import cn.entertech.affectivecloudsdk.entity.RealtimeAffectiveData;
import cn.entertech.affectivecloudsdk.entity.RealtimeBioData;
import cn.entertech.affectivecloudsdk.entity.Service;
import cn.entertech.affectivecloudsdk.interfaces.Callback;
import cn.entertech.affectivecloudsdk.interfaces.Callback2;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.Nullable;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static cn.entertech.affectivecloudsdk.utils.FileUtil.readFile;
import static junit.framework.TestCase.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
@PowerMockIgnore("javax.net.ssl.*")
public class EnterAffectiveCloudManagerTest {

    public static String APP_KEY = "6eabf68e-760e-11e9-bd82-0242ac140006";
    public static String APP_SECRET = "68a09cf8e4e06718b037c399f040fb7e";
    /*自己的用户ID：邮箱或者手机号码*/
    public static String USER_ID = "12809@qq.com";
    public static String EEG_TEST_FILE_PATH = "/Users/Enter/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/flowtime_eegdata.txt";
    public static String HR_TEST_FILE_PATH = "/Users/Enter/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/flowtime_hrdata.txt";
    public static String websocketAddress = "wss://server.affectivecloud.com/ws/algorithm/v1/";
    static List<Service> availableAffectiveServices =
            new ArrayList();
    static List<Service> availableBioServices =
            new ArrayList();
    private static BiodataSubscribeParams biodataSubscribeParams;
    private static AffectiveSubscribeParams affectiveSubscribeParams;
    private static EnterAffectiveCloudConfig enterAffectiveCloudConfig;
    private static EnterAffectiveCloudManager enterAffectiveCloudManager;
    private static Function1<String, Unit> rawJsonResponseFunction;
    private static Function1<String, Unit> rawJsonRequestFunction;

    @BeforeClass
    public static void init() {
        PowerMockito.mockStatic(Log.class);
        availableAffectiveServices.add(Service.ATTENTION);
        availableAffectiveServices.add(Service.PRESSURE);
        availableAffectiveServices.add(Service.AROUSAL);
        availableAffectiveServices.add(Service.RELAXATION);
        availableAffectiveServices.add(Service.PLEASURE);
        availableBioServices.add(Service.EEG);
        availableBioServices.add(Service.HR);
        biodataSubscribeParams = new BiodataSubscribeParams.Builder()
                .requestAllHrData()
                .requestAllEEGData()
                .build();

        affectiveSubscribeParams = new AffectiveSubscribeParams.Builder()
                .requestAttention()
                .requestRelaxation()
                .requestPressure()
                .requestPleasure()
                .requestArousal()
                .build();
        enterAffectiveCloudConfig = new EnterAffectiveCloudConfig.Builder(APP_KEY, APP_SECRET, USER_ID)
                .url(websocketAddress)
                .availableBiodataServices(availableBioServices)
                .availableAffectiveServices(availableAffectiveServices)
                .biodataSubscribeParams(biodataSubscribeParams)
                .affectiveSubscribeParams(affectiveSubscribeParams)
                .build();
        enterAffectiveCloudManager = new EnterAffectiveCloudManager(enterAffectiveCloudConfig);
        rawJsonResponseFunction = new Function1<String, Unit>() {
            @Override
            public Unit invoke(String s) {
                System.out.println(s);
                return null;
            }
        };
        rawJsonRequestFunction = new Function1<String, Unit>() {
            @Override
            public Unit invoke(String s) {
//                System.out.println(s);
                return null;
            }
        };
        enterAffectiveCloudManager.addRawJsonResponseListener(rawJsonResponseFunction);
        enterAffectiveCloudManager.addRawJsonRequestListener(rawJsonRequestFunction);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final boolean[] isSuccess = {false};

        enterAffectiveCloudManager.init(new Callback() {
            @Override
            public void onSuccess() {
                isSuccess[0] = true;
                countDownLatch.countDown();
            }

            @Override
            public void onError(@Nullable Error error) {
                isSuccess[0] = false;
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(true, isSuccess[0]);
    }

    @Test
    public void testRealtimeData() {
        PowerMockito.mockStatic(Log.class);
        final boolean[] results = new boolean[2];
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        enterAffectiveCloudManager.addBiodataRealtimeListener(new Function1<RealtimeBioData, Unit>() {
            @Override
            public Unit invoke(RealtimeBioData realtimeBioData) {
                results[0] = true;
                countDownLatch.countDown();
                return null;
            }
        });

        enterAffectiveCloudManager.addAffectiveDataRealtimeListener(new Function1<RealtimeAffectiveData, Unit>() {
            @Override
            public Unit invoke(RealtimeAffectiveData realtimeAffectiveData) {
                results[1] = true;
                countDownLatch.countDown();
                return null;
            }
        });
        uploadEEGRawData();
        uploadHRRawData();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(true, results[0]);
        assertEquals(true, results[1]);
    }

    @Test
    public void testReportData() {
        final double[] attention = {0.0};
        final double[] relaxation = {0.0};
        final double[] pleasure = {0.0};
        final double[] pressure = {0.0};
        final double[] arousal = {0.0};
        final double[] hr = {0.0};
        PowerMockito.mockStatic(Log.class);
        final boolean[] results = new boolean[2];
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        enterAffectiveCloudManager.getBiodataReport(new Callback2<HashMap<Object, Object>>() {
            @Override
            public void onSuccess(@Nullable HashMap<Object, Object> objectObjectHashMap) {
                Map<Object, Object> hrMap;
                if (objectObjectHashMap != null && objectObjectHashMap.containsKey("hr")) {
                    hrMap = (Map<Object, Object>) (objectObjectHashMap.get("hr"));
                    if (hrMap.containsKey("hr_avg")) {
                        hr[0] = (double) hrMap.get("hr_avg");
                    }
                }
                results[0] = true;
                countDownLatch.countDown();
            }

            @Override
            public void onError(@Nullable Error error) {
                results[0] = false;
                countDownLatch.countDown();
            }
        });
        enterAffectiveCloudManager.getAffectiveDataReport(new Callback2<HashMap<Object, Object>>() {
            @Override
            public void onSuccess(@Nullable HashMap<Object, Object> objectObjectHashMap) {
                Map<Object, Object> attentionMap;
                if (objectObjectHashMap != null && objectObjectHashMap.containsKey("attention")) {
                    attentionMap = (Map<Object, Object>) (objectObjectHashMap.get("attention"));
                    if (attentionMap.containsKey("attention_avg")) {
                        attention[0] = (double) attentionMap.get("attention_avg");
                    }
                }
                Map<Object, Object> relaxationMap;
                if (objectObjectHashMap != null && objectObjectHashMap.containsKey("relaxation")) {
                    relaxationMap = (Map<Object, Object>) (objectObjectHashMap.get("relaxation"));
                    if (relaxationMap.containsKey("relaxation_avg")) {
                        relaxation[0] = (double) relaxationMap.get("relaxation_avg");
                    }
                }
                Map<Object, Object> pleasureMap;
                if (objectObjectHashMap != null && objectObjectHashMap.containsKey("pleasure")) {
                    pleasureMap = (Map<Object, Object>) (objectObjectHashMap.get("pleasure"));
                    if (pleasureMap.containsKey("pleasure_avg")) {
                        pleasure[0] = (double) pleasureMap.get("pleasure_avg");
                    }
                }

                Map<Object, Object> pressureMap;
                if (objectObjectHashMap != null && objectObjectHashMap.containsKey("pressure")) {
                    pressureMap = (Map<Object, Object>) (objectObjectHashMap.get("pressure"));
                    if (pressureMap.containsKey("pressure_avg")) {
                        pressure[0] = (double) pressureMap.get("pressure_avg");
                    }
                }
                Map<Object, Object> arousalMap;
                if (objectObjectHashMap != null && objectObjectHashMap.containsKey("arousal")) {
                    arousalMap = (Map<Object, Object>) (objectObjectHashMap.get("arousal"));
                    if (arousalMap.containsKey("arousal_avg")) {
                        arousal[0] = (double) arousalMap.get("arousal_avg");
                    }
                }
                results[1] = true;
                countDownLatch.countDown();
            }

            @Override
            public void onError(@Nullable Error error) {
                results[1] = false;
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(true, results[0]);
        assertEquals(true, results[1]);
        assertEquals(61.6, attention[0], 1);
        assertEquals(44.14, relaxation[0], 1);
        assertEquals(45.07, pleasure[0], 1);
        assertEquals(35.02, pressure[0], 1);
        assertEquals(40.67, arousal[0], 1);
        assertEquals(67, hr[0], 1);
    }

    @AfterClass
    public static void release() {
        PowerMockito.mockStatic(Log.class);
        final boolean[] results = new boolean[1];
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        enterAffectiveCloudManager.release(new Callback() {
            @Override
            public void onSuccess() {
                results[0] = true;
                countDownLatch.countDown();
            }

            @Override
            public void onError(@Nullable Error error) {
                results[0] = false;
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        enterAffectiveCloudManager.removeRawJsonResponseListener(rawJsonResponseFunction);
        enterAffectiveCloudManager.removeRawJsonRequestListener(rawJsonRequestFunction);
        assertEquals(true, results[0]);
    }

    public void uploadEEGRawData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String data = readFile(EEG_TEST_FILE_PATH);
                String[] eeg = data.split(",");
                for (int i = 0; i < eeg.length; i = i + 20) {
                    byte[] eegs = new byte[20];
                    for (int j = 0; j < 20; j++) {
                        eegs[j] = (byte) Integer.parseInt(eeg[j + i]);
                    }
                    enterAffectiveCloudManager.appendEEGData(eegs, 600);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void uploadHRRawData() {
        String data = readFile(HR_TEST_FILE_PATH);
        String[] hr = data.split(",");
        for (int i = 0; i < hr.length; i++) {
            enterAffectiveCloudManager.appendHeartRateData(Integer.parseInt(hr[i]), 2);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}