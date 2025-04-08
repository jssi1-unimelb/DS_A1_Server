import com.google.gson.*;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// Responsible for CRUD operations on "words"
public class WordDictionary {
    private ConcurrentHashMap<String, List<String>> dict = new ConcurrentHashMap<String, List<String>>();

    public WordDictionary() {
        populateDictionary();
    }

    // Setup function that reads a pre-made text file to populate the dictionary
    private void populateDictionary() {
        // IO operations
        try {
            // Read information from the text file
            FileReader fr = new FileReader("initialDictionary.txt");
            StringBuilder sb = new StringBuilder("");
            int data = fr.read();
            while(data != -1) { // Not end of file
                sb.append((char) data);
                data = fr.read();
            }
            fr.close(); // Close file reader
            String jsonArray = sb.toString();

            // Parse JSON into a list of Word objects
            Gson gson = new Gson();

            // Describe to Gson that the JSON should be deserialized into a list of DictionaryEntries
            Type listType = new TypeToken<List<DictionaryEntry>>(){}.getType();

            // Deserialize
            List<DictionaryEntry> entries = gson.fromJson(jsonArray, listType);

            // Add to dictionary
            for(DictionaryEntry entry : entries) {
                dict.put(entry.word, entry.meaning);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper function that checks if a word exists in the dictionary
    private boolean wordExists(String word) {
        return dict.containsKey(word);
    }

    // Return the meaning of a word
    public String meaning(String word) {
        if(wordExists(word)) {
            List<String> meanings = dict.get(word);
            StringBuilder wordMeaningSB = new StringBuilder("Meaning:\n");
            int count = 1;
            for(String meaning: meanings) {
                wordMeaningSB.append(count).append(". ").append(meaning).append("\n\n");
            }
            return wordMeaningSB.toString();
        } else { // Word doesn't exist
            return null;
        }
    }

    // Add a new word to the dictionary
    public String newWord(String word, List<String> meanings) {
        try {
            List<String> value = dict.putIfAbsent(word, meanings); // Returns null if word doesn't exist
            return "Success";
        } catch (NullPointerException e) {
            return "Error: cannot create, word already exists";
        }
    }

    public String delete(String word) {
        try {
            dict.remove(word);
            return "Success";
        } catch (NullPointerException e) {
            return "Error: Cannot delete, word does not exist";
        }
    }

    public String addNewMeaning(String searchWord, String newMeaning) {
        try {
            List<String> value = dict.computeIfPresent(searchWord, (word, meanings) -> {
                if(meanings.contains(newMeaning)) {
                    return null;
                }
                meanings.add(newMeaning);
                return meanings;
            });

            if(value == null) { // Meaning already exists
                throw new MeaningAlreadyExistsException();
            }
            return "Success";
        } catch (NullPointerException npe) { // Word doesn't exist
            return "Error: cannot add meaning, word doesn't exist";
        }
        catch (MeaningAlreadyExistsException e) {
            return e.getMessage();
        } catch (RuntimeException re) {
            return "Error: an error occurred while adding the new meaning";
        }
    }

    public String updateMeaning(String searchWord, String oldMeaning, String newMeaning) {
        try {
            List<String> value = dict.computeIfPresent(searchWord, (word, meanings) -> {
                if(!meanings.contains(oldMeaning)) {
                    return null;
                }
                int index = meanings.indexOf(oldMeaning);
                meanings.set(index, newMeaning);
                return meanings;
            });

            if(value == null) {
                throw new MeaningDoesNotExistException();
            }
            return "Success";
        } catch (NullPointerException npe) { // Word doesn't exist
            return "Error: cannot update meaning, word doesn't exist";
        } catch (MeaningDoesNotExistException e) {
            return e.getMessage();
        } catch (RuntimeException re) {
            return "Error: an error occurred while updating the meaning";
        }
    }
}
