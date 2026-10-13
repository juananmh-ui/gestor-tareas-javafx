package com.ejemplo.gestortareas;

import javafx.application.Application;

/**
 * Punto de entrada independiente de JavaFX.
 *
 * <p>Esta clase evita que el lanzador de Java intente resolver JavaFX antes de
 * que Maven haya colocado sus módulos en el module path.</p>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Application.launch(GestorTareasApp.class, args);
    }
}
