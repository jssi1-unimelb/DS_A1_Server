// Jiachen Si 1085839
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// Responsible for CRUD operations on "words"
public class WordDictionary {
    private final ConcurrentHashMap<String, ArrayList<String>> dict = new ConcurrentHashMap<>();

    public WordDictionary(String initialFile) throws IOException {
        populateDictionary(initialFile);
    }

    // Setup function that reads a pre-made text file to populate the dictionary
    private void populateDictionary(String initialFile) throws IOException {
        // IO operations
        // Read information from the text file
        FileReader fr = new FileReader(initialFile);
        StringBuilder sb = new StringBuilder();
        int data = fr.read();
        while(data != -1) { // Not end of file
            sb.append((char) data);
            data = fr.read();
        }
        fr.close(); // Close file reader
        String jsonArray = sb.toString();

        // Describe to Gson that the JSON should be deserialized into a list of DictionaryEntries
        Type listType = new TypeToken<List<DictionaryEntry>>(){}.getType();

        // Deserialize
        List<DictionaryEntry> entries = GsonUtil.gson.fromJson(jsonArray, listType);

        // Add to dictionary
        for(DictionaryEntry entry : entries) {
            dict.put(entry.word, entry.meaning);
        }
    }

    public Response handleRequest(Request request) {
        String command = request.command;
        ArrayList<String> parameters = new ArrayList<>(Arrays.asList(request.parameters));

        Response response = null;
        switch(command) {
            case "remove":
                response = delete(parameters.getFirst());
                break;
            case "meaning":
                response = meaning(parameters.getFirst());
                break;
            case "new":
                String word = parameters.getFirst();
                ArrayList<String> meanings = new ArrayList<>(parameters.subList(1, parameters.size()));
                response = newWord(word, meanings);
                break;
            case "add_meaning":
                response = addNewMeaning(parameters.getFirst(), parameters.get(1));
                break;
            case "update":
                response = updateMeaning(parameters.getFirst(), parameters.get(1), parameters.get(2));
                break;
        }
        return response;
    }

    // Return the meaning of a word
    public Response meaning(String word) {
        try {
            if(dict.containsKey(word)) { // Word exists
                ArrayList<String> meanings = dict.get(word);
                StringBuilder wordMeaningSB = new StringBuilder("Meaning(s) of the word: " + word + "\n");
                int count = 1;
                for(String meaning: meanings) {
                    wordMeaningSB.append(count).append(". ").append(meaning);
                    if(count < meanings.size()) {
                        wordMeaningSB.append("\n");
                    }
                    count += 1;
                }
                return new Response(wordMeaningSB.toString());
            } else { // Word doesn't exist
                return new Response("Error: The word \"" + word + "\" does not exist in the dictionary");
            }
        } catch (NullPointerException e) {
            return new Response("Error: system error");
        }
    }

    // Add a new word to the dictionary
    public Response newWord(String word, ArrayList<String> meanings) {
        try {
            ArrayList<String> value = dict.putIfAbsent(word, meanings); // Returns null if word doesn't exist
            if(value != null) {
                return new Response("Error: The word \"" + word + "\" already exists");
            }
            return new Response("New word \"" + word + "\" has been added");
        } catch (NullPointerException e) {
            return new Response("Error: cannot create, word already exists");
        }
    }

    public Response delete(String word) {
        try {
            ArrayList<String> value = dict.remove(word);
            if(value == null) {
                return new Response("Error: The word \"" + word + "\" does not exist in the dictionary");
            }
            return new Response("\"" + word + "\" has been deleted");
        } catch (NullPointerException e) {
            return new Response("Error: Cannot delete, word does not exist");
        }
    }

    public Response addNewMeaning(String searchWord, String newMeaning) {
        try {
            final Response[] responseHolder = new Response[1];
            ArrayList<String> value = dict.computeIfPresent(searchWord, (word, meanings) -> {
                if(!meanings.contains(newMeaning)) {
                    meanings.add(newMeaning);
                    responseHolder[0] = new Response("New meaning added to \"" + searchWord + "\"");
                } else {
                    responseHolder[0] = new Response("Error: cannot add meaning, meaning already exists");
                }
                return meanings;
            });

            if(value == null) { // Word doesn't exist
                return new Response("Error: cannot add meaning, the word \"" + searchWord + "\" doesn't exist");
            }
            return responseHolder[0];
        } catch (NullPointerException npe) { // Word doesn't exist
            return new Response("Error: system error");
        } catch (RuntimeException re) {
            return new Response("Error: an error occurred while adding the new meaning");
        }
    }

    public Response updateMeaning(String searchWord, String oldMeaning, String newMeaning) {

        try {
            final Response[] responseHolder = new Response[1];
            ArrayList<String> value = dict.computeIfPresent(searchWord, (word, meanings) -> {
                // Old meaning found, new meaning not present, update
                if(meanings.contains(oldMeaning) && !meanings.contains(newMeaning)) {
                    responseHolder[0] = new Response("Meaning has been updated");
                    int index = meanings.indexOf(oldMeaning);
                    meanings.set(index, newMeaning);
                } else if (!meanings.contains(oldMeaning)) {
                    responseHolder[0] = new Response("Error: Cannot update meaning, old meaning doesn't exist");
                } else {
                    responseHolder[0] = new Response("Error: Cannot update meaning, new meaning already exists");
                }
                return meanings;
            });

            if(value == null) { // Word doesn't exist
                return new Response("Error: Cannot update meaning, the word \"" + searchWord + "\" does not exist");
            }
            return responseHolder[0];
        } catch (NullPointerException npe) { // Word doesn't exist
            return new Response("Error: system error");
        } catch (RuntimeException re) {
            return new Response("Error: an error occurred while updating the meaning");
        }
    }
}
