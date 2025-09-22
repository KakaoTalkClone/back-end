package com.ocean.piuda.global.api.exception;

import com.ocean.piuda.global.api.dto.ApiData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ApiData<Void> handleApplicationException(BusinessException e) {
    return ApiData.error(e.getExceptionType(), e.getDetails());
  }



  @ExceptionHandler(MethodArgumentNotValidException.class)
  public  ApiData<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    Map<String, String> details = new HashMap<>();
    List<FieldError> fieldErrors = e.getFieldErrors();
    fieldErrors.forEach(fieldError -> details.put(fieldError.getField(), fieldError.getDefaultMessage()));
    return ApiData.error(ExceptionType.NOT_VALID_REQUEST_FIELDS_ERROR, details);

  }


  @ExceptionHandler(Exception.class)
  public ApiData<Void> exception(Exception e) {
    Map<String, Object> details = Map.of("message",  e.getMessage());
    return ApiData.error(ExceptionType.UNEXPECTED_SERVER_ERROR,details);
  }

}
