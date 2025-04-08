public class MeaningDoesNotExistException extends Exception{
    public MeaningDoesNotExistException() {
        super("Error: cannot update, meaning does not exist");
    }
}
