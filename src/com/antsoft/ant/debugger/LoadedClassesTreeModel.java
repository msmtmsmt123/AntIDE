/*
 * Ant ( JDK wrapper Java IDE )
 * Version 1.0
 * Copyright (c) 1998-1999 Antsoft Co. All rights reserved.
 *  This program and source file is protected by Korea and international
 * Copyright laws.
 *
 * Author:       Kwon, Young Mo
 * $Header: /AntIDE/source/ant/debugger/LoadedClassesTreeModel.java 3     99-06-18 1:56a Bezant $
 * $Revision: 3 $
 * $History: LoadedClassesTreeModel.java $
 * 
 * *****************  Version 3  *****************
 * User: Bezant       Date: 99-06-18   Time: 1:56a
 * Updated in $/AntIDE/source/ant/debugger
 * ���� ���α׷��� ����Ǿ����� reset�����ִ�
 * ��ƾ�� �־����ϴ�.
 * 
 * *****************  Version 2  *****************
 * User: Bezant       Date: 99-06-13   Time: 10:28p
 * Updated in $/AntIDE/source/ant/debugger
 * nd pnl...
 * 
 * *****************  Version 1  *****************
 * User: Multipia     Date: 99-06-02   Time: 10:46p
 * Created in $/AntIDE/source/ant/debugger
 * Ant Debugger Class
 */
package com.antsoft.ant.debugger;

import java.util.StringTokenizer;
import javax.swing.tree.*;

/**
 *
 */
class LoadedClassesTreeModel extends DefaultTreeModel {
  public LoadedClassesTreeModel() {
    super(new DefaultMutableTreeNode("loaded classes"));
  }
  public final void reset(){
    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)getRoot();
    rootNode.removeAllChildren();
    reload();
  }

	/**
	 * Class �̸��� Ʈ�� �𵨿� �߰��Ѵ�.
	 * @param fullClassName Package �̸��� ������ full class �̸��� �����Ͽ��� ��.
	 *
	 */
	public void addClass( String fullClassName ) {
		//TODO: 1. package name�� class name�� ����.
		//      2. package name�� ������ MutableTreeNode reference�� ������.
		//      3. ������ MutableTreeNode reference�� parent�� �Ͽ�, reference��
		//        ������ �ε����� Class Name�� �߰�
		String packageName = null;
		String className = "";
		StringTokenizer t = new StringTokenizer(fullClassName, ".");
		className = t.nextToken();
		while ( t.hasMoreTokens() ) {
			if ( packageName == null )
				packageName = className;
			else
			  packageName = packageName + "." + className;

			className = t.nextToken();
		}

    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)getRoot();
		int childCount = rootNode.getChildCount();
		boolean packageFound = false;
		for ( int i = 0; i < childCount && packageFound != true; i++ ) {
			DefaultMutableTreeNode packageNode = (DefaultMutableTreeNode)rootNode.getChildAt(i);
			if ( packageNode.getUserObject().equals(packageName) ) {
			  // check the class is already loaded.
			  boolean classFound = false;
			  for ( int j = 0; j < packageNode.getChildCount(); j++ ) {
			    DefaultMutableTreeNode classNode = (DefaultMutableTreeNode)packageNode.getChildAt(j);
			  	if ( ((String)classNode.getUserObject()).equals(className) )
			  		classFound = true;
			  }

			  if ( !classFound )
			    insertNodeInto( new DefaultMutableTreeNode( className, false ),
                          packageNode, packageNode.getChildCount());
                          
				packageFound = true;
			}
		}		
		
		if ( packageFound == false ) {
			// ���ο� Package Node�� �����Ѵ�.
			DefaultMutableTreeNode newPackageNode = new DefaultMutableTreeNode( packageName, true );
			// ���ο� Class Node�� �����Ѵ�.
			DefaultMutableTreeNode newClassNode = new DefaultMutableTreeNode( className, false );
			// ������� ���ο� package�� root�� �߰�
			insertNodeInto( newPackageNode, rootNode, rootNode.getChildCount() );
			insertNodeInto( newClassNode, newPackageNode, newPackageNode.getChildCount() );
		}		
	}
}
