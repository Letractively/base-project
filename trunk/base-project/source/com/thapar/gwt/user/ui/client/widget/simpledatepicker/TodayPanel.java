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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/*
 * TodayPanel extends AbsolyePanel and is extemely simple panel. All it shows is a link which displays today's date in the textbox
 */
public class TodayPanel extends VerticalPanel implements ClickListener {

  DatePicker datePicker;
  // Holds the date format needed to be displayed
  private DateFormatter dateFormatter = new DateFormatter(DateFormatter.DATE_FORMAT_MMDDYYYY);

  /*
   * Default Constructor
   */
  public TodayPanel(DatePicker datePicker) {
    this.datePicker = datePicker;
    init();
  }

  /*
   * init
   * 
   * Does the initialization of the panel and all its children widgets
   */
  public void init() {
    setWidth("100%");
    Label todayLink = new Label("Today");
    todayLink.setStyleName("todayLink");
    todayLink.addClickListener(this);
    todayLink.setWidth("100%");
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    this.add(todayLink);
    this.setStyleName("todayPanel");
  }

  /*
   * setDateFormatter Getter method for DateFormat
   * 
   * @return DateFormat
   */
  public DateFormatter getDateFormatter() {
    return dateFormatter;
  }

  /*
   * setDateFormatter Setter method for DateFormat
   * 
   * @param DateFormat
   */
  public void setDateFormatter(DateFormatter dateFormatter) {
    this.dateFormatter = dateFormatter;
  }

  /*
   * onClick
   * 
   * Overridding method from ClickListener. Populates the textbox with the today's date. It uses DateFormatter Object to decide which format to be displayed.
   */
  public void onClick(Widget sender) {
    // this.datePicker.setText(DateFormatter.formatDate(new Date(), DateFormatter.MMDDYYYY));
    Date newDate = new Date();
    this.datePicker.setText(this.dateFormatter.formatDate(newDate));
    this.datePicker.setSelectedDate(newDate);
    this.datePicker.hide();
  }

}
