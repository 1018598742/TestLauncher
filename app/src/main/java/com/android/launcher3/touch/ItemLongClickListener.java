/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.launcher3.touch;

import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;

import com.android.launcher3.CellLayout;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DropTarget;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.config.TagConfig;
import com.android.launcher3.dragndrop.DragController;
import com.android.launcher3.dragndrop.DragOptions;
import com.android.launcher3.folder.Folder;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.android.launcher3.LauncherState.ALL_APPS;
import static com.android.launcher3.LauncherState.NORMAL;
import static com.android.launcher3.LauncherState.OVERVIEW;

/**
 * Class to handle long-clicks on workspace items and start drag as a result.
 * 用于处理工作区项目的长按并开始拖动的类。
 */
public class ItemLongClickListener {
    private static final String TAG = TagConfig.TAG;

//    public static OnLongClickListener INSTANCE_WORKSPACE =
//            ItemLongClickListener::onWorkspaceItemLongClick;

    public static OnLongClickListener INSTANCE_WORKSPACE = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return onWorkspaceItemLongClick(v);
        }
    };

//    public static OnLongClickListener INSTANCE_ALL_APPS =
//            ItemLongClickListener::onAllAppsItemLongClick;

    public static OnLongClickListener INSTANCE_ALL_APPS = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return onAllAppsItemLongClick(v);
        }
    };

    private static boolean onWorkspaceItemLongClick(View v) {
        Launcher launcher = Launcher.getLauncher(v.getContext());
        //不可以拖动直接返回false
        if (!canStartDrag(launcher))
            return false;
        //不是标准状态并且launcher是不是覆盖状态下直接返回false
        if (!launcher.isInState(NORMAL) && !launcher.isInState(OVERVIEW))
            return false;
        //不是 ItemInfo 的子类直接返回false
        if (!(v.getTag() instanceof ItemInfo))
            return false;
        //当拖动的是否 startActivityForResult 或者 requestPermissionResult 都不工作
        launcher.setWaitingForResult(null);
        //开始拖拽
        beginDrag(v, launcher, (ItemInfo) v.getTag(), new DragOptions());
        return true;
    }

    public static void beginDrag(View v, Launcher launcher, ItemInfo info,
                                 DragOptions dragOptions) {
        //位置
        Log.d(TAG, "ItemLongClickListener-beginDrag: "+info.toString());
        //info.container
        //CONTAINER_DESKTOP -100
        //CONTAINER_HOTSEAT -101
        if (info.container >= 0) {//文件夹
            Folder folder = Folder.getOpen(launcher);
            if (folder != null) {
                if (!folder.getItemsInReadingOrder().contains(v)) {
                    folder.close(true);
                } else {
                    folder.startDrag(v, dragOptions);
                    return;
                }
            }
        }

        CellLayout.CellInfo longClickCellInfo = new CellLayout.CellInfo(v, info);
        launcher.getWorkspace().startDrag(longClickCellInfo, dragOptions);
    }

    private static boolean onAllAppsItemLongClick(View v) {
        Launcher launcher = Launcher.getLauncher(v.getContext());
        if (!canStartDrag(launcher)) return false;
        // When we have exited all apps or are in transition, disregard long clicks
        if (!launcher.isInState(ALL_APPS) && !launcher.isInState(OVERVIEW)) return false;
        if (launcher.getWorkspace().isSwitchingState()) return false;

        // Start the drag
        final DragController dragController = launcher.getDragController();
        dragController.addDragListener(new DragController.DragListener() {
            /**
             * 开始拖动；由于之前注册了接口 addDragListener
             * @param dragObject The object being dragged
             * @param options Options used to start the drag
             */
            @Override
            public void onDragStart(DropTarget.DragObject dragObject, DragOptions options) {
                v.setVisibility(INVISIBLE);
            }

            @Override
            public void onDragEnd() {
                v.setVisibility(VISIBLE);
                dragController.removeDragListener(this);
            }
        });

        DeviceProfile grid = launcher.getDeviceProfile();
        DragOptions options = new DragOptions();
        options.intrinsicIconScaleFactor = (float) grid.allAppsIconSizePx / grid.iconSizePx;
        launcher.getWorkspace().beginDragShared(v, launcher.getAppsView(), options);
        return false;
    }

    public static boolean canStartDrag(Launcher launcher) {
        if (launcher == null) {
            return false;
        }
        // We prevent dragging when we are loading the workspace as it is possible to pick up a view
        // that is subsequently removed from the workspace in startBinding().
        if (launcher.isWorkspaceLocked()) return false;
        // Return early if an item is already being dragged (e.g. when long-pressing two shortcuts)
        if (launcher.getDragController().isDragging()) return false;

        return true;
    }
}
