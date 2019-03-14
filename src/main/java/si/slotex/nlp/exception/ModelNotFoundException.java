package si.slotex.nlp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception is thrown when there is no specified trained model available.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ModelNotFoundException extends RuntimeException
{
    public ModelNotFoundException(String message)
    {
        super(message);
    }
}
