package com.unapec.util;

public class Validaciones {

    public static boolean esCedulaValida(String cedula) {
        if (cedula == null) return false;

        String limpia = cedula.replace("-", "").trim();
        if (limpia.length() != 11 || !limpia.matches("\\d{11}")) return false;

        int[] multiplicadores = {1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1};
        int total = 0;

        for (int i = 0; i < 11; i++) {
            int digito = Character.getNumericValue(limpia.charAt(i));
            int producto = digito * multiplicadores[i];

            if (producto < 10) {
                total += producto;
            } else {
                total += producto / 10 + producto % 10;
            }
        }

        return total % 10 == 0;
    }

    public static boolean esRNCValido(String rnc) {
        if (rnc == null || !rnc.matches("\\d{9}")) return false;

        int[] peso = {7, 9, 8, 6, 5, 4, 3, 2};
        int suma = 0;

        for (int i = 0; i < 8; i++) {
            int digito = Character.getNumericValue(rnc.charAt(i));
            suma += digito * peso[i];
        }

        int resto = suma % 11;
        int digitoVerificador;

        if (resto == 0) {
            digitoVerificador = 2;
        } else if (resto == 1) {
            digitoVerificador = 1;
        } else {
            digitoVerificador = 11 - resto;
        }

        return digitoVerificador == Character.getNumericValue(rnc.charAt(8));
    }

}
