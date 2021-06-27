package unsw.skydiving;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Skydiver {
    private String name;
    private String licence;
    private String dropzone;

    //can hold 1 at a time
    private HashMap<LocalDateTime, LocalDateTime> bookedTimes;

    public Skydiver(String name, String licence) {
        setName(name);
        setLicence(licence);
        this.dropzone = null;

        bookedTimes = new HashMap<LocalDateTime, LocalDateTime>();
    }

    public String getName() {
        return name;
    }

    public String getLicence() {
        return licence;
    }

    public String getDropzone() {
        return dropzone;
    }

    public HashMap<LocalDateTime, LocalDateTime> getBookedTimes() {
        return this.bookedTimes;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setLicence(String licence) {
        this.licence = licence;
    }

    private void setDropzone(String dropzone) {
        this.dropzone = dropzone;
    }

    public void addNewBooking(LocalDateTime start, LocalDateTime end) {
        this.bookedTimes.put(start, end);
    }

    public void removeBooking() {
        this.bookedTimes.clear();
    }

    public LocalDateTime findEndTime(LocalDateTime start) {
        for(LocalDateTime t : this.bookedTimes.keySet()) {
            if(t.equals(start)) {
                return bookedTimes.get(t);
            }
        }

        return null;
    }

    public LocalDateTime setEndTimeWithPack(LocalDateTime time) {
        if(getLicence().equals("student")) {
            return time;
        } else {
            return (time.plusMinutes(10));
        }
    }

    public void addDropzone(String dropzone) {
        setDropzone(dropzone);
    }

}
