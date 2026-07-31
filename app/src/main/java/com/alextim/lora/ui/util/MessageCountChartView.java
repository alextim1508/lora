package com.alextim.lora.ui.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Линейный график зависимости количества сообщений ОДНОГО типа (отправленных ИЛИ полученных)
 * устройства от времени за последнее окно (по умолчанию - последняя минута), с разрешением в одну
 * секунду (один "бакет" на секунду, самый старый - слева, самый новый - справа). Не требует
 * внешних библиотек графиков - используется только Canvas/Paint.
 *
 * Отправленные и полученные сообщения показываются раздельно: на графике отдельного устройства
 * используются два экземпляра этого View один под другим (см. item_device_info.xml/DataFragment) -
 * с разными цветами линии, задаваемыми через {@link #setLineColor(int)}. Данные задаются через
 * {@link #setData(int[])}: массив количества сообщений в каждом секундном "бакете". Отрисовывается
 * как ломаная линия с точками на каждом бакете, поверх временной сетки: вертикальные линии через
 * фиксированный интервал секунд с подписями "-Nс" и горизонтальные линии на уровне
 * 0/половины/максимума шкалы Y с подписями количества.
 */
public class MessageCountChartView extends View {

    private static final int DEFAULT_COLOR = Color.parseColor("#2196F3");
    private static final int COLOR_AXIS = Color.parseColor("#BDBDBD");
    private static final int COLOR_GRID = Color.parseColor("#3D3D3D");
    private static final int COLOR_GRID_LABEL = Color.parseColor("#999999");

    // Шаг вертикальных линий сетки времени, в секундах - на 60-секундном окне дает 6 делений.
    private static final int GRID_STEP_SECONDS = 10;

    private int[] history = new int[0];

    private final Paint linePaint = new Paint();
    private final Paint pointPaint = new Paint();
    private final Paint axisPaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint gridLabelPaintX = new Paint();
    private final Paint gridLabelPaintY = new Paint();

    public MessageCountChartView(Context context) {
        super(context);
        init();
    }

    public MessageCountChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MessageCountChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setColor(DEFAULT_COLOR);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setAntiAlias(true);

        pointPaint.setColor(DEFAULT_COLOR);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        axisPaint.setColor(COLOR_AXIS);
        axisPaint.setStrokeWidth(2f);

        gridPaint.setColor(COLOR_GRID);
        gridPaint.setStrokeWidth(1f);

        gridLabelPaintX.setColor(COLOR_GRID_LABEL);
        gridLabelPaintX.setAntiAlias(true);
        gridLabelPaintX.setTextSize(spToPx(9f));

        gridLabelPaintY.setColor(COLOR_GRID_LABEL);
        gridLabelPaintY.setAntiAlias(true);
        gridLabelPaintY.setTextSize(spToPx(9f));
        gridLabelPaintY.setTextAlign(Paint.Align.RIGHT);
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * Задает цвет линии/точек графика (например, синий для отправленных, зеленый для полученных).
     * Вызывать один раз после inflate, до/после setData - перерисовывает View.
     */
    public void setLineColor(int color) {
        linePaint.setColor(color);
        pointPaint.setColor(color);
        invalidate();
    }

    /**
     * Задает данные для отрисовки: количество сообщений в каждом секундном "бакете", самый старый
     * бакет - первый элемент, самый новый - последний. Вызывает перерисовку.
     */
    public void setData(int[] history) {
        this.history = history != null ? history : new int[0];
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int buckets = history.length;
        if (width <= 0 || height <= 0 || buckets == 0) {
            return;
        }

        // Резервируем место под подписи сетки: слева (значения Y), снизу (время, "-Nс"),
        // сверху - половину высоты текста, иначе подпись максимального значения (рисуется по
        // центру самой верхней линии сетки) обрезается верхней границей View.
        float yLabelWidth = gridLabelPaintY.measureText("00") + 4f;
        float xLabelHeight = gridLabelPaintX.getTextSize() + 4f;
        float topPadding = gridLabelPaintY.getTextSize() / 2f + 2f;

        float chartLeft = yLabelWidth;
        float chartRight = width;
        float baseline = height - xLabelHeight;
        float top = topPadding;

        int max = 1;
        for (int v : history) {
            max = Math.max(max, v);
        }

        float usableHeight = baseline - top;
        float usableWidth = chartRight - chartLeft;

        drawTimeGrid(canvas, buckets, chartLeft, usableWidth, top, baseline, xLabelHeight);
        drawValueGrid(canvas, max, chartLeft, chartRight, baseline, usableHeight, yLabelWidth);

        canvas.drawLine(chartLeft, baseline, chartRight, baseline, axisPaint);

        // При одном бакете делить не на что - рисуем единственную точку по центру ширины.
        float stepX = buckets > 1 ? usableWidth / (buckets - 1) : 0;

        drawSeries(canvas, buckets, chartLeft, stepX, baseline, usableHeight, max);
    }

    /**
     * Рисует вертикальные линии сетки через каждые GRID_STEP_SECONDS секунд назад от самого
     * правого бакета, с подписями "-Nс" под ними. Самый правый бакет - это последняя ПОЛНОСТЬЮ
     * завершенная секунда (см. комментарий к DataFragment.DeviceData#bucketize), но подписывается
     * как "0с" (а не "-1с") - метки округляются "к ближайшему делению сетки", а не отражают
     * буквальный возраст бакета в мс, так пользователю проще ориентироваться ("0с" = самые
     * последние данные, "-50с" = начало окна). Округление здесь только визуальное: сама
     * группировка данных по бакетам (bucketize()) не меняется, поэтому фикс мерцания 0->1 не
     * затрагивается.
     */
    private void drawTimeGrid(Canvas canvas, int buckets, float chartLeft, float usableWidth,
                               float top, float baseline, float xLabelHeight) {
        if (buckets <= 1) {
            return;
        }
        float stepX = usableWidth / (buckets - 1);
        float labelY = baseline + xLabelHeight - 2f;

        for (int secondsAgo = 0; secondsAgo < buckets; secondsAgo += GRID_STEP_SECONDS) {
            int bucketIndex = buckets - 1 - secondsAgo;
            float x = chartLeft + bucketIndex * stepX;

            canvas.drawLine(x, top, x, baseline, gridPaint);

            String label = secondsAgo == 0 ? "0с" : ("-" + secondsAgo + "с");
            float textWidth = gridLabelPaintX.measureText(label);
            float textX = Math.max(chartLeft, Math.min(x - textWidth / 2f, getWidth() - textWidth));
            canvas.drawText(label, textX, labelY, gridLabelPaintX);
        }
    }

    /**
     * Рисует горизонтальные линии сетки на уровне 0, половины и максимума шкалы Y, с подписями
     * значений слева.
     */
    private void drawValueGrid(Canvas canvas, int max, float chartLeft, float chartRight,
                                float baseline, float usableHeight, float yLabelWidth) {
        int[] values = {0, max / 2, max};
        for (int value : values) {
            float y = baseline - usableHeight * value / max;
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
            canvas.drawText(String.valueOf(value), yLabelWidth - 4f, y + gridLabelPaintY.getTextSize() / 3f, gridLabelPaintY);
        }
    }

    /**
     * Рисует ступенчатый график: горизонтальный отрезок на уровне значения предыдущего бакета до
     * X-координаты текущего, затем вертикальный отрезок вверх/вниз до уровня текущего значения -
     * так изменение количества сообщений между секундами видно как "ступенька", а не как плавный
     * наклонный отрезок, который визуально подразумевал бы дробные промежуточные значения.
     */
    private void drawSeries(Canvas canvas, int buckets, float chartLeft, float stepX, float baseline,
                             float usableHeight, int max) {
        float prevX = 0;
        float prevY = 0;
        boolean hasPrev = false;

        for (int i = 0; i < buckets; i++) {
            float x = buckets > 1 ? chartLeft + i * stepX : chartLeft + (getWidth() - chartLeft) / 2f;
            int value = i < history.length ? history[i] : 0;
            float y = baseline - usableHeight * value / max;

            if (hasPrev) {
                canvas.drawLine(prevX, prevY, x, prevY, linePaint);
                canvas.drawLine(x, prevY, x, y, linePaint);
            }
            if (value > 0) {
                canvas.drawCircle(x, y, 4f, pointPaint);
            }

            prevX = x;
            prevY = y;
            hasPrev = true;
        }
    }
}
