public class MeaningAlreadyExistsException extends Exception{
    public MeaningAlreadyExistsException() {
        super("Error: cannot update, meaning already exists");
    }
}
