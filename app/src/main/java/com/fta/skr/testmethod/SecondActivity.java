package com.fta.skr.testmethod;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    private static final String IMAGEVIEW_TAG = "已经拖到目标区域了";
    private static final String TAG = "My_Test";
    private ImageView imageView;
    private LinearLayout container;
    private RelativeLayout topContainer;
    private TextView title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        initView();

        initHandle();
    }

    private void initView() {
        imageView = (ImageView) findViewById(R.id.img);
        imageView.setTag(IMAGEVIEW_TAG);
        container = (LinearLayout) findViewById(R.id.container);
        topContainer = (RelativeLayout) findViewById(R.id.topContainer);
        title = (TextView) findViewById(R.id.title);
    }


    private void initHandle() {
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData.Item item = new ClipData.Item((String) v.getTag());
                ClipData data = new ClipData(IMAGEVIEW_TAG,
                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                        item);
                v.startDrag(data, new View.DragShadowBuilder(v), null, 0);
                return true;
            }
        });

        topContainer.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
//                Log.i(TAG, "SecondActivity-onDrag: 顶层布局=" + action);
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED://1
                        Log.i(TAG, "SecondActivity-onDrag: 顶层布局=拖拽开始事件" );
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED://5
                        Log.i(TAG, "SecondActivity-onDrag: 顶层布局=被拖放View进入目标View" );
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION://2
                        Log.i(TAG, "SecondActivity-onDrag: 顶层布局=保持拖动状态2" );
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED://6 被拖放目标view脱离目标view
                        Log.i(TAG, "SecondActivity-onDrag: 顶层布局=被拖放View离开目标View" );
                        return true;
                    case DragEvent.ACTION_DROP://3
                        Log.i(TAG, "SecondActivity-onDrag: 顶层布局=释放拖放阴影，并获取移动数据" );
                        imageView.setX(event.getX() - imageView.getWidth() / 2);
                        imageView.setY(event.getY() - imageView.getHeight() / 2);
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED://4
                        Log.i(TAG, "SecondActivity-onDrag: 顶层布局=拖放事件完成" );
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });

        container.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
//                Log.i(TAG, "SecondActivity-onDrag: 下面的父布局=" + action);
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED://1
                        Log.i(TAG, "SecondActivity-onDrag: 下面的父布局=拖拽开始事件" );
                        //拖拽开始事件
                        if (event.getClipDescription().hasMimeType(
                                ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                            return true;
                        }
                        return false;
                    case DragEvent.ACTION_DRAG_ENTERED://5
                        //被拖放View进入目标View
                        Log.i(TAG, "SecondActivity-onDrag: 下面的父布局=被拖放View进入目标View" );
                        container.setBackgroundColor(Color.YELLOW);
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION://2
                        Log.i(TAG, "SecondActivity-onDrag: 下面的父布局=保持拖动状态2" );
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED://6
                        //被拖放View离开目标View
                        Log.i(TAG, "SecondActivity-onDrag: 下面的父布局=被拖放View离开目标View" );
                        container.setBackgroundColor(Color.BLUE);
                        title.setText("");
                        return true;
                    case DragEvent.ACTION_DROP://3
                        //释放拖放阴影，并获取移动数据
                        Log.i(TAG, "SecondActivity-onDrag: 下面的父布局=释放拖放阴影，并获取移动数据" );
                        ClipData.Item item = event.getClipData().getItemAt(0);
                        String dragData = item.getText().toString();
                        title.setText(dragData + event.getY() + ":" + event.getX());
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED://4
                        //拖放事件完成
                        Log.i(TAG, "SecondActivity-onDrag: 下面的父布局=拖放事件完成" );
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
    }
}
