package com.github.mikephil.charting.charts;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.entry.BarEntry;
import com.github.mikephil.charting.highlight.BarHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.data.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.data.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;

/**
 * Chart that draws bars.
 *
 * @author Philipp Jahoda
 */
public class BarChart extends BarLineChartBase<BarData> implements BarDataProvider {

    /**
     * flag that indicates whether the highlight should be full-bar oriented, or single-value?
     */
    protected boolean mHighlightFullBarEnabled = false;

    /**
     * if set to true, all values are drawn above their bars, instead of below their top
     */
    private boolean mDrawValueAboveBar = true;

    /**
     * if set to true, a grey area is drawn behind each bar that indicates the maximum value
     */
    private boolean mDrawBarShadow = false;

    private boolean mFitBars = false;

    public BarChart(Context context) {
        super(context);
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mRenderer = new BarChartRenderer(this, mAnimator, mViewPortHandler);
        setHighlighter(new BarHighlighter(this));
    }

    @Override
    protected void calcMinMax() {

        if (mAutoScaleMinMaxEnabled) {
            mData.calcMinMax();
        }

        if (mFitBars) {
            mXAxis.calculate(mData.getXMin() - mData.getBarWidth() / 2f,
                    mData.getXMax() + mData.getBarWidth() / 2f);
        } else {
            mXAxis.calculate(mData.getXMin(), mData.getXMax());
        }

        // calculate axis indexRange (minIndex / maxIndex) according to provided data
        mAxisLeft.calculate(mData.getYMin(YAxis.AxisDependency.LEFT), 
            mData.getYMax(YAxis.AxisDependency.LEFT));
        mAxisRight.calculate(mData.getYMin(YAxis.AxisDependency.RIGHT), 
            mData.getYMax(YAxis.AxisDependency.RIGHT));
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value 
     * at the given touch point inside the BarChart.
     *
     * @param x touch point.
     * @param y touch point.
     * @return the hitted hightlight instance.
     */
    @Override
    public Highlight getHighlightByTouchPoint(float x, float y) {

        if (mData == null) {
            Log.e(LOG_TAG, "Can't select by touch. No data set.");
            return null;
        }
        return getHighlighter().getHighlight(x, y);
    }

    /**
     * Returns the bounding box of the specified Entry in the specified DataSet. 
     * Performance-intensive code should use void getBarBounds(BarEntry, RectF) instead.
     *
     * @param e the bar entry.
     * @return the bar bounds or null if the entry could not be found.
     */
    public RectF getBarBounds(BarEntry e) {
        RectF bounds = new RectF();
        getBarBounds(e, bounds);
        return bounds;
    }

    /**
     * The passed outputRect will be assigned the values of the bounding box of 
     * the specified Entry in the specified DataSet.
     * The rect will be assigned Float.MIN_VALUE in all locations if the Entry could not be found 
     * in the charts data.
     *
     * @param e the bar entry
     * @param outputRect carries the pixel values.
     */
    public void getBarBounds(BarEntry e, RectF outputRect) {

        RectF bounds = outputRect;

        IBarDataSet set = mData.getDataSetForEntry(e);

        if (set == null) {
            bounds.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
            return;
        }

        float y = e.getY();
        float x = e.getX();

        float barWidth = mData.getBarWidth();

        float left = x - barWidth / 2f;
        float right = x + barWidth / 2f;
        float top = y >= 0 ? y : 0;
        float bottom = y <= 0 ? y : 0;

        bounds.set(left, top, right, bottom);

        getTransformer(set.getAxisDependency()).rectValueToPixel(outputRect);
    }

    /**
     * If set to true, all values are drawn above their bars, instead of below their top.
     * Show the value above the bar or not.
     */
    public void setDrawValueAboveBar(boolean enabled) {
        mDrawValueAboveBar = enabled;
    }

    /**
     * returns true if drawing values above bars is enabled, false if not
     */
    public boolean isDrawValueAboveBarEnabled() {
        return mDrawValueAboveBar;
    }

    /**
     * If set to true, a grey area is drawn behind each bar that indicates the maximum value. 
     * <b>Enabling his will reduce performance by about 50%.</b>
     */
    public void setDrawBarShadow(boolean enabled) {
        mDrawBarShadow = enabled;
    }

    /**
     * returns true if drawing shadows (maxvalue) for each bar is enabled, false if not
     */
    public boolean isDrawBarShadowEnabled() {
        return mDrawBarShadow;
    }

    /**
     * Set this to true to make the highlight operation full-bar oriented, 
     * false to make it highlight single values (relevant only for stacked). 
     * If enabled, highlighting operations will highlight the whole bar, 
     * even if only a single stack entry was tapped.
     *
     * @param enabled default false
     */
    public void setHighlightFullBarEnabled(boolean enabled) {
        mHighlightFullBarEnabled = enabled;
    }

    /**
     * Return true the highlight operation is be full-bar oriented, false if single-value
     */
    @Override
    public boolean isHighlightFullBarEnabled() {
        return mHighlightFullBarEnabled;
    }

    /**
     * Highlights the value at the given x-position in the given DataSet. Provide -1 as the 
     * dataSetIndex to undo all highlighting.
     *
     * @param x the x value
     * @param dataSetIndex the index
     * @param stackIndex   the index inside the stack - only relevant for stacked entries
     */
    public void highlightValue(float x, int dataSetIndex, int stackIndex) {
        highlightValue(new Highlight(x, dataSetIndex, stackIndex), false);
    }

    @Override
    public BarData getBarData() {
        return mData;
    }

    /**
     * Adds half of the bar width to each side of the x-axis indexRange in order to
     * allow the bars of the barchart to be fully displayed.
     *
     * @param enabled false for default
     */
    public void setFitBars(boolean enabled) {
        mFitBars = enabled;
    }

    /**
     * Groups all BarDataSet objects this data object holds together by modifying the x-position 
     * of their entries.
     * Previously set x-positions of entries will be overwritten. Leaves space between 
     * bars and groups as specified by the parameters.  Calls notifyDataSetChanged() afterwards.
     *
     * @param fromX      the starting point on the x-axis where the grouping should begin
     * @param groupSpace space between bar groups in values (not pixels) 
     *                   e.g. 0.8f for bar width 1f
     * @param barSpace   space between individual bar in values (not pixels) 
     *                   e.g. 0.1f for bar width 1f
     */
    public void groupBars(float fromX, float groupSpace, float barSpace) {
        if (getBarData() == null) {
            throw new RuntimeException("batData = null");
        } else {
            getBarData().groupBars(fromX, groupSpace, barSpace);
            notifyDataSetChanged();
        }
    }
}
