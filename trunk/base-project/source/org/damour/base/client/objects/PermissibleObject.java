package org.damour.base.client.objects;

import java.io.Serializable;

public class PermissibleObject implements Serializable, IHibernateFriendly {

  public Long id;
  public User owner;

  public String name;
  public String description;
  public PermissibleObject parent;
  public Long creationDate = System.currentTimeMillis();
  public Long lastModifiedDate = System.currentTimeMillis();

  public float averageRating = 0f;
  public long numRatingVotes = 0;

  public float averageAdvisory;
  public int numAdvisoryVotes;

  public boolean allowComments = true;
  public boolean moderateComments = false;

  public boolean hidden = false;

  public boolean globalRead = false;
  public boolean globalWrite = false;
  public boolean globalExecute = false;

  public PermissibleObject() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public boolean isFieldUnique(String fieldName) {
    return false;
  }

  public boolean isFieldKey(String fieldName) {
    return false;
  }

  public String getSqlUpdate() {
    return null;
  }

  public boolean isGlobalRead() {
    return globalRead;
  }

  public void setGlobalRead(boolean globalRead) {
    this.globalRead = globalRead;
  }

  public boolean isGlobalWrite() {
    return globalWrite;
  }

  public void setGlobalWrite(boolean globalWrite) {
    this.globalWrite = globalWrite;
  }

  public boolean isGlobalExecute() {
    return globalExecute;
  }

  public void setGlobalExecute(boolean globalExecute) {
    this.globalExecute = globalExecute;
  }

  public String getCachePolicy() {
    return "nonstrict-read-write";
  }

  public boolean isLazy() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PermissibleObject other = (PermissibleObject) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PermissibleObject getParent() {
    return parent;
  }

  public void setParent(PermissibleObject parent) {
    this.parent = parent;
  }

  public Long getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
  }

  public Long getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(Long lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  /**
   * @return the averageRating
   */
  public float getAverageRating() {
    return averageRating;
  }

  /**
   * @param averageRating
   *          the averageRating to set
   */
  public void setAverageRating(float averageRating) {
    this.averageRating = averageRating;
  }

  /**
   * @return the numRatingVotes
   */
  public long getNumRatingVotes() {
    return numRatingVotes;
  }

  /**
   * @param numRatingVotes
   *          the numRatingVotes to set
   */
  public void setNumRatingVotes(long numRatingVotes) {
    this.numRatingVotes = numRatingVotes;
  }

  /**
   * @return the averageAdvisory
   */
  public float getAverageAdvisory() {
    return averageAdvisory;
  }

  /**
   * @param averageAdvisory
   *          the averageAdvisory to set
   */
  public void setAverageAdvisory(float averageAdvisory) {
    this.averageAdvisory = averageAdvisory;
  }

  /**
   * @return the numAdvisoryVotes
   */
  public int getNumAdvisoryVotes() {
    return numAdvisoryVotes;
  }

  /**
   * @param numAdvisoryVotes
   *          the numAdvisoryVotes to set
   */
  public void setNumAdvisoryVotes(int numAdvisoryVotes) {
    this.numAdvisoryVotes = numAdvisoryVotes;
  }

  /**
   * @return the allowComments
   */
  public boolean isAllowComments() {
    return allowComments;
  }

  /**
   * @param allowComments
   *          the allowComments to set
   */
  public void setAllowComments(boolean allowComments) {
    this.allowComments = allowComments;
  }

  /**
   * @return the moderateComments
   */
  public boolean isModerateComments() {
    return moderateComments;
  }

  /**
   * @param moderateComments
   *          the moderateComments to set
   */
  public void setModerateComments(boolean moderateComments) {
    this.moderateComments = moderateComments;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

}