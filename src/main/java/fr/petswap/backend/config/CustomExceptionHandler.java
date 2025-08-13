package fr.petswap.backend.config;

import fr.petswap.backend.exception.InvalidCredentialsException;
import fr.petswap.backend.exception.UserAlreadyExistsException;
import fr.petswap.backend.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        return create(HttpStatus.NOT_FOUND, "Utilisateur non trouvé", ex, request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    protected ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        return create(HttpStatus.UNAUTHORIZED, "Identifiants invalides", ex, request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    protected ResponseEntity<ProblemDetail> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        return create(HttpStatus.CONFLICT, "Nom d'utilisateur déjà utilisé", ex, request);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ProblemDetail> handleAll(Exception e, WebRequest request) {
        return switch (e) {
            case IllegalArgumentException ex -> create(HttpStatus.BAD_REQUEST, "Bad Request", ex, request);
            case IllegalStateException ex -> create(HttpStatus.INTERNAL_SERVER_ERROR, "Illegal State", ex, request);
            case HttpStatusCodeException ex -> create(ex.getStatusCode(), ex.getStatusText(), ex, request);
            case RestClientException ex -> create(HttpStatus.INTERNAL_SERVER_ERROR, "RestClient error", ex, request);
            case AuthorizationDeniedException ex -> create(HttpStatus.FORBIDDEN, "Access Denied", ex, request);
            default -> create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e, request);
        };
    }


    private ResponseEntity<ProblemDetail> create(HttpStatusCode status, String title, Exception ex, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error(title, ex);
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, title);
        problemDetail.setTitle(title);
        problemDetail.setInstance(getURI(request));
        return ResponseEntity.of(problemDetail).build();
    }


    private URI getURI(WebRequest request) {
        return switch (request) {
            case ServletWebRequest r -> URI.create(r.getRequest().getRequestURI());
            default -> URI.create(request.getDescription(false));
        };
    }
}
