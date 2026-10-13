package com.ejemplo.gestortareas;

import java.util.Objects;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class GestorTareasApp extends Application {

    static final String TITULO_VENTANA = "Gestor de tareas";
    static final String TITULO_CABECERA = "Mis tareas";
    static final String MENSAJE_VALIDACION = "Escribe una tarea antes de añadirla.";
    private static final String AYUDA_ENTRADA = "Escribe una tarea y pulsa Enter o el botón Añadir.";
    private static final PseudoClass INVALIDA = PseudoClass.getPseudoClass("invalid");

    private final RegistroService registroService = new RegistroService();
    private final ObservableList<String> elementosVisibles = FXCollections.observableArrayList();

    private TextField entrada;
    private ListView<String> lista;
    private Label validacion;
    private Label contador;

    @Override
    public void start(Stage stage) {
        BorderPane raiz = new BorderPane();
        raiz.getStyleClass().add("app-shell");
        raiz.setTop(crearCabecera());
        raiz.setCenter(crearContenido());

        Scene scene = new Scene(raiz, 760, 620);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("styles.css"),
                "No se ha encontrado styles.css"
        ).toExternalForm());

        stage.setTitle(TITULO_VENTANA);
        stage.setMinWidth(640);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();

        Platform.runLater(entrada::requestFocus);
    }

    private VBox crearCabecera() {
        Label marca = new Label("LISTA PERSONAL");
        marca.getStyleClass().add("eyebrow");

        Label titulo = new Label(TITULO_CABECERA);
        titulo.getStyleClass().add("title");

        Label descripcion = new Label("Registra lo que tienes pendiente y consúltalo de un vistazo.");
        descripcion.getStyleClass().add("subtitle");
        descripcion.setWrapText(true);

        VBox cabecera = new VBox(7, marca, titulo, descripcion);
        cabecera.getStyleClass().add("header");
        cabecera.setPadding(new Insets(34, 44, 30, 44));
        return cabecera;
    }

    private VBox crearContenido() {
        Label etiquetaEntrada = new Label("Nueva tarea");
        etiquetaEntrada.getStyleClass().add("field-label");

        entrada = new TextField();
        entrada.setPromptText("Ej.: Preparar la reunión del viernes");
        entrada.setAccessibleText("Descripción de la nueva tarea");
        entrada.setAccessibleHelp(AYUDA_ENTRADA);
        entrada.setOnAction(event -> guardarEntrada());
        etiquetaEntrada.setLabelFor(entrada);

        Button botonAgregar = new Button("Añadir");
        botonAgregar.getStyleClass().add("primary-button");
        botonAgregar.setAccessibleHelp("Añade el texto escrito a la lista de tareas.");
        botonAgregar.setTooltip(new Tooltip("También puedes pulsar Enter"));
        botonAgregar.setOnAction(event -> guardarEntrada());

        HBox filaEntrada = new HBox(12, entrada, botonAgregar);
        filaEntrada.setAlignment(Pos.CENTER);
        HBox.setHgrow(entrada, Priority.ALWAYS);

        validacion = new Label(MENSAJE_VALIDACION);
        validacion.getStyleClass().add("validation-message");
        validacion.setVisible(false);
        validacion.managedProperty().bind(validacion.visibleProperty());

        entrada.textProperty().addListener((observable, anterior, actual) -> {
            if (RegistroService.esTextoValido(actual)) {
                ocultarValidacion();
            }
        });

        Label tituloLista = new Label("Tareas guardadas");
        tituloLista.getStyleClass().add("section-title");

        contador = new Label(textoContador(0));
        contador.getStyleClass().add("counter");

        Region separador = new Region();
        HBox.setHgrow(separador, Priority.ALWAYS);
        HBox cabeceraLista = new HBox(12, tituloLista, separador, contador);
        cabeceraLista.setAlignment(Pos.CENTER_LEFT);

        lista = new ListView<>(elementosVisibles);
        lista.getStyleClass().add("task-list");
        lista.setAccessibleText("Lista de tareas guardadas");
        lista.setPlaceholder(crearEstadoVacio());
        lista.setCellFactory(listView -> new CeldaTarea());
        VBox.setVgrow(lista, Priority.ALWAYS);

        VBox contenido = new VBox(10, etiquetaEntrada, filaEntrada, validacion, cabeceraLista, lista);
        contenido.getStyleClass().add("content-card");
        contenido.setPadding(new Insets(30, 36, 34, 36));
        contenido.setMaxWidth(900);

        StackPane envoltorio = new StackPane(contenido);
        envoltorio.setPadding(new Insets(0, 28, 30, 28));
        VBox.setVgrow(envoltorio, Priority.ALWAYS);

        VBox cuerpo = new VBox(envoltorio);
        VBox.setVgrow(envoltorio, Priority.ALWAYS);
        return cuerpo;
    }

    private VBox crearEstadoVacio() {
        Label icono = new Label("✓");
        icono.getStyleClass().add("empty-icon");

        Label titulo = new Label("Tu lista está vacía");
        titulo.getStyleClass().add("empty-title");

        Label detalle = new Label("Escribe una tarea arriba para empezar.");
        detalle.getStyleClass().add("empty-description");

        VBox estado = new VBox(7, icono, titulo, detalle);
        estado.setAlignment(Pos.CENTER);
        return estado;
    }

    private void guardarEntrada() {
        if (!registroService.agregar(entrada.getText())) {
            mostrarValidacion();
            entrada.requestFocus();
            return;
        }

        elementosVisibles.setAll(registroService.obtenerElementos());
        contador.setText(textoContador(registroService.cantidad()));
        lista.scrollTo(elementosVisibles.size() - 1);

        entrada.clear();
        ocultarValidacion();
        Platform.runLater(entrada::requestFocus);
    }

    private void mostrarValidacion() {
        validacion.setVisible(true);
        entrada.pseudoClassStateChanged(INVALIDA, true);
        entrada.setAccessibleHelp(MENSAJE_VALIDACION);
        entrada.notifyAccessibleAttributeChanged(AccessibleAttribute.HELP);
    }

    private void ocultarValidacion() {
        boolean estabaVisible = validacion.isVisible();
        validacion.setVisible(false);
        entrada.pseudoClassStateChanged(INVALIDA, false);
        if (estabaVisible) {
            entrada.setAccessibleHelp(AYUDA_ENTRADA);
            entrada.notifyAccessibleAttributeChanged(AccessibleAttribute.HELP);
        }
    }

    private static String textoContador(int cantidad) {
        return cantidad == 1 ? "1 tarea" : cantidad + " tareas";
    }

    private static final class CeldaTarea extends ListCell<String> {

        private final Label indicador = new Label("✓");
        private final Label texto = new Label();
        private final HBox fila = new HBox(12, indicador, texto);

        private CeldaTarea() {
            indicador.getStyleClass().add("task-check");
            texto.getStyleClass().add("task-text");
            texto.setWrapText(true);
            texto.setMinWidth(0);
            texto.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(texto, Priority.ALWAYS);

            fila.setAlignment(Pos.CENTER_LEFT);
            NumberBinding anchoDisponible = Bindings.max(0, widthProperty().subtract(30));
            fila.prefWidthProperty().bind(anchoDisponible);
            fila.maxWidthProperty().bind(anchoDisponible);
        }

        @Override
        protected void updateItem(String tarea, boolean vacia) {
            super.updateItem(tarea, vacia);

            if (vacia || tarea == null) {
                setText(null);
                setGraphic(null);
                setAccessibleText(null);
                return;
            }

            texto.setText(tarea);
            setText(null);
            setGraphic(fila);
            setAccessibleText(tarea);
        }
    }
}
