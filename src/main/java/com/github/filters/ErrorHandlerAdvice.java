package com.github.filters;

import com.github.domain.ResponseDomain;
import com.github.helpers.TransactionNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class ErrorHandlerAdvice {
	@ExceptionHandler(TransactionNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseDomain transactionNotFound(TransactionNotFoundException exception) {
		return ResponseDomain.builder()
			.errors(exception.getMessage())
			.build();
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseDomain validationError(ConstraintViolationException exception) {
		List<ValidationErrorInfo> errorsInfo = exception.getConstraintViolations().stream()
			.map(violation -> new ValidationErrorInfo(violation.getPropertyPath().toString(), violation.getMessage(), violation.getInvalidValue()))
			.toList();
		return ResponseDomain.builder()
			.errors(errorsInfo)
			.build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ResponseDomain validationError(MethodArgumentNotValidException exception) {
		List<ValidationErrorInfo> errorsInfo = exception.getBindingResult()
			.getFieldErrors().stream().sorted((e1, e2) -> new DotComparator().compare(e1.getField(), e2.getField()))
			.map(fieldError -> new ValidationErrorInfo(toSnakeCase(fieldError.getField()), fieldError.getDefaultMessage(), fieldError.getRejectedValue()))
			.toList();
		return ResponseDomain.builder()
			.errors(errorsInfo)
			.build();
	}

	public record ValidationErrorInfo(
		String propertyName,
		String message,
		Object provideValue
	) {
	}

	public String toSnakeCase(String word) {
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1_$2";
		return word.replaceAll(regex, replacement).toLowerCase();
	}

	public static class DotComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			return Long.compare(o1.chars().filter(c -> c == '.').count(), o2.chars().filter(c -> c == '.').count());
		}
	}

}
