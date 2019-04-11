# Android Launcher3
origin_test_origin 这个分支记录研究 launcher3 代码的信息

## 拖拽

- BaseItemDragListener (抽象类) 实现了拖拽监听的接口
  - onDrag 方法-》当拖动开始的时候初始化。其他交给 DragController 处理 (通过 onDragEvent 方法)
  - DragDriver 