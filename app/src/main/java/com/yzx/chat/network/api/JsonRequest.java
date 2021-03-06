package com.yzx.chat.network.api;


/**
 * Created by YZX on 2017年10月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class JsonRequest {

    private String token;
    private int status = 200;
    private Object params;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }
}
