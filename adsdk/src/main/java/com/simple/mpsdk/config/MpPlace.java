package com.simple.mpsdk.config;

/**
 * Created by Administrator on 2018/2/9.
 */

public class MpPlace {

    private String name;

    private String type;

    private String pid;

    private long cacheTime;

    private long loadTime;

    private String aid;

    private String eid;

    private String uniqueValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }

    public long getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(long loadTime) {
        this.loadTime = loadTime;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getUniqueValue() {
        return uniqueValue;
    }

    public void setUniqueValue(String uniqueValue) {
        this.uniqueValue = uniqueValue;
    }

    @Override
    public String toString() {
        return "adp{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", pid='" + pid + '\'' +
                '}';
    }
}
