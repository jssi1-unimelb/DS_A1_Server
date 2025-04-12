import com.google.gson.*;
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
    private final ConcurrentHashMap<String, ArrayList<String>> dict = new ConcurrentHashMap<String, ArrayList<String>>();

    public WordDictionary(String initialFile) {
        populateDictionary(initialFile);
    }

    // Setup function that reads a pre-made text file to populate the dictionary
    private void populateDictionary(String initialFile) {
        // IO operations
        try {
            // Read information from the text file
            FileReader fr = new FileReader(initialFile);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Response handleRequest(Request request) {
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

    // Helper function that checks if a word exists in the dictionary
    private boolean wordExists(String word) {
        return dict.containsKey(word);
    }

    // Return the meaning of a word
    public Response meaning(String word) {
        if(wordExists(word)) {
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
            ArrayList<String> value = dict.computeIfPresent(searchWord, (word, meanings) -> {
                if(meanings.contains(newMeaning)) {
                    return new ArrayList<>();
                } else {
                    meanings.add(newMeaning);
                }
                return meanings;
            });

            if(value == null) { // Word doesn't exist
                return new Response("Error: cannot add meaning, word doesn't exist");
            } else if (value.isEmpty()) { // Meaning already exists
                return new Response("Error: cannot add meaning, meaning already exists");
            }
            return new Response("New meaning added to \"" + searchWord + "\"");
        } catch (NullPointerException npe) { // Word doesn't exist
            return new Response("Error: system error");
        } catch (RuntimeException re) {
            return new Response("Error: an error occurred while adding the new meaning");
        }
    }

    public Response updateMeaning(String searchWord, String oldMeaning, String newMeaning) {
        try {
            ArrayList<String> value = dict.computeIfPresent(searchWord, (word, meanings) -> {
                if(!meanings.contains(oldMeaning)) {
                    return new ArrayList<>();
                }
                int index = meanings.indexOf(oldMeaning);
                meanings.set(index, newMeaning);
                return meanings;
            });

            if(value == null) {
                return new Response("Error: Cannot update meaning, word does not exist");
            } else if (value.isEmpty()) {
                return new Response("Error: Cannot update meaning, old meaning does not exist");
            }
            return new Response("Meaning has been updated");
        } catch (NullPointerException npe) { // Word doesn't exist
            return new Response("Error: system error");
        } catch (RuntimeException re) {
            return new Response("Error: an error occurred while updating the meaning");
        }
    }
}
