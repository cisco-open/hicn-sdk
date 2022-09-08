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

package io.fd.hicn.hicntools.adapter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HiGetOutputListViewElement implements Serializable {

    private String higetUrl;
    private String higetsavedPath;
    private String higetNameFile;
    private String higetMd5;
    private int higetSize;
    private Date higetDate;

    public HiGetOutputListViewElement(String higetUrl, String higetsavedPath, String higetNameFile, String higetMd5, int higetSize) {
        this.higetUrl = higetUrl;
        this.higetsavedPath = higetsavedPath;
        this.higetNameFile = higetNameFile;
        this.higetMd5 = higetMd5;
        this.higetSize = higetSize;
        this.higetDate = new Date();
    }

    public String getHigetUrl() {
        return higetUrl;
    }

    public void setHigetGetUrl(String higetUrl) {
        this.higetUrl = higetUrl;
    }

    public String getHigetSavedPath() {
        return higetsavedPath;
    }

    public void setHigetSavedPath(String higetsavedPath) {
        this.higetsavedPath = higetsavedPath;
    }

    public String getHigetNameFile() {
        return higetNameFile;
    }

    public void setHigetNameFile(String higetNameFile) {
        this.higetNameFile = higetNameFile;
    }

    public String getHigetMd5() {
        return higetMd5;
    }

    public void setHigetMd5(String higetMd5) {
        this.higetMd5 = higetMd5;
    }

    public int getHigetSize() {
        return higetSize;
    }

    public void setHigetSize(int higetSize) {
        this.higetSize = higetSize;
    }

    public Date getHigetDate() {
        return higetDate;
    }

    public void setHigetDate(Date higetDate) {
        this.higetDate = higetDate;
    }

    public String getHigetDateString(String format) {
        return new SimpleDateFormat(format).format(higetDate);
    }


}
