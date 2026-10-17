package com.papeleria;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroCategoria;
    @FXML private CheckBox chkSoloStock;

    @FXML private Label lblContador;

    private ProductoDAO dao = new ProductoDAO();
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    // producto seleccionado en la tabla (null si es uno nuevo)
    private Producto seleccionado = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        colId.setPrefWidth(50);
        colCodigo.setPrefWidth(90);
        colNombre.setPrefWidth(230);
        colCategoria.setPrefWidth(110);
        colPrecio.setPrefWidth(80);
        colStock.setPrefWidth(60);

        tablaProductos.setItems(listaProductos);
        tablaProductos.setPlaceholder(new Label("No hay productos para mostrar"));

        // categorias
        cbCategoria.getItems().addAll("Papelería", "Tecnología", "Accesorios");
        cbCategoria.setPromptText("Seleccione...");

        cbFiltroCategoria.getItems().addAll("Todas", "Papelería", "Tecnología", "Accesorios");
        cbFiltroCategoria.setValue("Todas");

        // al seleccionar una fila se pasan los datos al formulario
        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null) {
                llenarFormulario(nuevo);
            }
        });

        cargarProductos();
    }

    // cargar productos
    private void cargarProductos() {
        try {
            List<Producto> res = dao.getAll();
            listaProductos.setAll(res);
        } catch (SQLException e) {
            errorBD("No se pudieron cargar los productos.", e);
        }
        actualizarContador();
    }

    private void actualizarContador() {
        lblContador.setText("Registros: " + listaProductos.size());
    }

    private void llenarFormulario(Producto prod) {
        seleccionado = prod;
        txtCodigo.setText(prod.getCodigo());
        txtNombre.setText(prod.getNombre());
        cbCategoria.setValue(prod.getCategoria());
        txtPrecio.setText(prod.getPrecio().toPlainString());
        txtStock.setText(String.valueOf(prod.getStock()));
    }

    private void limpiarFormulario() {
        seleccionado = null;
        txtCodigo.clear();
        txtNombre.clear();
        cbCategoria.getSelectionModel().clearSelection();
        cbCategoria.setValue(null);
        txtPrecio.clear();
        txtStock.clear();
        tablaProductos.getSelectionModel().clearSelection();
        txtCodigo.requestFocus();
    }

    // ---------------- botones del formulario ----------------

    @FXML
    private void onNuevo() {
        limpiarFormulario();
    }

    @FXML
    private void onGuardar() {
        Producto prod = validarFormulario(null);
        if (prod == null) {
            return;
        }
        try {
            if (dao.insert(prod)) {
                mostrarInfo("Producto guardado correctamente.");
                limpiarFormulario();
                cargarProductos();
            }
        } catch (SQLException e) {
            errorBD("No se pudo guardar el producto.", e);
        }
    }

    @FXML
    private void onActualizar() {
        if (seleccionado == null) {
            mostrarError("Seleccione un producto de la tabla para actualizar.");
            return;
        }
        Producto prod = validarFormulario(seleccionado.getIdProducto());
        if (prod == null) {
            return;
        }
        prod.setIdProducto(seleccionado.getIdProducto());
        try {
            if (dao.update(prod)) {
                mostrarInfo("Producto actualizado correctamente.");
            } else {
                mostrarError("El producto ya no existe en la base de datos.");
            }
            limpiarFormulario();
            cargarProductos();
        } catch (SQLException e) {
            errorBD("No se pudo actualizar el producto.", e);
        }
    }

    @FXML
    private void onEliminar() {
        if (seleccionado == null) {
            mostrarError("Seleccione un producto de la tabla para eliminar.");
            return;
        }

        // confirmar antes de borrar
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText("¿Eliminar producto?");
        confirm.setContentText("Se eliminará: " + seleccionado.getCodigo() + " - " + seleccionado.getNombre());
        Optional<ButtonType> resp = confirm.showAndWait();
        if (!resp.isPresent() || resp.get() != ButtonType.OK) {
            return;
        }

        try {
            if (dao.delete(seleccionado.getIdProducto())) {
                mostrarInfo("Producto eliminado.");
            }
            limpiarFormulario();
            cargarProductos();
        } catch (SQLException e) {
            errorBD("No se pudo eliminar el producto.", e);
        }
    }

    // ---------------- botones de busqueda ----------------

    @FXML
    private void onBuscar() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim();
        String cat = cbFiltroCategoria.getValue();
        boolean soloStock = chkSoloStock.isSelected();
        try {
            List<Producto> res = dao.searchAndFilter(texto, cat, soloStock);
            listaProductos.setAll(res);
        } catch (SQLException e) {
            errorBD("Error al buscar productos.", e);
        }
        actualizarContador();
    }

    @FXML
    private void onLimpiarFiltros() {
        txtBuscar.clear();
        cbFiltroCategoria.setValue("Todas");
        chkSoloStock.setSelected(false);
        cargarProductos();
    }

    @FXML
    private void onRecargar() {
        cargarProductos();
    }

    // ---------------- validaciones ----------------

    // regresa el producto si todo esta bien, o null si hay algun error
    private Producto validarFormulario(Integer idActual) {
        // V1: quitar espacios
        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String precioTxt = txtPrecio.getText() == null ? "" : txtPrecio.getText().trim();
        String stockTxt = txtStock.getText() == null ? "" : txtStock.getText().trim();
        String categoria = cbCategoria.getValue();

        // V2: codigo en mayusculas
        codigo = codigo.toUpperCase();
        txtCodigo.setText(codigo);
        txtNombre.setText(nombre);

        // V3: codigo de 1 a 10 caracteres
        if (codigo.isEmpty()) {
            mostrarError("El código es obligatorio.");
            txtCodigo.requestFocus();
            return null;
        }
        if (codigo.length() > 10) {
            mostrarError("El código debe tener máximo 10 caracteres.");
            txtCodigo.requestFocus();
            return null;
        }

        // V4: nombre de 3 a 80 caracteres
        if (nombre.length() < 3 || nombre.length() > 80) {
            mostrarError("El nombre debe tener entre 3 y 80 caracteres.");
            txtNombre.requestFocus();
            return null;
        }

        // V5: categoria seleccionada
        if (categoria == null || categoria.isEmpty()) {
            mostrarError("Seleccione una categoría.");
            cbCategoria.requestFocus();
            return null;
        }

        // V6: precio numerico
        precioTxt = precioTxt.replace(",", ".");
        BigDecimal precio;
        try {
            precio = new BigDecimal(precioTxt);
        } catch (NumberFormatException e) {
            mostrarError("El precio debe ser un número válido (ejemplo: 25.50).");
            txtPrecio.requestFocus();
            return null;
        }

        // V7: precio mayor a 0
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarError("El precio debe ser mayor a 0.");
            txtPrecio.requestFocus();
            return null;
        }

        // V8: maximo 2 decimales (y que quepa en NUMERIC(10,2))
        if (precio.stripTrailingZeros().scale() > 2) {
            mostrarError("El precio solo puede tener hasta 2 decimales.");
            txtPrecio.requestFocus();
            return null;
        }
        if (precio.compareTo(new BigDecimal("99999999.99")) > 0) {
            mostrarError("El precio es demasiado grande.");
            txtPrecio.requestFocus();
            return null;
        }

        // V9: stock entero >= 0
        int stock;
        try {
            stock = Integer.parseInt(stockTxt);
        } catch (NumberFormatException e) {
            mostrarError("El stock debe ser un número entero.");
            txtStock.requestFocus();
            return null;
        }
        if (stock < 0) {
            mostrarError("El stock no puede ser negativo.");
            txtStock.requestFocus();
            return null;
        }

        // V10: codigo no repetido
        try {
            if (dao.existeCodigo(codigo, idActual)) {
                mostrarError("Ya existe un producto con el código " + codigo + ".");
                txtCodigo.requestFocus();
                return null;
            }
        } catch (SQLException e) {
            errorBD("No se pudo verificar el código.", e);
            return null;
        }

        return new Producto(codigo, nombre, categoria, precio.setScale(2), stock);
    }

    // ---------------- alertas ----------------

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Información");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // el detalle tecnico va a la consola, al usuario solo un mensaje simple
    private void errorBD(String msg, SQLException e) {
        System.err.println("[ERROR BD] " + msg + " SQLState=" + e.getSQLState() + " -> " + e.getMessage());

        String detalle = "Revise que PostgreSQL esté encendido y que db.properties sea correcto.";
        if ("23505".equals(e.getSQLState())) {
            detalle = "El código ya está registrado.";
        }
        mostrarError(msg + "\n" + detalle);
    }
}
