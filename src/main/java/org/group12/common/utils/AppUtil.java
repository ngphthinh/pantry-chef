package org.group12.common.utils;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.dto.response.ErrorResponse;
import org.group12.common.exception.ErrorCode;
import org.group12.common.exception.UnauthenticatedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Component
@NoArgsConstructor
public class AppUtil {

    public UUID generateUUID() {
        return UuidCreator.getTimeOrderedEpoch();
    }

    public Date expirationDate(long seconds) {
        return Date.from(Instant.now().plus(seconds, ChronoUnit.SECONDS));
    }

    public String generateOpaqueToken() {
        return generateUUID().toString();
    }

    public ErrorResponse generateErrorResponse(HttpServletRequest request, ErrorCode errorCode) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(errorCode.getHttpStatus().value())
                .error(HttpStatus.valueOf(errorCode.getHttpStatus().value()).getReasonPhrase())
                .message(errorCode.getMessage())
                .path(request.getRequestURI())
                .build();
    }


    public static AppUtil builder() {
        return new AppUtil();
    }


    /**
     * Get the user ID from the authentication context.
     *
     * @return the user ID as a UUID
     * @throws UnauthenticatedException if the user is not authenticated or the principal is invalid
     */
    public UUID userIdFromAuthentication() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthenticatedException();
        }

        String principal = authentication.getName();
        if (principal == null || principal.isBlank()) {
            throw new UnauthenticatedException();
        }

        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException ex) {
            throw new UnauthenticatedException();
        }
    }


    /**
     * Build a cursor-based pagination response.
     * @param items the list of items to include in the response
     * @param size the maximum number of items to include in the response
     * @param cursorExtractor a function to extract the cursor value from an item
     * @param mapper a function to map an item to the desired response type
     * @return a CursorResponse containing the mapped items, next cursor, and hasNext flag
     * @param <T> the type of the items in the input list
     * @param <R> the type of the items in the response
     */
    public <T, R> CursorResponse<R> buildCursorResponse(
            List<T> items,
            int size,
            Function<T, UUID> cursorExtractor,
            Function<T, R> mapper
    ) {

        boolean hasNext = items.size() > size;

        if (hasNext) {
            items = new ArrayList<>(items);
            items.remove(items.size() - 1);
        }

        UUID nextCursor = hasNext && !items.isEmpty()
                ? cursorExtractor.apply(items.get(items.size() - 1))
                : null;

        return CursorResponse.<R>builder()
                .content(
                        items.stream()
                                .map(mapper)
                                .toList()
                )
                .nextCursor(
                        nextCursor != null
                                ? nextCursor.toString()
                                : null
                )
                .hasNext(hasNext)
                .build();
    }

}