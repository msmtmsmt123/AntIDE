/*
 * MM JDBC Drivers for MySQL
 *
 * $Id: Statement.java,v 1.2 1998/08/25 00:53:48 mmatthew Exp $
 *
 * Copyright (C) 1998 Mark Matthews <mmatthew@worldserver.com>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * See the COPYING file located in the top-level-directory of
 * the archive of this library for complete text of license.
 *
 * Some portions:
 *
 * Copyright (c) 1996 Bradley McLean / Jeffrey Medeiros
 * Modifications Copyright (c) 1996/1997 Martin Rode
 * Copyright (c) 1997 Peter T Mount
 */

/**
 * A Statement object is used for executing a static SQL statement and
 * obtaining the results produced by it.
 *
 * <p>Only one ResultSet per Statement can be open at any point in time.
 * Therefore, if the reading of one ResultSet is interleaved with the
 * reading of another, each must have been generated by different
 * Statements.  All statement execute methods implicitly close a
 * statement's current ResultSet if an open one exists.
 *
 * @see java.sql.Statement
 * @see ResultSet
 */

package org.gjt.mm.mysql;

import java.sql.*;

public class Statement implements java.sql.Statement
{
  Connection Conn;                 // The connection who created us
  ResultSet Results = null;        // The current results
  java.sql.SQLWarning Warnings = null;      // The warnings chain.
  int timeout = 0;                 // The timeout for a query (not used)
  boolean escapeProcessing = true; // escape processing flag
  EscapeProcessor Escaper = null;  // The escape processor

  int max_field_size = MysqlIO.MAXBUF;
  int max_rows = MysqlDefs.MAX_ROWS;

  /**
   * Constructor for a Statement.  It simply sets the connection
   * that created us.
   *
   * @param c the Connection instantation that creates us
   */

  public Statement(Connection C)
  {
    Conn = C;
    Escaper = new EscapeProcessor();
  }

   /**
    * Execute a SQL statement that retruns a single ResultSet
    *
    * @param Sql typically a static SQL SELECT statement
    * @return a ResulSet that contains the data produced by the query
    * @exception java.sql.SQLException if a database access error occurs
    */

  public java.sql.ResultSet executeQuery(String Sql) throws java.sql.SQLException
  {
    if (Driver.debug)
      System.out.println(this + " executing query '" + Sql + "'");
      
    if (escapeProcessing) {
        Sql = Escaper.escapeSQL(Sql);
    }
       
    if (Results != null) {
	Results.close();
    }

    Results = Conn.execSQL(Sql);
    return Results;
  }

  /**
   * Execute a SQL INSERT, UPDATE or DELETE statement.  In addition
   * SQL statements that return nothing such as SQL DDL statements
   * can be executed
   *
   * Any IDs generated for AUTO_INCREMENT fields can be retrieved
   * by looking through the java.sql.SQLWarning chain of this statement
   * for warnings of the form "LAST_INSERTED_ID = 'some number', 
   * COMMAND = 'your sql'".
   *
   * @param Sql a SQL statement
   * @return either a row count, or 0 for SQL commands
   * @exception java.sql.SQLException if a database access error occurs
   */

  public int executeUpdate(String Sql) throws java.sql.SQLException
  {
    if (Driver.debug)
      System.out.println(this + " executing update '" + Sql + "'");

    if (escapeProcessing) {
        Sql = Escaper.escapeSQL(Sql);
    }
    
    ResultSet RS = Conn.execSQL(Sql);
    
    if (RS.reallyResult())
      throw new java.sql.SQLException("results returned");
    else {
    
      // Add the last id inserted for AUTO_INCREMENT fields to the
      // warning chain
      
	// int updateID = RS.getUpdateID();
      
	// if (updateID != -1) {
        // java.sql.SQLWarning NewWarning = new java.sql.SQLWarning("LAST_INSERT_ID = " + updateID + ", COMMAND = " + Sql);
      
        //if (Warnings == null) {
        //        Warnings = NewWarning;
        //}
        //else {
        //        Warnings.setNextWarning(NewWarning);
        //}
	//}
    
      return RS.getUpdateCount();
    }
  }

  /**
   * In many cases, it is desirable to immediately release a
   * Statement's database and JDBC resources instead of waiting
   * for this to happen when it is automatically closed.  The
   * close method provides this immediate release.
   *
   * <p><B>Note:</B> A Statement is automatically closed when it is
   * garbage collected.  When a Statement is closed, its current
   * ResultSet, if one exists, is also closed.
   *
   * @exception java.sql.SQLException if a database access error occurs (why?)
   */

  public void close() throws java.sql.SQLException
  {
    Results  = null;
    Conn     = null;
    Warnings = null;
    Escaper  = null;
  }
  
   /**
    * The maxFieldSize limit (in bytes) is the maximum amount of
    * data returned for any column value; it only applies to
    * BINARY, VARBINARY, LONGVARBINARY, CHAR, VARCHAR and LONGVARCHAR
    * columns.  If the limit is exceeded, the excess data is silently
    * discarded.
    *
    * @return the current max column size limit; zero means unlimited
    * @exception java.sql.SQLException if a database access error occurs
    */

  public int getMaxFieldSize() throws java.sql.SQLException
  {
    return max_field_size; // Init. set to MAXBUFFER in MysqlIO
  }

   /**
    * Sets the maxFieldSize
    *
    * @param max the new max column size limit; zero means unlimited
    * @exception java.sql.SQLException if size exceeds buffer size
    */

  public void setMaxFieldSize(int max) throws java.sql.SQLException
  {
    if (max > MysqlIO.MAXBUF)
      throw new java.sql.SQLException("Attempt to setMaxFieldSize failed - compile time default");
    else
      max_field_size = max;
  }

