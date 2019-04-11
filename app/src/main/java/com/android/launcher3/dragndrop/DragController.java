/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher3.dragndrop;

import android.content.ComponentName;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.android.launcher3.DeleteDropTarget;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.accessibility.DragViewStateAnnouncer;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.config.TagConfig;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.Thunk;
import com.android.launcher3.util.TouchController;
import com.android.launcher3.util.UiThreadHelper;

import java.util.ArrayList;

import static com.android.launcher3.LauncherAnimUtils.SPRING_LOADED_EXIT_DELAY;
import static com.android.launcher3.LauncherState.NORMAL;

/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class DragController implements DragDriver.EventListener, TouchController {
    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;
    private static final String TAG = TagConfig.TAG;

    @Thunk
    Launcher mLauncher;
    private FlingToDeleteHelper mFlingToDeleteHelper;

    // temporaries to avoid gc thrash
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];

    /**
     * Drag driver for the current drag/drop operation, or null if there is no active DND operation.
     * It's null during accessible drag operations.
     */
    private DragDriver mDragDriver = null;

    /**
     * Options controlling the drag behavior.
     */
    private DragOptions mOptions;

    /**
     * X coordinate of the down event.
     */
    private int mMotionDownX;

    /**
     * Y coordinate of the down event.
     */
    private int mMotionDownY;

    private DropTarget.DragObject mDragObject;

    /**
     * Who can receive drop events
     */
    private ArrayList<DropTarget> mDropTargets = new ArrayList<>();
    private ArrayList<DragListener> mListeners = new ArrayList<>();

    /**
     * The window token used as the parent for the DragView.
     */
    private IBinder mWindowToken;

    private View mMoveTarget;

    private DropTarget mLastDropTarget;

    @Thunk
    int mLastTouch[] = new int[2];
    @Thunk
    long mLastTouchUpTime = -1;
    @Thunk
    int mDistanceSinceScroll = 0;

    private int mTmpPoint[] = new int[2];
    private Rect mDragLayerRect = new Rect();

    private boolean mIsInPreDrag;

    /**
     * Interface to receive notifications when a drag starts or stops
     */
    public interface DragListener {
        /**
         * A drag has begun
         *
         * @param dragObject The object being dragged
         * @param options    Options used to start the drag
         */
        void onDragStart(DropTarget.DragObject dragObject, DragOptions options);

        /**
         * The drag has ended
         */
        void onDragEnd();
    }

    /**
     * Used to create a new DragLayer from XML.
     */
    public DragController(Launcher launcher) {
        mLauncher = launcher;
        mFlingToDeleteHelper = new FlingToDeleteHelper(launcher);
    }

    /**
     * Starts a drag.
     * When the drag is started, the UI automatically goes into spring loaded mode. On a successful
     * drop, it is the responsibility of the {@link DropTarget} to exit out of the spring loaded
     * mode. If the drop was cancelled for some reason, the UI will automatically exit out of this mode.
     * 当拖动开始时，UI自动进入弹簧加载模式。成功*删除后，{@link DropTarget}有责任退出弹簧加载*模式。如果由于某种原因取消了放置，UI将自动退出此模式
     *
     * @param b          The bitmap to display as the drag image.  It will be re-scaled to the
     *                   enlarged size.//要显示为拖动图像的位图。它将被重新缩放到*放大尺寸
     * @param dragLayerX The x position in the DragLayer of the left-top of the bitmap.位图左上角的DragLayer中的x位置。
     * @param dragLayerY The y position in the DragLayer of the left-top of the bitmap.位图左上角的DragLayer中的y位置。
     * @param source     An object representing where the drag originated 表示拖动源自的位置的对象
     * @param dragInfo   The data associated with the object that is being dragged 与正在拖动的对象关联的数据
     * @param dragRegion Coordinates within the bitmap b for the position of item being dragged. 位图b内的坐标，用于拖动项目的位置。
     *                   Makes dragging feel more precise, e.g. you can clip out a transparent border
     *                   <p>
     *                   开始拖动
     */
    public DragView startDrag(Bitmap b, int dragLayerX, int dragLayerY,
                              DragSource source, ItemInfo dragInfo, Point dragOffset, Rect dragRegion,
                              float initialDragViewScale, float dragViewScaleOnDrop, DragOptions options) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        //隐藏软键盘
        UiThreadHelper.hideKeyboardAsync(mLauncher, mWindowToken);

        mOptions = options;
        if (mOptions.systemDndStartPoint != null) {
            mMotionDownX = mOptions.systemDndStartPoint.x;
            mMotionDownY = mOptions.systemDndStartPoint.y;
        }

        final int registrationX = mMotionDownX - dragLayerX;
        final int registrationY = mMotionDownY - dragLayerY;

        final int dragRegionLeft = dragRegion == null ? 0 : dragRegion.left;
        final int dragRegionTop = dragRegion == null ? 0 : dragRegion.top;

        mLastDropTarget = null;

        mDragObject = new DropTarget.DragObject();

        mIsInPreDrag = mOptions.preDragCondition != null
                && !mOptions.preDragCondition.shouldStartDrag(0);

        final Resources res = mLauncher.getResources();
        final float scaleDps = mIsInPreDrag
                ? res.getDimensionPixelSize(R.dimen.pre_drag_view_scale) : 0f;
        //初始化拖动视图
        final DragView dragView = mDragObject.dragView = new DragView(mLauncher, b, registrationX,
                registrationY, initialDragViewScale, dragViewScaleOnDrop, scaleDps);
        dragView.setItemInfo(dragInfo);
        mDragObject.dragComplete = false;
        if (mOptions.isAccessibleDrag) {
            // For an accessible drag, we assume the view is being dragged from the center.
            mDragObject.xOffset = b.getWidth() / 2;
            mDragObject.yOffset = b.getHeight() / 2;
            mDragObject.accessibleDrag = true;
        } else {
            mDragObject.xOffset = mMotionDownX - (dragLayerX + dragRegionLeft);
            mDragObject.yOffset = mMotionDownY - (dragLayerY + dragRegionTop);
            mDragObject.stateAnnouncer = DragViewStateAnnouncer.createFor(dragView);

            //桌面图标拖动时初始化拖动驱动
            mDragDriver = DragDriver.create(mLauncher, this, mDragObject, mOptions);
        }

        mDragObject.dragSource = source;
        mDragObject.dragInfo = dragInfo;
        mDragObject.originalDragInfo = new ItemInfo();
        mDragObject.originalDragInfo.copyFrom(dragInfo);

        if (dragOffset != null) {
            dragView.setDragVisualizeOffset(new Point(dragOffset));
        }
        if (dragRegion != null) {
            dragView.setDragRegion(new Rect(dragRegion));
        }

        mLauncher.getDragLayer().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        dragView.show(mMotionDownX, mMotionDownY);
        mDistanceSinceScroll = 0;

        if (!mIsInPreDrag) {
            callOnDragStart();
        } else if (mOptions.preDragCondition != null) {
            mOptions.preDragCondition.onPreDragStart(mDragObject);
        }

        mLastTouch[0] = mMotionDownX;
        mLastTouch[1] = mMotionDownY;
        //
        Log.i(TAG, "DragController-startDrag: ");
        handleMoveEvent(mMotionDownX, mMotionDownY);
        mLauncher.getUserEventDispatcher().resetActionDurationMillis();
        return dragView;
    }

    private void callOnDragStart() {
        if (mOptions.preDragCondition != null) {
            mOptions.preDragCondition.onPreDragEnd(mDragObject, true /* dragStarted*/);
        }
        mIsInPreDrag = false;
        for (DragListener listener : new ArrayList<>(mListeners)) {
            listener.onDragStart(mDragObject, mOptions);
        }
    }

    /**
     * Call this from a drag source view like this:
     * <p>
     * <pre>
     *  @Override
     *  public boolean dispatchKeyEvent(KeyEvent event) {
     *      return mDragController.dispatchKeyEvent(this, event)
     *              || super.dispatchKeyEvent(event);
     * </pre>
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragDriver != null;
    }

    public boolean isDragging() {
        return mDragDriver != null || (mOptions != null && mOptions.isAccessibleDrag);
    }

    /**
     * Stop dragging without dropping.
     */
    public void cancelDrag() {
        if (isDragging()) {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mDragObject.deferDragViewCleanupPostAnimation = false;
            mDragObject.cancelled = true;
            mDragObject.dragComplete = true;
            if (!mIsInPreDrag) {
                dispatchDropComplete(null, false);
            }
        }
        endDrag();
    }

    private void dispatchDropComplete(View dropTarget, boolean accepted) {
        if (!accepted) {
            // If it was not accepted, cleanup the state. If it was accepted, it is the
            // responsibility of the drop target to cleanup the state.
            mLauncher.getStateManager().goToState(NORMAL, SPRING_LOADED_EXIT_DELAY);
            mDragObject.deferDragViewCleanupPostAnimation = false;
        }

        mDragObject.dragSource.onDropCompleted(dropTarget, mDragObject, accepted);
    }

    public void onAppsRemoved(ItemInfoMatcher matcher) {
        // Cancel the current drag if we are removing an app that we are dragging
        if (mDragObject != null) {
            ItemInfo dragInfo = mDragObject.dragInfo;
            if (dragInfo instanceof ShortcutInfo) {
                ComponentName cn = dragInfo.getTargetComponent();
                if (cn != null && matcher.matches(dragInfo, cn)) {
                    cancelDrag();
                }
            }
        }
    }

    private void endDrag() {
        if (isDragging()) {
            mDragDriver = null;
            boolean isDeferred = false;
            if (mDragObject.dragView != null) {
                isDeferred = mDragObject.deferDragViewCleanupPostAnimation;
                if (!isDeferred) {
                    mDragObject.dragView.remove();
                } else if (mIsInPreDrag) {
                    animateDragViewToOriginalPosition(null, null, -1);
                }
                mDragObject.dragView = null;
            }

            // Only end the drag if we are not deferred
            if (!isDeferred) {
                callOnDragEnd();
            }
        }

        mFlingToDeleteHelper.releaseVelocityTracker();
    }

    public void animateDragViewToOriginalPosition(final Runnable onComplete,
                                                  final View originalIcon, int duration) {
        Runnable onCompleteRunnable = new Runnable() {
            @Override
            public void run() {
                if (originalIcon != null) {
                    originalIcon.setVisibility(View.VISIBLE);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        };
        mDragObject.dragView.animateTo(mMotionDownX, mMotionDownY, onCompleteRunnable, duration);
    }

    private void callOnDragEnd() {
        if (mIsInPreDrag && mOptions.preDragCondition != null) {
            mOptions.preDragCondition.onPreDragEnd(mDragObject, false /* dragStarted*/);
        }
        mIsInPreDrag = false;
        mOptions = null;
        for (DragListener listener : new ArrayList<>(mListeners)) {
            listener.onDragEnd();
        }
    }

    /**
     * This only gets called as a result of drag view cleanup being deferred in endDrag();
     */
    void onDeferredEndDrag(DragView dragView) {
        dragView.remove();

        if (mDragObject.deferDragViewCleanupPostAnimation) {
            // If we skipped calling onDragEnd() before, do it now
            callOnDragEnd();
        }
    }

    /**
     * Clamps the position to the drag layer bounds.
     */
    private int[] getClampedDragLayerPos(float x, float y) {
        mLauncher.getDragLayer().getLocalVisibleRect(mDragLayerRect);
        mTmpPoint[0] = (int) Math.max(mDragLayerRect.left, Math.min(x, mDragLayerRect.right - 1));
        mTmpPoint[1] = (int) Math.max(mDragLayerRect.top, Math.min(y, mDragLayerRect.bottom - 1));
        return mTmpPoint;
    }

    public long getLastGestureUpTime() {
        if (mDragDriver != null) {
            return System.currentTimeMillis();
        } else {
            return mLastTouchUpTime;
        }
    }

    public void resetLastGestureUpTime() {
        mLastTouchUpTime = -1;
    }

    @Override
    public void onDriverDragMove(float x, float y) {
        final int[] dragLayerPos = getClampedDragLayerPos(x, y);

        handleMoveEvent(dragLayerPos[0], dragLayerPos[1]);
    }

    @Override
    public void onDriverDragExitWindow() {
        if (mLastDropTarget != null) {
            mLastDropTarget.onDragExit(mDragObject);
            mLastDropTarget = null;
        }
    }

    @Override
    public void onDriverDragEnd(float x, float y) {
        DropTarget dropTarget;
        Runnable flingAnimation = mFlingToDeleteHelper.getFlingAnimation(mDragObject);
        if (flingAnimation != null) {
            dropTarget = mFlingToDeleteHelper.getDropTarget();
        } else {
            dropTarget = findDropTarget((int) x, (int) y, mCoordinatesTemp);
        }

        drop(dropTarget, flingAnimation);

        endDrag();
    }

    @Override
    public void onDriverDragCancel() {
        cancelDrag();
    }

    /**
     * Call this from a drag source view.
     */
    @Override
    public boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        if (mOptions != null && mOptions.isAccessibleDrag) {
            return false;
        }

        // Update the velocity tracker
        mFlingToDeleteHelper.recordMotionEvent(ev);

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                break;
            case MotionEvent.ACTION_UP:
                mLastTouchUpTime = System.currentTimeMillis();
                break;
        }

        return mDragDriver != null && mDragDriver.onInterceptTouchEvent(ev);
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onDragEvent(long dragStartTime, DragEvent event) {
        mFlingToDeleteHelper.recordDragEvent(dragStartTime, event);
        return mDragDriver != null && mDragDriver.onDragEvent(event);
    }

    /**
     * Call this from a drag view.
     */
    public void onDragViewAnimationEnd() {
        if (mDragDriver != null) {
            mDragDriver.onDragViewAnimationEnd();
        }
    }

    /**
     * Sets the view that should handle move events.
     */
    public void setMoveTarget(View view) {
        mMoveTarget = view;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mMoveTarget != null && mMoveTarget.dispatchUnhandledMove(focused, direction);
    }

    private void handleMoveEvent(int x, int y) {
        mDragObject.dragView.move(x, y);

        // Drop on someone?
        final int[] coordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(x, y, coordinates);
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        checkTouchMove(dropTarget);

        // Check if we are hovering over the scroll areas
        mDistanceSinceScroll += Math.hypot(mLastTouch[0] - x, mLastTouch[1] - y);
        mLastTouch[0] = x;
        mLastTouch[1] = y;

        if (mIsInPreDrag && mOptions.preDragCondition != null
                && mOptions.preDragCondition.shouldStartDrag(mDistanceSinceScroll)) {
            callOnDragStart();
        }
    }

    public float getDistanceDragged() {
        return mDistanceSinceScroll;
    }

    public void forceTouchMove() {
        int[] dummyCoordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(mLastTouch[0], mLastTouch[1], dummyCoordinates);
        mDragObject.x = dummyCoordinates[0];
        mDragObject.y = dummyCoordinates[1];
        checkTouchMove(dropTarget);
    }

    /**
     * 从此进入显示框 并且 dropTarget 不为空
     * 上一个 DropTarget 跟设置这个不是一个
     * 开始拖动的是否 mLastDropTarget 设置为 null
     * @param dropTarget
     */
    private void checkTouchMove(DropTarget dropTarget) {
        if (dropTarget != null) {
            if (mLastDropTarget != dropTarget) {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragObject);
                }
                Log.i(TAG, "DragController-checkTouchMove: ");
                dropTarget.onDragEnter(mDragObject);
            }
            dropTarget.onDragOver(mDragObject);
        } else {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
        }
        mLastDropTarget = dropTarget;
    }

    /**
     * Call this from a drag source view.
     */
    @Override
    public boolean onControllerTouchEvent(MotionEvent ev) {
        if (mDragDriver == null || mOptions == null || mOptions.isAccessibleDrag) {
            return false;
        }

        // Update the velocity tracker
        mFlingToDeleteHelper.recordMotionEvent(ev);

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                break;
        }

        return mDragDriver.onTouchEvent(ev);
    }

    /**
     * Since accessible drag and drop won't cause the same sequence of touch events, we manually
     * inject the appropriate state.
     */
    public void prepareAccessibleDrag(int x, int y) {
        mMotionDownX = x;
        mMotionDownY = y;
    }

    /**
     * As above, since accessible drag and drop won't cause the same sequence of touch events,
     * we manually ensure appropriate drag and drop events get emulated for accessible drag.
     */
    public void completeAccessibleDrag(int[] location) {
        final int[] coordinates = mCoordinatesTemp;

        // We make sure that we prime the target for drop.
        DropTarget dropTarget = findDropTarget(location[0], location[1], coordinates);
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        checkTouchMove(dropTarget);

        dropTarget.prepareAccessibilityDrop();
        // Perform the drop
        drop(dropTarget, null);
        endDrag();
    }

    private void drop(DropTarget dropTarget, Runnable flingAnimation) {
        final int[] coordinates = mCoordinatesTemp;
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];

        // Move dragging to the final target.
        if (dropTarget != mLastDropTarget) {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mLastDropTarget = dropTarget;
            if (dropTarget != null) {
                Log.i(TAG, "DragController-drop: ");
                dropTarget.onDragEnter(mDragObject);
            }
        }

        mDragObject.dragComplete = true;
        if (mIsInPreDrag) {
            if (dropTarget != null) {
                dropTarget.onDragExit(mDragObject);
            }
            return;
        }

        // Drop onto the target.
        boolean accepted = false;
        if (dropTarget != null) {
            dropTarget.onDragExit(mDragObject);
            if (dropTarget.acceptDrop(mDragObject)) {
                if (flingAnimation != null) {
                    flingAnimation.run();
                } else {
                    dropTarget.onDrop(mDragObject, mOptions);
                }
                accepted = true;

                // TODO: 2019/4/10 拖动移除功能改为卸载
                if (FeatureFlags.REMOVE_DRAWER && dropTarget instanceof DeleteDropTarget &&
                        isNeedCancelDrag(mDragObject.dragInfo)) {
                    cancelDrag();
                }
            }
        }
        final View dropTargetAsView = dropTarget instanceof View ? (View) dropTarget : null;
        mLauncher.getUserEventDispatcher().logDragNDrop(mDragObject, dropTargetAsView);
        dispatchDropComplete(dropTargetAsView, accepted);
    }

    /**
     * 判断是否可以取消
     * @param item
     * @return
     */
    private boolean isNeedCancelDrag(ItemInfo item){
        return (item.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                item.itemType == LauncherSettings.Favorites.ITEM_TYPE_FOLDER);
    }

    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        mDragObject.x = x;
        mDragObject.y = y;

        final Rect r = mRectTemp;
        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i = count - 1; i >= 0; i--) {
            DropTarget target = dropTargets.get(i);
            if (!target.isDropEnabled())
                continue;

            target.getHitRectRelativeToDragLayer(r);
            if (r.contains(x, y)) {
                dropCoordinates[0] = x;
                dropCoordinates[1] = y;
                mLauncher.getDragLayer().mapCoordInSelfToDescendant((View) target, dropCoordinates);
                return target;
            }
        }
        // Pass all unhandled drag to workspace. Workspace finds the correct
        // cell layout to drop to in the existing drag/drop logic.
        dropCoordinates[0] = x;
        dropCoordinates[1] = y;
        mLauncher.getDragLayer().mapCoordInSelfToDescendant(mLauncher.getWorkspace(),
                dropCoordinates);
        return mLauncher.getWorkspace();
    }

    public void setWindowToken(IBinder token) {
        mWindowToken = token;
    }

    /**
     * Sets the drag listener which will be notified when a drag starts or ends.
     * 设置拖动开始或结束时将通知的拖动侦听器。
     */
    public void addDragListener(DragListener l) {
        mListeners.add(l);
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        mListeners.remove(l);
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     * 将DropTarget添加到接收放置事件的潜在位置列表中。
     */
    public void addDropTarget(DropTarget target) {
        mDropTargets.add(target);
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
    }

}
