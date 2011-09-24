package org.obudget.client;

import java.util.LinkedList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.visualizations.AreaChart;

class TimeLineCharter extends Composite {
	private VerticalPanel mPanel;
	private Application mApp;
	private ToggleButton mInfButton;
	private ToggleButton mOrigButton;
	private ToggleButton mPercentButton;
	private HorizontalPanel mDataTypePanel;
	private LayoutPanel mChartPanel;
	private LinkedList<BudgetLine> mList;
	private ToggleButton mNetAllocatedButton;
	private ToggleButton mNetRevisedButton;
	private ToggleButton mNetUsedButton;
	private ToggleButton mGrossAllocatedButton;
	private ToggleButton mGrossRevisedButton;
	private ToggleButton mGrossUsedButton;
	private boolean mEmbedded;
	private Label mChartTitle;
	private HTML mSimplePopupContents;
	private Integer mWidth;
	private Integer mHeight;
	private Integer mYear = null;

	public TimeLineCharter( Application app, boolean embedded, Integer width, Integer height ) {
		mApp = app;
		mPanel = new VerticalPanel();
		
		mWidth = width;
		mHeight = height;
		
		mInfButton = new ToggleButton("ריאלי");
		mInfButton.setWidth("50px");
		mInfButton.setDown(true);
		mInfButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if ( mInfButton.isDown() ) {
					mOrigButton.setDown(false);
					mPercentButton.setDown(false);
					redrawChart();
				} else {
					mInfButton.setDown(true);
				}
			}
		});

		mOrigButton = new ToggleButton("נומינלי");
		mOrigButton.setWidth("50px");
		mOrigButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if ( mOrigButton.isDown() ) {
					mInfButton.setDown(false);
					mPercentButton.setDown(false);
					redrawChart();
				} else {
					mOrigButton.setDown(true);
				}
			}
		});

		mPercentButton = new ToggleButton("יחסי");
		mPercentButton.setWidth("50px");
		mPercentButton.setTitle("אחוז מכלל התקציב באותה שנה");
		mPercentButton.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if ( mPercentButton.isDown() ) {
					mOrigButton.setDown(false);
					mInfButton.setDown(false);
					redrawChart();
				} else {
					mPercentButton.setDown(true);
				}
			}
		});
		
		mDataTypePanel = new HorizontalPanel();
		mChartTitle = new Label("");
		mChartTitle.setWidth((mWidth-160)+"px");
		mChartTitle.setStyleName("chart-title");
		mDataTypePanel.add(mChartTitle);
		mDataTypePanel.add(mInfButton);
		mDataTypePanel.add(mOrigButton);
		mDataTypePanel.add(mPercentButton);
		mPanel.add(mDataTypePanel);
		
		mChartPanel = new LayoutPanel();
		mChartPanel.setHeight((mHeight-90)+"px");
		mChartPanel.setWidth(mWidth+"px");
		mPanel.add(mChartPanel);

		mNetAllocatedButton = new ToggleButton("הקצאה נטו");
		mNetAllocatedButton.setWidth("100px");
		mNetAllocatedButton.addClickHandler( new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				redrawChart();				
			}
		});
		mNetRevisedButton = new ToggleButton("הקצאה מעודכנת נטו");
		mNetRevisedButton.setWidth("130px");
		mNetRevisedButton.addClickHandler( new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				redrawChart();				
			}
		});
		mNetUsedButton = new ToggleButton("שימוש נטו");
		mNetUsedButton.setWidth("100px");
		mNetUsedButton.addClickHandler( new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				redrawChart();				
			}
		});
		mGrossAllocatedButton = new ToggleButton("הקצאה ברוטו");
		mGrossAllocatedButton.setWidth("100px");
		mGrossAllocatedButton.addClickHandler( new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				redrawChart();				
			}
		});
		mGrossRevisedButton = new ToggleButton("הקצאה מעודכנת ברוטו");
		mGrossRevisedButton.setWidth("130px");
		mGrossRevisedButton.addClickHandler( new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				redrawChart();				
			}
		});
		mGrossUsedButton = new ToggleButton("שימוש ברוטו");
		mGrossUsedButton.setWidth("100px");
		mGrossUsedButton.addClickHandler( new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				redrawChart();				
			}
		});

		mNetAllocatedButton.setDown(true);
		mNetRevisedButton.setDown(true);
		mNetUsedButton.setDown(true);

		HorizontalPanel mDataFieldPanelNet = new HorizontalPanel();
		mDataFieldPanelNet.add( mNetAllocatedButton );
		mDataFieldPanelNet.add( mNetRevisedButton );
		mDataFieldPanelNet.add( mNetUsedButton );
		HorizontalPanel mDataFieldPanelGross = new HorizontalPanel();
		mDataFieldPanelGross.add( mGrossAllocatedButton );
		mDataFieldPanelGross.add( mGrossRevisedButton );
		mDataFieldPanelGross.add( mGrossUsedButton );

		mPanel.add(mDataFieldPanelNet);
		mPanel.add(mDataFieldPanelGross);
		
		mEmbedded = embedded;
		HTML embedLabel = null;
		if ( mEmbedded ) {
			embedLabel = new HTML("מ<a target='_blank' href='http://"+Window.Location.getHost()+"/"+Window.Location.getHash()+"'>אתר התקציב הפתוח</a>");
			
		} else {
			embedLabel = new HTML("<span class='embed-link'>שיבוץ התרשים באתר אחר (embed)<span>");
			
			final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
			String embedCode = "";
			mSimplePopupContents = new HTML( "<b>קוד HTML לשיבוץ התרשים באתר אחר:</b><textarea rows='3' cols='40' style='direction: ltr;'>"+embedCode+"</textarea>");
			simplePopup.setWidget( mSimplePopupContents );
			embedLabel.addClickHandler( new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
		            Widget source = (Widget) event.getSource();
		            int left = source.getAbsoluteLeft() + 150;
		            int top = source.getAbsoluteTop() - 100;
		            simplePopup.setPopupPosition(left, top);
		            simplePopup.show();				
				}
			});
		}
		mPanel.add( embedLabel );
		
		mPanel.setWidth((mWidth-5)+"px");

		initWidget(mPanel);
	}
	
	public void handleData( LinkedList<BudgetLine> list, Integer year ) {
		if ( list.size() == 0 ) {
			mChartPanel.clear();
			return; 
		}
		mList = list;
		mYear  = year;
		redrawChart();
	}
	
	private void setValueIfNotNull( DataTable data, int row, int column, Integer value ) {
		if ( value == null ) return;
		data.setValue(row, column, value);
	}

	private void setValueIfNotNull( DataTable data, int row, int column, Double value ) {
		if ( value == null ) return;
		data.setValue(row, column, value);
	}
	
	private void redrawChart() {
		
		if ( !mEmbedded ) {
			String embedCode = "<iframe scrolling=&quot;no&quot; frameborder=&quot;0&quot; style=&quot;width: 390px; height: 350px&quot; " +
							   "src=&quot;http://" + Window.Location.getHost() + "/embed_time.html?w=390&amp;h=350" + Window.Location.getHash() +
	   		   		           "&quot;>" +
	   		   		           "</iframe>";
			mSimplePopupContents.setHTML( "<b>קוד HTML לשיבוץ התרשים באתר אחר:</b><textarea rows='3' cols='40' style='direction: ltr;'>"+embedCode+"</textarea>");
		}

		Application.getInstance().stateChanged();
		
		AreaChart.Options options = AreaChart.Options.create();
		options.setWidth(mWidth-5);
		options.setHeight(mHeight-90);
		//options.setTitle( list.get(0).getTitle() + " - " + "הקצאה באלפי \u20AA, מותאם לאינפלציה" );
		//options.setTitleX("שנה");
		options.setLegend(LegendPosition.BOTTOM);
		options.setAxisFontSize(10);
		
		DataTable data = DataTable.create();
	    data.addColumn(ColumnType.STRING, "Year");
	    int column;
	    boolean netUsed = mNetUsedButton.isDown();
	    boolean netRevised = mNetRevisedButton.isDown();
	    boolean netAllocated = mNetAllocatedButton.isDown();
	    boolean grossUsed = mGrossUsedButton.isDown();
	    boolean grossRevised = mGrossRevisedButton.isDown();
	    boolean grossAllocated = mGrossAllocatedButton.isDown();
	    if ( netUsed    )   { data.addColumn(ColumnType.NUMBER, StringUtils.compStr( "שימוש בפועל - נטו" ) ); }
	    if ( netRevised )   { data.addColumn(ColumnType.NUMBER, StringUtils.compStr( "הקצאה מעודכנת - נטו" ) ); }
	    if ( netAllocated ) { data.addColumn(ColumnType.NUMBER, StringUtils.compStr( "הקצאת תקציב - נטו" ) ); }
	    if ( grossUsed    )   { data.addColumn(ColumnType.NUMBER, StringUtils.compStr( "שימוש בפועל - ברוטו" ) ); }
	    if ( grossRevised )   { data.addColumn(ColumnType.NUMBER, StringUtils.compStr( "הקצאה מעודכנת - ברוטו" ) ); }
	    if ( grossAllocated ) { data.addColumn(ColumnType.NUMBER, StringUtils.compStr( "הקצאת תקציב - ברוטו" ) ); }

	    if ( mEmbedded ) {
		    for ( int i = 0 ; i < mList.size() ; i ++ ) {
		    	if ( mList.get(i).getYear().equals(mYear) ) {
		    		mChartTitle.setText( StringUtils.compStr( mList.get(i).getTitle() ) );
		    		mChartTitle.setTitle( "מספר סעיף: "+mList.get(i).getCode() );
		    	}
	    	}
	    }
	    
	    data.addRows(mList.size());
	    for ( int i = 0 ; i < mList.size() ; i ++ ) {
		    data.setValue(mList.size()-i-1, 0, mList.get(i).getYear().toString() );
		    if ( mInfButton.isDown() ) {
		    	column = 1;
		    	if ( netUsed        ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.USED, true ) );      column++; }
		    	if ( netRevised     ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.REVISED, true ) );      column++; }
		    	if ( netAllocated   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.ALLOCATED, true ) );      column++; }
		    	if ( grossUsed      ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.USED, false) );      column++; }
		    	if ( grossRevised   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.REVISED, false) );      column++; }
		    	if ( grossAllocated ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getInf( BudgetLine.ALLOCATED, false ) );      column++; }
				options.setTitleY( StringUtils.compStr( "אלפי \u20AA ריאליים" ) );
		    } else if ( mOrigButton.isDown() ) {
		    	column = 1;
		    	if ( netUsed        ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.USED, true ) );      column++; }
		    	if ( netRevised     ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.REVISED, true ) );      column++; }
		    	if ( netAllocated   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.ALLOCATED, true ) );      column++; }
		    	if ( grossUsed      ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.USED, false) );      column++; }
		    	if ( grossRevised   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.REVISED, false) );      column++; }
		    	if ( grossAllocated ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getOriginal( BudgetLine.ALLOCATED, false ) );      column++; }
				options.setTitleY( StringUtils.compStr( "אלפי \u20AA נומינליים" ) );
		    } else if ( mPercentButton.isDown() ) {
		    	column = 1;
		    	if ( netUsed        ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.USED, true ) );      column++; }
		    	if ( netRevised     ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.REVISED, true ) );      column++; }
		    	if ( netAllocated   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.ALLOCATED, true ) );      column++; }
		    	if ( grossUsed      ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.USED, false) );      column++; }
		    	if ( grossRevised   ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.REVISED, false) );      column++; }
		    	if ( grossAllocated ) { setValueIfNotNull( data, mList.size()-i-1, column, mList.get(i).getPercent( BudgetLine.ALLOCATED, false ) );      column++; }
				options.setTitleY( StringUtils.compStr( "אחוזים" ) );
		    } 
	    }
		AreaChart areachart = new AreaChart( data, options );
		mChartPanel.add(areachart);
	}

	public void setState( Integer timeLineDataType,
						  Integer timeLineChartSelect0, Integer timeLineChartSelect1,
						  Integer timeLineChartSelect2, Integer timeLineChartSelect3,
						  Integer timeLineChartSelect4, Integer timeLineChartSelect5) {
		mInfButton.setDown( timeLineDataType == 0 );
		mOrigButton.setDown( timeLineDataType == 1 );
		mPercentButton.setDown( timeLineDataType == 2 );
		mNetAllocatedButton.setDown( timeLineChartSelect0 == 1);
		mNetRevisedButton.setDown( timeLineChartSelect1 == 1);
		mNetUsedButton.setDown( timeLineChartSelect2 == 1);
		mGrossAllocatedButton.setDown( timeLineChartSelect3 == 1);
		mGrossRevisedButton.setDown( timeLineChartSelect4 == 1);
		mGrossUsedButton.setDown( timeLineChartSelect5 == 1);
	}

	public String getState() {
		return (mInfButton.isDown() ? "0," : (mOrigButton.isDown() ? "1," : "2," )) +
			   (mNetAllocatedButton.isDown() ? "1," : "0,") +
		   	   (mNetRevisedButton.isDown() ? "1," : "0,") +
			   (mNetUsedButton.isDown() ? "1," : "0,") +
			   (mGrossAllocatedButton.isDown() ? "1," : "0,") +
			   (mGrossRevisedButton.isDown() ? "1," : "0,") +
			   (mGrossUsedButton.isDown() ? "1" : "0");
	}
}
