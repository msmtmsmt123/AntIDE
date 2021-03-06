/*
 * Ant ( JDK wrapper Java IDE )
 * Version 1.0
 * Copyright (c) 1998-1999 Antsoft Co. All rights reserved.
 *  This program and source file is protected by Korea and international
 * Copyright laws.
 *
 * $Header: /AntIDE/source/ant/codecontext/classdesigner/CDParamInfo.java 2     99-05-16 11:34p Multipia $
 * $Revision: 2 $
 * Part : class designer parameter information.
 * Author: Kim, Sung-hoon(kahn@antj.com)
 * Dated at 1999.1.21.22.
 */

package com.antsoft.ant.codecontext.classdesigner;

/**
  @author Kim, Sung-hoon.
  */
public class CDParamInfo {
	private String type;
	private String paramName;

	/**
	  setting the parameter type.

	  @param type the parameter type as the string value.
	  */
	public void setType(String type) {
		this.type=type;
	}

	/**
	  setting the parameter name.

	  @param name the parameter name as the string value.
	  */
	public void setParamName(String name) {
		paramName=name;
	}

	/**
	  getting the parameter type.

	  @return the parameter type as the string value.
	  */
	public String getType() {
		return type;
	}

	/**
	  getting the parameter name.

	  @return the parameter name as the string value.
	  */
	public String getParamName() {
		return paramName;
	}

	/**
	  returns the string representation of the object.

	  @return the string representation of this object.
	  */
	public String toString() {
		StringBuffer theCode=new StringBuffer();

		String type=getType();
		String name=getParamName();
		if (type!=null&&name!=null) 
			theCode.append(type+" "+name);

		return theCode.toString();
	}
}
