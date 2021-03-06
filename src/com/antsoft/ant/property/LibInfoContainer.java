/*
 * $Header: /usr/cvsroot/AntIDE/source/com/antsoft/ant/property/LibInfoContainer.java,v 1.3 1999/07/22 03:42:03 multipia Exp $
 * Ant ( JDK wrapper Java IDE )
 * Version 1.0
 * Copyright (c) 1998-1999 Antsoft Co. All rights reserved.
 *  This program and source file is protected by Korea and international
 * Copyright laws.
 *
 * $Revision: 1.3 $
 */
package com.antsoft.ant.property;

import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;

/**
 * LibInfo object container
 *
 * @author kim, sang kyun
 */
public class LibInfoContainer implements Serializable, Cloneable{

  private Vector libInfos;


  /** default constructor */
  public LibInfoContainer() {
    libInfos = new Vector(5, 2);
  }

  /**
   * add LibraryInfo object
   *
   * @param LibraryInfo LibraryInfo object
   */
  public void addLibraryInfo(Object libInfo){
    libInfos.addElement(libInfo);
  }

  public void setLibraryInfo(Object libInfo, int index){
    libInfos.setElementAt(libInfo, index);
  }  

  /**
   * remove Library info
   *
   * @param index index
   */
  public void removeLibraryInfo(int index){
    libInfos.removeElementAt(index);
  }

  /**
   * remove Library info
   *
   * @param libInfo libraryInfo object to remove
   */
  public void removeLibraryInfo(Object libInfo){
    libInfos.removeElement(libInfo);
  }

  /**
   * get LibraryInfos
   *
   * @return LibraryInfo Enumeration
   */
  public Enumeration getLibraryInfos(){
    return libInfos.elements();
  }

  /**
   * get Library info
   *
   * @param index index
   * @return LibraryInfo object
   */
  public Object getLibraryInfo(int index){
    return libInfos.elementAt(index);
  }

  public Object getLibraryInfo(String libName){
    LibInfo ret = null;
    if(libInfos != null)
    for(int i=0; i<libInfos.size(); i++){
      LibInfo info = (LibInfo)libInfos.elementAt(i);
      if(info.getName().equals(libName)) {
        ret = info;
        break;
      }  
    }

    return ret;
  }

  /**
   * get size
   *
   * @return Libraryinfo size
   */
  public int getSize(){
    return libInfos.size();
  }

  public int indexOf(Object libInfo){
    return libInfos.indexOf(libInfo);
  }

  public synchronized Object clone(){
    LibInfoContainer lic = new LibInfoContainer();
    if(libInfos != null){
      int size = libInfos.size();
      for(int i=0;i<size; i++){
        LibInfo info = (LibInfo)libInfos.elementAt(i);
        if(info != null) lic.libInfos.addElement( info.clone() );
      }
    }
    return lic;
  }  
}
