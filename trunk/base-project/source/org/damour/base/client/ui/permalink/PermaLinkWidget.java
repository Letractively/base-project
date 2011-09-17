package org.damour.base.client.ui.permalink;

import java.util.List;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.damour.base.client.utils.CursorUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PermaLinkWidget extends VerticalPanel implements ClickHandler {

  private PermissibleObject permissibleObject;
  private List<String> ignoredParameters;

  public PermaLinkWidget(final PermissibleObject permissibleObject, final List<String> ignoredParameters, final boolean usePathInfo) {
    this.permissibleObject = permissibleObject;
    this.ignoredParameters = ignoredParameters;
    Image permaLinkImage = new Image(BaseImageBundle.images.permalink());
    permaLinkImage.setTitle("Create a permanent link to this page");
    permaLinkImage.addClickHandler(this);
    add(permaLinkImage);
    CursorUtils.setHandCursor(permaLinkImage);
  }

  public void onClick(ClickEvent event) {

    String permaLinkStr = PermaLinkBuilder.getLink(permissibleObject, ignoredParameters);

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
