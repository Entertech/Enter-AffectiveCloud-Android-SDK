package cn.entertech.biomoduledemo;

public class ConvertUtil {
    public static int converUnchart(byte data) {
        return (data & 0xff);
    }
}
