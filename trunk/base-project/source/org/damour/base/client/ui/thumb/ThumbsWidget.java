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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ThumbsWidget extends HorizontalPanel {

  Image thumbUp = new Image();
  Image thumbDown = new Image();

  PermissibleObject permissibleObject;
  UserThumb userThumb;

  MouseOverHandler overHandler = new MouseOverHandler() {

    public void onMouseOver(MouseOverEvent event) {
      if (userThumb == null) {
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "hand");
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "pointer");
      }
    }
  };

  MouseOutHandler outHandler = new MouseOutHandler() {

    public void onMouseOut(MouseOutEvent event) {
      DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "default");
      loadThumbUI();
    }
  };

  public ThumbsWidget(final PermissibleObject permissibleObject, UserThumb userThumb, boolean fetchOnLoad) {
    this.permissibleObject = permissibleObject;
    this.userThumb = userThumb;

    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    BaseImageBundle.images.thumbUp().applyTo(thumbUp);
    BaseImageBundle.images.thumbDown().applyTo(thumbDown);
    thumbUp.setTitle("Like");
    thumbDown.setTitle("Dislike");

    thumbUp.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        setUserThumb(permissibleObject, true);
      }
    });
    thumbUp.addMouseOverHandler(overHandler);
    thumbUp.addMouseOutHandler(outHandler);

    thumbDown.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        setUserThumb(permissibleObject, false);
      }
    });
    thumbDown.addMouseOverHandler(overHandler);
    thumbDown.addMouseOutHandler(outHandler);

    if (userThumb == null && (permissibleObject.getNumUpVotes() > 0 || permissibleObject.getNumDownVotes() > 0) && fetchOnLoad) {
      getUserThumb(permissibleObject);
    } else {
      loadThumbUI();
    }
  }

  public void loadThumbUI() {
    clear();
    add(new Label("Up:" + permissibleObject.getNumUpVotes()));
    add(new Label("Dn:" + permissibleObject.getNumDownVotes()));
    add(thumbUp);
    add(thumbDown);
  }

  public void setUserThumb(final PermissibleObject permissibleObject, boolean like) {
    AsyncCallback<UserThumb> callback = new AsyncCallback<UserThumb>() {

      public void onSuccess(UserThumb userThumb) {
        if (userThumb != null) {
          ThumbsWidget.this.userThumb = userThumb;
          if (userThumb.getPermissibleObject() != null) {
            ThumbsWidget.this.permissibleObject = userThumb.getPermissibleObject();
          }
          loadThumbUI();
        }
      }

      public void onFailure(Throwable t) {
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
          if (userThumb.getPermissibleObject() != null) {
            ThumbsWidget.this.permissibleObject = userThumb.getPermissibleObject();
          }
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
}