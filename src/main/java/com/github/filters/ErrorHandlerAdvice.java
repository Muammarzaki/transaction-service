package com.github.filters;

import com.github.domain.ResponseDomain;
import com.github.helpers.ThirdPartyRequestErrorException;
import com.github.helpers.TransactionNotFoundException;
import com.github.helpers.UnReceiveInvoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandlerAdvice {
	@ExceptionHandler(TransactionNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseDomain transactionNotFound(TransactionNotFoundException exception) {
		log.warn("Transaction not Found, {}", exception.getMessage());
		return ResponseDomain.builder()
			.errors(exception.getMessage())
			.build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseDomain validationError(MethodArgumentNotValidException exception) {
		log.info("Validation invalid");
		log.debug("Violations : {}", exception.getFieldError());
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

	@ExceptionHandler({ThirdPartyRequestErrorException.class, UnReceiveInvoice.class})
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ResponseDomain thirdPartyErrors(RuntimeException runtimeException) {
		if (runtimeException instanceof UnReceiveInvoice exception)
			log.error("Third Party : {}", exception.getMessage());
		if (runtimeException instanceof ThirdPartyRequestErrorException exception)
			log.error("Third Party : {} give code {} with message {}", exception.getVendor(), exception.getStatusCode(), exception.getMessage());
		log.debug("Cause : {}", runtimeException.getCause().toString());
		return ResponseDomain.builder()
			.errors("service have some error with third party please wait for a minute")
			.build();
	}

}
