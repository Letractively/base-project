package org.damour.base.client.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.damour.base.client.objects.Category;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileComment;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.FileUserAdvisory;
import org.damour.base.client.objects.FileUserRating;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.MemoryStats;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BaseServiceAsync {
  
  public static final BaseServiceAsync service = (BaseServiceAsync) GWT.create(BaseService.class);
  
  public void getAuthenticatedUser(AsyncCallback<User> callback);
  public void createOrEditAccount(User user, String password, String captchaText, String captchaHash, AsyncCallback<User> callback);
  public void login(String username, String password, AsyncCallback<User> callback);
  public void logout(AsyncCallback<Void> callback);
  public void getLoginHint(String username, AsyncCallback<String> callback);
  
  // hibernate admin methods
  public void getHibernateStats(AsyncCallback<List<HibernateStat>> callback);
  public void resetHibernate(AsyncCallback<List<HibernateStat>> callback);  
  public void evictClassFromCache(String className, AsyncCallback<List<HibernateStat>> callback);
  public void getMemoryStats(AsyncCallback<MemoryStats> callback);
  public void requestGarbageCollection(AsyncCallback<MemoryStats> callback);
  public void getServerStartupDate(AsyncCallback<Date> callback);

  public void getUsernames(AsyncCallback<List<String>> callback);
  // users/group admin methods
  public void getUsers(AsyncCallback<List<User>> callback);
  public void getUsers(UserGroup group, AsyncCallback<List<User>> callback);
  public void getGroups(AsyncCallback<List<UserGroup>> callback);
  public void getGroups(User user, AsyncCallback<List<UserGroup>> callback);
  public void getOwnedGroups(User user, AsyncCallback<List<UserGroup>> callback);
  public void addUserToGroup(User user, UserGroup group, AsyncCallback<GroupMembership> callback);
  public void createOrEditGroup(UserGroup group, AsyncCallback<UserGroup> callback);
  public void deleteUser(User user, UserGroup group, AsyncCallback<Void> callback);
  public void deleteGroup(UserGroup group, AsyncCallback<Void> callback);  
  public void getPendingGroupMemberships(User user, AsyncCallback<List<PendingGroupMembership>> callback);
  public void submitPendingGroupMembershipApproval(User user, Set<PendingGroupMembership> memberships, boolean approve, AsyncCallback<List<PendingGroupMembership>> callback);

  // file/content/permissions methods
  public void getFile(Long id, AsyncCallback<File> callback);
  public void getRepositoryTree(AsyncCallback<RepositoryTreeNode> callback);
  public void createNewFolder(Folder newFolder, AsyncCallback<Folder> callback);
  public void deleteFile(File file, AsyncCallback<Void> callback);
  public void deleteFolder(Folder folder, AsyncCallback<Void> callback);
  public void renameFile(File file, AsyncCallback<Void> callback);
  public void renameFolder(Folder folder, AsyncCallback<Void> callback);
  public void getPermissions(PermissibleObject permissibleObject, AsyncCallback<List<Permission>> callback);
  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions, AsyncCallback<Void> callback);
  public void updatePermissibleObject(PermissibleObject permissibleObject, AsyncCallback<PermissibleObject> callback);
  public void getFileUploadStatus(AsyncCallback<FileUploadStatus> callback);

  // category methods
  public void getCategories(AsyncCallback<List<Category>> callback) throws Exception;
  public void getCategories(PermissibleObject permissibleObject, AsyncCallback<List<Category>> callback) throws Exception;
  public void createCategory(String categoryName, String categoryDescription, Category parentCategory, AsyncCallback<Void> callback) throws Exception;
  public void deleteCategory(Category category, AsyncCallback<Void> callback) throws Exception;
  public void removeFromCategory(Category category, PermissibleObject permissibleObject, AsyncCallback<Void> callback) throws Exception;
  public void addToCategory(Category category, PermissibleObject permissibleObject, AsyncCallback<Void> callback) throws Exception;
  
  // content rating & advisory
  public void setFileUserRating(File file, int rating, AsyncCallback<FileUserRating> callback);
  public void getFileUserRating(File file, AsyncCallback<FileUserRating> callback);
  public void setFileUserAdvisory(File file, int advisory, AsyncCallback<FileUserAdvisory> callback);
  public void getFileUserAdvisory(File file, AsyncCallback<FileUserAdvisory> callback);
  
  // file comments
  public void getCommentPage(File file, boolean sortDescending, int pageNumber, int pageSize, AsyncCallback<Page<FileComment>> callback);
  public void getComments(File file, AsyncCallback<List<FileComment>> callback);
  public void submitComment(FileComment comment, AsyncCallback<Boolean> callback);
  public void approveComment(FileComment comment, AsyncCallback<Boolean> callback);
  public void deleteComment(FileComment comment, AsyncCallback<Boolean> callback);
  
}
