package cn.entertech.affectivecloudsdk.utils;

import java.io.*;

public class FileUtil {

    /**
     * 读文件
     *
     * @param fileName
     * @return
     * @throws IOException
     */
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
                result = result + line;
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
