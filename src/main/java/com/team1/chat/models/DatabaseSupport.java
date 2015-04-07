package com.team1.chat.models;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

import com.team1.chat.interfaces.DatabaseSupportInterface;

public class DatabaseSupport implements DatabaseSupportInterface
{	
	Connection conn;
	
	/**
	 * Loads the driver and initializes the connection to the database.
	 */
	public DatabaseSupport()
	{
		try {   
	         // Load the driver (registers itself)
	         Class.forName ("com.mysql.jdbc.Driver");
	         } 
	    catch (Exception E) {
	            System.err.println ("Unable to load driver.");
	            E.printStackTrace ();
	    }
	    String dbUrl = "jdbc:mysql://104.236.206.121:3306/chat";
	    String user = "root";
	    String password = "362team1";
	    try {
			conn = DriverManager.getConnection (dbUrl, user, password);
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }
	    System.out.println ("*** Connected to the database ***"); 
	}

	/**
	 * Helper method to send an update to database.
	 * @param statement
	 * @return
	 */
	private boolean setData(String statement)
	{
		//Simply submit statement to the MySQL server.
		try {
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.executeUpdate();
			stmt.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	//Each index of the string list represents a row. 
	//Each line in a string represents a column.
	
	/**
	 * Helper method to pull data from the database.
	 * @param statement
	 * @return Returns an ArrayList<String>. 
	 * 		   Each string denotes a row. 
	 * 		   Each line of a string denotes a column value.
	 */
	private ArrayList<String> getData(String statement){
		Statement stmt;
		ResultSet rs;
		ResultSetMetaData rsmd;
		ArrayList<String> data = new ArrayList<String>();
		try {
			//Create Statement
			stmt = conn.createStatement();
			//Execute Query and get ResultSet
			rs = stmt.executeQuery(statement);
			//Get number of columns in a row.
			rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();
			
			for (int i = 0;rs.next();i++) {
				String row = "";
				for (int j = 0; j < numColumns;j++)
				{
					row = row + rs.getString(i)+"\n";
				}
				data.add(row);
			}
			stmt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	/**
	 * Puts a new User into the database. 
	 */
    public boolean putUser(User u)
    {
    	String statement = "INSERT INTO User " +
    					   "VALUES(DEFAULT,"+u.getUsername()+","+u.getPassword()+")";
        return setData(statement);
    }

    /**
     * Returns a User from the database by username and password.
     */
    public User getUser(String username, String password)
    {
    	String statement = "SELECT * " +
    					   "FROM User u " +
    					   "WHERE u.username = "+username+ 
    					   "AND u.password = "+ password;
    	ArrayList<String> result = getData(statement);
    	if (!(result.size()==3))
    	{
    		//First column: uid
    		String uid = result.get(0);
    	
    		//Second column: username
    		String uname = result.get(1);
    		
    		//Third column: password
    		String pw = result.get(2);
    		
    		User u = new User(uid,uname,pw);
    		
    		return u;
    	}
    	else return null;
		
    }

    /**
     * Returns a User from the database by user id.
     */
	public User getUser(String uid) {
		String statement = "SELECT * " + "FROM User u " + "WHERE u.uid ="+uid;
		ArrayList<String> result = getData(statement);
		if (!(result.size() == 3)) {
			// First column: uid
			String userId = result.get(0);

			// Second column: username
			String uname = result.get(1);

			// Third column: password
			String pw = result.get(2);

			User u = new User(userId, uname, pw);

			return u;
		} else
			return null;
	}

    public boolean nameAvailable(String newUsername)
    {
    	ArrayList<String> result = getData("SELECT * FROM USER u WHERE u.name = "+newUsername);
        return (result.size()==0);
    }

    public Channel getChannel(String name)
    {
		String statement = "SELECT * " + "FROM Channel c " + "WHERE c.name ="+name;
		ArrayList<String> result = getData(statement);
		if (!(result.size() == 6)) 
		{
			// First column: name
			String channelName = result.get(0);

			// Second column: ispublic
			boolean isPublic;
			if (result.get(1)=="true"){isPublic=true;}
			else isPublic = false;
			
			// Third column: admin
			String admin = result.get(2);
			
			// Fourth Column: whitelist
			String whitelistIds = result.get(3);
			Scanner scanIds = new Scanner(whitelistIds);
			ArrayList<User> whitelist=new ArrayList<User>();
			
			while (scanIds.hasNextLine())
			{
				// This might need some error handling. Not sure. 
				whitelist.add(getUser(scanIds.next()));
			}
			scanIds.close();
			
			Channel c = new Channel(channelName, isPublic, admin, whitelist);
			return c;
		} else
			return null;
    }

    public boolean putChannel(Channel c)
    {
    	String wList = "";
    	ArrayList<User> tempList = c.getWhiteList();
    	for (int i = 0; i < tempList.size();i++)
    	{
    		wList = wList + tempList.get(i).getId() + "\n";
    	}
    	String isPublic = String.valueOf(c.isPublic());
    	String statement = "INSERT INTO Channel " +
				   "VALUES("+c.getName()+","+isPublic+","+c.getAdminId()+","+wList+")";
    	return setData(statement);
    }
}
