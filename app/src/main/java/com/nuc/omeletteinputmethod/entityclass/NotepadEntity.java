package com.nuc.omeletteinputmethod.entityclass;

public class NotepadEntity {
    private int id;
    private String text;
    private String imageStr;

    public NotepadEntity(int id, String text, String imageStr) {
        this.id = id;
        this.text = text;
        this.imageStr = imageStr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageStr() {
        return imageStr;
    }

    public void setImageStr(String imageStr) {
        this.imageStr = imageStr;
    }
}
