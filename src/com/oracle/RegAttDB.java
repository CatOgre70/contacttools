package com.oracle;

import static com.oracle.CommonTools.*;
import java.sql.*;
import java.util.ArrayList;

public class RegAttDB {
    private static final String[] headers = { "attendance_id", "fk_event_id",
            "fk_id", "registration_status", "attendance_status" };
    private final int[] regAttDB;

    RegAttDB(){
        this.regAttDB = new int[5];
        for(int i = 0; i < 5; i++) this.regAttDB[i] = 0;
    }

    RegAttDB(int[] values){
        this.regAttDB = new int[5];
        if(values.length != 5) {
            System.out.println("Error in class RegAttDB constructor argument: shoud be int array of length 5!");
            return;
        }
        System.arraycopy(this.regAttDB, 0, values, 0, 5);
    }

    RegAttDB(int a, int b, int c, int d, int e){
        this.regAttDB = new int[5];
        this.regAttDB[0] = a;
        this.regAttDB[1] = b;
        this.regAttDB[2] = c;
        this.regAttDB[3] = d;
        this.regAttDB[4] = e;
    }

    public static String getHeaderByIndex(int i){
        if((i >= 0) & i < headers.length) {
            return headers[i];
        } else {
            System.out.println("Error in RegAttDB class static method getHeaderByIndex: wrong function argument (index)");
            return null;
        }
    }

    public int getValueByIndex(int i) {
        if( (i < 0) | (i >= 5)){
            System.out.println("Error in RegAttDB.getValueByIndex function argument: shoud be 1 <= i <= 5!");
            return 0;
        }
        return this.regAttDB[i];
    }

    public void setValueByIndex(int i, int v) {
        this.regAttDB[i] = v;
    }

    public int getValueByHeader(String h){
        for(int i = 0; i < 5; i++)
            if(RegAttDB.headers[i].compareTo(h) == 0) return this.regAttDB[i];
        System.out.println("Error in RegAttDB.getValueByHeader method: no such header");
        return -1;
    }

    public void setValueByHeader(String h, int v){
        boolean found = false;
        for(int i = 0; i < 5; i++)
            if(RegAttDB.headers[i].compareTo(h) == 0) {
                this.regAttDB[i] = v;
                found = true;
            }
        if(!found)
            System.out.println("Error in RegAttDB.setValueByHeader method: no such header");
    }

    public static RegAttDB[] readRADBfromMySQL(){

        // Array of Registration and Attendance Items
        ArrayList<RegAttDB> raDBA = new ArrayList<>();

        // Construct the query for MySQL
        StringBuilder str = new StringBuilder();
        str.append("select ");
        int n = 5; // Number of columns in attend_reg_status DB table

        for(int i = 0; i < n-1; i++) {
            str.append(RegAttDB.headers[i]);
            str.append(", ");
        }
        str.append(RegAttDB.headers[n-1]);
        str.append(" from attend_reg_status");
        String query = str.toString();

        // Open mySQL Connection and read Items
        try (Connection connection = DriverManager
                .getConnection(AppGlobalSettings.mySQLServerURL, AppGlobalSettings.mySQLServerUser,
                        AppGlobalSettings.mySQLServerPassword);

             // Step 2:Create a statement using connection object
             Statement stmt = connection.createStatement();

             // Step 3: Execute the query or update query
             ResultSet rs = stmt.executeQuery(query)) {

            // Step 4: Process the ResultSet object.

            while (rs.next()) {
                RegAttDB raDBItem = new RegAttDB();
                for(int i = 0; i < n; i++)
                    raDBItem.setValueByIndex(i, rs.getInt(RegAttDB.headers[i]));
                raDBA.add(raDBItem);
            }

        } catch (SQLException e) {
            printSQLException(e);
        }

        RegAttDB[] raDB = new RegAttDB[raDBA.size()];
        raDBA.toArray(raDB);
        return raDB;

    }

    public static void writeRADBToMySQL(RegAttDB[] raInDB){
        if(raInDB == null){
            System.out.println("There aren't new registration data in the input file. Nothing to do");
            return;
        }

        StringBuilder str = new StringBuilder();
        str.append("insert into attend_reg_status (");

        int n = 5; // Number of columns in the RegAttDB
        for(int i = 0; i < n-1; i++) {
            str.append(RegAttDB.headers[i]);
            str.append(", ");
        }
        str.append(RegAttDB.headers[n-1]);
        str.append(")  values (");
        str.append("?,".repeat(n - 1));
        str.append("?)");
        String query = str.toString();

        // System.out.println("QUERY1= " + query);

        try {
            // Create MySQL database connection
            Connection conn = DriverManager
                    .getConnection(AppGlobalSettings.mySQLServerURL,
                            AppGlobalSettings.mySQLServerUser,
                            AppGlobalSettings.mySQLServerPassword);


            for (RegAttDB attDB : raInDB) {
                // create the mysql insert prepared statement
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                for (int i = 0; i < 5; i++)
                    preparedStmt.setInt(i + 1, attDB.getValueByIndex(i));
                // execute the prepared statement
                preparedStmt.execute();
            }

            conn.close();

        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public static void updateRADBToMySQL(RegAttDB[] raUpdDB){
        if(raUpdDB == null){
            System.out.println("There aren't changed registration data in the input file. Nothing to do");
            return;
        }

        StringBuilder str = new StringBuilder();
        str.append("update attend_reg_status set ");
        int n = 5; // Number of columns in the RegAttDB
        for(int i = 0; i < n-1; i++) {
            str.append(RegAttDB.headers[i]);
            str.append(" = ?, ");
        }
        str.append(RegAttDB.headers[n-1]);
        str.append(" = ? where ");
        str.append(RegAttDB.headers[0]);
        str.append(" = ?;");
        String query = str.toString();

        System.out.println("QUERY1= " + query);

        try {
            // Create MySQL database connection
            Connection conn = DriverManager
                    .getConnection(AppGlobalSettings.mySQLServerURL,
                            AppGlobalSettings.mySQLServerUser,
                            AppGlobalSettings.mySQLServerPassword);


            for (RegAttDB attDB : raUpdDB) {
                // create the mysql insert prepared statement
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                for (int i = 0; i < 5; i++)
                    preparedStmt.setInt(i + 1, attDB.getValueByIndex(i));
                preparedStmt.setInt(6, attDB.getValueByIndex(0));
                // execute the prepared statement
                preparedStmt.executeUpdate();
            }

            conn.close();

        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public void println(){
        StringBuilder str = new StringBuilder();

        for(int i = 0; i <5 ; i++) {
            str.append(this.regAttDB[i]);
            str.append(", ");
        }

        System.out.println(str);
    }

}

