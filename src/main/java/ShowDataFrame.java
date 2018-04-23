import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ShowDataFrame extends JFrame {
    public ShowDataFrame(JsonArray outputs) throws HeadlessException {
        initialize(outputs);
    }

    private void initialize(JsonArray outputs) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        int maxLength = 0;
        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();

        for (JsonElement element : outputs) {
            ArrayList<String> stringList = new ArrayList<>();
            for (Map.Entry<String, JsonElement> property : element.getAsJsonObject().entrySet()) {
                stringList.add(property.getKey() + ": " + property.getValue().getAsString());
            }
            if (maxLength < element.getAsJsonObject().size()) maxLength = element.getAsJsonObject().size();
            arrayLists.add(stringList);
        }

        Object[][] tableData = new Object[arrayLists.size()][];
        int index = 0;
        for (ArrayList<String> row : arrayLists) {
            for (int i = row.size(); i < maxLength; i++) {
                row.add(" ");
            }
            tableData[index] = row.toArray();
            index++;
        }
        Object columnNames[] = new Object[maxLength];
        Arrays.fill(columnNames, " ");
        JTable table = new JTable(tableData, columnNames);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        setSize(700, 600);
        setVisible(true);
    }
}
