package test;

import test.Module1.client.*;
import javax.swing.UIManager;
import java.awt.*;

/**
* Template File
*   ClientApp.java.template
* IDL Object
*   Module1
* Generation Date
*   1999년 7월 28일 수요일 04시24분48초 오후
* IDL Source File
*   C:/WORK/ant/source/test/untitled1.idl
* Abstract
*   CORBA client application.
*/

public class Module1ClientApp {

  private boolean packFrame = false;

  private static org.omg.CORBA.ORB orb;

  public static org.omg.CORBA.ORB getORB() {
    return orb;
  }

  public Module1ClientApp(String[] args) {
    //(debug support)System.getProperties().put("ORBdebug", "true");
    //(debug support)System.getProperties().put("ORBwarn", "2");
    System.getProperties().put("ORBagentPort", "14000");
    orb = org.omg.CORBA.ORB.init(args, System.getProperties());

    ClientFrame frame = new ClientFrame();
    if (packFrame)
      frame.pack();
    else
      frame.validate();

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    try  {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
      //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    }
    catch (Exception e) {
    }

    new Module1ClientApp(args);
  }
}


