

public class FormProperty {
    private String type;
    private String title;
    private String description;
    private String name;
    private boolean required;

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public FormProperty(String name, String type, String title, String description, boolean required) {
        this.name = name;
        this.type = type;
        this.title = title;
        this.description = description;
        this.required = required;
    }
}
