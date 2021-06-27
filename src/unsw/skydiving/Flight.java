package unsw.skydiving;

import java.time.LocalDateTime;

public class Flight {
    private String id;
    private int maxload;
    private LocalDateTime starttime;
    private LocalDateTime endtime;
    private String dropzone;
    private int currentLoad;

    /**
     * 
     * @param id - id of Flight
     * @param maxload of flight
     * @param starttime of flight
     * @param endtime of flight
     * @param dropzone of flight
     */
    public Flight(String id, int maxload, LocalDateTime starttime, LocalDateTime endtime, String dropzone) {
        setId(id);
        setMaxload(maxload);
        setStarttime(starttime);
        setEndtime(endtime);
        setDropzone(dropzone);
    }
    
    /**
     * get flight id
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * get maxload of flight
     * @return
     */
    public int getMaxload() {
        return maxload;
    }

    /**
     * get start time of flight
     * @return
     */
    public LocalDateTime getStarttime() {
        return starttime;
    }

    /**
     * get end time of flight
     * @return
     */
    public LocalDateTime getEndtime() {
        return endtime;
    }

    /**
     * get dropzone of flight
     * @return
     */
    public String getDropzone() {
        return dropzone;
    }

    /**
     * 
     * @return number of people on flight
     */
    public int getCurrentLoad() {
        return currentLoad;
    }

    /**
     * set flight id
     * @param id 
     */
    private void setId(String id) {
        this.id = id;
    }

    /**
     * set maxload
     * @param maxload
     */
    private void setMaxload(int maxload) {
        this.maxload = maxload;
    }

    /**
     * set flight start time
     * @param starttime
     */
    private void setStarttime(LocalDateTime starttime) {
        this.starttime = starttime;
    }

    /**
     * set flight end time
     * @param endtime
     */
    private void setEndtime(LocalDateTime endtime) {
        this.endtime = endtime;
    }

    /**
     * set dropzone of flight
     * @param dropzone
     */
    private void setDropzone(String dropzone) {
        this.dropzone = dropzone;
    }

    /**
     * set how many people are currently on flight
     * @param currentLoad
     */
    private void setCurrentLoad(int currentLoad) {
        this.currentLoad = currentLoad;
    }

    /**
     * add a number (load) of people onto flight and update it's current load
     * @param load
     */
    public void addToLoad(int load) {
        if(this.currentLoad + load > this.maxload) {
            System.out.println("Error: Exceeding Flight" + this.id +"'s Max Capacity.");
        } else {
            this.currentLoad += load;
        }

    }

    /**
     * remove a number (load) of people from flight
     * @param load
     */
    public void removeFromLoad(int load) {
        int currentLoad = getCurrentLoad();
        currentLoad -= load;
        if(currentLoad < 0) {
            currentLoad = 0;
        } 

        this.currentLoad = currentLoad;
    }
}
