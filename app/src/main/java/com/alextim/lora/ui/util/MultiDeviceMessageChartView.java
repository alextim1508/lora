package com.alextim.lora.ui.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Collections;
import java.util.List;

/**
 * Гистограмма "Sent/Received" сразу по всем подключенным устройствам: по оси X - устройства
 * (каждое - своя группа из двух столбцов), по оси Y - количество сообщений. Синий столбец - Sent,
 * зеленый - Received (те же цвета, что раньше использовались в MessageCountChartView), одинаковые
 * для всех устройств - чтобы значение столбца определялось подписью снизу, а не цветом устройства.
 * Под каждой группой столбцов - две строки подписи: сокращенный MAC-адрес устройства и имя его
 * LoRa-модуля (если оно уже известно, иначе "--").
 *
 * Не требует внешних библиотек графиков - отрисовка через Canvas/Paint, как и в
 * MessageCountChartView.
 */
public class MultiDeviceMessageChartView extends View {

    private static final int COLOR_SENT = Color.parseColor("#2196F3");
    private static final int COLOR_RECEIVED = Color.parseColor("#4CAF50");
    private static final int COLOR_AXIS = Color.parseColor("#BDBDBD");
    private static final int COLOR_LABEL = Color.parseColor("#FFFFFF");
    private static final int COLOR_VALUE = Color.parseColor("#FFFFFF");
    private static final int COLOR_GRID = Color.parseColor("#3D3D3D");
    private static final int COLOR_GRID_LABEL = Color.parseColor("#999999");

    private List<String> deviceLabels = Collections.emptyList();
    private List<String> loraNames = Collections.emptyList();
    private long[] sentCounts = new long[0];
    private long[] receivedCounts = new long[0];

    private final Paint sentPaint = new Paint();
    private final Paint receivedPaint = new Paint();
    private final Paint axisPaint = new Paint();
    private final Paint labelPaint = new Paint();
    private final Paint valuePaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint gridLabelPaint = new Paint();

    public MultiDeviceMessageChartView(Context context) {
        super(context);
        init();
    }

    public MultiDeviceMessageChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiDeviceMessageChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        sentPaint.setColor(COLOR_SENT);
        sentPaint.setStyle(Paint.Style.FILL);
        receivedPaint.setColor(COLOR_RECEIVED);
        receivedPaint.setStyle(Paint.Style.FILL);
        axisPaint.setColor(COLOR_AXIS);
        axisPaint.setStrokeWidth(2f);
        labelPaint.setColor(COLOR_LABEL);
        labelPaint.setAntiAlias(true);
        labelPaint.setTextSize(spToPx(10f));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(COLOR_VALUE);
        valuePaint.setAntiAlias(true);
        valuePaint.setTextSize(spToPx(10f));
        valuePaint.setTextAlign(Paint.Align.CENTER);

        gridPaint.setColor(COLOR_GRID);
        gridPaint.setStrokeWidth(1f);

