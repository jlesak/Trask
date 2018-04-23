
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class JFrameForm extends JFrame {

    private static final int TEXT_FILED_WIDTH = 200;
    private static final int TEXT_FILED_HEIGHT = 25;
    private static final int LABEL_WIDTH = 100;
    private static final int LABEL_HEIGHT = 25;
    private static final int LINE_GAP = 5;
    private static final int LABEL_X_POSITION = 5;
    private static final int INPUT_X_POSITION = LABEL_X_POSITION + LABEL_WIDTH;

    private String schemaFile;
    private String outputFile;

    public JFrameForm(String schemaFile, String outputFile) throws HeadlessException {
        this.schemaFile = schemaFile;
        this.outputFile = outputFile;

        generateFormFromJSON(schemaFile);
        setSize(700, 500);
    }

    /**
     * Generate form based on JSON schema
     *
     * @param jsonPath JSON file with schema
     */
    public void generateFormFromJSON(String jsonPath) {
        //initialize JFrame
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);

        //get properties
        ArrayList<FormProperty> properties = new ArrayList<>();
        try {
            properties = getPropertiesFromJson(jsonPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<MyTextField> inputs = new ArrayList<>();

        //create label and input for each property
        int i = 0;
        for (FormProperty property : properties) {
            i++;
            switch (property.getType()) {
                case "string":
                    MyTextField textField = new MyTextField(property.getDescription());
                    textField.setBounds(INPUT_X_POSITION, i * (TEXT_FILED_HEIGHT + LINE_GAP), TEXT_FILED_WIDTH, TEXT_FILED_HEIGHT);
                    textField.setName(property.getName());
                    textField.setRequired(property.isRequired());
                    inputs.add(textField);

                    JLabel label = new JLabel(property.getTitle());
                    label.setLabelFor(textField);
                    label.setBounds(LABEL_X_POSITION, i * (TEXT_FILED_HEIGHT + LINE_GAP), LABEL_WIDTH, LABEL_HEIGHT);

                    getContentPane().add(label);
                    getContentPane().add(textField);
                    break;
                default:
                    JLabel unspecifiedType = new JLabel("Unspecified type " + property.getType());
                    unspecifiedType.setBounds(LABEL_X_POSITION, i * (TEXT_FILED_HEIGHT + LINE_GAP), LABEL_WIDTH, LABEL_HEIGHT);
                    getContentPane().add(unspecifiedType);
            }
        }

        //button for storing data
        JButton saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.setBounds(LABEL_X_POSITION, (i + 2) * (TEXT_FILED_HEIGHT + LINE_GAP), 100, LABEL_HEIGHT);
        saveButton.addActionListener(e -> {
            try {
                if (checkInputValues(inputs)) {
                    storeData(outputFile, inputs);
                    clearInputs(inputs);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        getContentPane().add(saveButton);

        //button for showing saved data
        JButton showButton = new JButton();
        showButton.setText("Show");
        showButton.setBounds(INPUT_X_POSITION, (i + 2) * (TEXT_FILED_HEIGHT + LINE_GAP), 100, LABEL_HEIGHT);
        showButton.addActionListener(e -> {
            try {
                ShowDataFrame dataFrame = new ShowDataFrame(readJsonFile(outputFile).get("outputs").getAsJsonArray());
                dataFrame.setVisible(true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        getContentPane().add(showButton);


    }

    /**
     * Check required inputs and their values. If required i not filled, mark them as required.
     *
     * @param inputs ArrayList of form inputs
     * @return false if one of required inputs is not filled. True if all required inputs are filled.
     */
    private boolean checkInputValues(ArrayList<MyTextField> inputs) {
        boolean result = true;
        for (MyTextField field : inputs) {
            if (field.isRequired() && (field.getText().isEmpty() || field.getText().equals(field.getPlaceholder()))) {
                field.markRequired();
                result = false;
            }
        }
        return result;
    }

    /**
     * Clears text values in all inputs
     *
     * @param inputs ArrayList of form inputs
     */
    private void clearInputs(ArrayList<MyTextField> inputs) {
        for (MyTextField field : inputs) {
            field.clear();
        }
    }

    /**
     * Parse properties in JSON schema and returns them as ArrayList of FormProperty objects
     *
     * @param jsonPath String path to JSON file
     * @return ArrayList of FormProperty objects
     * @throws IOException IOException if file reading fails
     */
    private ArrayList<FormProperty> getPropertiesFromJson(String jsonPath) throws IOException {
        JsonObject object = readJsonFile(jsonPath);

        Set<Map.Entry<String, JsonElement>> entries = object
                .get("definitions").getAsJsonObject()
                .get("TraskTest1").getAsJsonObject()
                .get("properties").getAsJsonObject().entrySet();

        ArrayList<FormProperty> properties = new ArrayList<>();

        String requiredFieldsString = object
                .get("definitions").getAsJsonObject()
                .get("TraskTest1").getAsJsonObject()
                .get("required").getAsJsonArray().toString();
        java.lang.reflect.Type arrayType = new TypeToken<ArrayList<String>>() {
        }.getType();
        Gson gson = new Gson();
        ArrayList<String> requiredFields = gson.fromJson(requiredFieldsString, arrayType);

        String name, type, title, description;
        boolean required;

        for (Map.Entry<String, JsonElement> entry : entries) {
            name = entry.getKey();
            type = entry.getValue().getAsJsonObject().get("type").getAsString();
            title = entry.getValue().getAsJsonObject().get("title").getAsString();
            description = entry.getValue().getAsJsonObject().get("description").getAsString();
            required = requiredFields.contains(name);
            properties.add(new FormProperty(name, type, title, description, required));
        }

        return properties;
    }

    /**
     * Stores data from form to JSON file
     *
     * @param jsonFile filename of output JSON file
     * @param inputs   ArrayList of form inputs
     * @throws IOException if writing to file fails
     */
    private void storeData(String jsonFile, ArrayList<MyTextField> inputs) throws IOException {
        //load outputs int file
        JsonObject outputsJson = readJsonFile(jsonFile);

        //append new form output
        JsonObject outputJsonObject = new JsonObject();
        outputJsonObject.addProperty("$schema", getSchema());
        outputJsonObject.addProperty("id", nextId(outputsJson));
        for (MyTextField input : inputs) {
            if (input.getText().equals(input.getPlaceholder()))
                outputJsonObject.addProperty(input.getName(), "");
            else
                outputJsonObject.addProperty(input.getName(), input.getText());
        }

        if (outputsJson == null) {
            outputsJson = new JsonObject();
        }
        if (!outputsJson.has("outputs")) {
            outputsJson.add("outputs", new JsonArray());
        }
        outputsJson.get("outputs").getAsJsonArray().add(outputJsonObject);

        writeJsonObjectToFile(jsonFile, outputsJson);
    }

    /**
     * Gets schema name from JSON schema file
     *
     * @return String schema name
     */
    private String getSchema() {
        try {
            JsonObject schemaObject = readJsonFile(schemaFile);
            return schemaObject.get("$schema").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Reads JSON from file and returns it as JsonObject
     *
     * @param filePath String filename
     * @return JsonObject
     * @throws IOException if file reading fails
     */
    private JsonObject readJsonFile(String filePath) throws IOException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(filePath));
        JsonObject inputJson = gson.fromJson(reader, JsonObject.class);
        reader.close();
        return inputJson;
    }

    /**
     * Writes JsonObject to JSON file
     *
     * @param filePath   String filename
     * @param jsonObject JsonObject to be written
     * @throws IOException if writing fails
     */
    private void writeJsonObjectToFile(String filePath, JsonObject jsonObject) throws IOException {
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonObject);
        FileWriter fileWriter;
        fileWriter = new FileWriter(filePath);
        fileWriter.write(jsonString);
        fileWriter.close();
    }

    /**
     * Gets latest id from outputs and returns incremented by 1
     *
     * @param outputs JsonObject outputs
     * @return String id for new output
     */
    private String nextId(JsonObject outputs) {
        int maxId = 0;
        if ((outputs != null) && outputs.has("outputs")) {
            JsonArray outputsArray = outputs.getAsJsonArray("outputs");

            for (JsonElement jsonElement : outputsArray) {
                int id = jsonElement.getAsJsonObject().get("id").getAsInt();
                if (id > maxId) maxId = id;
            }
        }

        return Integer.toString(maxId + 1);
    }

}
