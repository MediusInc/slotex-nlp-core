package si.slotex.nlp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception that is thrown when there is no training file found.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TrainDataFileNotFoundException extends RuntimeException
{
    public TrainDataFileNotFoundException(String message)
    {
        super(message);
    }

    public TrainDataFileNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
