package com.javamail.model;

import jakarta.persistence.*;

@Entity
@Table(name = "labels")
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_email", nullable = false, length = 150)
    private String userEmail;

    @Column(name = "label_name", nullable = false, length = 100)
    private String labelName;

    @Column(length = 20)
    private String color = "#4CAF50";

    public Label() {}

    public Label(String userEmail, String labelName, String color) {
        this.userEmail = userEmail;
        this.labelName = labelName;
        this.color = color;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getLabelName() { return labelName; }
    public void setLabelName(String labelName) { this.labelName = labelName; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