   /**
    * The maxRows limit is set to limit the number of rows that
    * any ResultSet can contain.  If the limit is exceeded, the
    * excess rows are silently dropped.
    *
    * @return the current maximum row limit; zero means unlimited
    * @exception java.sql.SQLException if a database access error occurs
    */

  public int getMaxRows() throws java.sql.SQLException
  {
    return max_rows;
  }

   /**
    * Set the maximum number of rows
    *
    * @param max the new max rows limit; zero means unlimited
    * @exception java.sql.SQLException if a database access error occurs
    * @see getMaxRows
    */

  public void setMaxRows(int max) throws java.sql.SQLException
  {
    if (max > MysqlDefs.MAX_ROWS) {
      throw new java.sql.SQLException("setMaxRows() out of range. " + max + " > " + MysqlDefs.MAX_ROWS + ".");
    }
    max_rows = max;
  }

   /**
    * If escape scanning is on (the default), the driver will do escape
    * substitution before sending the SQL to the database.
    *
    * @param enable true to enable; false to disable
    * @exception java.sql.SQLException if a database access error occurs
    */

  public void setEscapeProcessing(boolean enable) throws java.sql.SQLException
  {
    escapeProcessing = enable;
  } 

   /**
    * The queryTimeout limit is the number of seconds the driver
    * will wait for a Statement to execute.  If the limit is
    * exceeded, a java.sql.SQLException is thrown.
    *
    * @return the current query timeout limit in seconds; 0 = unlimited
    * @exception java.sql.SQLException if a database access error occurs
    */

  public int getQueryTimeout() throws java.sql.SQLException
  {
    return timeout;
  }

   /**
    * Sets the queryTimeout limit
    *
    * @param seconds - the new query timeout limit in seconds
    * @exception java.sql.SQLException if a database access error occurs
    */

  public void setQueryTimeout(int seconds) throws java.sql.SQLException
  {
    timeout = seconds;
  }
  
  /**
   * Cancel can be used by one thread to cancel a statement that
   * is being executed by another thread.  However this driver
   * is synchronous, so this really has no meaning - we
   * define it as a no-op (i.e. you can't cancel, but there is no
   * error if you try.)
   *
   * @exception java.sql.SQLException only because thats the spec.
   */

  public void cancel() throws java.sql.SQLException
  {
    // No-op
  }

  
   /**
    * The first warning reported by calls on this Statement is
    * returned.  A Statement's execute methods clear its java.sql.SQLWarning
    * chain.  Subsequent Statement warnings will be chained to this
    * java.sql.SQLWarning.
    *
    * <p>The Warning chain is automatically cleared each time a statement
    * is (re)executed.
    *
    * <p><B>Note:</B>  If you are processing a ResultSet then any warnings
    * associated with ResultSet reads will be chained on the ResultSet
    * object.
    *
    * @return the first java.sql.SQLWarning on null
    * @exception java.sql.SQLException if a database access error occurs
    */

  public java.sql.SQLWarning getWarnings() throws java.sql.SQLException
  {
    return Warnings;
  }

  
  /**
   * After this call, getWarnings returns null until a new warning
   * is reported for this Statement.
   *
   * @exception java.sql.SQLException if a database access error occurs (why?)
   */

  public void clearWarnings() throws java.sql.SQLException
  {
    Warnings = null;
  }

  /**
   * setCursorName defines the SQL cursor name that will be used by
   * subsequent execute methods.  This name can then be used in SQL
   * positioned update/delete statements to identify the current row
   * in the ResultSet generated by this statement.  If a database
   * doesn't support positioned update/delete, this method is a
   * no-op.
   *
   * <p><b>Note:</b> This MySQL driver does not support cursors.
   *
   *
   * @param name the new cursor name
   * @exception java.sql.SQLException if a database access error occurs
   */

  public void setCursorName(String name) throws java.sql.SQLException
  {
    // No-op
  }

   /**
    * Execute a SQL statement that may return multiple results. We
    * don't have to worry about this since we do not support multiple
    * ResultSets.   You can use getResultSet or getUpdateCount to
    * retrieve the result.
    *
    * @param sql any SQL statement
    * @return true if the next result is a ResulSet, false if it is
    *      an update count or there are no more results
    * @exception java.sql.SQLException if a database access error occurs
    */

  public boolean execute(String Sql) throws java.sql.SQLException
  {
    if (escapeProcessing) {
        Sql = Escaper.escapeSQL(Sql);
    }

    if (Results != null) {
	Results.close();
    }
    
    Results = Conn.execSQL(Sql);
    return (Results != null && Results.reallyResult());
  }
  
   /**
    * getResultSet returns the current result as a ResultSet.  It
    * should only be called once per result.
    *
    * @return the current result set; null if there are no more
    * @exception java.sql.SQLException if a database access error occurs (why?)
    */

  public java.sql.ResultSet getResultSet() throws java.sql.SQLException
  {
    return Results;
  }

  /**
   * getUpdateCount returns the current result as an update count,
   * if the result is a ResultSet or there are no more results, -1
   * is returned.  It should only be called once per result.
   *
   * @return the current result as an update count.
   * @exception java.sql.SQLException if a database access error occurs
   */

  public int getUpdateCount() throws java.sql.SQLException
  {
    if (Results == null)             
      return -1;
    if (Results.reallyResult())   
      return -1;
    return Results.getUpdateCount();
  }

   /**
    * getMoreResults moves to a Statement's next result.  If it returns
    * true, this result is a ResulSet.
    *
    * @return true if the next ResultSet is valid
    * @exception java.sql.SQLException if a database access error occurs
    */

  public boolean getMoreResults() throws java.sql.SQLException
  {
    // result = result.getNext();
    // return (result != null && result.reallyResultSet());
    return false;
  }
}
