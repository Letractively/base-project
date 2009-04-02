package org.damour.base.client.ui.admin;

import java.util.List;

import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.service.BaseServiceCache;
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

public class HibernateAdminPanel extends VerticalPanel {

  FlexTable statsTable = new FlexTable();
  NumberFormat nf = NumberFormat.getDecimalFormat();
  NumberFormat pf = NumberFormat.getPercentFormat();

  public HibernateAdminPanel() {
    initUI();
    fetchHibernateStats();
  }

  private void initUI() {
    statsTable.setText(0, 0, "Loading...");
    statsTable.setBorderWidth(1);
    statsTable.setCellSpacing(5);
    add(statsTable);
    Button refreshButton = new Button("Refresh");
    refreshButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        fetchHibernateStats();
      }
    });
    Button resetButton = new Button("Reset");
    resetButton.setTitle("Reset Hibernate");
    resetButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        resetHibernate();
      }
    });

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(refreshButton);
    buttonPanel.add(resetButton);
    add(buttonPanel);
  }

  public void populateUI(List<HibernateStat> stats) {
    statsTable.clear();
    int row = 0;

    statsTable.setWidget(row, 0, new Label("Region Name"));
    statsTable.setWidget(row, 1, new Label("Cache Puts"));
    statsTable.setWidget(row, 2, new Label("Cache Hits"));
    statsTable.setWidget(row, 3, new Label("Cache Misses"));
    statsTable.setWidget(row, 4, new Label("Cache Hit %"));
    statsTable.setWidget(row, 5, new Label("Objects Cached"));
    statsTable.setWidget(row, 6, new Label("Memory Used"));
    statsTable.setWidget(row, 7, new Label("Evict Cache"));
    statsTable.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 3, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 4, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 5, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 6, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 7, HasHorizontalAlignment.ALIGN_RIGHT);

    row++;
    for (final HibernateStat stat : stats) {
      Button evictButton = new Button("X");
      evictButton.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          evictClassFromCache(stat.getRegionName());
        }
      });

      statsTable.setWidget(row, 0, new Label(stat.getRegionName()));
      statsTable.setWidget(row, 1, new Label(nf.format(stat.getCachePuts())));
      statsTable.setWidget(row, 2, new Label(nf.format(stat.getCacheHits())));
      statsTable.setWidget(row, 3, new Label(nf.format(stat.getCacheMisses())));
      if (stat.getCacheHits() == 0 || stat.getCachePuts() == 0) {
        statsTable.setWidget(row, 4, new Label(pf.format(0)));
      } else {
        statsTable.setWidget(row, 4, new Label(pf.format(((float)stat.getCacheHits())/ ((float)(stat.getCacheHits() + stat.getCachePuts())))));
      }
      statsTable.setWidget(row, 5, new Label(nf.format(stat.getNumObjectsInCache())));
      statsTable.setWidget(row, 6, new Label(nf.format(stat.getMemoryUsed())));
      statsTable.setWidget(row, 7, evictButton);
      statsTable.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
      statsTable.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
      statsTable.getCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT);
      statsTable.getCellFormatter().setHorizontalAlignment(row, 3, HasHorizontalAlignment.ALIGN_RIGHT);
      statsTable.getCellFormatter().setHorizontalAlignment(row, 4, HasHorizontalAlignment.ALIGN_RIGHT);
      statsTable.getCellFormatter().setHorizontalAlignment(row, 5, HasHorizontalAlignment.ALIGN_RIGHT);
      statsTable.getCellFormatter().setHorizontalAlignment(row, 6, HasHorizontalAlignment.ALIGN_RIGHT);
      statsTable.getCellFormatter().setHorizontalAlignment(row, 7, HasHorizontalAlignment.ALIGN_CENTER);
      row++;
    }
  }

  private void evictClassFromCache(String className) {
    final AsyncCallback<List<HibernateStat>> callback = new AsyncCallback<List<HibernateStat>>() {
      public void onFailure(Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(List<HibernateStat> stats) {
        fetchHibernateStats();
      };
    };
    BaseServiceCache.getService().evictClassFromCache(className, callback);
  }

  private void fetchHibernateStats() {
    for (int row = 1; row < statsTable.getRowCount(); row++) {
      statsTable.setWidget(row, 1, new Label("."));
      statsTable.setWidget(row, 2, new Label("."));
      statsTable.setWidget(row, 3, new Label("."));
      statsTable.setWidget(row, 4, new Label("."));
      statsTable.setWidget(row, 5, new Label("."));
      statsTable.setWidget(row, 6, new Label("."));
    }
    final AsyncCallback<List<HibernateStat>> callback = new AsyncCallback<List<HibernateStat>>() {
      public void onFailure(Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(List<HibernateStat> stats) {
        populateUI(stats);
      };
    };
    BaseServiceCache.getService().getHibernateStats(callback);
  }

  private void resetHibernate() {
    final AsyncCallback<List<HibernateStat>> callback = new AsyncCallback<List<HibernateStat>>() {
      public void onFailure(Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(List<HibernateStat> stats) {
        fetchHibernateStats();
      };
    };
    BaseServiceCache.getService().resetHibernate(callback);
  }

}
