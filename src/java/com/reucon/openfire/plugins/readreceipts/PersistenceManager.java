package com.reucon.openfire.plugins.readreceipts;

import org.jivesoftware.database.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static java.lang.Math.toIntExact;

/**
 * Default implementation of the PersistenceManager interface.
 */
public class PersistenceManager
{
    private static final Logger Log = LoggerFactory.getLogger(ReadReceiptsPlugin.class);

    private static final String ADD_READ_RECEIPT =
        "INSERT INTO readReceipt (username, sender, ts) " +
            "VALUES (?, ?, ?)";

    private static final String UPDATE_READ_RECEIPT =
        "UPDATE readReceipt SET ts = ? " +
            "WHERE username = ? AND sender = ?";

    private static final String SELECT_READ_RECEIPT =
        "SELECT ts FROM readReceipt " +
            "WHERE username = ? AND sender = ?";

    public Integer getReceiptTimestamp(String username, String sender){

        Integer ts = null;
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_READ_RECEIPT);
            pstmt.setString(1, username);
            pstmt.setString(2, sender);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ts = rs.getInt("ts");
            }
        }
        catch (SQLException e) {
            Log.error("Unable to select readReceipt for " + username + " - " + sender, e);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }

        return ts;
    }

    public Integer saveReceipt(String username, String sender){

        Connection con = null;
        PreparedStatement pstmt = null;
        int rowsUpdated = 0;
        int ts = toIntExact(new Date().getTime() / 1000);
        ts = ts + 2; //correct delay
        Integer returnTs = null;

        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_READ_RECEIPT);
            pstmt.setInt(1, ts);
            pstmt.setString(2, username);
            pstmt.setString(3, sender);
            rowsUpdated = pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            Log.error("Unable to update readReceipt for " + username + " - " + sender, e);
        }
        finally
        {
            DbConnectionManager.closeConnection(pstmt, con);
        }

        // if update did not affect any rows insert a new row
        if (rowsUpdated == 0)
        {
            try
            {
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(ADD_READ_RECEIPT);
                pstmt.setString(1, username);
                pstmt.setString(2, sender);
                pstmt.setInt(3, ts);
                rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated != 0){
                    returnTs = ts;
                }
            }
            catch (SQLException e)
            {
                Log.error("Unable to insert readReceipt for " + username + " - " + sender, e);
            }
            finally
            {
                DbConnectionManager.closeConnection(pstmt, con);
            }
        }
        else{
            returnTs = ts;
        }

        return returnTs;
    }
}
