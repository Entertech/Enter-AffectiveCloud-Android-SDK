package cn.entertech.biomoduledemo.test;

import java.io.*;

public class FileUtil {
    public static String readFile(String path) {
        String result = "";
        File file = new File(path);
        if (!file.exists()) {
            return "no file exist";
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }
}
