package com.nuc.omeletteinputmethod.entityclass;

public class ScheduleEntity {
    private int id;
    private String time;
    private String info;

    public ScheduleEntity(int id, String time, String info) {
        this.id = id;
        this.time = time;
        this.info = info;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "ScheduleEntity{" +
                "id=" + id +
                ", time='" + time + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}
