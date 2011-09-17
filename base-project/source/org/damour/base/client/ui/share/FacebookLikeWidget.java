package org.damour.base.client.ui.share;

import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.ui.permalink.PermaLinkBuilder;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.SimplePanel;

public class FacebookLikeWidget extends SimplePanel {

  @SuppressWarnings("deprecation")
  public FacebookLikeWidget(PermissibleObject permissibleObject, List<String> ignoredParameters) {
    Frame frame = new Frame();
    frame.setUrl("http://www.facebook.com/plugins/like.php?&action=like&layout=standard&height=20&width=90&href="
        + URL.encodeComponent(PermaLinkBuilder.getLink(permissibleObject, ignoredParameters)));
    DOM.setElementAttribute(frame.getElement(), "frameBorder", "0");
    DOM.setElementAttribute(frame.getElement(), "allowTransparency", "true");
    DOM.setStyleAttribute(frame.getElement(), "height", "24px");
    DOM.setStyleAttribute(frame.getElement(), "width", "300px");
    // DOM.setStyleAttribute(frame.getElement(), "backgroundColor", "#e3e3e3");
    setWidget(frame);
  }

}
