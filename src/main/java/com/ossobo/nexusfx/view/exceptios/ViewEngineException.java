package com.ossobo.nexusfx.view.exceptios;

/**
 * 🎯 VIEW ENGINE EXCEPTION - Exceção unificada
 */
public class ViewEngineException extends RuntimeException {
    public ViewEngineException(String message) {
        super(message);
    }

    public ViewEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
