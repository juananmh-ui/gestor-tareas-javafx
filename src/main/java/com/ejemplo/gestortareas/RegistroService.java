package com.ejemplo.gestortareas;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona los elementos registrados y concentra sus reglas de validación.
 */
public final class RegistroService {

    private final List<String> elementos = new ArrayList<>();

    /**
     * Añade un texto no vacío a la lista, eliminando espacios exteriores.
     *
     * @param texto texto introducido por el usuario; puede ser {@code null}
     * @return {@code true} si el elemento fue añadido; {@code false} si no era válido
     */
    public boolean agregar(String texto) {
        if (texto == null) {
            return false;
        }

        String textoNormalizado = recortarSeparadoresInvisibles(texto);
        if (textoNormalizado.isEmpty()) {
            return false;
        }

        elementos.add(textoNormalizado);
        return true;
    }

    public static boolean esTextoValido(String texto) {
        return texto != null && !recortarSeparadoresInvisibles(texto).isEmpty();
    }

    /**
     * Devuelve una copia inmutable para impedir cambios fuera del servicio.
     */
    public List<String> obtenerElementos() {
        return List.copyOf(elementos);
    }

    public int cantidad() {
        return elementos.size();
    }

    private static String recortarSeparadoresInvisibles(String texto) {
        int inicio = 0;
        int fin = texto.length();

        while (inicio < fin) {
            int codigo = texto.codePointAt(inicio);
            if (!esSeparadorInvisible(codigo)) {
                break;
            }
            inicio += Character.charCount(codigo);
        }

        while (inicio < fin) {
            int codigo = texto.codePointBefore(fin);
            if (!esSeparadorInvisible(codigo)) {
                break;
            }
            fin -= Character.charCount(codigo);
        }

        return texto.substring(inicio, fin);
    }

    private static boolean esSeparadorInvisible(int codigo) {
        return Character.isWhitespace(codigo)
                || Character.isSpaceChar(codigo)
                || Character.isISOControl(codigo)
                || Character.getType(codigo) == Character.FORMAT;
    }
}
