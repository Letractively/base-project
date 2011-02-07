package org.damour.base.client.ui.scrolltable;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ScrollTable extends VerticalPanel {

  FlexTable headerTable = new FlexTable();
  FlexTable dataTable = new FlexTable();
  ScrollPanel headersScrollPanel = new ScrollPanel();
  ScrollPanel dataScrollPanel = new ScrollPanel();
  String[] columnWidths;
  boolean correctHeaderGap = true;

  public ScrollTable(String[] columnWidths, boolean correctHeaderGap) {
    this.columnWidths = columnWidths;
    this.correctHeaderGap = correctHeaderGap;
    headerTable.setCellPadding(0);
    headerTable.setCellSpacing(0);

    dataTable.setCellPadding(0);
    dataTable.setCellSpacing(0);

    headersScrollPanel.add(headerTable);
    dataScrollPanel.add(dataTable);

    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    add(headersScrollPanel);
    add(dataScrollPanel);
    if (correctHeaderGap) {
      setStyleName("baseTable");
    } else {
      setStyleName("baseTableNoScroll");
    }

    Window.addResizeHandler(new ResizeHandler() {
      public void onResize(ResizeEvent event) {
        clear();
        add(headersScrollPanel);
        add(dataScrollPanel);
      }
    });

  }

  public void setHeight(String height) {
    super.setHeight(height);
    dataScrollPanel.setHeight(height);
  }

  public void setHeaderWidget(int col, Widget w, HasHorizontalAlignment.HorizontalAlignmentConstant horizontalAlignment) {
    headerTable.setWidget(0, col, w);
    headerTable.getCellFormatter().setStyleName(0, col, "baseTableHeader");
    headerTable.getCellFormatter().setHorizontalAlignment(0, col, horizontalAlignment);
    DOM.setStyleAttribute(w.getElement(), "paddingLeft", "5px");
    DOM.setStyleAttribute(w.getElement(), "paddingRight", "5px");
    headerTable.getCellFormatter().setWidth(0, col, columnWidths[col]);

    if (correctHeaderGap) {
      if (col == columnWidths.length - 1) {
        Label gap = new Label();
        headerTable.setWidget(0, col + 1, gap);
        headerTable.getCellFormatter().setHorizontalAlignment(0, col + 1, horizontalAlignment);
        DOM.setStyleAttribute(gap.getElement(), "paddingLeft", "5px");
        DOM.setStyleAttribute(gap.getElement(), "paddingRight", "5px");
        headerTable.getCellFormatter().setWidth(0, col + 1, "17px");
      }
    }
  }

  public void setDataWidget(int row, int col, Widget w, HasHorizontalAlignment.HorizontalAlignmentConstant horizontalAlignment) {
    dataTable.setWidget(row, col, w);
    if (row == 0) {
      dataTable.getCellFormatter().setStyleName(row, col, "baseTableFirstRow");
    } else {
      dataTable.getCellFormatter().setStyleName(row, col, "baseTableBottomRow");
      try {
        dataTable.getCellFormatter().setStyleName(row - 1, col, "baseTableRow");
      } catch (Throwable t) {
      }
    }
    dataTable.getCellFormatter().setHorizontalAlignment(row, col, horizontalAlignment);
    DOM.setStyleAttribute(w.getElement(), "paddingLeft", "5px");
    DOM.setStyleAttribute(w.getElement(), "paddingRight", "5px");
    dataTable.getCellFormatter().setWidth(row, col, columnWidths[col]);
  }

  public void removeAllRows() {
    dataTable.removeAllRows();
  }
 
  public Widget getWidget(int row, int column) {
    return dataTable.getWidget(row, column);
  }
  
}
