package org.damour.base.client.ui.search;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.ui.IGenericCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;

public class SearchWidget extends HorizontalPanel {

  private TextBox searchTextBox = new TextBox();
  
  public SearchWidget(final IGenericCallback<String> callback) {

    searchTextBox.addKeyUpHandler(new KeyUpHandler() {

      public void onKeyUp(KeyUpEvent event) {
        switch (event.getNativeKeyCode()) {
        case KeyCodes.KEY_ENTER:
          callback.invoke(searchTextBox.getText());
          break;
        }
      }
    });
    Image searchIcon = new Image();
    BaseImageBundle.images.find16x16().applyTo(searchIcon);
    searchIcon.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        callback.invoke(searchTextBox.getText());
      }
    });
    searchIcon.setTitle("Search");
    searchIcon.setStyleName("hasHandCursor");
    add(searchTextBox);
    add(searchIcon);
  }

  public void setSearchText(String text) {
    searchTextBox.setText(text);
  }
  
  public String getSearchText() {
    return searchTextBox.getText();
  }
  
}
