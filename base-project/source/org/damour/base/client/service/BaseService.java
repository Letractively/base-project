package org.damour.base.client.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.damour.base.client.exceptions.LoginException;
import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.MemoryStats;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.Tag;
import org.damour.base.client.objects.TagMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.objects.UserRating;

import com.google.gwt.user.client.rpc.RemoteService;

public interface BaseService extends RemoteService {
  // login/auth
  public User getAuthenticatedUser() throws LoginException;
  public User createOrEditAccount(User user, String password, String captchaText) throws SimpleMessageException;
  public User login(String username, String password) throws SimpleMessageException;
  public void logout() throws SimpleMessageException;
  public String getLoginHint(String username) throws SimpleMessageException;
  public User submitAccountValidation(String username, String validationCode) throws SimpleMessageException;

  // hibernate/general admin methods
  public List<HibernateStat> getHibernateStats() throws SimpleMessageException;
  public void resetHibernate() throws SimpleMessageException;
  public void evictClassFromCache(String className) throws SimpleMessageException;
  public MemoryStats getMemoryStats() throws SimpleMessageException;
  public MemoryStats requestGarbageCollection() throws SimpleMessageException;
  public Date getServerStartupDate() throws SimpleMessageException;
  
  // users/group admin methods
  public List<String> getUsernames() throws SimpleMessageException;
  public List<User> getUsers() throws SimpleMessageException;
  public List<User> getUsers(UserGroup group) throws SimpleMessageException;
  public List<UserGroup> getGroups() throws SimpleMessageException;
  public List<UserGroup> getGroups(User user) throws SimpleMessageException;
  public List<UserGroup> getOwnedGroups(User user) throws SimpleMessageException;
  public GroupMembership addUserToGroup(User user, UserGroup group) throws SimpleMessageException;
  public UserGroup createOrEditGroup(UserGroup group) throws SimpleMessageException;
  public void deleteUser(User user, UserGroup group) throws SimpleMessageException;
  public void deleteGroup(UserGroup group) throws SimpleMessageException;
  public List<PendingGroupMembership> getPendingGroupMemberships(User user) throws SimpleMessageException;
  public List<PendingGroupMembership> submitPendingGroupMembershipApproval(User user, Set<PendingGroupMembership> members, boolean approve) throws SimpleMessageException;

  // file/content/permissions methods
  public PermissibleObject getPermissibleObject(Long id) throws SimpleMessageException;
  public RepositoryTreeNode getRepositoryTree() throws SimpleMessageException;
  public PermissibleObjectTreeNode getPermissibleObjectTree(PermissibleObject parent) throws SimpleMessageException;
  public PermissibleObject savePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException;
  public List<PermissibleObject> savePermissibleObjects(List<PermissibleObject> permissibleObjects) throws SimpleMessageException;
  public void deletePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException;
  public void deletePermissibleObjects(Set<PermissibleObject> permissibleObjects) throws SimpleMessageException;
  public List<PermissibleObject> getPermissibleObjects(PermissibleObject parent, String objectType) throws SimpleMessageException;
  public List<PermissibleObject> getMyPermissibleObjects(PermissibleObject parent, String objectType) throws SimpleMessageException;
  public Folder createNewFolder(Folder newFolder) throws SimpleMessageException;
  public void renameFile(File file) throws SimpleMessageException;
  public void renameFolder(Folder folder) throws SimpleMessageException;
  public List<Permission> getPermissions(PermissibleObject permissibleObject) throws SimpleMessageException;
  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions) throws SimpleMessageException;
  public PermissibleObject updatePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException;
  public FileUploadStatus getFileUploadStatus() throws SimpleMessageException;
  public List<PermissibleObject> searchPermissibleObjects(PermissibleObject parent, String query, String sortField, boolean sortDescending, String searchObjectType, boolean searchNames, boolean searchDescriptions, boolean searchKeywords, boolean useExactPhrase) throws SimpleMessageException;
  // for debug purposes: simply return what was given, proving the serialization of the desired object
  public PermissibleObject echoPermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException;

  // paging api
  public Page<PermissibleObject> getPage(PermissibleObject parent, String pageClassType, String sortField, boolean sortDescending, int pageNumber, int pageSize) throws SimpleMessageException;
  public PageInfo getPageInfo(PermissibleObject parent, String pageClassType, int pageSize) throws SimpleMessageException;
  
  // tag methods
  public List<Tag> getTags() throws SimpleMessageException;
  public List<Tag> getTags(PermissibleObject permissibleObject) throws SimpleMessageException;
  public List<PermissibleObject> getTaggedPermissibleObjects(Tag tag) throws SimpleMessageException;
  public void createTag(String tagName, String tagDescription, Tag parentTag) throws SimpleMessageException;
  public void deleteTag(Tag tag) throws SimpleMessageException;
  public void removeFromTag(Tag tag, PermissibleObject permissibleObject) throws SimpleMessageException;
  public void removeTagMembership(TagMembership tagMembership) throws SimpleMessageException;
  public void addToTag(Tag tag, PermissibleObject permissibleObject) throws SimpleMessageException;
  public void addToTag(TagMembership tagMembership) throws SimpleMessageException;
  
  // content rating & advisory
  public UserRating getUserRating(PermissibleObject permissibleObject) throws SimpleMessageException;
  public UserRating setUserRating(PermissibleObject permissibleObject, int rating) throws SimpleMessageException;
  public PermissibleObject getNextUnratedPermissibleObject(String objectType) throws SimpleMessageException;
  public UserAdvisory getUserAdvisory(PermissibleObject permissibleObject) throws SimpleMessageException;
  public UserAdvisory setUserAdvisory(PermissibleObject permissibleObject, int advisory) throws SimpleMessageException;

  // file comments
  public Boolean submitComment(Comment comment) throws SimpleMessageException;
  public Boolean approveComment(Comment comment) throws SimpleMessageException;
  public Boolean deleteComment(Comment comment) throws SimpleMessageException;
  
  // advertising/feedback rpc
  public Boolean submitAdvertisingInfo(String contactName, String email, String company, String phone, String comments) throws SimpleMessageException;
  public Boolean submitFeedback(String contactName, String email, String phone, String comments) throws SimpleMessageException;
}
