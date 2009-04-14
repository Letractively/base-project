package org.damour.base.client.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.damour.base.client.exceptions.LoginException;
import org.damour.base.client.objects.Category;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileUploadStatus;
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
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.objects.UserRating;

import com.google.gwt.user.client.rpc.RemoteService;

public interface BaseService extends RemoteService {
  public User getAuthenticatedUser() throws LoginException;
  public User createOrEditAccount(User user, String password, String captchaText) throws Exception;
  public User login(String username, String password) throws Exception;
  public void logout() throws Exception;
  public String getLoginHint(String username) throws Exception;

  // hibernate/general admin methods
  public List<HibernateStat> getHibernateStats() throws Exception;
  public void resetHibernate() throws Exception;
  public void evictClassFromCache(String className) throws Exception;
  public MemoryStats getMemoryStats() throws Exception;
  public MemoryStats requestGarbageCollection() throws Exception;
  public Date getServerStartupDate() throws Exception;
  
  public List<String> getUsernames() throws Exception;
  // users/group admin methods
  public List<User> getUsers() throws Exception;
  public List<User> getUsers(UserGroup group) throws Exception;
  public List<UserGroup> getGroups() throws Exception;
  public List<UserGroup> getGroups(User user) throws Exception;
  public List<UserGroup> getOwnedGroups(User user) throws Exception;
  public GroupMembership addUserToGroup(User user, UserGroup group) throws Exception;
  public UserGroup createOrEditGroup(UserGroup group) throws Exception;
  public void deleteUser(User user, UserGroup group) throws Exception;
  public void deleteGroup(UserGroup group) throws Exception;
  public List<PendingGroupMembership> getPendingGroupMemberships(User user) throws Exception;
  public List<PendingGroupMembership> submitPendingGroupMembershipApproval(User user, Set<PendingGroupMembership> members, boolean approve) throws Exception;

  // file/content/permissions methods
  public File getFile(Long id) throws Exception;
  public RepositoryTreeNode getRepositoryTree() throws Exception;
  public Folder createNewFolder(Folder newFolder) throws Exception;
  public void deleteFile(File file) throws Exception;
  public void deleteFolder(Folder folder) throws Exception;
  public void renameFile(File file) throws Exception;
  public void renameFolder(Folder folder) throws Exception;
  public List<Permission> getPermissions(PermissibleObject permissibleObject) throws Exception;
  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions) throws Exception;
  public PermissibleObject updatePermissibleObject(PermissibleObject permissibleObject) throws Exception;
  public FileUploadStatus getFileUploadStatus() throws Exception;
  
  // category methods
  public List<Category> getCategories() throws Exception;
  public List<Category> getCategories(PermissibleObject permissibleObject) throws Exception;
  public void createCategory(String categoryName, String categoryDescription, Category parentCategory) throws Exception;
  public void deleteCategory(Category category) throws Exception;
  public void removeFromCategory(Category category, PermissibleObject permissibleObject) throws Exception;
  public void addToCategory(Category category, PermissibleObject permissibleObject) throws Exception;
  
  // content rating & advisory
  public UserRating getUserRating(PermissibleObject permissibleObject) throws Exception;
  public UserRating setUserRating(PermissibleObject permissibleObject, int rating) throws Exception;
  public UserAdvisory getUserAdvisory(PermissibleObject permissibleObject) throws Exception;
  public UserAdvisory setUserAdvisory(PermissibleObject permissibleObject, int advisory) throws Exception;

  // file comments
  public Page<Comment> getCommentPage(PermissibleObject permissibleObject, boolean sortDescending, int pageNumber, int pageSize) throws Exception;
  public List<Comment> getComments(PermissibleObject permissibleObject) throws Exception;
  public Boolean submitComment(Comment comment) throws Exception;
  public Boolean approveComment(Comment comment) throws Exception;
  public Boolean deleteComment(Comment comment) throws Exception;
}
