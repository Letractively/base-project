package org.damour.base.client.ui.advisory;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.dialogs.MessageDialogBox;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AdvisoryWidget extends VerticalPanel {

  PopupPanel contentAdvisoryPopup = new PopupPanel(true, false);
  boolean popupShowing = false;
  boolean showStatsLabel = true;

  PermissibleObject permissibleObject;
  UserAdvisory fileAdvisory;
  Image G = new Image();
  Image PG = new Image();
  Image PG13 = new Image();
  Image R = new Image();
  Image NC17 = new Image();

  RadioButton GRB = new RadioButton("");
  RadioButton PGRB = new RadioButton("");
  RadioButton PG13RB = new RadioButton("");
  RadioButton RRB = new RadioButton("");
  RadioButton NC17RB = new RadioButton("");

  MouseListener mouseListener = new MouseListener() {

    public void onMouseDown(Widget sender, int x, int y) {
    }

    public void onMouseEnter(Widget sender) {
    }

    public void onMouseLeave(Widget sender) {
    }

    public void onMouseMove(Widget sender, int x, int y) {
      if (fileAdvisory == null) {
        // bring up content advisory popup
        if (!popupShowing) {
          contentAdvisoryPopup.setPopupPosition(sender.getAbsoluteLeft() + x + 10, sender.getAbsoluteTop() + y + 10);
          contentAdvisoryPopup.show();
          popupShowing = true;
          Timer timer = new Timer() {

            public void run() {
              contentAdvisoryPopup.hide();
            }
          };
          timer.schedule(3000);
        }
      }
    }

    public void onMouseUp(Widget sender, int x, int y) {
    }
  };
  ClickListener ratingListener = new ClickListener() {

    public void onClick(Widget sender) {
      if (fileAdvisory == null) {
        // do vote
        int vote = 0;
        if (sender == G || sender == GRB) {
          vote = 1;
        } else if (sender == PG || sender == PGRB) {
          vote = 2;
        } else if (sender == PG13 || sender == PG13RB) {
          vote = 3;
        } else if (sender == R || sender == RRB) {
          vote = 4;
        } else if (sender == NC17 || sender == NC17RB) {
          vote = 5;
        }
        setFileUserAdvisory(vote);
        contentAdvisoryPopup.hide();
      }
    }
  };

  public AdvisoryWidget(PermissibleObject permissibleObject, UserAdvisory fileAdvisory, boolean showStatsLabel) {
    this.permissibleObject = permissibleObject;
    this.fileAdvisory = fileAdvisory;
    this.showStatsLabel = showStatsLabel;
    if (fileAdvisory == null) {
      getFileUserAdvisory();
    }

    buildAdvisoryImagePanel();
    buildAdvisoryPopupPanel();
  }

  private void buildAdvisoryPopupPanel() {
    BaseImageBundle.images.advisoryG().applyTo(G);
    BaseImageBundle.images.advisoryPG().applyTo(PG);
    BaseImageBundle.images.advisoryPG13().applyTo(PG13);
    BaseImageBundle.images.advisoryR().applyTo(R);
    BaseImageBundle.images.advisoryNC17().applyTo(NC17);

    Grid ratingPanel = new Grid(5, 2);
    ratingPanel.setCellPadding(0);
    ratingPanel.setCellSpacing(0);
    ratingPanel.setWidget(0, 1, G);
    ratingPanel.setWidget(0, 0, GRB);
    ratingPanel.setWidget(1, 1, PG);
    ratingPanel.setWidget(1, 0, PGRB);
    ratingPanel.setWidget(2, 1, PG13);
    ratingPanel.setWidget(2, 0, PG13RB);
    ratingPanel.setWidget(3, 1, R);
    ratingPanel.setWidget(3, 0, RRB);
    ratingPanel.setWidget(4, 1, NC17);
    ratingPanel.setWidget(4, 0, NC17RB);

    G.addClickListener(ratingListener);
    PG.addClickListener(ratingListener);
    PG13.addClickListener(ratingListener);
    R.addClickListener(ratingListener);
    NC17.addClickListener(ratingListener);
    GRB.addClickListener(ratingListener);
    PGRB.addClickListener(ratingListener);
    PG13RB.addClickListener(ratingListener);
    RRB.addClickListener(ratingListener);
    NC17RB.addClickListener(ratingListener);

    contentAdvisoryPopup.addPopupListener(new PopupListener() {

      public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        popupShowing = false;
      }
    });
    contentAdvisoryPopup.setStyleName("advisoryPopup");
    contentAdvisoryPopup.setWidget(ratingPanel);
  }

  private void buildAdvisoryImagePanel() {
    clear();

    Image advisoryImage = new Image();
    Label statsLabel = new Label();
    DOM.setStyleAttribute(statsLabel.getElement(), "fontSize", "8pt");
    
    if (permissibleObject == null || permissibleObject.getAverageAdvisory() == 0) {
      BaseImageBundle.images.advisoryNR().applyTo(advisoryImage);
      statsLabel.setText("Not Rated");
    } else if (permissibleObject != null) {
      statsLabel.setText("Rating based on " + permissibleObject.getNumAdvisoryVotes() + " votes");
      if (permissibleObject.getAverageAdvisory() > 0 && permissibleObject.getAverageAdvisory() <= 1) {
        BaseImageBundle.images.advisoryG().applyTo(advisoryImage);
      } else if (permissibleObject.getAverageAdvisory() > 1 && permissibleObject.getAverageAdvisory() <= 2) {
        BaseImageBundle.images.advisoryPG().applyTo(advisoryImage);
      } else if (permissibleObject.getAverageAdvisory() > 2 && permissibleObject.getAverageAdvisory() <= 3) {
        BaseImageBundle.images.advisoryPG13().applyTo(advisoryImage);
      } else if (permissibleObject.getAverageAdvisory() > 3 && permissibleObject.getAverageAdvisory() <= 4) {
        BaseImageBundle.images.advisoryR().applyTo(advisoryImage);
      } else if (permissibleObject.getAverageAdvisory() > 4 && permissibleObject.getAverageAdvisory() <= 5) {
        BaseImageBundle.images.advisoryNC17().applyTo(advisoryImage);
      }
    }
    advisoryImage.addMouseListener(mouseListener);
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    add(advisoryImage);
    if (showStatsLabel) {
      if (fileAdvisory == null) {
        advisoryImage.setTitle("Content Advisory");
      } else {
        advisoryImage.setTitle("Content Advisory (You have already voted)");
      }
      add(statsLabel);
    } else {
      advisoryImage.setTitle(statsLabel.getText());
    }
  }

  public void setFileUserAdvisory(int advisory) {
    AsyncCallback<UserAdvisory> callback = new AsyncCallback<UserAdvisory>() {

      public void onSuccess(UserAdvisory userFileAdvisory) {
        if (userFileAdvisory != null) {
          AdvisoryWidget.this.fileAdvisory = userFileAdvisory;
          if (userFileAdvisory.getPermissibleObject() != null) {
            AdvisoryWidget.this.permissibleObject = userFileAdvisory.getPermissibleObject();
          }
        }
        buildAdvisoryImagePanel();
      }

      public void onFailure(Throwable t) {
        MessageDialogBox dialog = new MessageDialogBox("Error", t.getMessage(), false, true, true);
        dialog.center();
      }
    };
    BaseServiceCache.getService().setUserAdvisory(permissibleObject, advisory, callback);
  }

  public void getFileUserAdvisory() {
    AsyncCallback<UserAdvisory> callback = new AsyncCallback<UserAdvisory>() {

      public void onSuccess(UserAdvisory userFileAdvisory) {
        if (userFileAdvisory != null) {
          AdvisoryWidget.this.fileAdvisory = userFileAdvisory;
          if (userFileAdvisory.getPermissibleObject() != null) {
            AdvisoryWidget.this.permissibleObject = userFileAdvisory.getPermissibleObject();
          }
        }
        buildAdvisoryImagePanel();
      }

      public void onFailure(Throwable t) {
        clear();
        MessageDialogBox dialog = new MessageDialogBox("Error", t.getMessage(), false, true, true);
        dialog.center();
      }
    };
    BaseServiceCache.getService().getUserAdvisory(permissibleObject, callback);
  }
}
