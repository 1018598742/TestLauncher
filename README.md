# 配合 Launcher3 学习使用的方法

## MainActivity 中测试方法

- 获取屏幕信息

- xml pull 解析内置的布局信息

## SecondActivity 中测试拖拽方法

- View 中的 startDrag 拖拽方法
- View 中拖拽监听 setOnDragListener
  - DragEvent.ACTION_DRAG_STARTED 拖拽开始事件
  - DragEvent.ACTION_DRAG_ENTERED 被拖放View进入目标View
  - DragEvent.ACTION_DRAG_LOCATION 保持拖动状态
  - DragEvent.ACTION_DRAG_EXITED 被拖放View离开目标View
  - DragEvent.ACTION_DROP 释放拖放阴影，并获取移动数据
  - DragEvent.ACTION_DRAG_ENDED 拖放事件完成

