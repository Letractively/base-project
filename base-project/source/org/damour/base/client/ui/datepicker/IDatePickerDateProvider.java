package org.damour.base.client.ui.datepicker;

import java.util.Date;

public interface IDatePickerDateProvider {
  public boolean shouldHighlightDate(Date date);
}
