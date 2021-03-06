/*
 * $Header: /usr/cvsroot/AntIDE/source/com/antsoft/ant/designer/methoddesigner/DiagramItemWhile.java,v 1.3 1999/07/22 02:58:38 multipia Exp $
 * Ant ( JDK wrapper Java IDE )
 * Version 1.0
 * Copyright (c) 1998-1999 Antsoft Co. All rights reserved.
 *  This program and source file is protected by Korea and international
 * Copyright laws.
 *
 * $Revision: 1.3 $
 * $History: DiagramItemWhile.java $
 * 
 * *****************  Version 2  *****************
 * User: Multipia     Date: 99-05-16   Time: 11:47p
 * Updated in $/AntIDE/source/ant/designer/methoddesigner
 * 
 * *****************  Version 1  *****************
 * User: Flood        Date: 98-09-19   Time: 2:22p
 * Created in $/JavaProjects/src/ant/designer/methoddesigner
 */


package ant.designer.methoddesigner;


/**
 *
 *  while ( Method Designer )
 *
 */


class DiagramItemWhile extends DiagramItem
{
  DiagramItemSeq block = new DiagramItemSeq(this);

  /**
   *
   *  caculate bound
   *
   */

  public void calcBound()
  {
    bound = (DiagramBound) block.clone();

    bound.incLWidth(DiagramItem.UNIT_LWIDTH);
    bound.incRWidth(DiagramItem.UNIT_LWIDTH);
    bound.incHeight(DiagramItem.UNIT_LHEIGHT * 5 + UNIT_HEIGHT);
  }
}


