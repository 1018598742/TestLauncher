/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fta.skr.testmethod;

import android.graphics.Point;

/**
 * 不变设备配置文件
 */
public class InvariantDeviceProfile {


    // Profile-defining invariant properties
    public String name;
    public float minWidthDps;
    public float minHeightDps;

    /**
     * Number of icons per row and column in the workspace.
     */
    public int numRows;
    public int numColumns;

    /**
     * Number of icons per row and column in the folder.
     */
    public int numFolderRows;
    public int numFolderColumns;
    public float iconSize;
    public float landscapeIconSize;
    public int iconBitmapSize;
    public int fillResIconDpi;
    public float iconTextSize;

    /**
     * Number of icons inside the hotseat area.
     * 在 hotseat 图标数量。
     */
    public int numHotseatIcons;

    public int defaultLayoutId;
    public int demoModeLayoutId;


    public InvariantDeviceProfile(String n, float w, float h, int r, int c, int fr, int fc,
                                  float is, float lis, float its, int hs, int dlId, int dmlId) {
        name = n;
        minWidthDps = w;
        minHeightDps = h;
        numRows = r;
        numColumns = c;
        numFolderRows = fr;
        numFolderColumns = fc;
        iconSize = is;
        landscapeIconSize = lis;
        iconTextSize = its;
        numHotseatIcons = hs;
        defaultLayoutId = dlId;
        demoModeLayoutId = dmlId;
    }


    @Override
    public String toString() {
        return "InvariantDeviceProfile{" +
                "name='" + name + '\'' +
                ", minWidthDps=" + minWidthDps +
                ", minHeightDps=" + minHeightDps +
                ", numRows=" + numRows +
                ", numColumns=" + numColumns +
                ", numFolderRows=" + numFolderRows +
                ", numFolderColumns=" + numFolderColumns +
                ", iconSize=" + iconSize +
                ", landscapeIconSize=" + landscapeIconSize +
                ", iconBitmapSize=" + iconBitmapSize +
                ", fillResIconDpi=" + fillResIconDpi +
                ", iconTextSize=" + iconTextSize +
                ", numHotseatIcons=" + numHotseatIcons +
                ", defaultLayoutId=" + defaultLayoutId +
                ", demoModeLayoutId=" + demoModeLayoutId +
                '}';
    }
}