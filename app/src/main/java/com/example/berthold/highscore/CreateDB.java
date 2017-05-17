package com.example.berthold.highscore;

/**
 * Create.
 * 
 * This creates a new, empty, database if no databse of the same
 * name already exists.
 * 
 * @author Berthold Fritz 2016
 *
 */

import android.util.Log;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDB {
	
	// Const.

	public static final int ERROR=0;				// Index in 'result' for 'error' text
	public static final int ERROR_DESCRIPTION=1;	// Index in 'result' for description of error

	public static void make (String name) throws Exception
	{
		
		// DB- Conection
		
		String DB_DRIVER		=	"org.h2.Driver";
		String DB_CONNECTION	=	"jdbc:h2:";
		String DB_USER			=	"";
		String DB_PASSWORD		=	"";
	
		// Create database
		
		Connection conn;
		
		try
		{
			conn=DB.read(DB_DRIVER, DB_CONNECTION+name, DB_USER, DB_PASSWORD);
		}
		catch (SQLException se)
		{
			System.out.println("Error while creating database:"+DB_CONNECTION);
			System.out.println(se);
			return;
		}
		
		try
		{
			Statement stmt=null;
			
			stmt=conn.createStatement();

			// Games table

			stmt.executeUpdate 	("create table"
								+ "	games"
								+ " (key1 identity,"
								+ "key2 int,"
								+ "name char(255),"
								+ "type char(255),"
								+ "picture char(255))");

			// Scores

			stmt.executeUpdate 	("create table"
								+ "	scores"
								+ " (key1 identity,"
								+ "key2 int,"
								+ "date datetime,"
								+ "score int,"
								+ "comment char (255),"
								+ "evaluation char (255),"
								+ "magic int,"
								+ "picture char(255))");
			
			// Create sample entry in games
			
			stmt.executeUpdate("insert into games "
					+ "(key2,name) "
					+ "values (1,'Pitfall')");
			stmt.executeUpdate("insert into games "
					+ "(key2,name) "
					+ "values (1,'Pac Man')");
			stmt.executeUpdate("insert into games "
					+ "(key2,name) "
					+ "values (1,'Pitfall II')");

			stmt.executeUpdate("insert into games "
					+ "(key2,name) "
					+ "values (1,'Ghostbusters')");
		}
		catch (SQLException se)
		{
				System.out.println("Error while creating table");
				System.out.println(se);
				return;
		}
		
		
		// Close database
		
		DB.close(conn);
		Log.i ("---------","Closed connection");
	}
}
