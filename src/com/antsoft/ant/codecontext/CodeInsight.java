/*
 * $Header: /AntIDE/source/ant/codecontext/CodeInsight.java 57    99-06-21 3:57p Kahn $
 * Ant ( JDK wrapper Java IDE )
 * Version 1.0
 * Copyright (c) 1998-1999 Antsoft Co. All rights reserved.
 *  This program and source file is protected by Korea and international
 * Copyright laws.
 *
 * $Revision: 57 $
 * Part : the Intelisense Thread Class.
 * Author: Kim, Sung-hoon(kahn@antj.com)
 * dated 1999. 3. 3.
 */

package com.antsoft.ant.codecontext;

import com.antsoft.ant.codecontext.codeeditor.*;
import com.antsoft.ant.pool.classpool.*;
import com.antsoft.ant.main.*;
import com.antsoft.ant.manager.projectmanager.*;
import com.antsoft.ant.util.JavaDocViewer;
import com.antsoft.ant.util.QuickSorter;
import com.antsoft.ant.property.JdkInfo;
//import com.antsoft.ant.util.QSort;

import java.io.*;
import java.util.*;
import java.lang.reflect.Array;

/**
  @author Kim, Sung-Hoon.
  */
public class CodeInsight implements Runnable {
  private static String cachedType=null;
  private String theString=null;	// the string for parsing.
  private int theOffset;

  private boolean isStatic=false;

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// set the Code Insight delay time.
	public static int timeOut=Main.property.getIntellisenseDelay();

 	private Vector getSortedList(Vector v) {
    return QuickSorter.sort(v, QuickSorter.LESS_STRING);
 	}
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // intellisense methods.

 	synchronized private void activateIntellisense() {
    GetTokenForEvent maker=new GetTokenForEvent(theString);
    String prevToken=maker.getPrevTokenOfDot();
    if (prevToken==null) return;
    if (!Character.isJavaIdentifierStart(prevToken.charAt(0))) return;

    //System.out.println(" prev token is "+prevToken);

    EditFunctionEvent e=new EditFunctionEvent();
    e.setEventType(EditFunctionEvent.INTELLISENSE);

		// previous token을 evaluation하여 type을 생산하고 해당 type의 event를 return한다.
    Vector event=getEvent(prevToken,theOffset);
    if (event==null) return;
    Vector newEvent = new Vector();
    for (int i=0;i<event.size();++i) {
      SortingData data = new SortingData((String)event.elementAt(i));
      newEvent.addElement(data);
    }

    event=getSortedList(newEvent);
    //event = QSort.sort(event,true);

    // adding the events.
    for (int i=0;i<event.size();++i) {
			String s=((SortingData)event.elementAt(i)).getData();;
      StringTokenizer scanner = new StringTokenizer(s,":",false);
      String s1 = scanner.nextToken();
      String s2 = scanner.nextToken();
      String s3 = null;
      if (scanner.hasMoreTokens()) s3=scanner.nextToken();
      else s3="";
			e.addEvent(s2+"    "+s1+" : "+s3);
    }

   	try {
   		wait(timeOut);
   	} catch (InterruptedException except) {
  		System.out.println("in intellisenseAction, Exception Occurred :"+except.toString());
   	}

		// if another key is pressed before showing popup window, give up code insight.
   	if (keyPressed) return;

    // event를 만들어서 listener에게 보내준다.
 		//for (int i=0;i<editFunctionEventListener.size();++i) {
    //EditFunctionEventListener l=(EditFunctionEventListener)editFunctionEventListener.elementAt(i);
    //if (e!=null) l.showEventList(e);
    //}
    if (e!=null) editFunctionEventListener.showEventList(e);
  }

	private Vector getEvent(String tokenToBeEvaluated,int offset) {
		String evaluatedType=tokenizingAndEvaluating(tokenToBeEvaluated,offset);
		if (evaluatedType==null) return null;

    cachedType=evaluatedType; // caching the previous code insight class full type name.
		return makeEvent(evaluatedType);
	}

