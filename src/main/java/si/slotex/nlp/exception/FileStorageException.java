package si.slotex.nlp.exception;

/**
 * Exception that handles if there is an error regarding the creation/edit/deletion of files.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public class FileStorageException extends RuntimeException
{
    public FileStorageException(String message)
    {
        super(message);
    }

    public FileStorageException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
