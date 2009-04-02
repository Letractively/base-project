/*
 * Simple Date Picker Widget for GWT library of Google, Inc.
 * 
 * Copyright (c) 2006 Parvinder Thapar
 * http://psthapar.googlepages.com/
 * 
 * This library is free software; you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser 
 * General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or 
 * (at your option) any later version. This library is 
 * distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY  or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNULesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General 
 * PublicLicense along with this library; if not, write to the 
 * Free Software Foundation, Inc.,  
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA  
 */

package org.damour.base.client.ui.datepicker;

import java.util.Date;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/*
 * SimpleDatePicker is a date picker which sub classes
 * DatePicker and defines a calendar popup which allows
 * user to traverse back and forth in month and year
 */
public class SimpleDatePicker extends DatePicker implements ClickListener, FocusListener {

  protected PopupPanel calendarPopup = new PopupPanel(true);
  protected VerticalPanel vertPanel = new VerticalPanel();
  protected CalendarPanel calendarPanel = null;
  protected TodayPanel todayPanel = null;
  protected CalendarTraversalPanel calendarTraversalPanel = null;

  /*
   * Default Constructor
   */
  public SimpleDatePicker(Date initialDate) {
    super();
    setCurrentDate(initialDate);
    this.init();
    this.calendarPanel = new CalendarPanel(this, null);
    calendarPanel.setWeekendSelectable(true);
    this.calendarTraversalPanel = new CalendarTraversalPanel(this);
    this.todayPanel = new TodayPanel(this);
    this.addCalendar(this);
  }

  /*
   * Overridden Constructor
   * 
   * @param String
   * 
   * Uses the uniqueName to assign to the "uniqueName" value of the HTML textbox
   */
  public SimpleDatePicker(String name) {
    this(new Date());
    DOM.setAttribute(this.getElement(), "uniqueName", name);
  }

  /*
   * init
   * 
   * Does the initialization to the textbox
   */
  private void init() {
    this.setWidth(120 + "px");
    this.setStyleName("txtbox");
    addClickListener(this);
    addKeyboardListener(this);
    addFocusListener(this);
  }

  /*
   * addCalendar
   * 
   * Adds the popup panel which displays three main panels: a. CalendarTraversalPanel: DockPanel b. CalendarPanel: AbsolutePanel c. TodayPanel: AbsolutePanel
   * 
   * @param DatePicker
   */
  private void addCalendar(DatePicker datePicker) {
    vertPanel.add(calendarTraversalPanel);
    vertPanel.add(calendarPanel);
    vertPanel.add(todayPanel);
    calendarPopup.add(vertPanel);
    // RoundCornerPanel roundPanel = new RoundCornerPanel(vertPanel);
    // calendarPopup.add(roundPanel);
  }

  /*
   * showCalendar
   * 
   * Displays the popup calendar panel
   */
  private void showCalendar() {
    calendarPopup.setAnimationEnabled(true);
    calendarPopup.show();
    calendarPopup.setPopupPosition(this.getAbsoluteLeft(), this.getAbsoluteTop() + this.getOffsetHeight() + 4);
    calendarPopup.setHeight(120 + "px");
    calendarPopup.setWidth(165 + "px");
    calendarPopup.setStyleName("popupPanel");
  }

  /*
   * hideCalendar
   * 
   * Hides the popup calendar panel
   */
  public void hideCalendar() {
    this.calendarPopup.hide();
  }

  /*
   * redrawCalendar
   * 
   * Redraws the calendar panel with new month/ year
   */
  public void redrawCalendar() {
    this.calendarPanel.redrawCalendar();
  }

  /*
   * Methods from AbstractUIDatePicker
   */
  public void show() {
    this.redrawCalendar();
    this.showCalendar();
  }

  public void hide() {
    this.hideCalendar();
  }

  /*
   * Methods from ClickListener
   */
  public void onClick(Widget sender) {
    this.setSelectedDate(DateUtil.convertString2Date(this.getText()));
    this.show();
  }

  /*
   * getWeekendSelectable Getter method for isWeekendSelectable
   * 
   * @return boolean
   */
  public boolean isWeekendSelectable() {
    return this.calendarPanel.isWeekendSelectable();
  }

  /*
   * setWeekendSelectable Setter method for isWeekendSelectable
   * 
   * @param boolean
   */
  public void setWeekendSelectable(boolean isWeekendSelectable) {
    this.calendarPanel.setWeekendSelectable(isWeekendSelectable);
  }

  public DateFormatter getDateFormatter() {
    return super.getDateFormatter();
  }

  /*
   * setDateFormat Setter method set the format of the display date
   * 
   * @param boolean
   */
  public void setDateFormat(DateFormat dateFormat) {
    super.setDateFormat(dateFormat);
    this.calendarPanel.setDateFormatter(this.getDateFormatter());
    this.todayPanel.setDateFormatter(this.getDateFormatter());
  }

  public void onFocus(Widget sender) {
    if (getText() == null || "".equals(getText())) {
      show();
    }
  }

  public void onLostFocus(Widget sender) {

  }
}