	private int count=0;	// the subscription count.

	private String tokenizingAndEvaluating(String origin,int offset) {
		String type=null;
    //System.out.println(" origin is "+ origin);

		do {
			// token to be made by tokenizer.
			String[] result=tokenizing(origin,'.',offset);
      if (result==null) return null;

			String token=result[0];
			origin=result[1];

      //System.out.println(" token "+token+" origin "+origin);

			// for array variable
			count=0;
			for (int i=0;i<token.length();++i) if (token.charAt(i)=='[') count++;
			if (count!=0) token=token.substring(0,token.indexOf("["));

			// converted token by parameter to type converter.
			token=paramToType(token,offset);
      if (token==null) return null;

      //System.out.println(" token is ++++>"+token);
			// finally, evaluated type remains.
			if (type==null) type=evaluating(token,offset);
			else type=evaluating(type,token);
			if (type==null) return null;

      //System.out.println("type is "+type);

			if (type.charAt(0)=='[') {
				if (count==0) type="array";
				else {
					if (type.length()>2) type=type.substring(2,type.length()-1);
					else return null;
				}
			}
		} while (origin!=null);
		return type;
	}

	private String paramToType(String src,int offset) {
		int pos=src.indexOf("(");
		if (pos==-1) return src.toString();	// it is not method, type or variable.

		String body=src.substring(0,pos);
		String parameter=src.substring(pos+1,src.length()-1);

		if (parameter==null||parameter.length()==0)	return src.toString();	// this is method, but has no parameter.

		// method and has a or some parameter(s).
		StringBuffer buf=new StringBuffer();
		String token=null;
		do {
			String[] result=tokenizing(parameter,',',offset);
			token=result[0];
			parameter=result[1];

			String type=tokenizingAndEvaluating(token,offset);
      if (type==null) return null;
   		if (type.lastIndexOf(".")!=-1) type=type.substring(type.lastIndexOf(".")+1,type.length());
			buf.append(type+",");
		} while (parameter!=null);

		return body+"("+buf.toString().substring(0,buf.length()-1)+")";
	}

	private String[] tokenizing(String src,char delim,int offset) {
		// "this" keyword is replaced to the original class name.
		if (src.indexOf("this")!=-1) {
			SymbolTableIterator it=new SymbolTableIterator(symtab);
			String temp=it.getThisClassName(offset);
			if (temp==null) return null;

			// replace "this" to the class name.
			int x=src.indexOf("this");
			int y=x+4;
			int z=src.length();
			StringBuffer buf=new StringBuffer();
			if (x!=0) buf.append(src.substring(0,x));
			buf.append(temp);
			if (y<z) buf.append(src.substring(y,z));
			src=buf.toString();
		}

		int depth=0;
		String[] result=new String[2];

 		int i=0;
  	for (;i<src.length();++i) {
  	 	char ch=src.charAt(i);
	  	if (ch=='(') depth++;
  		else if (ch==')') depth--;
  		else if (ch==delim&&depth==0) break;
	 	}

		if (i==src.length()) {	// if the src string has no DOT.
			result[0]=src.toString();
			result[1]=null;

			return result;
		}

		result[0]=src.substring(0,i);
		result[1]=src.substring(i+1,src.length());

		return result;
	}

