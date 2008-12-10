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

package com.thapar.gwt.user.ui.client.widget.simpledatepicker;

import java.util.Date;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.thapar.gwt.user.ui.client.util.DateFormat;

/*
 * SimpleDatePicker is a date picker which sub classes DatePicker and defines a calendar popup which allows user to traverse back and forth in month and year
 */
public class DatePickerImpl extends DatePicker implements ClickListener {

  protected VerticalPanel vertPanel = new VerticalPanel();
  protected CalendarPanel calendarPanel = null;
  protected TodayPanel todayPanel = null;
  protected CalendarTraversalPanel calendarTraversalPanel = null;

  protected IDatePickerCallback callback = null;
  protected IDatePickerDateProvider dateProvider = null;
  
  /*
   * Default Constructor
   */
  public DatePickerImpl(IDatePickerCallback callback, IDatePickerDateProvider dateProvider) {
    super();
    this.init();
    this.calendarPanel = new CalendarPanel(this, dateProvider);
    this.calendarTraversalPanel = new CalendarTraversalPanel(this);
    this.todayPanel = new TodayPanel(this);
    this.addCalendar(this);
    this.callback = callback;
    this.dateProvider = dateProvider;
  }

  /*
   * init
   * 
   * Does the initialization to the textbox
   */
  private void init() {
    this.setWidth(80 + "px");
    addKeyboardListener(this);
  }

  /*
   * addCalendar
   * 
   * Adds the popup panel which displays three main panels: a. CalendarTraversalPanel: DockPanel b. CalendarPanel: AbsolutePanel c. TodayPanel: AbsolutePanel
   * 
   * @param DatePicker
   */
  private void addCalendar(DatePicker datePicker) {
    vertPanel.setWidth("100%");
    vertPanel.add(calendarTraversalPanel);
    vertPanel.add(calendarPanel);
    vertPanel.add(todayPanel);
  }

  /*
   * hideCalendar
   * 
   * Hides the popup calendar panel
   */
  public void hideCalendar() {
  }

  /*
   * redrawCalendar
   * 
   * Redraws the calendar panel with new month/ year
   */
  public void redrawCalendar() {
    this.calendarPanel.redrawCalendar();
  }

  public void show() {
    this.calendarPanel.redrawCalendar();
  }

  public void hide() {
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

  public Widget getUI() {
    return vertPanel;
  }

  public void setSelectedDate(Date selectedDate) {
    super.setSelectedDate(selectedDate);
    super.setCurrentDate(selectedDate);
    vertPanel.clear();
    boolean isWeekendSelectable = this.calendarPanel.isWeekendSelectable();
    this.calendarPanel = new CalendarPanel(this, dateProvider);
    this.calendarTraversalPanel = new CalendarTraversalPanel(this);
    this.todayPanel = new TodayPanel(this);
    this.calendarPanel.setWeekendSelectable(isWeekendSelectable);
    this.calendarPanel.setDateFormatter(this.getDateFormatter());
    this.todayPanel.setDateFormatter(this.getDateFormatter());
    vertPanel.add(calendarTraversalPanel);
    vertPanel.add(calendarPanel);
    vertPanel.add(todayPanel);
    redrawCalendar();
    onClick(this);
  }

  public void onClick(Widget sender) {
    callback.dateSelected(getSelectedDate());
  }

}