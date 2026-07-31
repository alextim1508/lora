package com.alextim.lora.ui.util;

import android.graphics.Color;

public class StatusColor {

    public static final int COLOR_IDLE = Color.parseColor("#808080");
    public static final int COLOR_SENDING = Color.parseColor("#FFA500");
    public static final int COLOR_SUCCESS = Color.parseColor("#4CAF50");
    public static final int COLOR_ERROR = Color.parseColor("#F44336");
    public static final int COLOR_TIMEOUT = Color.parseColor("#FF5722");

    // Цвета для раздельных графиков отправленных/полученных сообщений (см.
    // ui.util.MessageCountChartView, DataFragment#updateDevicesInfoDisplay) - сохранены такими же,
    // какими раньше были COLOR_SENT/COLOR_RECEIVED внутри самого MessageCountChartView, когда он
    // рисовал обе серии на одном графике.
    public static final int CHART_SENT_COLOR = Color.parseColor("#2196F3");
    public static final int CHART_RECEIVED_COLOR = Color.parseColor("#4CAF50");
}
