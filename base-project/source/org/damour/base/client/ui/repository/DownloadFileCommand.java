package org.damour.base.client.ui.repository;


import org.damour.base.client.objects.File;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

public class DownloadFileCommand implements Command {

  File object;

  public DownloadFileCommand(File object) {
    this.object = object;
  }

  public void execute() {
    String url = "/servlet/org.damour.base.server.GetFileService?file=" + object.getId() + "&name=" + object.getName() + "&download=true";
    if (!GWT.isScript()) {
      url = "http://localhost/files/" + object.getNameOnDisk() + "?download=true";
    }
    final Frame hidden = new Frame(url);
    DOM.setStyleAttribute(hidden.getElement(), "display", "none");
    RootPanel.get().add(hidden);
    return;
  }

}
