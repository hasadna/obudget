package org.obudget.client;

import java.util.List;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

class Application implements ValueChangeHandler<String> {

	private BudgetLines mChildrenBudgetLines = new BudgetLines();
	private BudgetLines  mHistoricBudgetLines = new BudgetLines();
	private ResultGrid mResultsGrid = null;
	private PieCharter mPieCharter = null;
	private HTML mBreadcrumbs = null;
	private TimeLineCharter mTimeLineCharter = null;
	private String mCode = null;
	private Integer mYear = null;
	private ListBox mYearSelection = null;
	private SuggestBox mSearchBox = null;
	private Label mSummary1 = null;
	private Label mSummary2 = null;
	private Label mSummary2_1 = null;
	private HTML mSummary3 = null;
	private HTML mSummary3_1 = null;
	private BudgetNews mBudgetNews = null;
	private HTML mCheatSheet = null;
	
	static private Application mInstance = null;
	private static boolean mEmbedded = false;
	
	static public Application getInstance() {
		if ( mInstance == null ) {
			mInstance = new Application();
			mInstance.init();
		}
		return mInstance;
	}
	
	public void init() {
		TotalBudget.getInstance();
		
		Integer height = null;
		Integer width = null;
		Map<String,List<String>> parameters = Window.Location.getParameterMap();
		if ( parameters.containsKey("w") && 
			 parameters.containsKey("h") ) {
			height= Integer.parseInt( parameters.get("h").get(0) );
			width = Integer.parseInt( parameters.get("w").get(0) );
		} 

		mResultsGrid = new ResultGrid();
		mResultsGrid.setWidth("100%");

		Integer pieWidth = width == null ? 485 : width;  
		Integer pieHeight = height == null ? 400 : height;  
		mPieCharter = new PieCharter(this, mEmbedded, pieWidth, pieHeight);
		mPieCharter.setWidth(pieWidth+"px");
		mPieCharter.setHeight(pieHeight+"px");
		
		Integer timeWidth = width == null ? 686 : width;  
		Integer timeHeight = height == null ? 400 : height;
		mTimeLineCharter = new TimeLineCharter(this, mEmbedded, timeWidth, timeHeight);
		mTimeLineCharter.setWidth(timeWidth+"px");
		mTimeLineCharter.setHeight(timeHeight+"px");
		
		mBreadcrumbs = new HTML("");
		mBreadcrumbs.setHeight("20px");
		mBreadcrumbs.setWidth("100%");
		
		mYearSelection = new ListBox();
		mYearSelection.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				Integer index = mYearSelection.getSelectedIndex();
				String yearStr = mYearSelection.getItemText(index);
				Integer year;
				try {
					year = Integer.parseInt(yearStr);
				} catch (Exception e) {
					yearStr = yearStr.split(" ")[0];					
					year = Integer.parseInt(yearStr);
				}
				selectYear(year);
			}
		});
		
		mSearchBox = new SuggestBox(new BudgetSuggestionOracle());
		mSearchBox.setWidth("300px");
		mSearchBox.setAutoSelectEnabled(false);
		mSearchBox.addSelectionHandler( new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				final BudgetSuggestion bs = (BudgetSuggestion) event.getSelectedItem();
				
				BudgetAPICaller api = new BudgetAPICaller();
				api.setCode(bs.getCode());
				api.setParameter("depth", "0");
				api.setParameter("text", bs.getTitle());
				
				api.go( new BudgetAPICallback() {
					
					@Override
					public void onSuccess(JSONArray data) {
						if ( data.size() > 0 ) {
							Integer year = (int) data.get(0).isObject().get("year").isNumber().doubleValue();
							newCodeAndYear(bs.getCode(),year);
						}
					}
				});
				
			}
		});

		mSummary1 = new Label();
		mSummary2 = new Label();
		mSummary2_1 = new Label();
		mSummary3 = new HTML();
		mSummary3_1 = new HTML();
		
		mBudgetNews = new BudgetNews();
		
		mCheatSheet = new HTML("(הסברים)");
		final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
		simplePopup.setWidth("450px");
		HTML simplePopupContents = new HTML( "<h4>מונחון מקוצר</h4>"+
											 "<lu>"+
											 "<li><b>נטו</b>: <u>תקציב הוצאה נטו</u> – הסכום המותר להוצאה בשנה כלשהי כפי שמפורט בחוק התקציב. תקציב זה מכונה גם \"תקציב המזומנים\".</li>"+
											 "<li><b>ברוטו</b>: <u>תקציב ההוצאה נטו</u> בתוספת <u>תקציב ההוצאה המותנית בהכנסה</u> – תקציב נוסף המותר בהוצאה, ובלבד שיתקבלו תקבולים למימון ההוצאה מגורמים חוץ-ממשלתיים. תקבולים אלו אינם כוללים אגרה המשולמת לאוצר המדינה שהוטלה על-פי חיקוק שנחקק אחרי תחילת שנת הכספים 1992, ואינה כוללת הכנסה שמקורה במלווה (חוץ מתקציבי פיתוח וחשבון הון).</li>"+
											 "<li><b>הקצאה</b>: <u>תקציב מקורי</u> – התקציב שאושר בכנסת במסגרת חוק התקציב. ייתכנו הבדלים בין הצעת התקציב לבין התקציב שיאושר בכנסת בסופו של דבר.</li>"+
											 "<li><b>הקצאה מעודכנת</b>: <u>תקציב על שינוייו</u> – תקציב המדינה עשוי להשתנות במהלך השנה. שינויים אלו כוללים תוספות, הפחתות והעברות תקציביות בין סעיפי תקציב (באישור ועדת הכספים של הכנסת). נוסף על כך, פעמים רבות מועברים עודפים מחויבים משנה קודמת הנכללים בתקציב זה. רוב השינויים בתקציב דורשים את אישורה של ועדת הכספים של הכנסת. התקציב בסוף השנה הכולל את השינויים שנעשו בו במהלך השנה נקרא התקציב על שינוייו או התקציב המאושר.</li>"+
											 "<li><b>שימוש</b>:  <u>ביצוע</u> – התקציב שכבר נוצל ושולם בפועל על-ידי החשב.</li>"+
											 "<li><b>ערך ריאלי ונומינלי</b>:  ראו הסבר ב<a href='http://he.wikipedia.org/wiki/%D7%A2%D7%A8%D7%9A_%D7%A8%D7%99%D7%90%D7%9C%D7%99_%D7%95%D7%A2%D7%A8%D7%9A_%D7%A0%D7%95%D7%9E%D7%99%D7%A0%D7%9C%D7%99' target ='_blank'>ויקיפדיה</a>.</li>"+
											 "<li><b>ערך יחסי</b>:  האחוז היחסי של סעיף זה מכלל תקציב המדינה</li>"+
											 "</lu>"+
											 "<br/>"+
											 "<i>לחץ מחוץ לחלונית זו לסגירתה</i>"+
											 "<br/>"+
											 "מקור: <a href='http://www.knesset.gov.il/mmm/data/docs/m02217.doc' target='_blank'>מסמך Word ממחלקת המחקר של הכנסת</a>" +
											 "<p><b>התקציב הפתוח הוא פרויקט התנדבותי של עמותת 'הסדנא לידע ציבורי'. הנתונים המופיעים"+
											 " בתקציב הפתוח נסמכים על פרסומי המדינה ואינם באים לשקף כל דעה שהיא. אין לסדנא לידע ציבורי"+
											 " כל אחריות על השימוש בפרויקט וראוי לבדוק את הנתונים לגופם במקורותיהם לפני הסתמכות עליהם.</b></p>"
		);
		simplePopupContents.setStyleName("obudget-cheatsheet-popup");
		simplePopup.setWidget( simplePopupContents );
		mCheatSheet.addClickHandler( new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
	            Widget source = (Widget) event.getSource();
	            int left = source.getAbsoluteLeft() + 10;
	            int top = source.getAbsoluteTop() + 10;
	            simplePopup.setPopupPosition(left, top);
	            simplePopup.show();				
			}
		});
		
		History.addValueChangeHandler( this );		
	}
	
	public ResultGrid getResultsGrid() {
		return mResultsGrid;
	}

	public PieCharter getPieCharter() {
		return mPieCharter;
	}

	public void selectYear( Integer year ) {
		if ( mCode != null ) {
			selectBudgetCode(mCode, year);
		}
	}

	public void selectBudgetCode( String code, Integer year ) {
		mCode = code;
		mYear = year;
		
		BudgetAPICaller generalInfo = new BudgetAPICaller();
		
		// Load single record
		generalInfo.setCode(code);
		generalInfo.setParameter("year", year.toString() );
		generalInfo.setParameter("depth", "0");
		generalInfo.go( new BudgetAPICallback() {
			
			@Override
			public void onSuccess(JSONArray data) {
				if ( data.size() < 1 ) {
					return;
				}
				
				JSONObject firstResult = data.get(0).isObject(); 
				
				String title = firstResult.get("title").isString().stringValue();
				String code = firstResult.get("budget_id").isString().stringValue();
				mSearchBox.setValue(title);

				//mYearSelection.setSelectedIndex( mYear - 1992 );

				mBudgetNews.update("\""+title+"\"");
				
				Window.setTitle("תקציב המדינה - "+title+" ("+mYear+")");
				mSummary1.setText( title );
				final Integer revisedSum;
				final String revisedSumType;
				if ( (firstResult.get("gross_amount_revised") != null) &&
					 (firstResult.get("gross_amount_revised").isNumber() != null) ) {
					revisedSumType = "gross_amount_revised";
				} else if ( (firstResult.get("gross_amount_allocated") != null) &&
					 (firstResult.get("gross_amount_allocated").isNumber() != null) ) {
					revisedSumType = "gross_amount_allocated";
				} else if ( (firstResult.get("net_amount_revised") != null) &&
					 (firstResult.get("net_amount_revised").isNumber() != null) ) {
					revisedSumType = "net_amount_revised";
				} else if ( (firstResult.get("net_amount_allocated") != null) &&
					 (firstResult.get("net_amount_allocated").isNumber() != null) ) {
					revisedSumType = "net_amount_allocated";
				} else {
					revisedSumType = null;
				}
				if ( revisedSumType != null ) {
					if ( ( firstResult.get(revisedSumType) != null ) && 
						   firstResult.get(revisedSumType).isNumber() != null ) {
						revisedSum = (int) firstResult.get(revisedSumType).isNumber().doubleValue();
						if ( revisedSum != 0 ) {
							mSummary2.setText( NumberFormat.getDecimalFormat().format(revisedSum)+",000" );
						} else {
							mSummary2.setText( "0" );
						}
						mSummary2_1.setText( (revisedSumType.startsWith("net") ? " (נטו)" : "") );
					} else {
						revisedSum = null;
					}
				} else {
					revisedSum = null;
					mSummary2.setText("");
					mSummary2_1.setText("");
				}
				
				mSummary3.setHTML("");							
				mSummary3_1.setHTML("");							
				if ( firstResult.get("parent") != null ) {
					JSONArray parents = firstResult.get("parent").isArray();	
					if ( parents.size() > 0 ) {
						final String parentCode = parents.get(0).isObject().get("budget_id").isString().stringValue();
						final String parentTitle = parents.get(0).isObject().get("title").isString().stringValue();
						
						BudgetAPICaller percent = new BudgetAPICaller();
						percent.setCode(parentCode);
						percent.setParameter("year", mYear.toString());
						percent.setParameter("depth", "0");
						percent.go( new BudgetAPICallback() {						
							@Override
							public void onSuccess(JSONArray data) {
								if ( (data.get(0).isObject().get(revisedSumType) != null) && 
									 (data.get(0).isObject().get(revisedSumType).isNumber() != null) &&
									 (data.get(0).isObject().get(revisedSumType).isNumber().doubleValue() > 0) ) {
									double percent = revisedSum / data.get(0).isObject().get(revisedSumType).isNumber().doubleValue();
									mSummary3.setHTML( "שהם " + NumberFormat.getPercentFormat().format(percent) );
									mSummary3_1.setHTML( "מתקציב <a href='#"+hashForCode(parentCode)+"'>"+parentTitle+"</a>");
								}
							}
						});
					}			
				
					String breadcrumbs = "";
					for ( int i = 0 ; i < parents.size() ; i++ ) {
						String ptitle = parents.get(i).isObject().get("title").isString().stringValue();
						String pcode = parents.get(i).isObject().get("budget_id").isString().stringValue();
						breadcrumbs = "<a href='#"+hashForCode(pcode)+"'>"+ptitle+"</a> ("+pcode+") "+"&nbsp;&gt;&nbsp;" + breadcrumbs;
					}
					breadcrumbs += "<a href='#"+hashForCode(code)+"'>"+title+"</a> ("+code+")";
					mBreadcrumbs.setHTML(breadcrumbs);			
				}
			}
		});

		// Load all children
		BudgetAPICaller childrenLines = new BudgetAPICaller();
		childrenLines.setCode(code);
		childrenLines.setParameter("year", year.toString());
		childrenLines.setParameter("depth", "1");
		childrenLines.go( new BudgetAPICallback() {
			
			@Override
			public void onSuccess(JSONArray data) {
				mChildrenBudgetLines.parseJson(data);
				mResultsGrid.handleData(mChildrenBudgetLines);
				mPieCharter.handleData(mChildrenBudgetLines);				
			}
		});
		
		// Load same record, over the years
		BudgetAPICaller historicLines = new BudgetAPICaller();
		historicLines.setCode(code);
		historicLines.setParameter("depth", "0");
		historicLines.go( new BudgetAPICallback() {
			
			@Override
			public void onSuccess(JSONArray data) {
				mHistoricBudgetLines.parseJson(data);

				String lastLine = null;
				boolean allEqual = true;
				for ( BudgetLine bl : mHistoricBudgetLines ) {
					if ( lastLine != null ) {
						allEqual = allEqual && lastLine.equals(bl.getTitle());
					}
					lastLine = bl.getTitle();
				}

				mYearSelection.clear();
				Integer selectedIndex = 0;
				for ( BudgetLine bl : mHistoricBudgetLines ) {
					String year = bl.getYear().toString();
					if (year.equals( mYear.toString() )) {
						mYearSelection.addItem( year );
						selectedIndex = mYearSelection.getItemCount() - 1;
					} else {
						if ( allEqual ) {
							mYearSelection.addItem( year );
						} else {
							mYearSelection.addItem( year + " - " + bl.getTitle() );							
						}
					}
				}
				mYearSelection.setSelectedIndex( selectedIndex );
				if ( !allEqual ) {
					mYearSelection.setWidth("55px");
				}

				mTimeLineCharter.handleData(mHistoricBudgetLines, mYear);				
			}
		});
		
	}

	public TimeLineCharter getTimeLineCharter() {
		return mTimeLineCharter;
	}

	public String hash( String code, Integer year ) {
		return code+","+year+","+mTimeLineCharter.getState()+","+mPieCharter.getState()+","+mResultsGrid.getState();
	}

	public String hashForCode( String code ) {
		return hash( code, mYear );
	}

	public void newYear( Integer year ) {
		newCodeAndYear( mCode, year);
	}

	public void newCodeAndYear( String code, Integer year ) {
		mYear = year;
		mCode = code;
		History.newItem(hash(mCode, mYear));
	}
	
	public void stateChanged() {
		History.newItem(hash(mCode, mYear),false);
	}

	public static native void recordAnalyticsHit(String pageName) /*-{
 		$wnd._gaq.push(['_trackPageview', pageName]);
	}-*/;
	
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String hash = event.getValue();
		
		recordAnalyticsHit( Window.Location.getPath() + Window.Location.getHash() );
		
		String[] parts = hash.split(",");
		
		if ( parts.length == 12 ) {
			String code = parts[0];
			Integer year = Integer.decode(parts[1]);
			try {
				Integer timeLineDataType = Integer.decode(parts[2]);
				Integer timeLineChartSelect0 = Integer.decode(parts[3]);
				Integer timeLineChartSelect1 = Integer.decode(parts[4]);
				Integer timeLineChartSelect2 = Integer.decode(parts[5]);
				Integer timeLineChartSelect3 = Integer.decode(parts[6]);
				Integer timeLineChartSelect4 = Integer.decode(parts[7]);
				Integer timeLineChartSelect5 = Integer.decode(parts[8]);
				Integer pieChartDataType = Integer.decode(parts[9]);
				Integer pieChartNet= Integer.decode(parts[10]);
				Integer resultsGridNet= Integer.decode(parts[11]);
				selectBudgetCode(code, year);
				mTimeLineCharter.setState( timeLineDataType,
										   timeLineChartSelect0, 
										   timeLineChartSelect1, 
										   timeLineChartSelect2, 
										   timeLineChartSelect3, 
										   timeLineChartSelect4, 
										   timeLineChartSelect5 );
				mPieCharter.setState( pieChartDataType,
									  pieChartNet );
				mResultsGrid.setState( resultsGridNet );
			} catch (Exception e){
				Log.error("Application::onValueChange: Error while parsing url", e);
				newCodeAndYear("00", 2010);
			}
		} else {
			Log.error("Application::onValueChange: Error while parsing url");
			newCodeAndYear("00", 2010);
		}
	}

	public Widget getYearSelection() {
		return mYearSelection;
	}

	public Widget getSearchBox() {
		return mSearchBox;
	}

	public Widget getBreadcrumbs() {
		return mBreadcrumbs;
	}

	public Widget getSummary1() {
		return mSummary1;
	}

	public Widget getSummary2() {
		return mSummary2;
	}

	public Widget getSummary3() {
		return mSummary3;
	}

	public Widget getSummary2_1() {
		return mSummary2_1;
	}

	public Widget getSummary3_1() {
		return mSummary3_1;
	}

	public Widget getBudgetNews() {
		return mBudgetNews;
	}

	public Widget getCheatSheet() {
		return mCheatSheet;
	}

	public static void setEmbedded( boolean embedded ) {
		mEmbedded  = embedded;
	}
	
}
