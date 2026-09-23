package com.bookshelf.auth.dto;

public record LoginResponse( String token, String name, String email ) {
}
