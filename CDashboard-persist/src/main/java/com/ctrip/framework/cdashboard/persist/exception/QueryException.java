package com.ctrip.framework.cdashboard.persist.exception;

/**
 * User: huang_jie
 * Date: 11/22/13
 * Time: 3:52 PM
 */
public class QueryException extends RuntimeException {
    public QueryException() {
    }

    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryException(Throwable cause) {
        super(cause);
    }
}
