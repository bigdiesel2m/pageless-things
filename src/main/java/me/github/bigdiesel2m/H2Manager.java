package me.github.bigdiesel2m;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.sql.*;

@Slf4j
public class H2Manager {
    private Connection connection;
    private PreparedStatement checkObjectID;
    private PreparedStatement checkNPCID;

    public H2Manager(Path path) {
        try {
            // force driver to load and register into the jdbc provider map
            // since hub loading does not handle jar service loading it seems?
            Class.forName("org.h2.Driver");

            String pathString = path.toString();
            pathString = pathString.substring(0, pathString.lastIndexOf(".mv.db"));
            connection = DriverManager.getConnection("jdbc:h2:" + pathString);
            checkObjectID = connection.prepareStatement("SELECT COUNT (*) FROM OBJECTS WHERE ID = ?");
            checkNPCID = connection.prepareStatement("SELECT COUNT (*) FROM NPCS WHERE ID = ?");
        } catch (SQLException exception) {
            log.warn("Failed to load database: ", exception);
            connection = null;
        }
    }

    public boolean objectNeedsPage(int ID) {
        if (connection == null) {
            return false;
        }
        try {
            checkObjectID.setInt(1, ID);
            ResultSet resultSet = checkObjectID.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getInt(1) == 1) {
                    return false;
                }
            }

        } catch (SQLException ignored) {
            return false;
        }
        return true;
    }

    public boolean npcNeedsPage(int ID) {
        if (connection == null) {
            return false;
        }
        try {
            checkNPCID.setInt(1, ID);
            ResultSet resultSet = checkNPCID.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getInt(1) == 1) {
                    return false;
                }
            }

        } catch (SQLException ignored) {
            return false;
        }
        return true;
    }
}
