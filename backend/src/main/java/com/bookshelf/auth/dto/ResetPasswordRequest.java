package com.bookshelf.auth.dto;

public record ResetPasswordRequest( String token, String password ) {
}
