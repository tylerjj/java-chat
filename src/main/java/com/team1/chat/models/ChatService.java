package com.team1.chat.models;

import com.team1.chat.interfaces.ChatServiceInterface;

import java.util.ArrayList;

public class ChatService implements ChatServiceInterface
{
    // class-level instantiation of DatabaseSupport object.
    private DatabaseSupport dbs = null;

    /**
     * Creates a new User in the database.
     */
    public boolean createAccount(String username, String password)
    {
        //check if name is available
        if(!getDatabaseSupportInstance().nameAvailable(username)) {
            System.out.println("Error in createAccount: Name already in use.");
            return false;
        }

        //check if password is well-formed
        if(!checkWellFormed(password)) {
            return false;
        }

        User u = new User();
        u.createUser(username, password);

        if (dbs.putUser(u)) {
            return true;
        }
        else return false;
    }

    /**
     * Gets User data from the database.
     * User joins the default channel.
     *
     * @return Returns the user's id.
     * If the database could not retrieve a valid user, id returned is empty.
     */
    public String login(String username, String password)
    {
        User u = dbs.getUser(username, password);
        String id = u.getId();

        if (!id.isEmpty()) {
            joinChannel("0", id);
        }
        return id;
    }

    //TODO Need to eventually discuss what actions we want to occur on logout.

    /**
     * User logs out and leaves the default channel.
     *
     * @return true on success, false on fail.
     */
    public boolean logout(String uid)
    {
        User u = dbs.getUserById(uid);
        if (u.getId() != "") {
            //TODO In iteration-3, need to leave all channels.
            leaveChannel("0", u.getId());

            return true;
        }
        else return false;
    }

    /**
     * Fetch this uid associated object from database and update username.
     *
     * @param uid
     * @param newUsername
     * @return true on successful update, false otherwise.
     */
    public boolean setUsername(String uid, String newUsername)
    {
        // get User object from database.
        User u = this.getDatabaseSupportInstance().getUserById(uid);

        // if(u == null)
        //throw error here.

        // use setUsername method in User object to place new username.
        u.setUsername(uid, newUsername);

        // return User object to database, if returns false, we have an error.
        if (dbs.putUser(u)) {
            System.out.println("Database put error");
            return false;
        }
        return true;
    }

    /**
     * Fetch this uid associated object from database and update username.
     *
     * @param uid
     * @param newPassword
     * @return true on successful update, false otherwise.
     */
    public boolean setPassword(String uid, String newPassword)
    {
        // get User object from database.
        User u = getDatabaseSupportInstance().getUserById(uid);

        // if(u == null)
        //throw error here.

        // use setUsername method in User object to place new username.
        u.setPassword(uid, newPassword);

        // return User object to database, if returns false, we have an error.
        if (!dbs.putUser(u)) {
            System.out.println("Database put error");
            return false;
        }
        return true;
    }

    /**
     * Method that removes a user from the channel by calling the removeChannelUser() method
     *
     * @param cname channel name
     * @param uid   user id
     * @return true if successful: false otherwise
     */
    public boolean leaveChannel(String cname, String uid)
    {
        Channel ch = this.getDatabaseSupportInstance().getChannelByName(cname);
        User u = this.getDatabaseSupportInstance().getUserById(uid);

        if (ch != null && u != null && ch.removeChannelUser(u)) {

            this.getDatabaseSupportInstance().putChannel(ch);
            return true;
        }
        return false;
    }

    /**
     * Method that adds a user to the channel by calling the addChannelUser() method
     *
     * @param cname channel id
     * @param uid   user id
     * @return true if successful: false otherwise
     */
    public boolean joinChannel(String cname, String uid)
    {
        Channel ch = this.getDatabaseSupportInstance().getChannelByName(cname);
        User u = this.getDatabaseSupportInstance().getUserById(uid);

        if (ch != null && u != null && ch.addChannelUser(u)) {
            this.getDatabaseSupportInstance().putChannel(ch);
            return true;
        }
        return false;
    }

    /**
     * Method that returns a list of users in the channel by calling the listChannelUsers() method
     *
     * @param cname channel name
     * @param uid   user id
     * @return a list of channel users if successful: null otherwise
     */
    public ArrayList<User> listChannelUsers(String cname, String uid)
    {

        Channel ch = this.getDatabaseSupportInstance().getChannelByName(cname);
        User u = this.getDatabaseSupportInstance().getUserById(uid);

        if (ch != null && u != null && ch.isWhiteListed(u)) {
            return ch.listChannelUsers();
        }
        return null;
    }

    // iteration 2

    /**
     * Method that creates a new channel and adds the current user to that channel
     *
     * @param cname channel name
     * @param aid   users id
     * @return true on success
     */
    public boolean createChannel(String cname, String aid)
    {
        if (this.getDatabaseSupportInstance().getChannelByName(cname) == null) {
            Channel c = new Channel(cname, aid);
            User u = this.getDatabaseSupportInstance().getUserById(aid);
            u.addChannel(c);

            // update database
            this.getDatabaseSupportInstance().putChannel(c);
            this.getDatabaseSupportInstance().putUser(u);

            return true;
        }
        return false;
    }

