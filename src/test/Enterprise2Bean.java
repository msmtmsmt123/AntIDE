//Title:        Ant Developer
//Version:      
//Copyright:    Copyright (c) 1999
//Author:       Kwon, Young Mo
//Company:      Antsoft Co.
//Description:  Ant Developer Project

package test;

import java.rmi.*;
import javax.ejb.*;

public class Enterprise2Bean implements SessionBean {
  private SessionContext sessionContext;

  public void ejbCreate() throws RemoteException, CreateException {
  }

  public void ejbActivate() throws RemoteException {
  }

  public void ejbPassivate() throws RemoteException {
  }

  public void ejbRemove() throws RemoteException {
  }

  public void setSessionContext(SessionContext context) throws RemoteException {
    sessionContext = context;
  }
}

