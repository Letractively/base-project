package org.damour.base.client.ui.admin;

import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.buttons.Button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HQLPanel extends VerticalPanel {

  private TextArea queryTextBox = new TextArea();
  private TextArea queryResultTextBox = new TextArea();
  private CheckBox executeUpdate = new CheckBox("Execute Update");
  
  public HQLPanel() {
    initUI();
  }
  
  public void initUI() {
    queryResultTextBox.setEnabled(false);
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    queryTextBox.setWidth("1024px");
    queryResultTextBox.setWidth("1024px");
    queryTextBox.setVisibleLines(5);
    queryResultTextBox.setVisibleLines(20);
    
    add(queryTextBox);
    Button executeQueryButton = new Button("Execute");
    executeQueryButton.addClickHandler(new ClickHandler() {
      
      public void onClick(ClickEvent event) {
        executeQuery();
      }
    });
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(executeUpdate);
    buttonPanel.add(executeQueryButton);
    add(buttonPanel);
    add(queryResultTextBox);
  }
  
  private void executeQuery() {
    String query = queryTextBox.getText();
    BaseServiceCache.getService().executeHQL(query, executeUpdate.getValue(), new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        queryResultTextBox.setText(caught.getMessage());
      }
      public void onSuccess(String result) {
        queryResultTextBox.setText(result);
      }
    });
  }
  
}
