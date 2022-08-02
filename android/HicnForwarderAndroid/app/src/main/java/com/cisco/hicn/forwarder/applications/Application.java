/*
 * Copyright (c) 2019-2020 Cisco and/or its affiliates.
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

package com.cisco.hicn.forwarder.applications;

import android.graphics.drawable.Drawable;

public class Application {
    public Application(String name, String packageName, Drawable icon)
    {
        name_ = name;
        packageName_ = packageName;
        icon_ = icon;
    }

    String getName()
    {
        return name_;
    }

    String getPackageName()
    {
        return packageName_;
    }

    Drawable getIcon()
    {
        return icon_;
    }

    boolean getPuntByDefault() { return puntByDefault_; }
    void setPuntByDefault(boolean value) { puntByDefault_ = value; }

    private String name_;
    private String packageName_;
    private Drawable icon_;
    private boolean puntByDefault_;
}
