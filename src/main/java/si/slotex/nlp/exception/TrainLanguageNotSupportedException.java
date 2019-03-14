package si.slotex.nlp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception is thrown when the language specified in the training data is not supported.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TrainLanguageNotSupportedException extends RuntimeException
{

    public TrainLanguageNotSupportedException(String message)
    {
        super(message);
    }
}
