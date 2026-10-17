package com.papeleria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // pasar una fila del ResultSet a un Producto
    private Producto leerFila(ResultSet rs) throws SQLException {
        Producto prod = new Producto();
        prod.setIdProducto(rs.getInt("id_producto"));
        prod.setCodigo(rs.getString("codigo"));
        prod.setNombre(rs.getString("nombre"));
        prod.setCategoria(rs.getString("categoria"));
        prod.setPrecio(rs.getBigDecimal("precio"));
        prod.setStock(rs.getInt("stock"));
        return prod;
    }

    // cargar todos los productos
    public List<Producto> getAll() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id_producto, codigo, nombre, categoria, precio, stock FROM producto ORDER BY id_producto";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(leerFila(rs));
            }
        }
        return lista;
    }

    // revisar si el codigo ya existe (idExcluir sirve para cuando se actualiza)
    public boolean existeCodigo(String codigo, Integer idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE codigo = ? AND id_producto <> ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.setInt(2, idExcluir == null ? -1 : idExcluir);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean insert(Producto prod) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, prod.getCodigo());
            stmt.setString(2, prod.getNombre());
            stmt.setString(3, prod.getCategoria());
            stmt.setBigDecimal(4, prod.getPrecio());
            stmt.setInt(5, prod.getStock());
            int res = stmt.executeUpdate();
            return res > 0;
        }
    }

    public boolean update(Producto prod) throws SQLException {
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, categoria = ?, precio = ?, stock = ? WHERE id_producto = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, prod.getCodigo());
            stmt.setString(2, prod.getNombre());
            stmt.setString(3, prod.getCategoria());
            stmt.setBigDecimal(4, prod.getPrecio());
            stmt.setInt(5, prod.getStock());
            stmt.setInt(6, prod.getIdProducto());
            int res = stmt.executeUpdate();
            return res > 0;
        }
    }

    public boolean delete(int idProducto) throws SQLException {
        String sql = "DELETE FROM producto WHERE id_producto = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            int res = stmt.executeUpdate();
            return res > 0;
        }
    }
}