	private String evaluating(String value,int offset) {
    String typeOfToken=null;
    if (value.equals("super")) {
			SymbolTableIterator it=new SymbolTableIterator(symtab);
			value=it.getThisClassName(offset);
      SymbolTableEntry entry=(SymbolTableEntry)symtab.get(value);
      typeOfToken=entry.getSuperClass();
    }
		else {// Symbol table에서 찾아본다.
  		SymbolTableIterator iter=new SymbolTableIterator(symtab);
	  	typeOfToken=iter.search(value,offset);
      //System.out.println(" type of token is "+typeOfToken);
		  if (typeOfToken==null) {
        String upper = null;
   			SymbolTableIterator it=new SymbolTableIterator(symtab);
    		String temp=it.getThisClassName(offset);
        SymbolTableEntry entry=(SymbolTableEntry)symtab.get(temp);
        upper = entry.getSuperClass();
        typeOfToken = evaluating(upper,value);
        if (typeOfToken==null) {
          typeOfToken=value;
          isStatic=true;
        }
      }
      else isStatic=false;
    }

    // for array type
		int cnt=0;
		for (int i=0;i<typeOfToken.length();++i) if (typeOfToken.charAt(i)=='[') cnt++;
		if ((cnt-count)>0) { return "array"; }

		if (cnt>0) typeOfToken=typeOfToken.substring(0,typeOfToken.indexOf("["));

		String result=searchFullClassName(typeOfToken);
    //System.out.println(" result ===>"+result);
		if (result!=null) return result;
		else {
			if (typeOfToken.indexOf("int")!=-1) return typeOfToken;
			if (typeOfToken.indexOf("char")!=-1) return typeOfToken;
			if (typeOfToken.indexOf("short")!=-1) return typeOfToken;
			if (typeOfToken.indexOf("long")!=-1) return typeOfToken;
			if (typeOfToken.indexOf("boolean")!=-1) return typeOfToken;
			if (typeOfToken.indexOf("float")!=-1) return typeOfToken;
			if (typeOfToken.indexOf("double")!=-1) return typeOfToken;
		}

		return null;
	}

	private String searchInProject(String type) {
    Enumeration e = imptab.keys();
    ClassMemberContainer cmc;
    String packname;
    while (e!=null&&e.hasMoreElements()) {
      packname=(String)e.nextElement();
      int pos=packname.lastIndexOf("*");
      if (pos!=-1) packname=packname.substring(0,pos)+type;
      else {
				int lastDot=packname.lastIndexOf(".");
				if (!type.equals(packname.substring(lastDot+1,packname.length()))) continue;
			}
      //System.out.println("package name is "+packname);
  		// package에서 현재 type이 존재하는지 검사.
	  	cmc=(ClassMemberContainer)containers.getClassMemberList(packname);
		  if (cmc!=null) return packname;

      String filename = (String)classtable.get(packname);
      //System.out.println("filename ==> "+filename);
      if (filename!=null) {
        File f = new File(filename);
        codeContext.doParsing(f);
        containers=codeContext.getClassMemberContainerList();
        symtabs=codeContext.getSymTabs();
        //return searchInProject(type);
        return packname;
      }
    }

    packname=nameOfPackage+"."+type;
  	cmc=(ClassMemberContainer)containers.getClassMemberList(packname);
    if (cmc!=null) return packname;

    String file = (String)classtable.get(packname);
    if (file!=null) {
      File f = new File(file);
      codeContext.doParsing(f);
      containers=codeContext.getClassMemberContainerList();
      symtabs=codeContext.getSymTabs();
      //return searchInProject(type);
      return packname;
    }

    return null;
	}

	private String searchFullClassName(String type) {
		// search in currently open project.
		String string=searchInProject(type);
    //System.out.println("string is "+string);
		if (string!=null) return string;

		// search in JDK api.
		return searchInJDK(type);
	}

	public String searchInJDK(String type) {
    if (type==null) return null;
    //System.out.println(" search in jdk, type ==>"+type);

		// package에 없으면 import list의 것을 차례로 검사한다.
		Enumeration enu=imptab.keys();
		while (enu!=null&&enu.hasMoreElements()) {
			String impString=(String)enu.nextElement();
      //System.out.println(" import string .. "+impString);
      //System.out.println(" package name "+nameOfPackage);

			if (impString.equals(nameOfPackage)) continue;
      String fullClassName=null;
      int pos=impString.lastIndexOf("*");
      if (pos!=-1) fullClassName=impString.substring(0,pos)+type;
      else {
				int lastDot=impString.lastIndexOf(".");
				if (!type.equals(impString.substring(lastDot+1,impString.length()))) continue;
       	fullClassName=impString;
			}

      //System.out.println(" full class name ==> "+fullClassName);
      if (ClassPool.exist(fullClassName)) return fullClassName;
		}

    // the java.lang package is imported with default.
    if (ClassPool.exist("java.lang."+type)) return "java.lang."+type;
		if (type.indexOf(".")!=-1) return searchFullClassName(type.substring(type.indexOf(".")+1,type.length()));
		return null;
	}

