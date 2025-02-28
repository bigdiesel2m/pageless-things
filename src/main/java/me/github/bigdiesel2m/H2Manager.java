package me.github.bigdiesel2m;

import java.nio.file.Path;
import java.sql.*;

public class H2Manager {
    private Connection connection;
    private PreparedStatement checkObjectID;

    public H2Manager(Path path) {
        try {
            connection = DriverManager.getConnection("jdbc:h2:" + path.toString());
            checkObjectID = connection.prepareStatement("SELECT COUNT (*) FROM OBJECTS WHERE ID = ?");
        } catch (SQLException exception) {
            connection = null;
        }
    }

    public boolean hasPage(int ID) {
        if (connection == null) {
            return false;
        }
        try {
            checkObjectID.setInt(1, ID);
            ResultSet resultSet = checkObjectID.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getInt(1) == 1) {
                    return true;
                }
            }

        } catch (SQLException ignored) {
        }
        return false;
    }
}
