/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifis.skysim2.data.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christoph
 */
public class DbDumpFileCreator {
    // full JDBC URL

    private static final String url = "jdbc:db2://is60.idb.cs.tu-bs.de:50000/prefs";
    // name of desired columns (must be numeric!)
    private static final String[] columns = {"gp", "pts", "reb", "asts", "fgm", "ftm"};
    // preferences for the columns: true=positive, false=negativ
    private static final boolean[] colPref = {true, true, true, true, true, true};
    // the desired full qualified table name
    private static final String table = "nba.player_regular_season";
    // additional filter which is inserted into where clause
    private static final String filter = "NOT team='TOT'";
    // the filename of the dump file
    private static final String dumpfileName = "nba_dump.txt";

    private Connection createNewConnection(String user, String password) throws SQLException {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        properties.setProperty("retrieveMessagesFromServerOnGetMessage", "true");
        Connection connection = DriverManager.getConnection(url, properties);
        return connection;
    }

    /**
     * The SQL string for returning all tuples (max=false) or the max values of all colums (max=true).
     * @param max see above
     * @return a string
     */
    private String buildSQL(boolean max) {
        StringBuffer sql = new StringBuffer("SELECT ");
        // SELECT
        for (int i = 0; i < columns.length; i++) {
            if (!max) {
                sql.append(columns[i]);
            } else {
                sql.append("max(").append(columns[i]).append(")");
            }
            if (i < columns.length - 1) {
                sql.append(',');
            }
        }
        // FROM
        sql.append(" FROM ").append(table);
        // WHERE
        if (filter != null && filter.length() > 0) {
            sql.append(" WHERE ").append(filter);
        }
        Logger.getLogger(DbDumpFileCreator.class.getName()).finest("Using SQL : " + sql.toString());
        return sql.toString();
    }

    /**
     * Returns all maximum values for the desired colums.
     * @return array containing max values
     */
    private float[] getMaxValues(Statement stmt) throws SQLException {
        float[] max = new float[columns.length];
        ResultSet rs = stmt.executeQuery(buildSQL(true));
        // iterate over all result tuples
        while (rs.next()) {
            // for each column, do:
            for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                max[i - 1] = rs.getFloat(i);
            }
        }
        rs.close();
        return max;
    }

    /**
     * Writes the file header (4 lines: SQL, #columns, #rows, blank).
     * @param out 
     * @param stm
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    private void writeFileHeader(Writer out, Statement stm) throws SQLException, IOException {
        StringBuffer sql = new StringBuffer("SELECT count(*) FROM ").append(table);
        if (filter != null && filter.length() > 0) {
            sql.append(" WHERE ").append(filter);
        }
        ResultSet rs = stm.executeQuery(sql.toString());
        rs.next();
        int rows = rs.getInt(1);
        rs.close();
        //
        out.write("used SQL: " + buildSQL(false) + "\n");
        out.write("numOfColumns: " + columns.length + "\n");
        out.write("numOfRows: " + rows + "\n\n");
    }

    private void dumpDataToFile(String user, String password) throws SQLException, IOException {
        // init file
        File file = new File(dumpfileName);
        FileWriter out = new FileWriter(file);

        // connect to db
        Connection conn = createNewConnection(user, password);
        String sql = buildSQL(false);
        Statement stm = conn.createStatement();
        float[] max = getMaxValues(stm);

        // write header
        writeFileHeader(out, stm);

        // iterate over all result tuples
        ResultSet rs = stm.executeQuery(sql.toString());
        int count = 1;
        while (rs.next()) {
            // for each column, do:
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                float value = rs.getFloat(i + 1) / max[i];
                if (!colPref[i]) {
                    value = 1 - value;
                }
                out.write(value + "\n");
            }
            count++;
        }
        //
        out.close();
        conn.close();
        Logger.getLogger(DbDumpFileCreator.class.getName()).info(count + " tuples wriiten to dumpfile " + file.getAbsolutePath());
    }

    public static void main(String[] args) {
        try {
            DbDumpFileCreator nbaGenerator = new DbDumpFileCreator();
            String user = null;
            String password = null;
            if ((user == null) || (password == null)) {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Please enter your DB user name: ");
                user = scanner.nextLine();
                System.out.print("Please enter your DB password: ");
                password = scanner.nextLine();
            }
            nbaGenerator.dumpDataToFile(user, password);
        } catch (Exception ex) {
            Logger.getLogger(DbDumpFileCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