	private String evaluating(String type,String token) {
    isStatic=false;
		int flag=type.indexOf(".");
		if (flag==-1) type=searchFullClassName(type);

		Vector bundle;

  	ClassMemberContainer cmc=(ClassMemberContainer)containers.getClassMemberList(type);
    if (cmc!=null) {
			bundle=cmc.getContainer();

			for (int i=0;i<bundle.size();++i) {
				ClassMember member=(ClassMember)bundle.elementAt(i);
				if (token.equals(member.getFullName())) return member.getType();
			}

      String name=type.substring(type.lastIndexOf(".")+1,type.length());
      Enumeration e = symtabs.elements();
      while (e!=null&&e.hasMoreElements()) {
        Hashtable t=(Hashtable)e.nextElement();
        SymbolTableEntry entry=(SymbolTableEntry)t.get(name);
        if (entry==null) return null;
        type=entry.getSuperClass();
        if (type!=null) return evaluating(type,token);
      }
		}
		else {
   	  if (token.indexOf("(")!=-1) {  // 최종의 type을 결정할 겻이, method의 형태일 때
			  bundle=classPool.getMethodSignatures(type);

			  for (int i=0;bundle!=null&&i<bundle.size();++i) {
				  SigModel m=(SigModel)bundle.elementAt(i);

          // formal parameter가 Object type일 경우, actual parameter가 무엇이든 match시켜 준다.
				  if (token.equals(m.toString())) return m.getTypeFullName();
        	  if (token.indexOf("(")==token.indexOf(")")-1) continue;
        	  String temp=token.substring(0,token.indexOf("("));
          	if (temp.equals(m.getName())) {
          		Class[] paramTypes=m.getParameterTypes();
          		for (int j=0;j<paramTypes.length;++j)
           			if (paramTypes[j].getName().equals("java.lang.Object"))	return m.getTypeFullName();
       	  	}
    		}
		  }
		  else {
 			  bundle=classPool.getFieldSignatures(type);

			  for (int i=0;bundle!=null&&i<bundle.size();++i) {
				  SigModel m=(SigModel)bundle.elementAt(i);
				  if (token.equals(m.toString())) return m.getTypeFullName();
			  }
		  }
		}	// outside else

		return null;
	}

