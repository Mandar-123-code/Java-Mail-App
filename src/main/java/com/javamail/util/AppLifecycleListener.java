package com.javamail.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Closes JDBC resources and deregisters drivers on redeploy/shutdown
 * to avoid Tomcat classloader leak warnings.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Connection is created lazily via DBConnection.getConnection()
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBConnection.closeConnection();
        ClassLoader cl = getClass().getClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ignored) {
                    // ignore
                }
            }
        }
    }
}
