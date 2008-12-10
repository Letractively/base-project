package org.damour.base.client.ui.admin;

import org.damour.base.client.objects.MemoryStats;
import org.damour.base.client.service.BaseServiceAsync;
import org.damour.base.client.ui.buttons.Button;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MemoryAdminPanel extends VerticalPanel {

  FlexTable statsTable = new FlexTable();
  NumberFormat nf = NumberFormat.getDecimalFormat();
  NumberFormat pf = NumberFormat.getPercentFormat();

  public MemoryAdminPanel() {
    initUI();
    fetchMemoryStats();
  }

  private void initUI() {
    statsTable.setText(0, 0, "Loading...");
    statsTable.setBorderWidth(1);
    statsTable.setCellSpacing(5);
    add(statsTable);
    Button refreshButton = new Button("Refresh");
    refreshButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        fetchMemoryStats();
      }
    });
    Button gcButton = new Button("Request GC");
    gcButton.setTitle("Request Garbage Collection");
    gcButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        requestGarbageCollection();
      }
    });

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(refreshButton);
    buttonPanel.add(gcButton);
    add(buttonPanel);
  }

  public void populateUI(MemoryStats stats) {
    statsTable.clear();
    int row = 0;

    statsTable.setWidget(row, 0, new Label("Max Memory"));
    statsTable.setWidget(row, 1, new Label("Allocated Memory"));
    statsTable.setWidget(row, 2, new Label("Used Memory"));
    statsTable.setWidget(row, 3, new Label("Free Memory"));
    statsTable.setWidget(row, 4, new Label("% Free Memory"));
    statsTable.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 3, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 4, HasHorizontalAlignment.ALIGN_RIGHT);

    row++;
    statsTable.setWidget(row, 0, new Label(nf.format(stats.getMaxMemory())));
    statsTable.setWidget(row, 1, new Label(nf.format(stats.getTotalMemory())));
    statsTable.setWidget(row, 2, new Label(nf.format(stats.getTotalMemory() - stats.getFreeMemory())));
    statsTable.setWidget(row, 3, new Label(nf.format(stats.getFreeMemory())));
    statsTable.setWidget(row, 4, new Label(pf.format((float) stats.getFreeMemory() / (float) stats.getTotalMemory())));
    statsTable.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 3, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 4, HasHorizontalAlignment.ALIGN_RIGHT);
  }

  private void fetchMemoryStats() {
    for (int row = 1; row < statsTable.getRowCount(); row++) {
      statsTable.setWidget(row, 0, new Label("."));
      statsTable.setWidget(row, 1, new Label("."));
      statsTable.setWidget(row, 2, new Label("."));
      statsTable.setWidget(row, 3, new Label("."));
      statsTable.setWidget(row, 4, new Label("."));
    }
    final AsyncCallback<MemoryStats> callback = new AsyncCallback<MemoryStats>() {
      public void onFailure(Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(MemoryStats stats) {
        populateUI(stats);
      };
    };
    BaseServiceAsync.service.getMemoryStats(callback);
  }

  private void requestGarbageCollection() {
    final AsyncCallback<MemoryStats> callback = new AsyncCallback<MemoryStats>() {
      public void onFailure(Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(MemoryStats stats) {
        populateUI(stats);
      };
    };
    BaseServiceAsync.service.requestGarbageCollection(callback);
  }

}
