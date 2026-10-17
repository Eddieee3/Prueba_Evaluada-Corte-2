package com.papeleria;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    // el archivo esta en la raiz del proyecto, no dentro del codigo
    private static final String ARCHIVO = "db.properties";
    private static Properties props = null;

    // leer db.properties una sola vez
    private static void cargarPropiedades() throws IOException {
        if (props != null) {
            return;
        }
        Properties p = new Properties();
        FileInputStream fis = new FileInputStream(ARCHIVO);
        try {
            p.load(fis);
        } finally {
            fis.close();
        }
        props = p;
    }

    // conexion a la base de datos
    public static Connection getConnection() throws SQLException {
        try {
            cargarPropiedades();
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el archivo " + ARCHIVO, e);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        if (url == null || user == null || pass == null) {
            throw new SQLException("Faltan datos en " + ARCHIVO + " (db.url, db.user, db.password)");
        }

        return DriverManager.getConnection(url, user, pass);
    }
}
