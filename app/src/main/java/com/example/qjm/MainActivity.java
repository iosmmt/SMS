package com.example.qjm;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PickupInfoAdapter.OnItemClickListener {
    private RecyclerView pickupInfoRecyclerView;
    private PickupInfoAdapter adapter;
    private List<PickupInfo> pickupInfoList;
    private PickupInfoDatabase db;
    private BottomNavigationView bottomNavigationView;
    private EditText searchEditText;
    private Button searchButton;
    private Button addRuleButton;
    private Button historyButton;
    private Button statisticsButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar topAppBar;
    private int currentFilter = FILTER_ALL;

    // 权限请求码
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    // 过滤器常量
    private static final int FILTER_ALL = 0;
    private static final int FILTER_PENDING = 1;
    private static final int FILTER_COLLECTED = 2;

    private static final String TAG = "MainActivity";

    // 用于撤销删除的变量
    private PickupInfo deletedItem;
    private int deletedItemPosition;

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction() != null && intent.getAction().equals("com.example.qjm.UPDATE_PICKUP_LIST")) {
                    Log.d(TAG, "接收到更新取件列表广播");
                    loadPickupInfos(currentFilter);
                }
            } catch (Exception e) {
                Log.e(TAG, "处理广播接收时发生异常", e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: 开始创建Activity");
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: 已设置布局");

        try {
            // 初始化数据库
            Log.d(TAG, "onCreate: 开始初始化数据库");
            db = PickupInfoDatabase.getInstance(this);
            Log.d(TAG, "onCreate: 数据库初始化完成");

            // 初始化视图
            Log.d(TAG, "onCreate: 开始初始化视图");
            pickupInfoRecyclerView = findViewById(R.id.pickupInfoRecyclerView);
            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            searchEditText = findViewById(R.id.searchEditText);
            searchButton = findViewById(R.id.searchButton);
            addRuleButton = findViewById(R.id.addRuleButton);
            historyButton = findViewById(R.id.historyButton);
            statisticsButton = findViewById(R.id.statisticsButton);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            topAppBar = findViewById(R.id.topAppBar);
            Log.d(TAG, "onCreate: 视图初始化完成");

            // 设置Toolbar
            setSupportActionBar(topAppBar);

            // 设置RecyclerView
            Log.d(TAG, "onCreate: 开始设置RecyclerView");
            pickupInfoList = new ArrayList<>();
            adapter = new PickupInfoAdapter(pickupInfoList);
            adapter.setOnItemClickListener(this);
            pickupInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            pickupInfoRecyclerView.setAdapter(adapter);
            Log.d(TAG, "onCreate: RecyclerView设置完成");

            // 设置下拉刷新
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadPickupInfos(currentFilter);
            });

            // 添加左滑删除功能
            setUpItemTouchHelper();

            // 加载取件信息
            Log.d(TAG, "onCreate: 开始加载取件信息");
            loadPickupInfos(FILTER_ALL);
            Log.d(TAG, "onCreate: 取件信息加载请求已发送");

            // 设置底部导航菜单监听
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                try {
                    if (item.getItemId() == R.id.nav_all) {
                        currentFilter = FILTER_ALL;
                        loadPickupInfos(FILTER_ALL);
                        return true;
                    } else if (item.getItemId() == R.id.nav_pending) {
                        currentFilter = FILTER_PENDING;
                        loadPickupInfos(FILTER_PENDING);
                        return true;
                    } else if (item.getItemId() == R.id.nav_collected) {
                        currentFilter = FILTER_COLLECTED;
                        loadPickupInfos(FILTER_COLLECTED);
                        return true;
                    } else if (item.getItemId() == R.id.nav_settings) {
                        // 打开设置页面
                        Toast.makeText(MainActivity.this, "设置功能待实现", Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "处理底部导航菜单点击时发生异常", e);
                    return false;
                }
            });

            // 设置搜索按钮监听
            searchButton.setOnClickListener(v -> {
                try {
                    String keyword = searchEditText.getText().toString().trim();
                    if (!keyword.isEmpty()) {
                        searchPickupInfos(keyword);
                    } else {
                        loadPickupInfos(currentFilter);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "处理搜索按钮点击时发生异常", e);
                    Toast.makeText(MainActivity.this, "搜索时发生错误", Toast.LENGTH_SHORT).show();
                }
            });

            // 设置添加规则按钮监听
            addRuleButton.setOnClickListener(v -> {
                try {
                    // 打开添加规则页面
                    Intent intent = new Intent(MainActivity.this, AddRuleActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "打开添加规则页面时发生异常", e);
                    Toast.makeText(MainActivity.this, "打开添加规则页面时发生错误", Toast.LENGTH_SHORT).show();
                }
            });

            // 设置历史记录按钮监听
            historyButton.setOnClickListener(v -> {
                try {
                    // 打开历史记录页面
                    Intent intent = new Intent(MainActivity.this, PickupHistoryActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "打开历史记录页面时发生异常", e);
                    Toast.makeText(MainActivity.this, "打开历史记录页面时发生错误", Toast.LENGTH_SHORT).show();
                }
            });

            // 设置统计数据按钮监听
            statisticsButton.setOnClickListener(v -> {
                try {
                    // 打开统计数据页面
                    Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "打开统计数据页面时发生异常", e);
                    Toast.makeText(MainActivity.this, "打开统计数据页面时发生错误", Toast.LENGTH_SHORT).show();
                }
            });

            // 检查并请求必要权限
            checkAndRequestPermissions();
            
            // 显示Activity创建完成的Toast
            Toast.makeText(this, "MainActivity创建完成", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: MainActivity创建完成");

            // 注册广播接收器
            IntentFilter filter = new IntentFilter("com.example.qjm.UPDATE_PICKUP_LIST");
            registerReceiver(updateReceiver, filter);
        } catch (Exception e) {
            Log.e(TAG, "onCreate: 创建Activity时发生异常", e);
            Toast.makeText(this, "应用初始化失败", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // 注销广播接收器
            unregisterReceiver(updateReceiver);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: 注销广播接收器时发生异常", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                boolean allPermissionsGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
                
                if (allPermissionsGranted) {
                    Log.d(TAG, "所有权限已授予");
                    Toast.makeText(this, R.string.permission_granted_message, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "部分权限被拒绝");
                    Toast.makeText(this, R.string.permission_denied_message, Toast.LENGTH_LONG).show();
                    // 检查是否需要显示权限说明
                    showPermissionRationale();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "处理权限请求结果时发生异常", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume: Activity恢复");
            // 延迟加载数据，确保UI已经完全初始化
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "onResume: 延迟加载取件信息");
                loadPickupInfos(currentFilter);
            }, 500); // 延迟500毫秒
        } catch (Exception e) {
            Log.e(TAG, "onResume: Activity恢复时发生异常", e);
        }
    }

    // 检查并请求必要权限
    private void checkAndRequestPermissions() {
        try {
            List<String> permissionsNeeded = new ArrayList<>();
            
            // 检查短信权限
            if (checkSelfPermission(android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.READ_SMS);
            }
            
            if (checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.RECEIVE_SMS);
            }
            
            // 检查通知权限 (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS);
                }
            }
            
            // 检查电话权限
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.CALL_PHONE);
            }
            
            // 如果有需要申请的权限，则请求权限
            if (!permissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, 
                    permissionsNeeded.toArray(new String[0]), 
                    PERMISSION_REQUEST_CODE);
            } else {
                Log.d(TAG, "所有权限已授予");
            }
        } catch (Exception e) {
            Log.e(TAG, "检查并请求权限时发生异常", e);
        }
    }
    
    // 显示权限说明
    private void showPermissionRationale() {
        try {
            // 检查是否需要显示权限说明
            boolean shouldShowRationale = false;
            
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_SMS) ||
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECEIVE_SMS)) {
                shouldShowRationale = true;
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    shouldShowRationale = true;
                }
            }
            
            if (shouldShowRationale) {
                // 显示权限说明Activity
                Intent intent = new Intent(this, PermissionActivity.class);
                startActivity(intent);
            } else {
                // 用户选择了"不再询问"，引导用户手动开启权限
                showManualPermissionDialog();
            }
        } catch (Exception e) {
            Log.e(TAG, "显示权限说明时发生异常", e);
        }
    }
    
    // 显示手动开启权限对话框
    private void showManualPermissionDialog() {
        try {
            Toast.makeText(this, "请在设置中手动开启短信和通知权限，以确保应用正常工作", Toast.LENGTH_LONG).show();
            // 跳转到应用设置页面
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "显示手动开启权限对话框时发生异常", e);
        }
    }

    // 加载取件信息
    private void loadPickupInfos(int filter) {
        Log.d(TAG, "loadPickupInfos: 开始加载取件信息，filter = " + filter);
        new Thread(() -> {
            try {
                List<PickupInfo> infos;
                switch (filter) {
                    case FILTER_PENDING:
                        Log.d(TAG, "loadPickupInfos: 加载待取件信息");
                        infos = db.pickupInfoDao().getPickupInfosByStatus(PickupInfo.STATUS_UNCOLLECTED);
                        break;
                    case FILTER_COLLECTED:
                        Log.d(TAG, "loadPickupInfos: 加载已取件信息");
                        infos = db.pickupInfoDao().getPickupInfosByStatus(PickupInfo.STATUS_COLLECTED);
                        break;
                    default:
                        Log.d(TAG, "loadPickupInfos: 加载所有取件信息");
                        infos = db.pickupInfoDao().getAllPickupInfos();
                        break;
                }
                
                Log.d(TAG, "loadPickupInfos: 取件信息加载完成，共 " + infos.size() + " 条记录");
                pickupInfoList.clear();
                pickupInfoList.addAll(infos);
                runOnUiThread(() -> {
                    Log.d(TAG, "loadPickupInfos: 更新UI列表");
                    adapter.updateList(pickupInfoList);
                    // 停止下拉刷新动画
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (Exception e) {
                Log.e(TAG, "loadPickupInfos: 加载取件信息时发生异常", e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "加载数据失败", Toast.LENGTH_SHORT).show();
                    // 停止下拉刷新动画
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    // 搜索取件信息
    private void searchPickupInfos(String keyword) {
        new Thread(() -> {
            try {
                List<PickupInfo> infos = db.pickupInfoDao().searchPickupInfos("%" + keyword + "%");
                pickupInfoList.clear();
                pickupInfoList.addAll(infos);
                runOnUiThread(() -> {
                    adapter.updateList(pickupInfoList);
                    if (infos.isEmpty()) {
                        Toast.makeText(MainActivity.this, "没有找到匹配的取件信息", Toast.LENGTH_SHORT).show();
                    }
                    // 停止下拉刷新动画
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (Exception e) {
                Log.e(TAG, "searchPickupInfos: 搜索取件信息时发生异常", e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "搜索失败", Toast.LENGTH_SHORT).show();
                    // 停止下拉刷新动画
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    // 设置左滑删除功能
    private void setUpItemTouchHelper() {
        try {
            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                Drawable background;
                Drawable deleteIcon;

                {
                    try {
                        background = new ColorDrawable(Color.RED);
                        deleteIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    } catch (Exception e) {
                        Log.e(TAG, "初始化ItemTouchHelper资源时发生异常", e);
                    }
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    try {
                        int position = viewHolder.getAdapterPosition();
                        deleteItem(position);
                    } catch (Exception e) {
                        Log.e(TAG, "处理左滑删除时发生异常", e);
                    }
                }

                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    try {
                        View itemView = viewHolder.itemView;
                        
                        if (dX < 0) {
                            background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                            background.draw(c);
                            
                            if (deleteIcon != null) {
                                int itemHeight = itemView.getBottom() - itemView.getTop();
                                int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                                int intrinsicHeight = deleteIcon.getIntrinsicHeight();
                                
                                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                                int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
                                int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
                                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                                int deleteIconBottom = deleteIconTop + intrinsicHeight;
                                
                                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                                deleteIcon.draw(c);
                            }
                        }
                        
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    } catch (Exception e) {
                        Log.e(TAG, "绘制左滑删除效果时发生异常", e);
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                }
            };
            
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(pickupInfoRecyclerView);
        } catch (Exception e) {
            Log.e(TAG, "设置左滑删除功能时发生异常", e);
        }
    }

    // 删除项目
    private void deleteItem(int position) {
        try {
            // 保存被删除的项目和位置，用于撤销操作
            deletedItem = adapter.getItem(position);
            deletedItemPosition = position;
            
            // 从数据库和列表中删除
            new Thread(() -> {
                try {
                    if (deletedItem != null) {
                        db.pickupInfoDao().delete(deletedItem);
                        runOnUiThread(() -> {
                            try {
                                if (position >= 0 && position < pickupInfoList.size()) {
                                    pickupInfoList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                                
                                // 显示撤销删除的Snackbar
                                showUndoSnackbar();
                            } catch (Exception e) {
                                Log.e(TAG, "更新UI删除项目时发生异常", e);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "删除项目时发生异常", e);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "删除项目失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "准备删除项目时发生异常", e);
        }
    }

    // 显示撤销删除的Snackbar
    private void showUndoSnackbar() {
        try {
            View view = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(view, "已删除取件信息", Snackbar.LENGTH_LONG);
            snackbar.setAction("撤销", v -> {
                try {
                    // 撤销删除操作
                    undoDelete();
                } catch (Exception e) {
                    Log.e(TAG, "撤销删除操作时发生异常", e);
                }
            });
            snackbar.show();
        } catch (Exception e) {
            Log.e(TAG, "显示撤销删除Snackbar时发生异常", e);
        }
    }

    // 撤销删除操作
    private void undoDelete() {
        try {
            if (deletedItem != null) {
                new Thread(() -> {
                    try {
                        db.pickupInfoDao().insert(deletedItem);
                        runOnUiThread(() -> {
                            try {
                                pickupInfoList.add(deletedItemPosition, deletedItem);
                                adapter.notifyItemInserted(deletedItemPosition);
                                // 重新加载数据以确保一致性
                                loadPickupInfos(currentFilter);
                            } catch (Exception e) {
                                Log.e(TAG, "更新UI撤销删除时发生异常", e);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "撤销删除操作时发生异常", e);
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "撤销删除失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            }
        } catch (Exception e) {
            Log.e(TAG, "准备撤销删除操作时发生异常", e);
        }
    }

    @Override
    public void onMarkCollectedClick(int position) {
        try {
            if (position < 0 || position >= pickupInfoList.size()) {
                return;
            }
            
            PickupInfo info = pickupInfoList.get(position);
            // 切换状态
            if (info.getStatus() == PickupInfo.STATUS_UNCOLLECTED) {
                info.setStatus(PickupInfo.STATUS_COLLECTED);
            } else {
                info.setStatus(PickupInfo.STATUS_UNCOLLECTED);
            }
            
            new Thread(() -> {
                try {
                    db.pickupInfoDao().update(info);
                    runOnUiThread(() -> {
                        try {
                            adapter.notifyItemChanged(position);
                            if (info.getStatus() == PickupInfo.STATUS_COLLECTED) {
                                Toast.makeText(MainActivity.this, "已标记为已取件", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "已标记为未取件", Toast.LENGTH_SHORT).show();
                            }
                            // 如果当前筛选的是待取件或已取件，则根据状态变化决定是否移除该项
                            if ((currentFilter == FILTER_PENDING && info.getStatus() == PickupInfo.STATUS_COLLECTED) ||
                                (currentFilter == FILTER_COLLECTED && info.getStatus() == PickupInfo.STATUS_UNCOLLECTED)) {
                                if (position >= 0 && position < pickupInfoList.size()) {
                                    pickupInfoList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                            }
                            // 重新排序列表
                            loadPickupInfos(currentFilter);
                        } catch (Exception e) {
                            Log.e(TAG, "更新UI标记状态时发生异常", e);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "更新取件信息状态时发生异常", e);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "更新状态失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "处理标记已取件点击时发生异常", e);
        }
    }

    @Override
    public void onDeleteClick(int position) {
        try {
            deleteItem(position);
        } catch (Exception e) {
            Log.e(TAG, "处理删除点击时发生异常", e);
        }
    }
    
    @Override
    public void onItemClick(int position) {
        try {
            // 项点击事件，打开详情页面
            if (position >= 0 && position < pickupInfoList.size()) {
                PickupInfo info = pickupInfoList.get(position);
                Intent intent = new Intent(this, PickupInfoDetailActivity.class);
                intent.putExtra(PickupInfoDetailActivity.EXTRA_PICKUP_INFO_ID, info.getId());
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "处理项点击时发生异常", e);
            Toast.makeText(this, "打开详情页面时发生错误", Toast.LENGTH_SHORT).show();
        }
    }
}