	private Vector makeEvent(String type) {
		Vector result=new Vector();
    Hashtable table = new Hashtable();

		if (type.equals("array")) {
			result.addElement("length:Field:int");
			return result;
		}

		int flag=type.indexOf(".");
		if (flag==-1) type=searchFullClassName(type);
   	if (type==null) return null;

		Vector bundle;
		ClassMemberContainer cmc=(ClassMemberContainer)containers.getClassMemberList(type);
    if (cmc!=null) {
			bundle=cmc.getContainer();

			for (int i=0;bundle!=null&&i<bundle.size();++i) {
				ClassMember m=(ClassMember)bundle.elementAt(i);
				if (m.getMemberType()==ClassMember.METHOD)
					if (!result.contains(m.getSig())) result.addElement(m.getSig());
			}

			for (int i=0;bundle!=null&&i<bundle.size();++i) {
				ClassMember m=(ClassMember)bundle.elementAt(i);
				if (m.getMemberType()==ClassMember.FIELD)
					if (!result.contains(m.getSig())) result.addElement(m.getSig());
			}

			for (int i=0;bundle!=null&&i<bundle.size();++i) {
				ClassMember m=(ClassMember)bundle.elementAt(i);
				if (m.getMemberType()==ClassMember.INNERCLASS)
					if (!result.contains(m.getSig())) result.addElement(m.getSig());
			}

      String name=type.substring(type.lastIndexOf(".")+1,type.length());
      Enumeration e = symtabs.elements();
      while (e!=null&&e.hasMoreElements()) {
        Hashtable t=(Hashtable)e.nextElement();
        SymbolTableEntry entry=(SymbolTableEntry)t.get(name);
        if (entry==null) continue;
        type=entry.getSuperClass();
        if (type!=null) {
          Vector temp=makeEvent(type);
          if (temp!=null) {
            for (int i=0;i<temp.size();++i) 
              if (!result.contains((String)temp.elementAt(i))) result.addElement((String)temp.elementAt(i));
          }
        }
        break;
      }
		}
    // jdk에서 내용을 뽑을때
		else {
      if (isStatic) bundle=classPool.getStaticMethodSignatures(type);
			else bundle=classPool.getMethodSignatures(type);
			for (int i=0;bundle!=null&&i<bundle.size();++i) {
				SigModel m=(SigModel)bundle.elementAt(i);
        String msig=new String(m.getName()+":Method:"+m.getTypeShortName());
				if (!result.contains(msig)) result.addElement(msig);
			}

      if (isStatic) bundle=classPool.getStaticFieldSignatures(type);
			else bundle=classPool.getFieldSignatures(type);
			for (int i=0;bundle!=null&&i<bundle.size();++i) {
				SigModel m=(SigModel)bundle.elementAt(i);
        String msig=new String(m.getName()+":Member:"+m.getTypeShortName());
				if (!result.contains(msig)) result.addElement(msig);
			}
		}

		if (result.size()==0) return null;
		return result;
	}
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // parameterizing methods.

	synchronized private boolean activateParameterizing() {
    int offset=theOffset;

    GetTokenForEvent maker=new GetTokenForEvent(theString);
    String prevToken=maker.getPrevTokenOfLeftParen();
    if (prevToken==null) return false;

    if (!Character.isJavaIdentifierStart(prevToken.charAt(0))) return false;

		Vector eventString=null;

		String methodName,sub;
		String type;
		if (prevToken.lastIndexOf(".")!=-1) {
			methodName=prevToken.substring(prevToken.lastIndexOf(".")+1,prevToken.length());
			sub=prevToken.substring(0,prevToken.lastIndexOf("."));

      // cached type을 생각해보자
      if (cachedType==null) type=tokenizingAndEvaluating(sub,offset);
      else type=cachedType;

      if(type != null) eventString=makeEventForParam(type,methodName);
		}
		else {        // 거의가 Constructor의 parameterizing이므로 super에 대해서는 생각안함.
			// "this" keyword is replaced to the original class name.
			if (prevToken.equals("this")) {
				SymbolTableIterator it=new SymbolTableIterator(symtab);
				prevToken=it.getThisClassName(offset);
				if (prevToken==null) return false;
			}

      if (prevToken.equals("super")) {
				SymbolTableIterator it=new SymbolTableIterator(symtab);
				prevToken=it.getThisClassName(offset);
        SymbolTableEntry entry=(SymbolTableEntry)symtab.get(prevToken);
        if (entry==null) return false;
        prevToken=entry.getSuperClass();
        if (prevToken==null) return false;
      }

			SymbolTableIterator iter=new SymbolTableIterator(symtab);
			eventString=iter.searchAllOverloadedMethod(prevToken,offset);

			if (eventString==null) {  // not in project, so will find it in the library.
				String typeOfMethod=searchFullClassName(prevToken);
				if (typeOfMethod!=null) {
          boolean found = false;
          if (Main.isParamLoadEnd&&Main.paramHash.size()!=0) {
            Object o=Main.paramHash.get(typeOfMethod+"."+prevToken);
            if (o!=null) {
              if (o instanceof String) {
                if (eventString==null) eventString=new Vector();
                eventString.addElement((String)o);
              }
              else {
                eventString=(Vector)o;
              }

              found = true;
            }
            else found = false;
          }

          if (!found) {
            Vector bundle=null;
            bundle=classPool.getConstructorSignatures(typeOfMethod);

            for (int i=0;bundle!=null&&i<bundle.size();++i) {
              SigModel m=(SigModel)bundle.elementAt(i);
              String mstr=m.getName();
              if (prevToken.equals(mstr.substring(mstr.lastIndexOf(".")+1,mstr.length()))) {
                if (eventString==null) eventString = new Vector();
                String param=m.toString();
                int x=param.indexOf("(");
                int y=param.indexOf(")",x);
                if (y==x+1) eventString.addElement("<no parameter>");
                else {
                  param=param.substring(x+1,y);
                  StringBuffer buff=new StringBuffer();
                  StringTokenizer st=new StringTokenizer(param,",");
                  int count=0;
                  while (st!=null&&st.hasMoreTokens()) {
                    buff.append((String)st.nextToken());
                    buff.append(" x"+count+",");
                    count++;
                  }
                  param=buff.toString();
                  eventString.addElement(param.substring(0,param.length()-1));
                }
              }
		  		  }
				  }
      	}
      }
		} // else {

		if (eventString==null) return false;

    EditFunctionEvent e=new EditFunctionEvent();
    e.setEventType(EditFunctionEvent.PARAMETERING);

    // adding the events.
    for (int i=0;i<eventString.size();++i) {
			String s=(String)eventString.elementAt(i);
			e.addEvent(s);
    }

    // event를 만들어서 listener에게 보내준다.
    //for (int i=0;i<editFunctionEventListener.size();++i) {
      //EditFunctionEventListener l=(EditFunctionEventListener)editFunctionEventListener.elementAt(i);
      //if (e!=null) l.showEventList(e);
    //}
    if (e!=null) editFunctionEventListener.showEventList(e);

    cachedType=null;
    return true;
  }


