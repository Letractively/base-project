package org.damour.base.client.ui.datepicker;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

public class RoundCornerPanel extends FlexTable {

	public RoundCornerPanel(Widget widget){
		super();
		init(widget);
	}
	
	private void init(Widget widget){
	    this.setCellPadding(0);
	    this.setCellSpacing(0);
	    //this.setBorderWidth(1);
	    // Row 1
	    CellFormatter cellFormatter = this.getCellFormatter(); 
		this.setHTML(0, 0, "<img src='images/top_lef.gif' width='7' height='7'>");
		cellFormatter.setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
		this.setHTML(0, 1, "<img src='images/top_mid.gif' width='7' height='7'>");
		cellFormatter.setHeight(0, 0, 6+"px");
		cellFormatter.setWidth(0, 0, 7+"px");
		
		Element elem = cellFormatter.getElement(0, 1);
		DOM.setAttribute(elem, "background", "images/top_mid.gif");
		cellFormatter.setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		cellFormatter.setHeight(0, 1, 6+"px");
		cellFormatter.setWidth(0, 1, 700+"px");
		
		this.setHTML(0, 2, "<img src='images/top_rig.gif' width='7' height='7'>");
		cellFormatter.setAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP);
		cellFormatter.setHeight(0, 2, 6+"px");
		cellFormatter.setWidth(0, 2, 7+"px");
		
		// Row 2
		/*
		this.setHTML(1, 0, "<img src='images/cen_lef.gif' width='7' height='7'>");
		elem = cellFormatter.getElement(1, 0);
		DOM.setAttribute(elem, "background", "images/cen_lef.gif");
		cellFormatter.setHeight(1, 0, 7+"px");
		//this.setHTML(1, 1, "Sample Text. This sould appear in a rounded box");
		this.setWidget(1, 1, widget);
		
		this.setHTML(1, 2, "<img src='images/cen_rig.gif' width='7' height='7'>");
		elem = cellFormatter.getElement(1, 2);
		DOM.setAttribute(elem, "background", "images/cen_rig.gif");
		cellFormatter.setHeight(1, 2, 7+"px");
		*/
		this.setWidget(1, 0, widget);
		//this.setHTML(1, 0, "Sample Text. This sould appear in a rounded box");
		FlexCellFormatter flexCellFormatter = this.getFlexCellFormatter();
		flexCellFormatter.setColSpan(1, 0, 3);
		
		// Row 3
		this.setHTML(2, 0, "<img src='images/bot_lef.gif' width='7' height='7'>");
		cellFormatter.setHeight(2, 0, 7+"px");
		cellFormatter.setWidth(2, 0, 7+"px");
		
		cellFormatter.setAlignment(2, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
		this.setHTML(2, 1, "<img src='images/bot_mid.gif' width='7' height='7'>");
		elem = cellFormatter.getElement(2, 1);
		DOM.setAttribute(elem, "background", "images/bot_mid.gif");
		cellFormatter.setAlignment(2, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
		this.setHTML(2, 2, "<img src='images/bot_rig.gif' width='7' height='7'>");
		cellFormatter.setWidth(2, 1, 500+"px");
		
		cellFormatter.setHeight(2, 2, 7+"px");
		cellFormatter.setWidth(2, 2, 7+"px");
		cellFormatter.setAlignment(2, 2, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP);
	}
}
