package org.schors.demu.client;

import org.apache.log4j.Logger;
import org.h2.tools.RunScript;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class Dao {

    public static final String DB_UTIL_TEST_MODE = "DB_UTIL_SCRIPTS_DIR";
    private static final Logger logger = Logger.getLogger(Dao.class);
    private final InputStream createSqlScript, dropSqlScript;
    private Connection connection;

    private Dao() {
        try {
            Class.forName("org.h2.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:h2:~/demu", "sa", "");
            createSqlScript = TheApp.class.getClassLoader().getResourceAsStream("create-db.sql");
            dropSqlScript = TheApp.class.getClassLoader().getResourceAsStream("drop-db.sql");
//            createSqlScript = Files.readAllBytes(Paths.get("create-db.sql"));
//            dropSqlScript = Files.readAllBytes(Paths.get("drop-db.sql"));
            initDatabase(connection);
        } catch (Throwable e) {
            logger.fatal(e, e);
            throw new RuntimeException("No way I will get up!!");
        }
    }

    public static Dao get() {
        return Holder.instance;
    }

    public static void closeAll(AutoCloseable... closeables) {
        for (AutoCloseable ac : closeables) {
            if (ac != null) {
                try {
                    ac.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public boolean initDatabase(Connection conn) throws SQLException {
        logger.trace("checkExists()");
        try {
            boolean tableExists = false;
            Statement st = null;
            ResultSet rs = null;
            try {
                st = conn.createStatement();
                rs = st.executeQuery("SELECT COUNT(*) TOTAL FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='CONFIG'");
                rs.next();
                tableExists = rs.getInt("TOTAL") == 1;
            } finally {
                closeAll(null, st, rs);
            }

            if (!tableExists) {
                logger.debug("Table not exists. Create tables");
                try {
//                    RunScript.execute(conn, new InputStreamReader(new ByteArrayInputStream(createSqlScript)));
                    RunScript.execute(conn, new InputStreamReader(createSqlScript));
                    logger.info("The DB initialized successfully.");
                } catch (SQLException exc) {
                    logger.warn("CreateDB isn't executing properly. Try drop DB via SQL script.");
//                    RunScript.execute(conn, new InputStreamReader(new ByteArrayInputStream(dropSqlScript)));
                    RunScript.execute(conn, new InputStreamReader(dropSqlScript));
                    logger.info("The DB successfully dropped.");
//                    RunScript.execute(conn, new InputStreamReader(new ByteArrayInputStream(createSqlScript)));
                    RunScript.execute(conn, new InputStreamReader(createSqlScript));
                }
                return false;
            }
        } catch (SQLException e) {
            logger.error("Can't init Database", e);
            throw e;
        }
        return true;
    }

    public static class Holder {
        public static final Dao instance = new Dao();
    }

}
