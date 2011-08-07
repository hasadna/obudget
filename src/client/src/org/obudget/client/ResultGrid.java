package org.obudget.client;

import java.util.LinkedList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;

class ResultGrid extends Composite {
	private Grid mGrid;

	private HeaderLabel mNetAllocated;
	private HeaderLabel mNetRevised;
	private HeaderLabel mNetUsed;
	private HeaderLabel mGrossAllocated;
	private HeaderLabel mGrossRevised;
	private HeaderLabel mGrossUsed;

	private ToggleButton mNetButton;

	private LinkedList<BudgetLine> mList = null;

	class HeaderLabel extends Label {
		public HeaderLabel(String contents) {
			super(contents);
			setWordWrap(false);
			setStylePrimaryName("resultgrid-header");
		}
	}

	public ResultGrid() {
		mGrid = new Grid(1,7);
		mGrid.setStylePrimaryName("resultgrid");
		mGrid.setCellSpacing(0);
		mGrid.setCellPadding(2);

		mNetButton = new ToggleButton("נטו","ברוטו");
		mNetButton.addClickHandler( new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				redrawTable();
			}
		});

		mNetAllocated = new HeaderLabel("הקצאה נטו");
		mNetRevised = new HeaderLabel("הקצאה מעודכנת נטו");
		mNetUsed = new HeaderLabel("שימוש נטו");
		mGrossAllocated = new HeaderLabel("הקצאה ברוטו");
		mGrossRevised = new HeaderLabel("הקצאה מעודכנת ברוטו");
		mGrossUsed = new HeaderLabel("שימוש ברוטו");
		
		mGrid.setWidget(0, 0, mNetButton );

		initWidget(mGrid);
	}
	
	private NumberFormat decimalFormat = NumberFormat.getDecimalFormat();

	private String formatNumber( Integer num ) {
		if ( num == null ) return "-";
		return decimalFormat.format( num )+ (num == 0 ? "" : ",000");
	}
	
	public void handleData( LinkedList<BudgetLine> list ) {
		mList = list;
		redrawTable();
	}
	
	private void redrawTable() {
		
		if ( mList == null ) { 
			mGrid.resizeRows(1);
			return;
		}

		Application.getInstance().stateChanged();

		mGrid.resizeRows(mList.size()+1);

		Boolean net = !mNetButton.isDown();
		if ( net ) {
			mGrid.setWidget(0, 1, mNetAllocated );
			mGrid.setWidget(0, 2, mNetRevised );
			mGrid.setWidget(0, 3, mNetUsed );
		} else {
			mGrid.setWidget(0, 1, mGrossAllocated );
			mGrid.setWidget(0, 2, mGrossRevised );
			mGrid.setWidget(0, 3, mGrossUsed );
		}

		for ( int r = 0 ; r < mList.size() ; r ++ ) {
			BudgetLine bl = mList.get(r);
			String title;
			if ( r != 0 ) {
				title = "<a href='#"+Application.getInstance().hashForCode( bl.getCode() )+"'>"+bl.getTitle()+"</a>";
			} else {
				title = "סה\"כ:";				
			}
			HTML titleHtml = new HTML(title);
			//titleHtml.setWordWrap(false);
			mGrid.setWidget(r+1, 0, titleHtml);
			mGrid.setText(r+1, 1, formatNumber( bl.getOriginal(BudgetLine.ALLOCATED, net) ) );
			mGrid.setText(r+1, 2, formatNumber( bl.getOriginal(BudgetLine.REVISED, net) ) );
			mGrid.setText(r+1, 3, formatNumber( bl.getOriginal(BudgetLine.USED, net) ) );
			mGrid.getCellFormatter().setStyleName(r+1, 0, "resultgrid-title");
			mGrid.getCellFormatter().setStyleName(r+1, 1, "resultgrid-data");
			mGrid.getCellFormatter().setStyleName(r+1, 2, "resultgrid-data");
			mGrid.getCellFormatter().setStyleName(r+1, 3, "resultgrid-data");
			if ( "0000".equals(bl.getCode()) ) {
				mGrid.getRowFormatter().setStyleName(r+1, "resultgrid-income-data-row");
			} else {
				mGrid.getRowFormatter().setStyleName(r+1, "resultgrid-data-row");				
			}
		}

		mGrid.getRowFormatter().addStyleName(0, "resultgrid-header-row");
		if ( mGrid.getRowCount() > 1 ) { 
			mGrid.getRowFormatter().addStyleName(1, "resultgrid-totals-row");
		}

	}

	public void setState(Integer resultsGridNet) {
		mNetButton.setDown( resultsGridNet == 1 );
	}

	public String getState() {
		return mNetButton.isDown() ? "1" : "0";
	}
}
