/*
 * $Header: /usr/cvsroot/AntIDE/source/com/antsoft/ant/manager/projectmanager/ProjectPanelTreeNode.java,v 1.3 1999/08/30 08:08:05 remember Exp $
 * Ant ( JDK wrapper Java IDE )
 * Version 1.0
 * Copyright (c) 1998-1999 Antsoft Co. All rights reserved.
 *  This program and source file is protected by Korea and international
 * Copyright laws.
 *
 * $Revision: 1.3 $
 * Project Panel�� ������ �߰��� �� �ֵ��� �ϱ� ���ؼ� Ʈ���� ��带 �����ϴ�
 * ���ο� ������Ʈ�� �ʿ伺�� �ִ�.
 * �ϴ� ������ ������ ������ �� �ʿ䰡 �ִ�.
 */

package com.antsoft.ant.manager.projectmanager;

import java.io.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *  class ProjectPanelTreeEntry
 *
 *  @author Jinwoo Baek
 */
public class ProjectPanelTreeNode extends DefaultMutableTreeNode implements Serializable {
	/** ����� Ÿ���� ��Ÿ���� ������ */
	static final int FOLDER = 0;
	static final int FILE   = 1;               
  private boolean isOpened = false;

	/** ��尡 ������ ��ü(ProjectFileEntry �Ǵ� String) */
	private Object obj = null;

	/** ����� Ÿ�� */
	private int type = 0;
  private String id = "0";
  private String parent = "0";

	/**
	 *  Constructor
	 *
	 *  @param obj ProjectFileEntry �Ǵ� ���� �̸�
	 *  @param type ��� Ÿ��
	 */
	public ProjectPanelTreeNode(Object obj) {
		super(obj);
		if (obj instanceof String) {
			this.obj = obj;
			this.type = FOLDER;
		}
		else if (obj instanceof ProjectFileEntry) {
			this.obj = obj;
			this.type = FILE;
		}
	}

  public ProjectPanelTreeNode(Object obj, String id, String pid){
    this(obj);
    this.id = id;
    this.parent = pid;
  }

  public void setOpened(boolean flag){
    isOpened = flag;
  }

  public boolean isOpened(){
    return isOpened;
  }

	/**
	 *  �� ����� Ÿ���� ��´�.
	 *
	 *  @return int �� ����� Ÿ��
	 */
	int getType() {
		return type;
	}

	/**
	 *  �� ��尡 �������� Ȯ���Ѵ�.
	 *
	 *  @return boolean ����尡 �����̸� true �ƴϸ� false
	 */
	boolean isFolder() {
		if (type == FOLDER) return true;
		else return false;
	}

	/**
	 *  �� ��尡 �������� Ȯ���Ѵ�.
	 *
	 *  @return boolean �� ��尡 �����̸� true �ƴϸ� false
	 */
	boolean isFile() {
		if (type == FILE) return true;
		else return false;
	}

  public String getID() {
  	return id;
  }

  public void setID(String id) {
  	this.id = id;
  }

  public String getParentID() {
  	return parent;
  }

  public void setParentID(String parent) {
  	this.parent = parent;
  }

	/**
	 *  �� ��尡 �����ϰ� �ִ� ��ü�� ��ȯ�Ѵ�.
	 *
	 *  @return Object �� ��尡 �����ϰ� �ִ� ��ü
	 */
	Object getObject() {
		return obj;
	}

	/**
	 *  �����ϴ� ��ü�� �����Ѵ�.
	 *
	 *  @param Object ������ ��ü
	 */
	void setObject(Object obj) {
		if (obj != null) {
			if (obj instanceof String) {
				this.type = FOLDER;
				this.obj = obj;
			}
			else if (obj instanceof ProjectFileEntry) {
				this.type = FILE;
				this.obj = obj;
			}
		}
	}

  public Object getUseObject() {
  	return obj;
  }

  public boolean equals(Object object) {
  	if (object != null) {
    	if (object instanceof ProjectPanelTreeNode) {
      	ProjectPanelTreeNode pptn = (ProjectPanelTreeNode)object;
      	if ((pptn.getType() == type) && pptn.getUserObject().equals(obj))
        	return true;
      }
    }
    return false;
  }

	/**
	 *  �� ��带 ��Ÿ���� String�� ��ȯ�Ѵ�.
	 *
	 *  @return String �� ��带 ��Ÿ���� String
	 */
	public String toString() {
		if (obj != null) {
			return obj.toString();
			//if (obj instanceof ProjectFileEntry) return ((ProjectFileEntry)obj).toString();
			//else return obj.toString();
		}
		else return null;
	}
}