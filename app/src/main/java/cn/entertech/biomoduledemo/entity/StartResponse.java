package cn.entertech.biomoduledemo.entity;

public class StartResponse {
    private String request_id;
    private String session_id;

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

    @Override
    public String toString() {
        return "StartResponse{" +
                "request_id='" + request_id + '\'' +
                ", session_id='" + session_id + '\'' +
                '}';
    }
}
