/*
 * $Id: AppletRunner.java,v 1.4 1999/08/25 03:26:33 multipia Exp $
 * Ant ( JDK wrapper Java IDE )
 * Version 1.0
 * Copyright (c) 1998-1999 Antsoft Co. All rights reserved.
 *  This program and source file is protected by Korea and international
 * Copyright laws.
 *
 * $Revision: 1.4 $
 */
package com.antsoft.ant.runner;

import java.io.*;
import java.lang.*;
import java.util.*;

import com.antsoft.ant.manager.projectmanager.*;
import com.antsoft.ant.property.*;

/**
 *  Applet Runner를 실행시켜 주는 Class
 *  TODO: 1. stderr로 출력되는 메시지가 표시되도록 수정해야 한다.
 *    
 */
public class AppletRunner implements Runnable {
   private String appletViewerEXE;
   private Process p = null;
   private String pathName, fileName;
   private char drive;

   private Project project = null;
   private Output output = null;

   private FileOutputStream out;
   File file;

   public AppletRunner(Project project) {

      this.project = project;

   }

   public void appletRun(String pathName, String fileName, Output output) {
      this.output = output;
      this.pathName = pathName;

      JdkInfo info = project.getPathModel().getCurrentJdkInfo();
      drive = pathName.charAt(0);
      System.out.println(drive);

      if(info != null) {
         appletViewerEXE = info.getAppletviewerEXEPath();
         this.fileName = fileName;
      }
      output.appendText(appletViewerEXE + " " + pathName + "\\" + fileName + "\n");

      Thread t = new Thread (this);
      t.start();
   }
   
   void write( String source )  {
		try{
			out.write( source.getBytes() );
		} catch( IOException e ) {
			System.out.println( "Exception occurred.." + e.toString() );
		}
	}

   public void run() {
      try {
         file = new File(pathName + File.separator + "runApplet.bat");

         try {
            out = new FileOutputStream(file);
         } catch(IOException e) {
            System.err.println(e);
         }

         write("@echo off\r\n");
         write("c:\r\n");
         write(drive + ":\r\n");
         write("cd " + pathName.substring(2) + "\r\n");
         write(appletViewerEXE + " " + fileName + "\r\n");
         out.close();

         String command = pathName + File.separator + "runApplet.bat";

         System.out.println(command);

         Runtime rt = Runtime.getRuntime();
         p = rt.exec(command);

        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(p.getInputStream()));
				String outStr = null;

			while((outStr = lnr.readLine()) != null){
				output.appendText(outStr);
			}
			lnr.close();

      file.delete(); // 잠시 batch 파일을 만들어 둔것을 삭제한다.. 완전범죄..
      if (p != null) p.destroy();
         System.out.println("called");
      } catch(IOException e) {
         System.out.println("Exception in PE : " + e);
      }
   }
}
/*
 * $Log: AppletRunner.java,v $
 * Revision 1.4  1999/08/25 03:26:33  multipia
 * 메시지를 Output에 출력하도록 수정
 *
 */
