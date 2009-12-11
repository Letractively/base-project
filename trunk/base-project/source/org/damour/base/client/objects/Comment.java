package org.damour.base.client.objects;

import java.io.Serializable;

public class Comment extends PermissibleObject implements Serializable {

  public User author;
  public String comment;
  public Comment parentComment;
  //
  // if there is no user, we will store the users other info: like email/ip address
  // so that we can notify them if they would like to get updates when someone
  // 
  public String email;
  public String authorIP;
  public long commentDate = System.currentTimeMillis();
  public boolean approved = false;

  public Comment() {
  }

  /**
   * @return the author
   */
  public User getAuthor() {
    return author;
  }

  /**
   * @param author
   *          the author to set
   */
  public void setAuthor(User author) {
    this.author = author;
  }

  /**
   * @return the commentDate
   */
  public long getCommentDate() {
    return commentDate;
  }

  /**
   * @param commentDate
   *          the commentDate to set
   */
  public void setCommentDate(long commentDate) {
    this.commentDate = commentDate;
  }

  /**
   * @return the approved
   */
  public boolean isApproved() {
    return approved;
  }

  /**
   * @param approved
   *          the approved to set
   */
  public void setApproved(boolean approved) {
    this.approved = approved;
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param email
   *          the email to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return the authorIP
   */
  public String getAuthorIP() {
    return authorIP;
  }

  /**
   * @param authorIP
   *          the authorIP to set
   */
  public void setAuthorIP(String authorIP) {
    this.authorIP = authorIP;
  }

  /**
   * @return the comment
   */
  public String getComment() {
    return comment;
  }

  /**
   * @param comment
   *          the comment to set
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * @return the parentComment
   */
  public Comment getParentComment() {
    return parentComment;
  }

  /**
   * @param parentComment
   *          the parentComment to set
   */
  public void setParentComment(Comment parentComment) {
    this.parentComment = parentComment;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Comment other = (Comment) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}