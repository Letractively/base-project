package org.damour.base.client.ui.permalink;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.damour.base.client.utils.CursorUtils;
import org.damour.base.client.utils.ParameterParser;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PermaLinkWidget extends VerticalPanel implements ClickHandler {

  private PermissibleObject permissibleObject;
  private boolean usePathInfo = false;

  public PermaLinkWidget(final PermissibleObject permissibleObject, final boolean usePathInfo) {
    this.permissibleObject = permissibleObject;
    this.usePathInfo = usePathInfo;
    Image permaLinkImage = new Image();
    BaseImageBundle.images.permalink().applyTo(permaLinkImage);
    permaLinkImage.setTitle("Create a permanent link to this page");
    permaLinkImage.addClickHandler(this);
    add(permaLinkImage);
    CursorUtils.setHandCursor(permaLinkImage);
  }

  public void onClick(ClickEvent event) {
    // build a hashmap of pathInfo parameters, query parameters and history token parameters
    ParameterParser pathParameters = new ParameterParser(ParameterParser.convertRESTtoQueryString(Window.Location.getPath()));
    ParameterParser queryStringParameters = new ParameterParser(Window.Location.getQueryString());
    ParameterParser historyParameters = new ParameterParser(History.getToken());

    List<String> parameterOrder = new ArrayList<String>(historyParameters.getOrderedParameterNames());
    for (String queryStringParam : queryStringParameters.getOrderedParameterNames()) {
      if (!parameterOrder.contains(queryStringParam)) {
        parameterOrder.add(queryStringParam);
      }
    }
    for (String pathParam : pathParameters.getOrderedParameterNames()) {
      if (!parameterOrder.contains(pathParam)) {
        parameterOrder.add(pathParam);
      }
    }
    String permaLinkStr = Window.Location.getProtocol() + "//" + Window.Location.getHostName()
        + ((Window.Location.getPort().equals("80") || Window.Location.getPort().equals("")) ? "" : ":" + Window.Location.getPort());
    for (String parameterName : parameterOrder) {
      if ("name".equalsIgnoreCase(parameterName) && permissibleObject != null && !StringUtils.isEmpty(permissibleObject.getName())) {
        continue;
      }
      if (!StringUtils.isEmpty(historyParameters.getParameter(parameterName))) {
        permaLinkStr += "/" + parameterName + "/" + historyParameters.getParameter(parameterName);
      } else if (!StringUtils.isEmpty(queryStringParameters.getParameter(parameterName))) {
        permaLinkStr += "/" + parameterName + "/" + queryStringParameters.getParameter(parameterName);
      } else if (usePathInfo && !StringUtils.isEmpty(pathParameters.getParameter(parameterName))) {
        permaLinkStr += "/" + parameterName + "/" + pathParameters.getParameter(parameterName);
      }
    }
    if (permissibleObject != null && !StringUtils.isEmpty(permissibleObject.getName())) {
      permaLinkStr += "/name/" + StringUtils.patchURL(permissibleObject.getName()) + ".html";
    }

    final TextBox textBox = new TextBox();
    textBox.setVisibleLength(100);
    textBox.setText(permaLinkStr);
    textBox.addFocusHandler(new FocusHandler() {

      public void onFocus(FocusEvent event) {
        textBox.selectAll();
      }
    });
    textBox.addClickHandler(new ClickHandler() {
      
      public void onClick(ClickEvent event) {
        textBox.selectAll();
      }
    });
    PromptDialogBox linkDialog = new PromptDialogBox("Paste link in email or IM", "OK", null, null, true, true);
    // linkDialog.setAnimationEnabled(false);
    linkDialog.setContent(textBox);
    linkDialog.center();
    Timer selectAllTimer = new Timer() {
      public void run() {
        textBox.selectAll();
      }
    };
    selectAllTimer.schedule(300);
  }

}
