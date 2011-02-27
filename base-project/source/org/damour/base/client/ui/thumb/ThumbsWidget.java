package org.damour.base.client.ui.thumb;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.UserThumb;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.dialogs.MessageDialogBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ThumbsWidget extends HorizontalPanel {

  private NumberFormat formatter = NumberFormat.getFormat("#,###");

  private boolean showLikesLabel = true;
  private boolean showDislikesLabel = true;
  private boolean showLabelsOnLeft = true;

  private Image thumbUp = new Image();
  private Image thumbDown = new Image();

  private PermissibleObject permissibleObject;
  private UserThumb userThumb;

  private boolean isSubmitting = false;
  private boolean interactive = true;

  MouseOverHandler overHandler = new MouseOverHandler() {

    public void onMouseOver(MouseOverEvent event) {
      if (interactive && userThumb == null) {
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "hand");
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "pointer");
      } else if (interactive && isSubmitting) {
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "wait");
      } else {
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "default");
      }
    }
  };

  MouseOutHandler outHandler = new MouseOutHandler() {

    public void onMouseOut(MouseOutEvent event) {
      DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "default");
      // loadThumbUI();
    }
  };

  public ThumbsWidget(final PermissibleObject permissibleObject, UserThumb userThumb, boolean interactive, boolean fetchOnLoad, boolean showLabelsOnLeft,
      boolean showLikesLabel, boolean showDislikesLabel) {
    this.permissibleObject = permissibleObject;
    this.userThumb = userThumb;
    this.interactive = interactive;
    this.showLikesLabel = showLikesLabel;
    this.showDislikesLabel = showDislikesLabel;
    this.showLabelsOnLeft = showLabelsOnLeft;

    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    BaseImageBundle.images.thumbUp().applyTo(thumbUp);
    BaseImageBundle.images.thumbDown().applyTo(thumbDown);

    thumbUp.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        if (ThumbsWidget.this.interactive && ThumbsWidget.this.userThumb == null) {
          DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "wait");
          setUserThumb(permissibleObject, true);
        }
      }
    });
    thumbUp.addMouseOverHandler(overHandler);
    thumbUp.addMouseOutHandler(outHandler);

    thumbDown.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        if (ThumbsWidget.this.interactive && ThumbsWidget.this.userThumb == null) {
          DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "wait");
          setUserThumb(permissibleObject, false);
        }
      }
    });
    thumbDown.addMouseOverHandler(overHandler);
    thumbDown.addMouseOutHandler(outHandler);

    if (interactive && userThumb == null && (permissibleObject.getNumUpVotes() > 0 || permissibleObject.getNumDownVotes() > 0) && fetchOnLoad) {
      getUserThumb(permissibleObject);
    } else {
      loadThumbUI();
    }

  }

  private void loadLabels() {
    VerticalPanel statsPanel = new VerticalPanel();
    DOM.setStyleAttribute(statsPanel.getElement(), "fontSize", "7pt");
    if (permissibleObject.getNumUpVotes() > 0 || permissibleObject.getNumDownVotes() > 0) {
      if (showLikesLabel) {
        if (permissibleObject.getNumUpVotes() == 1) {
          statsPanel.add(new Label(BaseApplication.getMessages().getString("thumbsUpOnePersonStatsLabel", "1 person likes this")));
        } else { // if (permissibleObject.getNumUpVotes() > 1) {
          statsPanel.add(new Label(BaseApplication.getMessages().getString("thumbsUpManyPeopleStatsLabel", "{0} people like this",
              formatter.format(permissibleObject.getNumUpVotes()))));
        }
      }
      if (showDislikesLabel) {
        if (permissibleObject.getNumDownVotes() == 1) {
          statsPanel.add(new Label(BaseApplication.getMessages().getString("thumbsDownOnePersonStatsLabel", "1 person dislikes this")));
        } else if (permissibleObject.getNumDownVotes() > 1) {
          statsPanel.add(new Label(BaseApplication.getMessages().getString("thumbsDownManyPeopleStatsLabel", "{0} people dislike this",
              formatter.format(permissibleObject.getNumDownVotes()))));
        }
      }
    } else if (interactive) {
      DOM.setStyleAttribute(statsPanel.getElement(), "fontSize", "8pt");
      statsPanel.add(new Label("Like this!"));
    } else {
      if (showLikesLabel) {
        if (permissibleObject.getNumUpVotes() == 1) {
          statsPanel.add(new Label(BaseApplication.getMessages().getString("thumbsUpOnePersonStatsLabel", "1 person likes this")));
        } else { // if (permissibleObject.getNumUpVotes() > 1) {
          statsPanel.add(new Label(BaseApplication.getMessages().getString("thumbsUpManyPeopleStatsLabel", "{0} people like this",
              formatter.format(permissibleObject.getNumUpVotes()))));
        }
      }
      if (showDislikesLabel) {
        if (permissibleObject.getNumDownVotes() == 1) {
          statsPanel.add(new Label(BaseApplication.getMessages().getString("thumbsDownOnePersonStatsLabel", "1 person dislikes this")));
        } else if (permissibleObject.getNumDownVotes() > 1) {
          statsPanel.add(new Label(BaseApplication.getMessages().getString("thumbsDownManyPeopleStatsLabel", "{0} people dislike this",
              formatter.format(permissibleObject.getNumDownVotes()))));
        }
      }

    }
    if (statsPanel.getWidgetCount() > 0) {
      add(statsPanel);
    }
  }

  private void loadThumbs() {
    if (userThumb != null) {
      if (userThumb.isLikeThumb()) {
        thumbUp.setTitle(BaseApplication.getMessages().getString("youLikeThis", "You like this"));
        add(thumbUp);
      } else {
        thumbDown.setTitle(BaseApplication.getMessages().getString("youDislikeThis", "You dislike this"));
        add(thumbDown);
      }
    } else if (interactive) {
      add(thumbUp);
      add(thumbDown);
      thumbUp.setTitle(BaseApplication.getMessages().getString("like", "Like"));
      thumbDown.setTitle(BaseApplication.getMessages().getString("dislike", "Dislike"));
    } else {
      if (permissibleObject.getNumUpVotes() >= permissibleObject.getNumDownVotes()) {
        add(thumbUp);
      } else {
        add(thumbDown);
      }
    }
  }

  private void loadThumbUI() {
    clear();
    if (showLabelsOnLeft) {
      loadLabels();
      loadThumbs();
    } else {
      loadThumbs();
      loadLabels();
    }
  }

  public void setUserThumb(final PermissibleObject permissibleObject, boolean like) {
    if (isSubmitting) {
      return;
    }
    isSubmitting = true;
    AsyncCallback<UserThumb> callback = new AsyncCallback<UserThumb>() {

      public void onSuccess(UserThumb userThumb) {
        isSubmitting = false;
        if (userThumb != null) {
          ThumbsWidget.this.userThumb = userThumb;
          userThumb.getPermissibleObject().mergeInto(permissibleObject);
          loadThumbUI();
        }
      }

      public void onFailure(Throwable t) {
        isSubmitting = false;
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), t.getMessage(), false, true, true);
        dialog.center();
      }
    };
    BaseServiceCache.getService().setUserThumb(permissibleObject, like, callback);
  }

  public void getUserThumb(final PermissibleObject permissibleObject) {
    AsyncCallback<UserThumb> callback = new AsyncCallback<UserThumb>() {

      public void onSuccess(UserThumb userThumb) {
        if (userThumb != null) {
          ThumbsWidget.this.userThumb = userThumb;
          userThumb.getPermissibleObject().mergeInto(permissibleObject);
        }
        loadThumbUI();
      }

      public void onFailure(Throwable t) {
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), t.getMessage(), false, true, true);
        dialog.center();
        clear();
      }
    };
    BaseServiceCache.getService().getUserThumb(permissibleObject, callback);
  }

  public UserThumb getUserThumb() {
    return userThumb;
  }

  public void setUserThumb(UserThumb userThumb) {
    this.userThumb = userThumb;
  }
}