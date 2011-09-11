package org.damour.base.client.ui.share;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.ui.permalink.PermaLinkBuilder;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.SimplePanel;

public class TwitterWidget extends SimplePanel {

  public TwitterWidget(PermissibleObject permissibleObject) {
    Frame frame = new Frame();
    
    @SuppressWarnings("deprecation")
    String url = URL.encodeComponent(PermaLinkBuilder.getLink(permissibleObject));
    
    frame.setUrl("http://platform.twitter.com/widgets/tweet_button.html?count=horizontal&url=" + url);
    DOM.setElementAttribute(frame.getElement(), "frameBorder", "0");
    DOM.setElementAttribute(frame.getElement(), "allowTransparency", "true");
    DOM.setElementAttribute(frame.getElement(), "scrolling", "no");
    DOM.setStyleAttribute(frame.getElement(), "height", "20px");
    DOM.setStyleAttribute(frame.getElement(), "width", "130px");
    // DOM.setStyleAttribute(frame.getElement(), "backgroundColor", "#e3e3e3");
    setWidget(frame);    
  }  
}