  private Vector makeEventForParam(String type,String methodName) {
    Vector eventString = null;
    int flag = type.indexOf(".");
    if (flag == -1) type = searchFullClassName(type);
    if (type == null) return null;

    Vector bundle;
    ClassMemberContainer cmc = (ClassMemberContainer)containers.getClassMemberList(type);
    if (cmc != null) {
      bundle=cmc.getContainer();

      for (int i=0;bundle!=null&&i<bundle.size();++i) {
        ClassMember m=(ClassMember)bundle.elementAt(i);
        if (m.getMemberType()==ClassMember.METHOD) {
          if (methodName.equals(m.getName())) {
            if (eventString==null) eventString=new Vector();

            if (m.getParamsAsString()==null) eventString.addElement("<no parameter>");
            else eventString.addElement(m.getParamsAsString());
          }
        }
      }

      String name=type.substring(type.lastIndexOf(".")+1,type.length());
      Enumeration e = symtabs.elements();
      while (e!=null&&e.hasMoreElements()) {
        Hashtable t = (Hashtable)e.nextElement();
        SymbolTableEntry entry=(SymbolTableEntry)t.get(name);
        if (entry!=null) {
          type=entry.getSuperClass();
          if (type!=null) {
            Vector temp=makeEventForParam(type,methodName);
            if (temp!=null) {
              if (eventString==null) eventString=new Vector();
              for (int i=0;i<temp.size();++i)
                if (!eventString.contains((String)temp.elementAt(i))) eventString.addElement((String)temp.elementAt(i));
            }
          }
          break;
        }  // if (entry!=null)
      }
    }
    else {  // in jdk library
      boolean Found=false;
      String clsType=type;

      if(Main.isParamLoadEnd&&Main.paramHash.size()!=0) {
        while (!Found) {
          Object o=Main.paramHash.get(clsType+"."+methodName);
          if (o!=null) {
            if (o instanceof String) {
              if (eventString==null) eventString=new Vector();
              eventString.addElement((String)o);
            }
            else {
              eventString=(Vector)o;
            }
            Found=true;
            break;
          }
          if (clsType.equals("java.lang.Object")) break;
          clsType=classPool.getSuperClass(clsType);
        }
      }

      if (!Found) {
        bundle=classPool.getMethodSignatures(type);
        for (int i=0;bundle!=null&&i<bundle.size();++i) {
          SigModel m=(SigModel)bundle.elementAt(i);
          if (methodName.equals(m.getName())) {
            if (eventString==null) eventString=new Vector();

            String param=m.toString();
            int x=param.indexOf("(");
            int y=param.indexOf(")",x);
            if (y==x+1) eventString.addElement("<no parameter>");
            else {
              param=param.substring(x+1,y);
              StringBuffer buff=new StringBuffer();
              StringTokenizer st=new StringTokenizer(param,",");
              int count=0;
              while (st!=null&&st.hasMoreTokens()) {
                buff.append((String)st.nextToken());
                buff.append(" x"+count+",");
                count++;
              }
              param=buff.toString();
              eventString.addElement(param.substring(0,param.length()-1));
            }
          }
        }
        bundle=classPool.getConstructorSignatures(type);
        for (int i=0;bundle!=null&&i<bundle.size();++i) {
          SigModel m=(SigModel)bundle.elementAt(i);
          if (methodName.equals(m.getName())) {
            if (eventString==null) eventString=new Vector();

            String param=m.toString();
            int x=param.indexOf("(");
            int y=param.indexOf(")",x);
            if (y==x+1) eventString.addElement("<no parameter>");
            else {
              param=param.substring(x+1,y);
              StringBuffer buff=new StringBuffer();
              StringTokenizer st=new StringTokenizer(param,",");
              int count=0;
              while (st!=null&&st.hasMoreTokens()) {
                buff.append((String)st.nextToken());
                buff.append(" x"+count+",");
                count++;
              }
              param=buff.toString();
              eventString.addElement(param.substring(0,param.length()-1));
            }
          }
        }
      }  // if (!found);
    } // outer else

    return eventString;
  }
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // constructor and thread running method.

