package sinbad2.domain.numeric.integer.ui.jfreechart;

import java.awt.BasicStroke;
import java.awt.Color;

import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.DefaultIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

import sinbad2.domain.Domain;
import sinbad2.domain.numeric.NumericIntegerDomain;
import sinbad2.domain.ui.jfreechart.DomainChart;

public class NumericIntegerDomainChart extends DomainChart {
	
	private NumericIntegerDomain _domain;
	private ValueMarker _numMarker; 

	public NumericIntegerDomainChart() {
		super();
		_domain = null;
		_chart = null;
		_chartComposite = null;
		_numMarker = null;
	}
	
	@Override
	public void refreshChart() {
		
		if(_chart == null) {
			_chart = createChart(createIntervalDataset());
		} else {
			_chart.getXYPlot().setDataset(createIntervalDataset());
		}
		
		long upperLimit = _domain.getMax(), lowerLimit = _domain.getMin();
		boolean inRange = _domain.getInRange();
		
		long rangeSize = upperLimit - lowerLimit;
		double margin = (rangeSize > 0) ? rangeSize / 4d : 1;
		
		_chart.getXYPlot().getRangeAxis().setRange(lowerLimit - margin, upperLimit + margin);
		_chart.getXYPlot().getRangeAxis().setTickLabelsVisible(inRange);
		
		if(inRange) {
			_chart.getXYPlot().getRangeAxis().setLabel("Domain");
		} else {
			//TODO intentar hacer esto menos chapucero
			_chart.getXYPlot().getRangeAxis().setLabel(Double.toString(Double.NEGATIVE_INFINITY) + 
					"                                         " + 
					Double.toString(Double.POSITIVE_INFINITY));
		}
		
	}
	
	@Override
	public void setDomain(Domain domain) {
		//TODO validator
		_domain = (NumericIntegerDomain) domain;
		refreshChart();
	}

	@Override
	public void setSelection(Object selection) {
		
		if(_numMarker != null) {
			_chart.getXYPlot().removeRangeMarker(_numMarker);
		}
		
		_numMarker = new ValueMarker((Integer) selection);
		_numMarker.setPaint(Color.RED);
		_numMarker.setStroke(new BasicStroke(4));
		_chart.getXYPlot().addRangeMarker(_numMarker);
		
	}

	@Override
	public void initialize(Domain domain, Composite container, int width, int height, int style) {
		
		setDomain(domain);
		_chartComposite = new ChartComposite(container, style, _chart, true);
		_chartComposite.setSize(width, height);
		
	}
	
	private JFreeChart createChart(IntervalXYDataset intervalXYDataset) {
		JFreeChart result = ChartFactory.createXYBarChart("", "X", false, "", intervalXYDataset, 
				PlotOrientation.HORIZONTAL, false, false, false);
		
		result.setBackgroundPaint(Color.WHITE);
		XYPlot xyplot = (XYPlot) result.getPlot();
		
		XYBarRenderer xyBarRenderer = (XYBarRenderer) xyplot.getRenderer();
		xyBarRenderer.setSeriesPaint(0, Color.BLUE);
		xyBarRenderer.setUseYInterval(true);
		xyBarRenderer.setBarPainter(new StandardXYBarPainter());
		
		xyplot.setBackgroundPaint(Color.WHITE);
		xyplot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		xyplot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		xyplot.getDomainAxis().setVisible(false);
		
		return result;
	}
	
	private IntervalXYDataset createIntervalDataset() {
		DefaultIntervalXYDataset result = new DefaultIntervalXYDataset();
		
		double upperLimit = _domain.getMax(), lowerLimit = _domain.getMin();
		
		double[] x = new double[] {1};
		double[] xStart = new double[] {0.1};
		double[] xEnd = new double[] {0.9};
		double[] y = new double[] {upperLimit - lowerLimit};
		double[] yStart = new double[] {lowerLimit};
		double[] yEnd = new double[] {upperLimit};
		double[][] data = new double[][] {x, xStart, xEnd, y, yStart, yEnd};
		result.addSeries("Range", data);
		
		return result;
	}

}