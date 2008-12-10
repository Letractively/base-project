package org.damour.base.client.objects;

import java.io.Serializable;

public class File extends PermissibleObject implements Serializable, IHibernateFriendly {

  public String contentType;

  public float averageRating = 0f;
  public long numRatingVotes = 0;

  public float averageAdvisory;
  public int numAdvisoryVotes;

  public boolean allowComments = true;
  public boolean moderateComments = false;

  public boolean hidden = false;
  
  public String nameOnDisk;
  
  // stored here so we can lazily load fileData
  public long size = -1;

  public File() {
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

  /**
   * @return the contentType
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * @param contentType
   *          the contentType to set
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
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
   * @return the size
   */
  public long getSize() {
    return size;
  }

  /**
   * @param size
   *          the size to set
   */
  public void setSize(long size) {
    this.size = size;
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
   * @param moderateComments the moderateComments to set
   */
  public void setModerateComments(boolean moderateComments) {
    this.moderateComments = moderateComments;
  }

  public String getNameOnDisk() {
    return nameOnDisk;
  }

  public void setNameOnDisk(String nameOnDisk) {
    this.nameOnDisk = nameOnDisk;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

}