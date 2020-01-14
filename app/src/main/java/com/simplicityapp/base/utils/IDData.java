package com.simplicityapp.base.utils;

public class IDData {
    private String inputText;
    private String surname;
    private String name;
    private String gender;
    private String idNumber;
    private String birthDate;

    public IDData() {

    }

    public void processIdData(String input) {
        inputText = input.substring(input.indexOf("@") +1 );
        this.surname = inputText.substring(0, inputText.indexOf("@"));
        inputText = inputText.substring(inputText.indexOf("@") + 1);
        this.name = inputText.substring(0, inputText.indexOf("@"));
        inputText = inputText.substring(inputText.indexOf("@") + 1);
        this.gender = inputText.substring(0, inputText.indexOf("@"));
        inputText = inputText.substring(inputText.indexOf("@") + 1);
        this.idNumber = inputText.substring(0, inputText.indexOf("@"));
        inputText = inputText.substring(inputText.indexOf("@") + 1);
        inputText = inputText.substring(inputText.indexOf("@") + 1);
        this.birthDate = inputText.substring(0, inputText.indexOf("@"));

    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}
