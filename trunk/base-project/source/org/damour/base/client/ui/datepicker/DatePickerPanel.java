package org.damour.base.client.ui.datepicker;

import java.util.Date;


import com.google.gwt.user.client.ui.VerticalPanel;

/*
 * SimpleDatePicker is a date picker which sub classes
 * DatePicker and defines a calendar popup which allows
 * user to traverse back and forth in month and year
 */
public class DatePickerPanel extends VerticalPanel {

  DatePickerImpl datePicker = null;

  public DatePickerPanel(boolean isWeekendSelectable, DateFormat dateFormat, IDatePickerCallback callback, IDatePickerDateProvider dateProvider) {
    super();
    datePicker = new DatePickerImpl(callback, dateProvider);
    datePicker.setWidth("100%");
    datePicker.setWeekendSelectable(isWeekendSelectable);
    datePicker.setDateFormat(dateFormat);
    datePicker.redrawCalendar();
    add(datePicker.getUI());
  }

  public void setDate(Date date) {
    datePicker.setSelectedDate(date);
  }
}