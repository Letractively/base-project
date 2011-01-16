package org.damour.base.client.ui.rating;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.UserRating;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.dialogs.MessageDialogBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class RatingWidget extends VerticalPanel {

  Label statsLabel = null;
  boolean showStatsLabel = true;
  HorizontalPanel starPanel = new HorizontalPanel();
  PermissibleObject permissibleObject;
  UserRating fileRating;
  Image star1 = new Image();
  Image star2 = new Image();
  Image star3 = new Image();
  Image star4 = new Image();
  Image star5 = new Image();

  ClickHandler starClickHandler = new ClickHandler() {

    public void onClick(ClickEvent event) {
      if (fileRating == null) {
        // do vote
        int vote = 0;
        if (event.getSource() == star1) {
          vote = 1;
        } else if (event.getSource() == star2) {
          vote = 2;
        } else if (event.getSource() == star3) {
          vote = 3;
        } else if (event.getSource() == star4) {
          vote = 4;
        } else if (event.getSource() == star5) {
          vote = 5;
        }
        setUserRating(permissibleObject, vote);
      }
    }
  };

  MouseOverHandler starOverHandler = new MouseOverHandler() {

    public void onMouseOver(MouseOverEvent event) {
      if (fileRating == null) {
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "hand");
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "pointer");
        starMoused((Widget) event.getSource());
      }
    }
  };

  MouseOutHandler starOutHandler = new MouseOutHandler() {

    public void onMouseOut(MouseOutEvent event) {
      DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "default");
      setStars();
    }
  };

  MouseUpHandler starUpHandler = new MouseUpHandler() {

    public void onMouseUp(MouseUpEvent event) {
      setStars();
    }
  };

  public RatingWidget(PermissibleObject permissibleObject, UserRating fileRating, boolean showStatsLabel) {
    this.showStatsLabel = showStatsLabel;
    this.permissibleObject = permissibleObject;
    this.fileRating = fileRating;
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    setupStar(star1);
    setupStar(star2);
    setupStar(star3);
    setupStar(star4);
    setupStar(star5);

    add(starPanel);

    statsLabel = new Label(BaseApplication.getMessages().getString("ratingStatsLabel", "{0} rating from {1} users",
        NumberFormat.getFormat("#.#").format(permissibleObject.getAverageRating()), "" + permissibleObject.getNumRatingVotes()), false);
    DOM.setStyleAttribute(statsLabel.getElement(), "fontSize", "8pt");
    if (showStatsLabel) {
      add(statsLabel);
    }
    if (fileRating == null) {
      getUserRating(permissibleObject);
    } else {
      setStars();
    }
  }

  public void starMoused(Widget sender) {
    if (sender == star1) {
      BaseImageBundle.images.starHover().applyTo(star1);
      star1.setTitle(BaseApplication.getMessages().getString("ratingAwful", "Awful"));
      BaseImageBundle.images.starEmpty().applyTo(star2);
      BaseImageBundle.images.starEmpty().applyTo(star3);
      BaseImageBundle.images.starEmpty().applyTo(star4);
      BaseImageBundle.images.starEmpty().applyTo(star5);
    } else if (sender == star2) {
      BaseImageBundle.images.starHover().applyTo(star1);
      BaseImageBundle.images.starHover().applyTo(star2);
      star2.setTitle(BaseApplication.getMessages().getString("ratingPoor", "Poor"));
      BaseImageBundle.images.starEmpty().applyTo(star3);
      BaseImageBundle.images.starEmpty().applyTo(star4);
      BaseImageBundle.images.starEmpty().applyTo(star5);
    } else if (sender == star3) {
      BaseImageBundle.images.starHover().applyTo(star1);
      BaseImageBundle.images.starHover().applyTo(star2);
      BaseImageBundle.images.starHover().applyTo(star3);
      star3.setTitle(BaseApplication.getMessages().getString("ratingNotBad", "Not Bad"));
      BaseImageBundle.images.starEmpty().applyTo(star4);
      BaseImageBundle.images.starEmpty().applyTo(star5);
    } else if (sender == star4) {
      BaseImageBundle.images.starHover().applyTo(star1);
      BaseImageBundle.images.starHover().applyTo(star2);
      BaseImageBundle.images.starHover().applyTo(star3);
      BaseImageBundle.images.starHover().applyTo(star4);
      star4.setTitle(BaseApplication.getMessages().getString("ratingGood", "Good"));
      BaseImageBundle.images.starEmpty().applyTo(star5);
    } else if (sender == star5) {
      BaseImageBundle.images.starHover().applyTo(star1);
      BaseImageBundle.images.starHover().applyTo(star2);
      BaseImageBundle.images.starHover().applyTo(star3);
      BaseImageBundle.images.starHover().applyTo(star4);
      BaseImageBundle.images.starHover().applyTo(star5);
      star5.setTitle(BaseApplication.getMessages().getString("ratingGreat", "Great"));
    }
  }

  private Image setupStar(Image star) {
    star.addClickHandler(starClickHandler);
    star.addMouseOverHandler(starOverHandler);
    star.addMouseOutHandler(starOutHandler);
    star.addMouseUpHandler(starUpHandler);
    DOM.setStyleAttribute(star.getElement(), "margin", "0px");
    DOM.setStyleAttribute(star.getElement(), "padding", "0px");
    starPanel.add(star);
    star.setTitle("");
    return star;
  }

  public void setStars() {
    String statText = BaseApplication.getMessages().getString("ratingStatsLabel", "{0} rating from {1} users",
        NumberFormat.getFormat("#.#").format(permissibleObject.getAverageRating()), "" + permissibleObject.getNumRatingVotes());
    statsLabel.setText(statText);
    if (permissibleObject.getNumRatingVotes() == 0) {
      BaseImageBundle.images.starNoVotes().applyTo(star1);
      BaseImageBundle.images.starNoVotes().applyTo(star2);
      BaseImageBundle.images.starNoVotes().applyTo(star3);
      BaseImageBundle.images.starNoVotes().applyTo(star4);
      BaseImageBundle.images.starNoVotes().applyTo(star5);
    } else {
      float rating = permissibleObject.getAverageRating();
      if (rating < .25) {
        // 0
        BaseImageBundle.images.starEmpty().applyTo(star1);
        BaseImageBundle.images.starEmpty().applyTo(star2);
        BaseImageBundle.images.starEmpty().applyTo(star3);
        BaseImageBundle.images.starEmpty().applyTo(star4);
        BaseImageBundle.images.starEmpty().applyTo(star5);
      } else if (rating >= .25 && rating < .75) {
        // .5
        BaseImageBundle.images.starHalf().applyTo(star1);
        BaseImageBundle.images.starEmpty().applyTo(star2);
        BaseImageBundle.images.starEmpty().applyTo(star3);
        BaseImageBundle.images.starEmpty().applyTo(star4);
        BaseImageBundle.images.starEmpty().applyTo(star5);
      } else if (rating >= .75 && rating < 1.25) {
        // 1.0
        BaseImageBundle.images.starFull().applyTo(star1);
        BaseImageBundle.images.starEmpty().applyTo(star2);
        BaseImageBundle.images.starEmpty().applyTo(star3);
        BaseImageBundle.images.starEmpty().applyTo(star4);
        BaseImageBundle.images.starEmpty().applyTo(star5);
      } else if (rating >= 1.25 && rating < 1.75) {
        // 1.5
        BaseImageBundle.images.starFull().applyTo(star1);
        BaseImageBundle.images.starHalf().applyTo(star2);
        BaseImageBundle.images.starEmpty().applyTo(star3);
        BaseImageBundle.images.starEmpty().applyTo(star4);
        BaseImageBundle.images.starEmpty().applyTo(star5);
      } else if (rating >= 1.75 && rating < 2.25) {
        // 2.0
        BaseImageBundle.images.starFull().applyTo(star1);
        BaseImageBundle.images.starFull().applyTo(star2);
        BaseImageBundle.images.starEmpty().applyTo(star3);
        BaseImageBundle.images.starEmpty().applyTo(star4);
        BaseImageBundle.images.starEmpty().applyTo(star5);
      } else if (rating >= 2.25 && rating < 2.75) {
        // 2.5
        BaseImageBundle.images.starFull().applyTo(star1);
        BaseImageBundle.images.starFull().applyTo(star2);
        BaseImageBundle.images.starHalf().applyTo(star3);
        BaseImageBundle.images.starEmpty().applyTo(star4);
        BaseImageBundle.images.starEmpty().applyTo(star5);
      } else if (rating >= 2.75 && rating < 3.25) {
        // 3.0
        BaseImageBundle.images.starFull().applyTo(star1);
        BaseImageBundle.images.starFull().applyTo(star2);
        BaseImageBundle.images.starFull().applyTo(star3);
        BaseImageBundle.images.starEmpty().applyTo(star4);
        BaseImageBundle.images.starEmpty().applyTo(star5);
      } else if (rating >= 3.25 && rating < 3.75) {
        // 3.5
        BaseImageBundle.images.starFull().applyTo(star1);
        BaseImageBundle.images.starFull().applyTo(star2);
        BaseImageBundle.images.starFull().applyTo(star3);
        BaseImageBundle.images.starHalf().applyTo(star4);
        BaseImageBundle.images.starEmpty().applyTo(star5);
      } else if (rating >= 3.75 && rating < 4.25) {
        // 4.0
        BaseImageBundle.images.starFull().applyTo(star1);
        BaseImageBundle.images.starFull().applyTo(star2);
        BaseImageBundle.images.starFull().applyTo(star3);
        BaseImageBundle.images.starFull().applyTo(star4);
        BaseImageBundle.images.starEmpty().applyTo(star5);
      } else if (rating >= 4.25 && rating < 4.75) {
        // 4.5
        BaseImageBundle.images.starFull().applyTo(star1);
        BaseImageBundle.images.starFull().applyTo(star2);
        BaseImageBundle.images.starFull().applyTo(star3);
        BaseImageBundle.images.starFull().applyTo(star4);
        BaseImageBundle.images.starHalf().applyTo(star5);
      } else if (rating >= 4.75) {
        // 5
        BaseImageBundle.images.starFull().applyTo(star1);
        BaseImageBundle.images.starFull().applyTo(star2);
        BaseImageBundle.images.starFull().applyTo(star3);
        BaseImageBundle.images.starFull().applyTo(star4);
        BaseImageBundle.images.starFull().applyTo(star5);
      }
    }

    if (showStatsLabel && fileRating != null) {
      String title = BaseApplication.getMessages().getString("ratingAlready", "Content Rating (You have already voted)");
      star1.setTitle(title);
      star2.setTitle(title);
      star3.setTitle(title);
      star4.setTitle(title);
      star5.setTitle(title);
    } else {
      star1.setTitle(statText);
      star2.setTitle(statText);
      star3.setTitle(statText);
      star4.setTitle(statText);
      star5.setTitle(statText);
    }
  }

  public void setUserRating(final PermissibleObject permissibleObject, int rating) {
    AsyncCallback<UserRating> callback = new AsyncCallback<UserRating>() {

      public void onSuccess(UserRating userFileRating) {
        if (userFileRating != null) {
          RatingWidget.this.fileRating = userFileRating;
          if (userFileRating.getPermissibleObject() != null) {
            RatingWidget.this.permissibleObject = userFileRating.getPermissibleObject();
          }
        }
        setStars();
      }

      public void onFailure(Throwable t) {
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), t.getMessage(), false, true, true);
        dialog.center();
      }
    };
    BaseServiceCache.getService().setUserRating(permissibleObject, rating, callback);
  }

  public void getUserRating(final PermissibleObject permissibleObject) {
    AsyncCallback<UserRating> callback = new AsyncCallback<UserRating>() {

      public void onSuccess(UserRating userFileRating) {
        if (userFileRating != null) {
          RatingWidget.this.fileRating = userFileRating;
          if (userFileRating.getPermissibleObject() != null) {
            RatingWidget.this.permissibleObject = userFileRating.getPermissibleObject();
          }
        }
        setStars();
      }

      public void onFailure(Throwable t) {
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), t.getMessage(), false, true, true);
        dialog.center();
        clear();
      }
    };
    BaseServiceCache.getService().getUserRating(permissibleObject, callback);
  }
}
