package com.example.ips;
/*
    Used to store single wifi info
 */
public class WIFI {
    public int level;
    public String bssid;
    public String ssid;
    public long freq;

    public WIFI(int level, String bssid, String ssid, long freq) {
        this.level = level;
        this.bssid = bssid ;
        this.ssid = ssid;
        this.freq = freq;
    }
}
