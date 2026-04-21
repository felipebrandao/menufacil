package br.com.felipebrandao.menufacil.expection;

public class ImageDeleteException extends RuntimeException {
    public ImageDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}

