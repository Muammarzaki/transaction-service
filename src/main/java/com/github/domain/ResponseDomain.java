package com.github.domain;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.helpers.ResponseView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResponseDomain {
	@NotBlank
	@NotNull
	private String status;
	@NotNull
	private int statusCode;
	@JsonView(ResponseView.Success.class)
	private Object data;
	@JsonView(ResponseView.Fail.class)
	private Object message;

}