        gridLabelPaint.setColor(COLOR_GRID_LABEL);
        gridLabelPaint.setAntiAlias(true);
        gridLabelPaint.setTextSize(spToPx(9f));
        gridLabelPaint.setTextAlign(Paint.Align.RIGHT);
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * Задает данные для отрисовки: подписи устройств (обычно MAC-адреса), имена их LoRa-модулей
     * и параллельные им массивы количества отправленных/полученных сообщений. Все параметры
     * должны быть одинаковой длины (loraNames может быть null - тогда вторая строка подписи не
     * рисуется). Вызывает перерисовку.
     */
    public void setData(List<String> deviceLabels, List<String> loraNames, long[] sentCounts, long[] receivedCounts) {
        this.deviceLabels = deviceLabels != null ? deviceLabels : Collections.emptyList();
        this.loraNames = loraNames != null ? loraNames : Collections.emptyList();
        this.sentCounts = sentCounts != null ? sentCounts : new long[0];
        this.receivedCounts = receivedCounts != null ? receivedCounts : new long[0];
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int deviceCount = deviceLabels.size();
        if (width <= 0 || height <= 0 || deviceCount == 0) {
            return;
        }

        float lineHeight = labelPaint.getTextSize() + 4f;
        // Две строки подписи снизу: MAC-адрес и имя LoRa-модуля.
        float labelBlockHeight = lineHeight * 2 + 4f;
        float valueHeight = valuePaint.getTextSize() + 4f;
        // Небольшой дополнительный отступ сверху, иначе подпись значения над самым высоким
        // столбцом (когда он достигает максимума шкалы) обрезается верхней границей View.
        float topPadding = gridLabelPaint.getTextSize() / 2f + 2f;
        // Слева резервируем место под подписи оси Y (сетка значений количества сообщений).
        float yLabelWidth = gridLabelPaint.measureText("000") + 4f;

        float chartLeft = yLabelWidth;
        float chartRight = width;
        float baseline = height - labelBlockHeight;
        float chartTop = valueHeight + topPadding;

        long max = 1;
        for (int i = 0; i < deviceCount; i++) {
            max = Math.max(max, sentCounts[i]);
            max = Math.max(max, i < receivedCounts.length ? receivedCounts[i] : 0);
        }

        float usableHeight = baseline - chartTop;

        drawValueGrid(canvas, max, chartLeft, chartRight, baseline, usableHeight, yLabelWidth);

        canvas.drawLine(chartLeft, baseline, chartRight, baseline, axisPaint);

        float usableWidth = chartRight - chartLeft;
        float slotWidth = usableWidth / deviceCount;
        float barWidth = Math.max(2f, slotWidth / 2f - 4f);

        for (int i = 0; i < deviceCount; i++) {
            float slotStart = chartLeft + i * slotWidth;
            float slotCenter = slotStart + slotWidth / 2f;

            long sent = sentCounts[i];
            float sentX = slotCenter - barWidth - 1f;
            if (sent > 0) {
                float barHeight = usableHeight * sent / max;
                canvas.drawRect(sentX, baseline - barHeight, sentX + barWidth, baseline, sentPaint);
                canvas.drawText(String.valueOf(sent), sentX + barWidth / 2f, baseline - barHeight - 4f, valuePaint);
            }

            long received = i < receivedCounts.length ? receivedCounts[i] : 0;
            float receivedX = slotCenter + 1f;
            if (received > 0) {
                float barHeight = usableHeight * received / max;
                canvas.drawRect(receivedX, baseline - barHeight, receivedX + barWidth, baseline, receivedPaint);
                canvas.drawText(String.valueOf(received), receivedX + barWidth / 2f, baseline - barHeight - 4f, valuePaint);
            }

            String label = deviceLabels.get(i);
            // Показываем только последние 5 символов MAC-адреса, чтобы подпись помещалась под
            // узкой группой столбцов - полный адрес и так виден в карточке устройства ниже.
            String shortLabel = label.length() > 5 ? label.substring(label.length() - 5) : label;
            canvas.drawText(shortLabel, slotCenter, height - lineHeight - 2f, labelPaint);

            String loraName = i < loraNames.size() && loraNames.get(i) != null ? loraNames.get(i) : "--";
            canvas.drawText(loraName, slotCenter, height - 2f, labelPaint);
        }
    }

    /**
     * Рисует горизонтальные линии сетки на уровне 0, половины и максимума шкалы Y (количество
     * сообщений), с подписями значений слева - аналогично сетке в MessageCountChartView.
     */
    private void drawValueGrid(Canvas canvas, long max, float chartLeft, float chartRight,
                                float baseline, float usableHeight, float yLabelWidth) {
        long[] values = {0, max / 2, max};
        for (long value : values) {
            float y = baseline - usableHeight * value / max;
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
            canvas.drawText(String.valueOf(value), yLabelWidth - 4f, y + gridLabelPaint.getTextSize() / 3f, gridLabelPaint);
        }
    }
}
