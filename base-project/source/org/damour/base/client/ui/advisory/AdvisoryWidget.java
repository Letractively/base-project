package org.damour.base.client.ui.advisory;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.dialogs.MessageDialogBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AdvisoryWidget extends VerticalPanel {

  private static PopupPanel contentAdvisoryPopup = new PopupPanel(true, false);

  private static Timer timer = new Timer() {

    public void run() {
      contentAdvisoryPopup.hide();
    }
  };

  boolean showStatsLabel = true;

  PermissibleObject permissibleObject;
  UserAdvisory userAdvisory;

  Image G = new Image();
  Image PG = new Image();
  Image PG13 = new Image();
  Image R = new Image();
  Image NC17 = new Image();

  private Grid ratingPanel = new Grid(5, 2);
  RadioButton GRB = new RadioButton("");
  RadioButton PGRB = new RadioButton("");
  RadioButton PG13RB = new RadioButton("");
  RadioButton RRB = new RadioButton("");
  RadioButton NC17RB = new RadioButton("");

  private boolean isSubmitting = false;
  
  private MouseMoveHandler mouseMoveHandler = new MouseMoveHandler() {

    public void onMouseMove(MouseMoveEvent event) {
      if (userAdvisory == null) {
        // bring up content advisory popup
        if (contentAdvisoryPopup.getWidget() == ratingPanel && contentAdvisoryPopup.isShowing()) {
          return;
        }
        contentAdvisoryPopup.setStyleName("advisoryPopup");
        contentAdvisoryPopup.setWidget(ratingPanel);
        contentAdvisoryPopup.setPopupPosition(event.getClientX(), event.getClientY());
        contentAdvisoryPopup.show();
        timer.cancel();
        timer.schedule(4000);
      }
    }
  };
  private ClickHandler ratingHandler = new ClickHandler() {

    public void onClick(ClickEvent event) {
      if (userAdvisory == null) {
        // do vote
        int vote = 0;
        if (event.getSource() == G || event.getSource() == GRB) {
          vote = 1;
        } else if (event.getSource() == PG || event.getSource() == PGRB) {
          vote = 2;
        } else if (event.getSource() == PG13 || event.getSource() == PG13RB) {
          vote = 3;
        } else if (event.getSource() == R || event.getSource() == RRB) {
          vote = 4;
        } else if (event.getSource() == NC17 || event.getSource() == NC17RB) {
          vote = 5;
        }
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "wait");
        setFileUserAdvisory(vote);
        contentAdvisoryPopup.hide();
      }
    }
  };

  public AdvisoryWidget(PermissibleObject permissibleObject, UserAdvisory fileAdvisory, boolean fetchOnLoad, boolean showStatsLabel) {
    this.permissibleObject = permissibleObject;
    this.userAdvisory = fileAdvisory;
    this.showStatsLabel = showStatsLabel;
    if (fileAdvisory == null && permissibleObject.getNumAdvisoryVotes() > 0 && fetchOnLoad) {
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

    G.addClickHandler(ratingHandler);
    PG.addClickHandler(ratingHandler);
    PG13.addClickHandler(ratingHandler);
    R.addClickHandler(ratingHandler);
    NC17.addClickHandler(ratingHandler);
    GRB.addClickHandler(ratingHandler);
    PGRB.addClickHandler(ratingHandler);
    PG13RB.addClickHandler(ratingHandler);
    RRB.addClickHandler(ratingHandler);
    NC17RB.addClickHandler(ratingHandler);
  }

  private void buildAdvisoryImagePanel() {
    clear();

    Image advisoryImage = new Image();
    Label statsLabel = new Label();
    DOM.setStyleAttribute(statsLabel.getElement(), "fontSize", "8pt");

    if (permissibleObject == null || permissibleObject.getAverageAdvisory() == 0) {
      BaseImageBundle.images.advisoryNR().applyTo(advisoryImage);
      statsLabel.setText(BaseApplication.getMessages().getString("notRated", "Not Rated"));
    } else if (permissibleObject != null) {
      statsLabel
          .setText(BaseApplication.getMessages().getString("advisoryStatsLabel", "Rating based on {0} votes", "" + permissibleObject.getNumAdvisoryVotes()));
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
    advisoryImage.addMouseMoveHandler(mouseMoveHandler);
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    add(advisoryImage);
    if (showStatsLabel) {
      if (userAdvisory == null) {
        advisoryImage.setTitle(BaseApplication.getMessages().getString("contentAdvisory", "Content Advisory"));
      } else {
        advisoryImage.setTitle(BaseApplication.getMessages().getString("contentAdvisoryAlready", "Content Advisory (You have already voted)"));
      }
      add(statsLabel);
    } else {
      advisoryImage.setTitle(statsLabel.getText());
    }
  }

  public UserAdvisory getUserAdvisory() {
    return userAdvisory;
  }

  public void setUserAdvisory(UserAdvisory userAdvisory) {
    this.userAdvisory = userAdvisory;
  }
  
  public void setFileUserAdvisory(int advisory) {
    if (isSubmitting) {
      return;
    }
    isSubmitting = true;
    AsyncCallback<UserAdvisory> callback = new AsyncCallback<UserAdvisory>() {

      public void onSuccess(UserAdvisory userFileAdvisory) {
        isSubmitting = false;
        if (userFileAdvisory != null) {
          AdvisoryWidget.this.userAdvisory = userFileAdvisory;
          if (userFileAdvisory.getPermissibleObject() != null) {
            AdvisoryWidget.this.permissibleObject = userFileAdvisory.getPermissibleObject();
          }
        }
        buildAdvisoryImagePanel();
      }

      public void onFailure(Throwable t) {
        isSubmitting = false;
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), t.getMessage(), false, true, true);
        dialog.center();
      }
    };
    BaseServiceCache.getService().setUserAdvisory(permissibleObject, advisory, callback);
  }

  public void getFileUserAdvisory() {
    AsyncCallback<UserAdvisory> callback = new AsyncCallback<UserAdvisory>() {

      public void onSuccess(UserAdvisory userFileAdvisory) {
        if (userFileAdvisory != null) {
          AdvisoryWidget.this.userAdvisory = userFileAdvisory;
          if (userFileAdvisory.getPermissibleObject() != null) {
            AdvisoryWidget.this.permissibleObject = userFileAdvisory.getPermissibleObject();
          }
        }
        buildAdvisoryImagePanel();
      }

      public void onFailure(Throwable t) {
        clear();
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), t.getMessage(), false, true, true);
        dialog.center();
      }
    };
    BaseServiceCache.getService().getUserAdvisory(permissibleObject, callback);
  }
}
