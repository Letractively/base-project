package org.damour.base.client.ui.advisory;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.utils.CursorUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
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

  private boolean showStatsLabel = true;

  private PermissibleObject permissibleObject;
  private UserAdvisory userAdvisory;

  private Image advisoryImage = new Image();
  private Image G = new Image();
  private Image PG = new Image();
  private Image PG13 = new Image();
  private Image R = new Image();
  private Image NC17 = new Image();

  private Grid ratingPanel = new Grid(5, 2);
  private RadioButton GRB = new RadioButton("");
  private RadioButton PGRB = new RadioButton("");
  private RadioButton PG13RB = new RadioButton("");
  private RadioButton RRB = new RadioButton("");
  private RadioButton NC17RB = new RadioButton("");

  private boolean isSubmitting = false;
  private boolean interactive = true;

  private ClickHandler ratingHandler = new ClickHandler() {

    public void onClick(ClickEvent event) {
      if (interactive && userAdvisory == null) {
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

  public AdvisoryWidget(PermissibleObject permissibleObject, UserAdvisory fileAdvisory, boolean interactive, boolean fetchOnLoad, boolean showStatsLabel) {
    this.permissibleObject = permissibleObject;
    this.userAdvisory = fileAdvisory;
    this.showStatsLabel = showStatsLabel;
    this.interactive = interactive;
    if (interactive && fileAdvisory == null && permissibleObject.getNumAdvisoryVotes() > 0 && fetchOnLoad) {
      getFileUserAdvisory();
    }

    buildAdvisoryImagePanel();
    buildAdvisoryPopupPanel();
  }

  private void buildAdvisoryPopupPanel() {
    G.setResource(BaseImageBundle.images.advisoryG());
    PG.setResource(BaseImageBundle.images.advisoryPG());
    PG13.setResource(BaseImageBundle.images.advisoryPG13());
    R.setResource(BaseImageBundle.images.advisoryR());
    NC17.setResource(BaseImageBundle.images.advisoryNC17());

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

    advisoryImage = new Image();
    
    Label statsLabel = new Label();
    DOM.setStyleAttribute(statsLabel.getElement(), "fontSize", "8pt");

    if (permissibleObject == null || permissibleObject.getAverageAdvisory() == 0) {
      advisoryImage.setResource(BaseImageBundle.images.advisoryNR());
      statsLabel.setText(BaseApplication.getMessages().getString("notRated", "Not Rated"));
    } else if (permissibleObject != null) {
      statsLabel.setText(BaseApplication.getMessages().getString("advisoryStatsLabel", "Rating based on {0} votes",
          "" + permissibleObject.getNumAdvisoryVotes()));
      if (permissibleObject.getAverageAdvisory() > 0 && permissibleObject.getAverageAdvisory() <= 1) {
        advisoryImage.setResource(BaseImageBundle.images.advisoryG());
      } else if (permissibleObject.getAverageAdvisory() > 1 && permissibleObject.getAverageAdvisory() <= 2) {
        advisoryImage.setResource(BaseImageBundle.images.advisoryPG());
      } else if (permissibleObject.getAverageAdvisory() > 2 && permissibleObject.getAverageAdvisory() <= 3) {
        advisoryImage.setResource(BaseImageBundle.images.advisoryPG13());
      } else if (permissibleObject.getAverageAdvisory() > 3 && permissibleObject.getAverageAdvisory() <= 4) {
        advisoryImage.setResource(BaseImageBundle.images.advisoryR());
      } else if (permissibleObject.getAverageAdvisory() > 4 && permissibleObject.getAverageAdvisory() <= 5) {
        advisoryImage.setResource(BaseImageBundle.images.advisoryNC17());
      }
    }

    advisoryImage.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        if (interactive && userAdvisory == null) {
          // bring up content advisory popup
          if (contentAdvisoryPopup.getWidget() == ratingPanel && contentAdvisoryPopup.isShowing()) {
            return;
          }
          contentAdvisoryPopup.setStyleName("advisoryPopup");
          contentAdvisoryPopup.setWidget(ratingPanel);
          contentAdvisoryPopup.setPopupPosition(event.getClientX(), event.getClientY());
          contentAdvisoryPopup.show();
        }
      }
    });
    if (interactive && userAdvisory == null) {
      CursorUtils.setHandCursor(advisoryImage);
    }
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
          CursorUtils.setDefaultCursor(advisoryImage);
          AdvisoryWidget.this.userAdvisory = userFileAdvisory;
          userFileAdvisory.getPermissibleObject().mergeInto(permissibleObject);
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
          CursorUtils.setDefaultCursor(advisoryImage);
          AdvisoryWidget.this.userAdvisory = userFileAdvisory;
          userFileAdvisory.getPermissibleObject().mergeInto(permissibleObject);
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