  /**
    Constructor.
    */
  public CodeInsight(String str,int offset,CodeContext cc,int what) {
    theString=str;
    theOffset=offset;
    actionType=what;
    codeContext=cc;

    editFunctionEventListener=codeContext.getEditFunctionEventListener();

    symtab=codeContext.getSymTab();
    imptab=codeContext.getImpTab();
    symtabs=codeContext.getSymTabs();

    nameOfPackage=codeContext.getNameOfPackage();
    classPool=codeContext.classPool;
    containers=codeContext.getClassMemberContainerList();

    classtable=codeContext.getClassTable();
  }

  private Hashtable classtable=null;
  private EditFunctionEventListener editFunctionEventListener;
  private Hashtable symtab=null;
  private Hashtable imptab=null;
  private Hashtable symtabs=null;
  private ClassPool classPool;
  private ClassMemberContainerList containers;

  private String nameOfPackage;
  private CodeContext codeContext;

  int actionType;

	/**
	  the core executable code for Code Insight thread.
    */
 	public void run() {
  	if (theString==null) {
	  	activateThisIntellisense();
		  return;
	  }

    switch (actionType) {
   	  case 1: activateIntellisense(); break;
   	  case 2:
        if (!activateParameterizing()) {
          if (cachedType!=null) {
            cachedType=null;
            activateParameterizing();
          }
        }
        break;
   	  case 3: activateSensitiveHelper(); break;
    }
  }

  boolean keyPressed=false;
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // util method.

  /**
   	setting the key pressed flag after pressing dot or left parenthesis.

   	@param the key pressed flag.
   	*/
  public void setKeyPressed(boolean flag) {
   	keyPressed=flag;
  }
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // cotext sensitive help

  //private member
  private static JavaDocViewer docViewer = null;

