package org.damour.base.client.ui.repository.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.BaseServiceAsync;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GeneralPanel extends FlexTable {

  private VerticalPanel globalPermissionsPanel = new VerticalPanel();
  private PermissibleObject permissibleObject;
  private TextBox nameTextBox = new TextBox();

  private CheckBox globalReadCheckBox = new CheckBox("Read");
  private CheckBox globalWriteCheckBox = new CheckBox("Write");
  private CheckBox globalExecuteCheckBox = new CheckBox("Execute");

  private boolean dirty = false;

  public GeneralPanel(PermissibleObject permissibleObject) {
    this.permissibleObject = permissibleObject;
    populateGlobalPermissionsPanel();
    buildUI();
  }

  public void buildUI() {

    globalReadCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        dirty = true;
      }
    });
    globalWriteCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        dirty = true;
      }
    });
    globalExecuteCheckBox.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        dirty = true;
      }
    });

    nameTextBox.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
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
    globalReadCheckBox.setChecked(permissibleObject.isGlobalRead());
    globalWriteCheckBox.setChecked(permissibleObject.isGlobalWrite());
    globalExecuteCheckBox.setChecked(permissibleObject.isGlobalExecute());
    globalPermissionsPanel.setHeight("100%");
    globalPermissionsPanel.add(globalReadCheckBox);
    globalPermissionsPanel.add(globalWriteCheckBox);
    globalPermissionsPanel.add(globalExecuteCheckBox);
  }

  private String getCreationDate() {
    if (permissibleObject instanceof Folder) {
      return (new Date(((Folder) permissibleObject).getCreationDate())).toLocaleString();
    } else if (permissibleObject instanceof File) {
      return (new Date(((File) permissibleObject).getCreationDate())).toLocaleString();
    }
    return "";
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
    return "";
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
    BaseImageBundle.images.file32().applyTo(fileTypeIcon);
    if (permissibleObject instanceof Folder) {
      BaseImageBundle.images.folder32().applyTo(fileTypeIcon);
    } else if (permissibleObject instanceof File) {
      File file = (File) permissibleObject;
      if (StringUtils.isEmpty(file.getContentType())) {
        BaseImageBundle.images.file32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("image/x-png")) {
        BaseImageBundle.images.png32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("image/jpeg")) {
        BaseImageBundle.images.jpg32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("image/pjpeg")) {
        BaseImageBundle.images.jpg32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("image")) {
        BaseImageBundle.images.png32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("video")) {
        BaseImageBundle.images.movie32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("audio")) {
        BaseImageBundle.images.audio32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("text/plain")) {
        BaseImageBundle.images.text32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("text/html")) {
        BaseImageBundle.images.html32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("application/x-java-archive")) {
        BaseImageBundle.images.jar32().applyTo(fileTypeIcon);
      } else if (file.getContentType().contains("application/x-zip-compressed")) {
        BaseImageBundle.images.archive32().applyTo(fileTypeIcon);
      }
    }
    return fileTypeIcon;
  }

  public void apply(final AsyncCallback<Void> callback) {
    if (dirty) {
      PermissibleObject newPermissibleObject = new PermissibleObject();
      newPermissibleObject.setId(permissibleObject.getId());
      newPermissibleObject.setName(nameTextBox.getText());
      newPermissibleObject.setOwner(permissibleObject.getOwner());
      newPermissibleObject.setGlobalRead(globalReadCheckBox.isChecked());
      newPermissibleObject.setGlobalWrite(globalWriteCheckBox.isChecked());
      newPermissibleObject.setGlobalExecute(globalExecuteCheckBox.isChecked());
      AsyncCallback<PermissibleObject> updatePermissibleObjectCallback = new AsyncCallback<PermissibleObject>() {
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(PermissibleObject result) {
          // update our copy (cheaper than refetching or inserting back into tree)
          permissibleObject.setName(result.getName());
          permissibleObject.setGlobalRead(result.isGlobalRead());
          permissibleObject.setGlobalWrite(result.isGlobalWrite());
          permissibleObject.setGlobalExecute(result.isGlobalExecute());
          callback.onSuccess(null);
        }
      };
      BaseServiceCache.getService().updatePermissibleObject(newPermissibleObject, updatePermissibleObjectCallback);
    }
  }

}
