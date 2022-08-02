/*
 * Copyright (c) 2019 Cisco and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cisco.hicn.forwarder.interfaces;

public class Facelet {
    private int id;
    private String netdevice;
    private String netdeviceType;
    private String family;
    private String localAddr;
    private int localPort;
    private String remoteAddr;
    private int remotePort;
    private String faceType;
    private String status;
    private boolean error;

    public Facelet(int id, String netdevice,
                   String netdeviceType,
                   String family,
                   String localAddr,
                   int localPort,
                   String remoteAddr,
                   int remotePort,
                   String faceType,
                   String status,
                   boolean error) {
        this.id = id;
        this.netdevice = netdevice;
        this.netdeviceType = netdeviceType;
        this.family = family;
        this.localAddr = localAddr;
        this.localPort = localPort;
        this.remoteAddr = remoteAddr;
        this.remotePort = remotePort;
        this.faceType = faceType;
        this.status = status;
        this.error = error;
    }

    public int getId() { return id;}

    public String getNetdevice() {
        return netdevice;
    }

    public void setNetdevice(String netdevice) {
        this.netdevice = netdevice;
    }

    public String getNetdeviceType() {
        return netdeviceType;
    }

    public void setNetdeviceType(String netdeviceType) {
        this.netdeviceType = netdeviceType;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getFaceType() {
        return faceType;
    }

    public void setFaceType(String faceType) {
        this.faceType = faceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
