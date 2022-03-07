package net.sf.etl.parsers.literals;

import net.sf.etl.parsers.ErrorInfo;

public record ParseResult<T>(T result, ErrorInfo errors) {
    public static <T> ParseResult<T> of(T result, ErrorInfo errors) {
        return new ParseResult<>(result, errors);
    }

    public static <T> ParseResult<T> of(T result) {
        return new ParseResult<>(result, null);
    }
}
