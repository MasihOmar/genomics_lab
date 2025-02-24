package com.genomicslab.backend;

import java.sql.Connection;
import java.sql.DriverManager;

//public class DatabaseManager {
//}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author MarcoMan
 * Subscribe our Channel --> https://www.youtube.com/channel/UCPgcmw0LXToDn49akUEJBkQ
 * Thanks for the support guys! <3
 */
public class DatabaseManager {

    public static Connection connectDb(){

        try{

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connect = DriverManager.getConnection("jdbc:mysql://localhost/marmaraLab", "root", "");
            return connect;
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

}