package org.damour.base.client.ui.repository;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.ui.buttons.IconButton;
import org.damour.base.client.ui.toolbar.ToolBar;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FileManagerPanel extends VerticalPanel implements TreeListener, IRepositoryCallback {

  IconButton openImageButton = new IconButton(null, false, BaseImageBundle.images.open_32(), BaseImageBundle.images.open_32(), BaseImageBundle.images.open_32(), BaseImageBundle.images.open_disabled_32());
  IconButton downloadImageButton = new IconButton(null, false, BaseImageBundle.images.download(), BaseImageBundle.images.download(), BaseImageBundle.images.download(), BaseImageBundle.images.download_disabled());
  IconButton uploadFileButton = new IconButton(null, false, BaseImageBundle.images.upload(), BaseImageBundle.images.upload(), BaseImageBundle.images.upload(), BaseImageBundle.images.upload_disabled());
  IconButton uploadPhotoButton = new IconButton(null, false, BaseImageBundle.images.upload(), BaseImageBundle.images.upload(), BaseImageBundle.images.upload(), BaseImageBundle.images.upload_disabled());
  IconButton propertiesImageButton = new IconButton(null, false, BaseImageBundle.images.properties16(), BaseImageBundle.images.properties16(), BaseImageBundle.images.properties16(), BaseImageBundle.images.properties_disabled_16());
  IconButton newFolderImageButton = new IconButton(null, false, BaseImageBundle.images.newFolder(), BaseImageBundle.images.newFolder(), BaseImageBundle.images.newFolder(), BaseImageBundle.images.newFolder_disabled());
  IconButton renameImageButton = new IconButton(null, false, BaseImageBundle.images.rename(), BaseImageBundle.images.rename(), BaseImageBundle.images.rename(), BaseImageBundle.images.rename_disabled());
  IconButton deleteImageButton = new IconButton(null, false, BaseImageBundle.images.delete(), BaseImageBundle.images.delete(), BaseImageBundle.images.delete(), BaseImageBundle.images.delete_disabled());

  RepositoryTree repositoryTree;
  boolean showFoldersOnly = false;
  // a hidden file starts with a dot (.)
  boolean showHiddenFiles = false;
  
  public FileManagerPanel(String title) {
    this(title, null, false);
  }

  public FileManagerPanel(String title, RepositoryTreeNode treeNode, boolean showFoldersOnly) {
    this.showFoldersOnly = showFoldersOnly;
    add(buildToolbar(title));
    add(buildRepositoryTree(treeNode));
    setCellHeight(repositoryTree, "100%");
  }

  private RepositoryTree buildRepositoryTree(RepositoryTreeNode treeNode) {
    repositoryTree = new RepositoryTree(treeNode, this, showFoldersOnly, showHiddenFiles);
    repositoryTree.addTreeListener(this);
    repositoryTree.setHeight("100%");
    return repositoryTree;
  }

  private Widget buildToolbar(String title) {
    ToolBar toolbar = new ToolBar();
    toolbar.add(new Label(title, false));

    IconButton reloadImageButton = new IconButton(null, false, BaseImageBundle.images.refresh_16(), BaseImageBundle.images.refresh_16(), BaseImageBundle.images.refresh_16(), BaseImageBundle.images.refresh_disabled_16());
    reloadImageButton.setTitle("Refresh File Manager");
    reloadImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        repositoryTree.fetchRepositoryTree(FileManagerPanel.this);
      }
    });
    toolbar.add(reloadImageButton);

    final IconButton sortImageButton = new IconButton(null, false, BaseImageBundle.images.sort(), BaseImageBundle.images.sort(), BaseImageBundle.images.sort(), BaseImageBundle.images.sort());
    sortImageButton.setTitle("Sort Files by Name");
    sortImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        repositoryTree.setAnimationEnabled(false);
        repositoryTree.setSortAToZ(!repositoryTree.isSortAToZ(), FileManagerPanel.this);
        repositoryTree.setAnimationEnabled(true);
      }
    });
    toolbar.add(sortImageButton);

    final IconButton toggleHiddenFilesImageButton = new IconButton(null, false, BaseImageBundle.images.showHide16(), BaseImageBundle.images.showHide16(), BaseImageBundle.images.showHide16(), BaseImageBundle.images.showHide16());
    toggleHiddenFilesImageButton.setTitle("Show/Hide Hidden Files");
    toggleHiddenFilesImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        repositoryTree.setAnimationEnabled(false);
        repositoryTree.setShowHiddenFiles(!repositoryTree.isShowHiddenFiles(), FileManagerPanel.this);
        repositoryTree.setAnimationEnabled(true);
      }
    });
    toolbar.add(toggleHiddenFilesImageButton);    
    
    propertiesImageButton.setEnabled(false);
    propertiesImageButton.setTitle("Properties");
    propertiesImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        ManageObjectPropertiesCommand cmd = new ManageObjectPropertiesCommand((PermissibleObject) repositoryTree.getLastItem().getUserObject());
        cmd.execute();
      }
    });
    toolbar.add(propertiesImageButton);

    newFolderImageButton.setEnabled(false);
    newFolderImageButton.setTitle("Create New Folder");
    newFolderImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        CreateNewFolderCommand cmd = new CreateNewFolderCommand(repositoryTree, FileManagerPanel.this);
        cmd.execute();
      }
    });
    toolbar.add(newFolderImageButton);

    renameImageButton.setEnabled(false);
    renameImageButton.setTitle("Rename");
    renameImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        RenameObjectCommand cmd = new RenameObjectCommand((PermissibleObject) repositoryTree.getLastItem().getUserObject(), FileManagerPanel.this);
        cmd.execute();

      }
    });
    toolbar.add(renameImageButton);

    deleteImageButton.setEnabled(false);
    deleteImageButton.setTitle("Delete");
    deleteImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        DeleteObjectCommand cmd = new DeleteObjectCommand((PermissibleObject) repositoryTree.getLastItem().getUserObject(), FileManagerPanel.this);
        cmd.execute();
      }
    });
    toolbar.add(deleteImageButton);

    openImageButton.setEnabled(false);
    openImageButton.setTitle("Open File");
    openImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        OpenFileCommand cmd = new OpenFileCommand((File) repositoryTree.getLastItem().getUserObject(), false);
        cmd.execute();
      }
    });
    toolbar.add(openImageButton);

    uploadFileButton.setEnabled(false);
    uploadFileButton.setTitle("Upload File to Selected Folder");
    uploadFileButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        UploadFileCommand cmd = new UploadFileCommand((PermissibleObject) repositoryTree.getLastItem().getUserObject(), FileManagerPanel.this);
        cmd.execute();
      }
    });
    toolbar.add(uploadFileButton);

    downloadImageButton.setEnabled(false);
    downloadImageButton.setTitle("Download File");
    downloadImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        DownloadFileCommand cmd = new DownloadFileCommand((File) repositoryTree.getLastItem().getUserObject());
        cmd.execute();
      }
    });
    toolbar.add(downloadImageButton);

    Label spacer = new Label();
    toolbar.add(spacer);
    toolbar.setCellWidth(spacer, "100%");
    return toolbar;
  }

  private void updateButtonState(TreeItem item) {
    // update button state
    openImageButton.setEnabled(item != null && item.getUserObject() != null && item.getUserObject() instanceof File);
    downloadImageButton.setEnabled(item != null && item.getUserObject() != null && item.getUserObject() instanceof File);
    uploadFileButton.setEnabled(item != null);
    uploadPhotoButton.setEnabled(item != null);
    propertiesImageButton.setEnabled(item != null && item.getUserObject() != null && item.getUserObject() instanceof PermissibleObject);
    newFolderImageButton.setEnabled(item != null);
    renameImageButton.setEnabled(item != null && item.getUserObject() != null && item.getUserObject() instanceof PermissibleObject);
    deleteImageButton.setEnabled(item != null && item.getUserObject() != null && item.getUserObject() instanceof PermissibleObject);
  }

  public void onTreeItemSelected(TreeItem item) {
    updateButtonState(item);
    if (item.getUserObject() != null && item.getUserObject() instanceof Folder) {
      item.setSelected(true);
    }
  }

  public void onTreeItemStateChanged(TreeItem item) {
    updateButtonState(item);
  }

  public void repositoryLoaded() {
    updateButtonState(repositoryTree.getLastItem());
  }

  public void objectRenamed(PermissibleObject object) {
    repositoryTree.fetchRepositoryTree(this);
  }

  public void fileUploaded(String id) {
    repositoryTree.setLastItemId(new Long(id));
    repositoryTree.fetchRepositoryTree(this);
  }

  public void fileDeleted() {
    repositoryTree.fetchRepositoryTree(this);
  }

  public RepositoryTree getRepositoryTree() {
    return repositoryTree;
  }

}