	private void activateSensitiveHelper() {
		String name=searchInJDK(theString);
		if (name==null) return;

		// full path name of the html help(document) file.
    JdkInfo jdkInfo = ProjectManager.getCurrentProject().getPathModel().getCurrentJdkInfo();
    if (jdkInfo == null) return;
		String docRootDir=jdkInfo.getDocPath();
		if (docRootDir==null) return;
		String exeArgv=docRootDir+File.separator+name+".html";

		// check that the html help file exist actually.
		File f=new File(exeArgv);
		if (!f.exists()) {
			name=name.replace('.',File.separatorChar);

			// full path name of the html help(document) file.
			exeArgv=docRootDir+File.separator+name+".html";

			if (exeArgv==null) return;

 			// check that the html help file exist actually.
  		f=new File(exeArgv);
			if (!f.exists()) return;
		}

    /** 내부 JavaDoc Viewer를 사용한다면 */
    if(Main.property.isInternalHelpViewerUse()){
      final String name2 = name;
      final String docRootDir2 = docRootDir;

        javax.swing.SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          if(docViewer == null){
            docViewer = JavaDocViewer.createJavaDocViewer(docRootDir2, name2+".html");
            docViewer.setBounds(20, 20, 800, 600);
            docViewer.setVisible(true);
          }
          else{
            docViewer.setDocFile(docRootDir2, name2+".html");
            docViewer.setVisible(true);
          }}
        });
    }
    /** 일반 웹브라우저를 사용한다면 */
    else{
  		// full path name of the web browser(i.e. iexplore.exe ...)
	  	String exeFile=Main.property.getWebBrowserPath();
		  if (exeFile==null) return;

  		String[] command=new String[2];
	  	command[0]=exeFile;
  		command[1]=exeArgv;

      try {
          Runtime runtime=Runtime.getRuntime();
		      if (runtime!=null) runtime.exec(command);
      } catch (IOException e) {}
  	}
  }
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // this intellisense method.

  // Ctrl+Space를 눌렀을 경우, this의 Code Insight와 같은 행동을 한다.
	private void activateThisIntellisense() {
    // intellisense의 event를 만들고 action을 취한다.
    EditFunctionEvent e=new EditFunctionEvent();
    e.setEventType(EditFunctionEvent.INTELLISENSE);

    // previous token을 evaluation하여 type을 생산하고 해당 type의 event를 return한다.
    Vector event=getEvent("this",theOffset);

    SymbolTableIterator sIterator=new SymbolTableIterator(symtab);
    Vector localVar=sIterator.getLocalVarEvent(theOffset);
    if (event == null && localVar == null) return;

    if (event == null) event = localVar;
    else if (localVar != null) {
      for (int i=0; i<localVar.size(); ++i) event.addElement(localVar.elementAt(i));
    }

    Vector newEvent = new Vector();
    for (int i=0;i<event.size();++i) {
      SortingData data = new SortingData((String)event.elementAt(i));
      newEvent.addElement(data);
    }
    event = getSortedList(newEvent);

    // adding the events(Field and Method)
    for (int i=0;i<event.size();++i) {
      String s=((SortingData)event.elementAt(i)).getData();
      StringTokenizer scanner = new StringTokenizer(s,":",false);
      String s1 = scanner.nextToken();
      String s2 = scanner.nextToken();
      String s3 = null;
      if (scanner.hasMoreTokens()) s3=scanner.nextToken();
      else s3="";
			e.addEvent(s2+"    "+s1+" : "+s3);
			//System.out.println(s2+"    "+s1+" : "+s3);
    }

    // event를 만들어서 listener에게 보내준다.
 		//for (int i=0;i<editFunctionEventListener.size();++i) {
      //EditFunctionEventListener l=(EditFunctionEventListener)editFunctionEventListener.elementAt(i);
      //if (e!=null) l.showEventList(e);
    //}
    if (e!=null) editFunctionEventListener.showEventList(e);
  }
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
