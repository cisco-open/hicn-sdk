package com.cisco.hicn.facemgrlibrary.utility;

public enum NetdeviceTypeEnum {
    NETDEVICE_TYPE_UNDEFINED(0),
    NETDEVICE_TYPE_LOOPBACK(1),
    NETDEVICE_TYPE_WIRED(2),
    NETDEVICE_TYPE_WIFI(3),
    NETDEVICE_TYPE_CELLULAR(4),
    NETDEVICE_TYPE_VPN(5);

    private int value;

    NetdeviceTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
