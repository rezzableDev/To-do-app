package org.example.todo_app.model;

public enum Priority {
    LOW("Низкий", "Past"),
    MEDIUM("Средний", "O'rta"),
    HIGH("Высокий", "Yuqori");

    private final String labelRu;
    private final String labelUz;

    Priority(String labelRu, String labelUz) {
        this.labelRu = labelRu;
        this.labelUz = labelUz;
    }

    public String getLabelRu() {
        return labelRu;
    }

    public String getLabelUz() {
        return labelUz;
    }
}