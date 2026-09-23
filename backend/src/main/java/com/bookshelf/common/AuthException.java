package com.bookshelf.common;

/** A 401 whose body carries a CODE the front end turns into the right message. */
public class AuthException extends RuntimeException {

    public static final String WRONG_CREDENTIALS = "WRONG_CREDENTIALS";
    public static final String NOT_ACTIVATED = "NOT_ACTIVATED";
    public static final String BAD_TOKEN = "BAD_TOKEN";

    private final String code;

    public AuthException( String code, String message ) {
        super( message );
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
