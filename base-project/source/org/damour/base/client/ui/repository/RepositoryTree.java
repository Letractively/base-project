package org.damour.base.client.ui.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Photo;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.service.BaseServiceAsync;
import org.damour.base.client.ui.ToolTip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;

public class RepositoryTree extends Tree implements TreeListener {

  private RepositoryTreeNode repositoryTree;
  private List<Long> openFolderIds = new ArrayList<Long>();
  private boolean sortAToZ = true;
  private boolean buildingTree = false;
  private boolean createRootItem = true;
  private Label rootItemLabel = new Label("/");
  private TreeItem rootItem = new TreeItem(rootItemLabel);
  private TreeItem lastItem = null;
  private Long lastItemId = null;
  private boolean showOnlyFolders = false;
  private boolean showHiddenFiles = false;

  public RepositoryTree(IRepositoryCallback repositoryCallback) {
    this(null, repositoryCallback, false, false);
  }

  public RepositoryTree(final RepositoryTreeNode repositoryTree, final IRepositoryCallback repositoryCallback, boolean showOnlyFolders, boolean showHiddenFiles) {
    super(BaseImageBundle.images, true);
    this.showOnlyFolders = showOnlyFolders;
    this.repositoryTree = repositoryTree;
    this.showHiddenFiles = showHiddenFiles;
    setAnimationEnabled(true);
    if (repositoryTree == null) {
      // fetch it
      fetchRepositoryTree(repositoryCallback);
    } else {
      // use it
      buildRepositoryTreeAndRestoreState(repositoryTree, null);
    }
    addTreeListener(this);
    sinkEvents(Event.ONDBLCLICK);
  }
  
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if (event.getTypeInt() == Event.ONDBLCLICK) {
      OpenFileCommand cmd = new OpenFileCommand((File)getLastItem().getUserObject(), true);
      cmd.execute();
    }
  }

  public void buildRepositoryTreeAndRestoreState(final RepositoryTreeNode repositoryTreeNode, final TreeItem parentItem) {
    buildingTree = true;
    buildRepositoryTree(repositoryTreeNode, parentItem);
    buildingTree = false;
    if (createRootItem) {
      rootItem.setState(true);
      rootItemLabel.setText("/");
      new ToolTip(rootItemLabel, null, "File System");
      if (rootItem.getChildCount() == 0) {
        TreeItem hiddenItem = new TreeItem();
        rootItem.addItem(hiddenItem);
        new ToolTip(rootItemLabel, null, "File System (empty)");
        hiddenItem.setVisible(false);
      }
    }
    restoreTreeState();
  }

  private void buildRepositoryTree(final RepositoryTreeNode repositoryTreeNode, final TreeItem parentItem) {
    if (parentItem == null) {
      // make sure the tree is clear from previous use
      clear();
      if (createRootItem) {
        rootItem.removeItems();
        addItem(rootItem);
        rootItem.setState(true);
      }
    }
    // add folders
    List<Folder> folders = new ArrayList<Folder>(repositoryTreeNode.getFolders().keySet());
    Collections.sort(folders, new FolderAlphaComparator(sortAToZ));
    for (Folder folder : folders) {
      Label label = new Label(folder.getName());
      TreeItem folderItem = new TreeItem(label);
      if (!showHiddenFiles && folder.isHidden()) {
        folderItem.setVisible(false);
      }
      folderItem.setUserObject(folder);
      if (parentItem != null) {
        parentItem.addItem(folderItem);
      } else if (createRootItem) {
        rootItem.addItem(folderItem);
      } else {
        addItem(folderItem);
      }
      RepositoryTreeNode treeNode = repositoryTreeNode.getFolders().get(folder);
      if (showOnlyFolders && treeNode.getFolders().size() == 0) {
        TreeItem hiddenItem = new TreeItem();
        folderItem.addItem(hiddenItem);
        String tooltip = folder.getDescription();
        new ToolTip(label, null, tooltip);
        hiddenItem.setVisible(false);
      } else if (treeNode.getFiles().size() == 0 && treeNode.getFolders().size() == 0) {
        TreeItem hiddenItem = new TreeItem();
        folderItem.addItem(hiddenItem);
        String tooltip = folder.getDescription() + " (empty)";
        new ToolTip(label, null, tooltip);
        hiddenItem.setVisible(false);
      } else {
        String tooltip = folder.getDescription();
        new ToolTip(label, null, tooltip);
        buildRepositoryTree(treeNode, folderItem);
      }
    }
    if (showOnlyFolders) {
      return;
    }
    // add files
    Collections.sort(repositoryTreeNode.getFiles(), new FileAlphaComparator(sortAToZ));
    for (File file : repositoryTreeNode.getFiles()) {
      Label treeItemLabel = new Label(file.getName());
      TreeItem fileItem = new TreeItem(treeItemLabel);
      if (!showHiddenFiles && file.isHidden()) {
        fileItem.setVisible(false);
      }
      NumberFormat formatter = NumberFormat.getFormat("#,###");
      String tooltip = "";
      tooltip += "Description: " + file.getDescription();
      tooltip += "<BR>";
      tooltip += "Type: " + file.getContentType();
      tooltip += "<BR>";
      tooltip += "Date Created: " + (new Date(file.getCreationDate()).toLocaleString());
      tooltip += "<BR>";
      tooltip += "Last Modified: " + (new Date(file.getLastModifiedDate()).toLocaleString());
      tooltip += "<BR>";
      tooltip += "Owner: " + file.getOwner().getUsername();
      tooltip += "<BR>";
      tooltip += "Size: " + formatter.format(file.getSize()) + " bytes";

      String thumbnailImageURL = null;
      if (file instanceof Photo) {
        Photo photo = (Photo) file;
        if (photo.getThumbnailImage() != null) {
          if (GWT.isScript()) {
            thumbnailImageURL = "/servlet/GetFileService?file=" + photo.getThumbnailImage().getId() + "&name=" + photo.getName();
          } else {
            thumbnailImageURL = "http://localhost/files/" + photo.getThumbnailImage().getNameOnDisk();
          }
        }
      }
      new ToolTip(treeItemLabel, thumbnailImageURL, tooltip);
      fileItem.setUserObject(file);
      if (parentItem != null) {
        parentItem.addItem(fileItem);
      } else if (createRootItem) {
        rootItem.addItem(fileItem);
      } else {
        addItem(fileItem);
      }
    }
  }

  public void fetchRepositoryTree(final IRepositoryCallback repositoryCallback) {
    AsyncCallback<RepositoryTreeNode> callback = new AsyncCallback<RepositoryTreeNode>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(final RepositoryTreeNode result) {
        RepositoryTree.this.repositoryTree = result;
        setAnimationEnabled(false);
        buildRepositoryTreeAndRestoreState(repositoryTree, null);
        setAnimationEnabled(true);
        if (repositoryCallback != null) {
          repositoryCallback.repositoryLoaded();
        }
      }
    };
    BaseServiceAsync.service.getRepositoryTree(callback);
  }

  public void onTreeItemSelected(TreeItem item) {
    if (buildingTree) {
      return;
    }
    if (lastItem != null && lastItem != item) {
      lastItem.setSelected(false);
    }
    lastItem = item;
    PermissibleObject permissibleObject = (PermissibleObject) item.getUserObject();
    if (permissibleObject != null) {
      lastItemId = permissibleObject.getId();
    }
  }

  public void onTreeItemStateChanged(TreeItem item) {
    // do not add new items to the tree if we are adding to it
    if (buildingTree) {
      return;
    }
    if (lastItem != null && lastItem != item) {
      lastItem.setSelected(false);
    }
    lastItem = item;
    PermissibleObject permissibleObject = (PermissibleObject) item.getUserObject();
    if (permissibleObject != null) {
      lastItemId = permissibleObject.getId();
    }
    if (permissibleObject instanceof Folder) {
      if (item.getState()) {
        openFolderIds.add(((Folder) permissibleObject).getId());
      } else if (permissibleObject != null) {
        openFolderIds.remove(((Folder) permissibleObject).getId());
      }
    }
  }

  public void restoreTreeState() {
    buildingTree = true;
    for (Long folderIdToOpen : openFolderIds) {
      for (int i = 0; i < getItemCount(); i++) {
        findAndOpenNode(folderIdToOpen, getItem(i));
      }
    }
    for (int i = 0; i < getItemCount(); i++) {
      findAndOpenNode(lastItemId, getItem(i));
    }
    buildingTree = false;
  }

  private void findAndOpenNode(Long folderIdToOpen, TreeItem parent) {
    PermissibleObject permissibleObject = (PermissibleObject) parent.getUserObject();
    if (permissibleObject != null && permissibleObject.getId().equals(lastItemId)) {
      if (parent.getParentItem() != null) {
        parent.getParentItem().setState(true);
      }
      setSelectedItem(parent, false);
      parent.setSelected(true);
      lastItem = parent;
    }

    if (permissibleObject instanceof Folder) {
      Folder myFolder = (Folder) permissibleObject;
      if (myFolder.getId().equals(folderIdToOpen)) {
        parent.setState(true);
      }
    }
    // do children
    for (int i = 0; i < parent.getChildCount(); i++) {
      findAndOpenNode(folderIdToOpen, parent.getChild(i));
    }
  }

  public boolean isSortAToZ() {
    return sortAToZ;
  }

  public void setSortAToZ(boolean sortAToZ, IRepositoryCallback callback) {
    this.sortAToZ = sortAToZ;
    buildRepositoryTreeAndRestoreState(repositoryTree, null);
    if (callback != null) {
      callback.repositoryLoaded();
    }
  }

  public boolean isShowHiddenFiles() {
    return showHiddenFiles;
  }

  public void setShowHiddenFiles(boolean showHiddenFiles, IRepositoryCallback callback) {
    this.showHiddenFiles = showHiddenFiles;
    buildRepositoryTreeAndRestoreState(repositoryTree, null);
    if (callback != null) {
      callback.repositoryLoaded();
    }
  }

  public boolean isCreateRootItem() {
    return createRootItem;
  }

  public void setCreateRootItem(boolean createRootItem) {
    this.createRootItem = createRootItem;
  }

  public Long getLastItemId() {
    return lastItemId;
  }

  public void setLastItemId(Long lastItemId) {
    this.lastItemId = lastItemId;
  }

  public TreeItem getLastItem() {
    return lastItem;
  }

  public void setLastItem(TreeItem lastItem) {
    this.lastItem = lastItem;
  }

}
