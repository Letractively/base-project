package org.damour.base.client.objects;

import java.io.Serializable;

public class PermissibleObject implements Serializable, IHibernateFriendly {

  public Long id;
  public User owner;
  
  public String name;
  public String description;
  public Folder parentFolder;
  public Long creationDate = System.currentTimeMillis();
  public Long lastModifiedDate = System.currentTimeMillis();
  
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

  public Folder getParentFolder() {
    return parentFolder;
  }

  public void setParentFolder(Folder parentFolder) {
    this.parentFolder = parentFolder;
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

}