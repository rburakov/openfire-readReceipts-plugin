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
        "INSERT INTO readReceipt (userJID, senderJID, ts) " +
            "VALUES (?, ?, ?)";

    private static final String UPDATE_READ_RECEIPT =
        "UPDATE readReceipt SET ts = ? " +
            "WHERE userJID = ? AND senderJID = ?";

    private static final String SELECT_READ_RECEIPT =
        "SELECT ts FROM readReceipt " +
            "WHERE userJID = ? AND senderJID = ?";

    public Integer getReceiptTimestamp(String userJID, String senderJID){

        Integer ts = null;
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_READ_RECEIPT);
            pstmt.setString(1, userJID);
            pstmt.setString(2, senderJID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ts = rs.getInt("ts");
            }
        }
        catch (SQLException e) {
            Log.error("Unable to select readReceipt for " + userJID + " - " + senderJID, e);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }

        return ts;
    }

    public Integer saveReceipt(String userJID, String senderJID){

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
            pstmt.setString(2, userJID);
            pstmt.setString(3, senderJID);
            rowsUpdated = pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            Log.error("Unable to update readReceipt for " + userJID + " - " + senderJID, e);
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
                pstmt.setString(1, userJID);
                pstmt.setString(2, senderJID);
                pstmt.setInt(3, ts);
                rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated != 0){
                    returnTs = ts;
                }
            }
            catch (SQLException e)
            {
                Log.error("Unable to insert readReceipt for " + userJID + " - " + senderJID, e);
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
