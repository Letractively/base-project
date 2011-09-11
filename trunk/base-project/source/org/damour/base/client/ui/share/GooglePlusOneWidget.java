package org.damour.base.client.ui.share;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;

public class GooglePlusOneWidget extends SimplePanel{

  private String url;
  
  private GooglePlusOneWidget() {
  }
  
  private GooglePlusOneWidget(String url) {
    this.url = url;
  }
  
  protected void onAttach() {
    super.onAttach();
    render(getElement(), url);
  }

  private static native void render(Element element, String url)
  /*-{
    $wnd.gapi.plusone.render(element, {"size": "medium", "annotation": "bubble", "href":url});
  }-*/;
  
  public static GooglePlusOneWidget create(String url) {
    return new GooglePlusOneWidget(url);
  }
  
}
