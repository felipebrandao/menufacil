package br.com.felipebrandao.menufacil.expection;

public class InvalidImageException extends RuntimeException {
    public InvalidImageException(String message) {
        super(message);
    }
}

