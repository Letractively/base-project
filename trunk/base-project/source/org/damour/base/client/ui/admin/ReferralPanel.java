package org.damour.base.client.ui.admin;

import java.util.List;

import org.damour.base.client.objects.Referral;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.scrolltable.ScrollTable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ReferralPanel extends VerticalPanel {

  public ReferralPanel() {
    initUI();
  }

  public void initUI() {
    String[] columnWidths = new String[] { "600px", "400px", "60px" };

    final ScrollTable table = new ScrollTable(columnWidths, false);
    clear();
    add(table);
    Button refreshButton = new Button("Refresh");
    refreshButton.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        fetchReferrals(table);
      }
    });
    add(refreshButton);

    table.setHeaderWidget(0, new Label("Local URL"), HasHorizontalAlignment.ALIGN_LEFT);
    table.setHeaderWidget(1, new Label("Referrer"), HasHorizontalAlignment.ALIGN_LEFT);
    table.setHeaderWidget(2, new Label("Count"), HasHorizontalAlignment.ALIGN_RIGHT);

    // DateTimeFormat dateFormat = DateTimeFormat.getFormat(LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().dateFormatShort());

    fetchReferrals(table);

  }

  private void fetchReferrals(final ScrollTable table) {
    BaseServiceCache.getService().getReferrals(null, new AsyncCallback<List<Referral>>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(List<Referral> referrals) {
        int row = 0;
        for (Referral referral : referrals) {
          int col = 0;
          table.setDataWidget(row, col++, new Label(referral.getUrl()), HasHorizontalAlignment.ALIGN_LEFT);
          table.setDataWidget(row, col++, new Label(referral.getReferralURL()), HasHorizontalAlignment.ALIGN_LEFT);
          table.setDataWidget(row, col++, new Label(referral.getCounter() + ""), HasHorizontalAlignment.ALIGN_RIGHT);
          row++;
        }
      }
    });
  }
}
