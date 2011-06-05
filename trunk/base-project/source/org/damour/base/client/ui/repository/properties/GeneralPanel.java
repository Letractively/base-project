package org.damour.base.client.ui.repository.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GeneralPanel extends FlexTable {

  private VerticalPanel globalPermissionsPanel = new VerticalPanel();
  private PermissibleObject permissibleObject;
  private TextBox nameTextBox = new TextBox();

  private CheckBox globalReadCheckBox = new CheckBox("Read");
  private CheckBox globalWriteCheckBox = new CheckBox("Write");
  private CheckBox globalExecuteCheckBox = new CheckBox("Execute");
  private CheckBox globalCreateChildrenCheckBox = new CheckBox("Create Children");

  private boolean dirty = false;

  public GeneralPanel(PermissibleObject permissibleObject) {
    this.permissibleObject = permissibleObject;
    populateGlobalPermissionsPanel();
    buildUI();
  }

  public void buildUI() {

    globalReadCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dirty = true;
      }
    });
    globalWriteCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dirty = true;
      }
    });
    globalExecuteCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dirty = true;
      }
    });
    globalCreateChildrenCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dirty = true;
      }
    });
    nameTextBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dirty = true;
      }
    });

    int row = 0;
    // folder icon
    setWidget(row, 0, getFileTypeIcon());
    getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
    nameTextBox.setVisibleLength(30);
    nameTextBox.setText(getName());
    // filename
    setWidget(row, 1, nameTextBox);
    // type
    setWidget(++row, 0, new Label("Type:"));
    setWidget(row, 1, new Label(getType(), false));
    // location
    setWidget(++row, 0, new Label("Location:"));
    setWidget(row, 1, new Label(getLocation(), false));
    // size
    setWidget(++row, 0, new Label("Size:"));
    setWidget(row, 1, new Label(getSize(), false));
    // created
    setWidget(++row, 0, new Label("Created:"));
    setWidget(row, 1, new Label(getCreationDate(), false));
    // owner
    setWidget(++row, 0, new Label("Owner:"));
    setWidget(row, 1, buildOwnerLabel());
    // global permissions
    CaptionPanel globalPermissionsPanelWrapper = new CaptionPanel("Global Permissions");
    globalPermissionsPanelWrapper.setContentWidget(globalPermissionsPanel);
    setWidget(++row, 0, globalPermissionsPanelWrapper);
    getFlexCellFormatter().setColSpan(row, 0, 2);
  }

  private Label buildOwnerLabel() {
    Label label = new Label(permissibleObject.getOwner().getUsername(), false);
    label.setTitle(permissibleObject.getOwner().getFirstname() + " " + permissibleObject.getOwner().getLastname());
    return label;
  }

  private void populateGlobalPermissionsPanel() {
    globalReadCheckBox.setValue(permissibleObject.isGlobalRead());
    globalWriteCheckBox.setValue(permissibleObject.isGlobalWrite());
    globalExecuteCheckBox.setValue(permissibleObject.isGlobalExecute());
    globalCreateChildrenCheckBox.setValue(permissibleObject.isGlobalCreateChild());
    globalPermissionsPanel.setHeight("100%");
    globalPermissionsPanel.add(globalReadCheckBox);
    globalPermissionsPanel.add(globalWriteCheckBox);
    globalPermissionsPanel.add(globalExecuteCheckBox);
    globalPermissionsPanel.add(globalCreateChildrenCheckBox);
  }

  @SuppressWarnings("deprecation")
  private String getCreationDate() {
    if (permissibleObject instanceof Folder) {
      return (new Date(((Folder) permissibleObject).getCreationDate())).toLocaleString();
    } else if (permissibleObject instanceof File) {
      return (new Date(((File) permissibleObject).getCreationDate())).toLocaleString();
    }
    return (new Date(permissibleObject.getCreationDate())).toLocaleString();
  }

  private String getSize() {
    long size = 0;
    if (permissibleObject instanceof File) {
      size = ((File) permissibleObject).getSize();
    }
    NumberFormat formatter = NumberFormat.getFormat("#,###");
    return formatter.format(size) + " bytes";
  }

  private String getName() {
    if (permissibleObject instanceof Folder) {
      return ((Folder) permissibleObject).getName();
    } else if (permissibleObject instanceof File) {
      return ((File) permissibleObject).getName();
    }
    return permissibleObject.getName();
  }

  private String getType() {
    if (permissibleObject instanceof Folder) {
      return "File Folder";
    } else if (permissibleObject instanceof File) {
      return ((File) permissibleObject).getContentType();
    }
    return "Unknown";
  }

  private String getLocation() {
    List<String> parentFolders = new ArrayList<String>();
    PermissibleObject parentFolder = permissibleObject.getParent();
    while (parentFolder != null) {
      parentFolders.add(parentFolder.getName());
      parentFolder = parentFolder.getParent();
    }
    Collections.reverse(parentFolders);
    String location = "";
    for (String parent : parentFolders) {
      location += "/" + parent;
    }
    if ("".equals(location)) {
      location = "/";
    }
    return location;
  }

  private Image getFileTypeIcon() {
    Image fileTypeIcon = new Image();
    fileTypeIcon.setUrl(BaseImageBundle.images.file32().getURL());
    if (permissibleObject instanceof Folder) {
      fileTypeIcon.setUrl(BaseImageBundle.images.folder32().getURL());
    } else if (permissibleObject instanceof File) {
      File file = (File) permissibleObject;
      if (StringUtils.isEmpty(file.getContentType())) {
        fileTypeIcon.setUrl(BaseImageBundle.images.file32().getURL());
      } else if (file.getContentType().contains("image/x-png")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.png32().getURL());
      } else if (file.getContentType().contains("image/jpeg")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.jpg32().getURL());
      } else if (file.getContentType().contains("image/pjpeg")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.jpg32().getURL());
      } else if (file.getContentType().contains("image")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.png32().getURL());
      } else if (file.getContentType().contains("video")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.movie32().getURL());
      } else if (file.getContentType().contains("audio")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.audio32().getURL());
      } else if (file.getContentType().contains("text/plain")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.text32().getURL());
      } else if (file.getContentType().contains("text/html")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.html32().getURL());
      } else if (file.getContentType().contains("application/x-java-archive")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.jar32().getURL());
      } else if (file.getContentType().contains("application/x-zip-compressed")) {
        fileTypeIcon.setUrl(BaseImageBundle.images.archive32().getURL());
      }
    }
    return fileTypeIcon;
  }

  public void apply(final AsyncCallback<Void> callback) {
    if (dirty) {
      permissibleObject.setName(nameTextBox.getText());
      permissibleObject.setOwner(permissibleObject.getOwner());
      permissibleObject.setGlobalRead(globalReadCheckBox.getValue());
      permissibleObject.setGlobalWrite(globalWriteCheckBox.getValue());
      permissibleObject.setGlobalExecute(globalExecuteCheckBox.getValue());
      permissibleObject.setGlobalCreateChild(globalCreateChildrenCheckBox.getValue());
      AsyncCallback<PermissibleObject> updatePermissibleObjectCallback = new AsyncCallback<PermissibleObject>() {
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(PermissibleObject result) {
          // update our copy (cheaper than refetching or inserting back into tree)
          result.mergeInto(permissibleObject);
          callback.onSuccess(null);
        }
      };
      BaseServiceCache.getService().updatePermissibleObject(permissibleObject, updatePermissibleObjectCallback);
    }
  }

}
