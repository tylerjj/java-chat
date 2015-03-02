package com.team1.chat.interfaces;

public interface UserInterface
{
    public boolean createUser(String username, String password);

    public String getId();

    // TODO what is toInactive() ?

    public boolean setUsername(String uid, String newUsername);

    public String getUsername();

    public boolean setPassword(String uid, String newPassword);


}