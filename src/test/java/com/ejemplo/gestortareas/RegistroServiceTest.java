package com.ejemplo.gestortareas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistroServiceTest {

    private RegistroService servicio;

    @BeforeEach
    void prepararServicio() {
        servicio = new RegistroService();
    }

    @Test
    void agregaUnaDescripcionValida() {
        assertTrue(servicio.agregar("Preparar presentación"));

        assertEquals(1, servicio.cantidad());
        assertEquals("Preparar presentación", servicio.obtenerElementos().getFirst());
    }

    @Test
    void eliminaEspaciosExterioresAntesDeGuardar() {
        assertTrue(servicio.agregar("  Llamar al proveedor  "));

        assertEquals("Llamar al proveedor", servicio.obtenerElementos().getFirst());
    }

    @Test
    void conservaElOrdenDeInsercion() {
        servicio.agregar("Primera");
        servicio.agregar("Segunda");
        servicio.agregar("Tercera");

        assertEquals(
                java.util.List.of("Primera", "Segunda", "Tercera"),
                servicio.obtenerElementos()
        );
    }

    @Test
    void rechazaTextoVacioOSoloConEspacios() {
        assertFalse(servicio.agregar(""));
        assertFalse(servicio.agregar("   \t\n"));
        assertFalse(servicio.agregar("\u00a0\u200b\ufeff"));
        assertFalse(servicio.agregar(null));

        assertEquals(0, servicio.cantidad());
    }

    @Test
    void recortaSeparadoresUnicodeEnLosExtremos() {
        assertTrue(servicio.agregar("\u00a0\u200bRevisar contrato\ufeff\u00a0"));

        assertEquals("Revisar contrato", servicio.obtenerElementos().getFirst());
    }

    @Test
    void noPermiteModificarLaListaDesdeFuera() {
        servicio.agregar("Elemento protegido");

        assertThrows(
                UnsupportedOperationException.class,
                () -> servicio.obtenerElementos().add("Elemento externo")
        );
        assertEquals(1, servicio.cantidad());
    }
}