    /**
     * Method that deletes a channel
     *
     * @param cname channel name
     * @param aid   users id
     * @return true on success
     */
    public boolean deleteChannel(String cname, String aid)
    {
        int i;
        Channel c;
        User user;
        ArrayList<User> users;

        c = this.getDatabaseSupportInstance().getChannelByName(cname);
        if (c != null) {
            users = c.deleteChannel(aid);
            if (users != null) {
                // the user is confirmed to be the admin and we have a list of users in the channel
                for (i = 0; i < users.size(); i++) {
                    user = this.getDatabaseSupportInstance().getUserById(users.get(i).getId());
                    user.deleteChannel(c);
                    this.getDatabaseSupportInstance().putUser(user);
                }
                this.getDatabaseSupportInstance().deleteChannel(c.getName());
            }
        }
        return false;
    }

    /**
     * Method that invites a user to a channel
     *
     * @param cname channel name
     * @param aid   current user id
     * @param uname name of user to invite
     * @return true on success
     */
    public boolean inviteUserToChannel(String cname, String aid, String uname)
    {
        Channel c;
        User u;

        c = this.getDatabaseSupportInstance().getChannelByName(cname);
        u = this.getDatabaseSupportInstance().getUserByName(uname);

        if (c != null && u != null) {
            if (c.whiteListUser(aid, u) && u.addChannelInvite(c)) {
                this.getDatabaseSupportInstance().putChannel(c);
                this.getDatabaseSupportInstance().putUser(u);

                return true;
            }
        }
        return false;
    }

    /**
     * Method that removes a user from a channel
     *
     * @param cname channel name
     * @param aid   current user id
     * @param uname name of user to remove
     * @return true on success
     */
    public boolean removeUserFromChannel(String cname, String aid, String uname)
    {
        Channel c;
        User u;

        c = this.getDatabaseSupportInstance().getChannelByName(cname);
        u = this.getDatabaseSupportInstance().getUserByName(uname);

        if (c != null && u != null) {
            if (c.removeUser(aid, u) && u.removeFromChannel(c)) {
                this.getDatabaseSupportInstance().putChannel(c);
                this.getDatabaseSupportInstance().putUser(u);

                return true;
            }
        }
        return false;
    }

    /**
     * Method that gets a list of channels the user is invited to
     *
     * @param uid users id
     * @return a list of channels the user is invited to
     */
    public ArrayList<Channel> viewInvitedChannels(String uid)
    {
        User u;

        u = this.getDatabaseSupportInstance().getUserById(uid);

        return u.viewInvitedChannels();
    }

    /**
     * Method that gets a list of channels the user has joined
     *
     * @param uid users id
     * @return a list of private channels
     */
    public ArrayList<Channel> viewPrivateChannels(String uid)
    {
        User u;

        u = this.getDatabaseSupportInstance().getUserById(uid);

        return u.viewPrivateChannels();
    }

    /**
     * Method that gets a list of public channels
     *
     * @return a list of public channels
     */
    public ArrayList<Channel> viewPublicChannels(String uid)
    {
        User u;

        u = this.getDatabaseSupportInstance().getUserById(uid);

        return u.viewPublicChannels();
    }

    /**
     * Method to check if password is well-formed
     * Criteria:
     * 1) 8 <= length <= 45
     * 2) string contains at least two numerics [0-9]
     *
     * @param newPass
     * @return
     */
    public boolean checkWellFormed(String newPass)
    {

        // Check min & max length limits
        if (newPass.length() > 45) {
            System.out.println("Error: Password must be at most 45 characters. Length: " + newPass.length());
            return false;
        }
        if (newPass.length() < 8) {
            System.out.println("Error: Password must be at least 8 characters. Length: " +  newPass.length());
            return false;
        }

        int count = 0;
        // loop newPass char by char, increment count if isDigit.
        for (int i = 0; i < newPass.length(); i++) {
            if (Character.isDigit(newPass.charAt(i)))
                count++;
        }
        if (count >= 2)
            return true;
        else{
            System.out.println("Password must contain at least two digits [0-9]. Number of digits: " + count);
            return false;
        }

    }


    /**
     * Method that toggles visibility of a channel
     *
     * @param cname channel name
     * @param aid   users id
     * @return true on success
     */
    public boolean toggleChannelVisibility(String cname, String aid)
    {
        Channel c;

        c = this.getDatabaseSupportInstance().getChannelByName(cname);

        return c != null && c.toggleChannelVisibility(aid);
    }

    /**
     * @return a new instance of DatabaseSupport class
     */
    private DatabaseSupport getDatabaseSupportInstance()
    {
        if (dbs == null)
            dbs = new DatabaseSupport();
        return dbs;
    }
}
