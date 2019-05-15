package cn.entertech.biomoduledemo.entity;

import java.util.Arrays;

public class DataEntity {
    private String request_id;
    private String command;
    private String device_id;
    private String session_id;
    private int[] data;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int[] getData() {
        return data;
    }

    public void setInt(int num){

    }
    public void setData(int[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataEntity{" +
                "command='" + command + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }
}
