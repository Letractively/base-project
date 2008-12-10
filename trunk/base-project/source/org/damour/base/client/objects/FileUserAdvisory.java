package org.damour.base.client.objects;

import java.io.Serializable;

public class FileUserAdvisory implements Serializable, IHibernateFriendly {

  public Long id;
  public User voter;
  public File file;
  public String voterIP;
  public int rating = 0; // 0-5
  public long ratingDate = System.currentTimeMillis();

  public FileUserAdvisory() {
  }

  public boolean isLazy() {
    return false;
  }

  public boolean isFieldUnique(String fieldName) {
    return false;
  }

  public boolean isFieldKey(String fieldName) {
    return false;
    // if (fieldName.equals("voter")) {
    // return true;
    // } else if (fieldName.equals("file")) {
    // return true;
    // }
    // return false;
  }

  public String getSqlUpdate() {
    return null;
  }

  public String getCachePolicy() {
    return "nonstrict-read-write";
  }

  public User getVoter() {
    return voter;
  }

  public void setVoter(User voter) {
    this.voter = voter;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public long getRatingDate() {
    return ratingDate;
  }

  public void setRatingDate(long ratingDate) {
    this.ratingDate = ratingDate;
  }

  public int getRating() {
    return rating;
  }

  public void setRating(int rating) {
    this.rating = rating;
  }



  /**
   * @return the voterIP
   */
  public String getVoterIP() {
    return voterIP;
  }

  /**
   * @param voterIP
   *          the voterIP to set
   */
  public void setVoterIP(String voterIP) {
    this.voterIP = voterIP;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /* (non-Javadoc)
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
    FileUserAdvisory other = (FileUserAdvisory) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}