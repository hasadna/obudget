package org.obudget.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AreaChart;
import com.google.gwt.visualization.client.visualizations.PieChart;

public class OBudgetMain implements EntryPoint {

	private Application mApp;
	
	public void onModuleLoad() {

		Runnable onLoadCallback = new Runnable() {
	        public void run() {
	        	init();
	        }
	      };

	    VisualizationUtils.loadVisualizationApi(onLoadCallback, PieChart.PACKAGE, AreaChart.PACKAGE );
	}

	private void addWidgetToId( String id, Widget widget ) {
		RootPanel p = RootPanel.get(id);
		if ( p != null ) {
			p.add(widget);
		}
	}
	
	private void init() {

		Boolean hasChartsDiv = Document.get().getElementById("charts") != null;
		Application.setEmbedded( !hasChartsDiv );
		mApp = Application.getInstance();

		addWidgetToId("obudget-searchbox",mApp.getSearchBox());
		addWidgetToId("obudget-summary-1",mApp.getSummary1() );
		addWidgetToId("obudget-breadcrumbs",mApp.getBreadcrumbs() );
		addWidgetToId("obudget-year-selection",mApp.getYearSelection() );
		addWidgetToId("obudget-summary-2",mApp.getSummary2() );
		addWidgetToId("obudget-summary-3",mApp.getSummary3() );
		addWidgetToId("obudget-summary-2-1",mApp.getSummary2_1() );
		addWidgetToId("obudget-summary-3-1",mApp.getSummary3_1() );
		addWidgetToId("obudget-detailed-results",mApp.getResultsGrid() );
		if ( mApp.getBudgetNews() != null ) {
			addWidgetToId("obudget-news",mApp.getBudgetNews() );
		}
		addWidgetToId("obudget-cheatsheet",mApp.getCheatSheet() );
		addWidgetToId("obudget-piechart",mApp.getPieCharter());
		addWidgetToId("obudget-timeline",mApp.getTimeLineCharter());

		History.fireCurrentHistoryState();
	}
}
