package unsw.skydiving;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Jump {
    private String id;
    private String type;
    private LocalDateTime time;
    private int numPeople;

    private ArrayList<Skydiver> skydivers;
    private Skydiver passenger;
    private Skydiver trainee;
    private Skydiver tandemMaster;
    private Skydiver instructor;

    private Flight flight;

    /**
     * 
     * @param id - unique id for Jump
     * @param type - type of jump
     * @param time - start time of jump
     */
    public Jump(String id, String type, LocalDateTime time) {
        setId(id);
        setType(type);

        this.time = time;
        this.numPeople = 0;
        
        skydivers = new ArrayList<Skydiver>();
        this.instructor = null;
        this.passenger = null;
        this.trainee = null;
        this.tandemMaster = null;
        
    }

    /**
     * 
     * @return jump.id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @return jump.type
     */
    public String getType() {
        return type;
    }

    /**
     * 
     * @return jump.time (start time of jump)
     */
    public LocalDateTime getTime() {
        return time;
    }

    public int getNumPeople() {
        return numPeople;
    }

    /**
     * 
     * @return jump.flight
     */
    public Flight getFlight() {
        return this.flight;
    }

    /**
     * 
     * @return list of skydivers for fun-type jumps
     */
    public ArrayList<Skydiver> getSkydivers() {
        return this.skydivers;
    }

    /**
     * 
     * @return passenger skydiver for tandem jumps
     */
    public Skydiver getPassenger() {
        return this.passenger;
    }

    /**
     * 
     * @return trainee skydiver for training jumps
     */
    public Skydiver getTrainee() {
        return this.trainee;
    }

    /**
     * 
     * @return instructor skydiver for training jumps
     */
    public Skydiver getInstructor() {
        return this.instructor;
    }

    /**
     * 
     * @return tandem master skydiver for tandem jump
     */
    public Skydiver getTandemMaster() {
        return this.tandemMaster;
    }

    /**
     * set jump id
     * @param id
     */
    private void setId(String id) {
        this.id = id;
    }

    /**
     * set jump type
     * @param type
     */
    private void setType(String type) {
        this.type = type;
    }

    /**
     * set jump start time
     * @param time
     */
    private void setTime(LocalDateTime time) {
        this.time = time;
    }

    /**
     * set number of people on this jump
     * @param n
     */
    private void setNumPeople(int n) {
        this.numPeople = n;
    }

    /**
     * set passenger for jump
     * @param passenger
     */
    private void setPassenger(Skydiver passenger) {
        this.passenger = passenger;
    }

    /**
     * set trainee for jump
     * @param trainee
     */
    private void setTrainee(Skydiver trainee) {
        this.trainee = trainee;
    }

    /**
     * set instructor for jump
     * @param instructor
     */
    private void setInstructor(Skydiver instructor) {
        this.instructor = instructor;
    }

    /**
     * set tandem master for jump
     * @param tandemMaster
     */
    private void setTandemMaster(Skydiver tandemMaster) {
        this.tandemMaster = tandemMaster;
    }

    /**
     * add a skydiver to the ArrayList<Skydiver> skydivers (for fun-jump)
     * @param funDiver
     */
    public void addFunDiver(Skydiver funDiver) {
        if(funDiver != null) {
            this.skydivers.add(funDiver);
        }
    }

    /**
     * add n people to existing jump
     * @param n
     */
    public void addNumPeople(int n) {
        int curr = getNumPeople();
        setNumPeople(curr+n);
    }

    /**
     * 
     * @return number of people for fun-jumps (ArrayList<Skydiver> skydivers)
     */
    public int funDiverSize() {
        return getSkydivers().size();
    }

    /**
     * add passenger
     * @param passenger
     */
    public void addPassenger(Skydiver passenger) {
        setPassenger(passenger);
    }

    /**
     * add trainee
     * @param trainee
     */
    public void addTrainee(Skydiver trainee) {
        setTrainee(trainee);
    }

    /**
     * add instructor
     * @param insturctor
     */
    public void addInstructor(Skydiver i) {
        
        setInstructor(i);
    }

    /**
     * add tandem master
     * @param tandemMaster
     */
    public void addTandemMaster(Skydiver tandemMaster) {
        setTandemMaster(tandemMaster);
    }

    /**
     * calculate number of people on the jump
     * @return number of people
     */
    public int calculateNumPeople() {
        if(this.type.equals("fun")) {
            return this.getSkydivers().size();
        } else {
            //if not a fun jump, always have 2 people
            return 2;
        }
    }

    /**
     * Attempts to assign a suitable flight for a jump
     * @param flightList - full list of flights reigstered
     * @param type - type of jump
     * @param time - request's starttime
     * @param numSkydivers - number of skydivers
     */

    public void assignFlight(ArrayList<Flight> flightList, String type, LocalDateTime time, int numSkydivers) {

        Flight found = null;

        if(type.equals("tandem")) {
            time = time.plusMinutes(5);
        }

        for(Flight i : flightList) {
            LocalDateTime flightStart = i.getStarttime();

            if((checkIfSameDate(flightStart, time)) == 1) {
                if((time.equals(flightStart)) || (time.isBefore(flightStart))) {
                    //check if maxload is full
                    if(found != null) {
                        if(findEarlierTime(i.getStarttime(), found.getStarttime())){
                            int currentLoad = i.getCurrentLoad();
                            int maxload = i.getMaxload();
                            if(numSkydivers + currentLoad <= maxload) {
                                //found a flight
                                found = i;
                            }
                        }
                    } else {
                        int currentLoad = i.getCurrentLoad();
                        int maxload = i.getMaxload();
                        if(numSkydivers + currentLoad <= maxload) {
                            //found a flight
                            found = i;
                        }
                    }

                }
            }
        }

        if(found != null) {
            found.addToLoad(numSkydivers);
            this.setTime(found.getStarttime());
        }

        this.flight = found;
        
    }

    /**
     * Check if LocalDateTime a is before b
     * @param a
     * @param b
     * @return 1 if a is before b, 0 if not
     */
    public boolean findEarlierTime(LocalDateTime a, LocalDateTime b) {
        return(a.isBefore(b));
    }

    public int checkIfSameDate(LocalDateTime a, LocalDateTime b) {
        int aDate = a.getDayOfYear();
        int bDate = b.getDayOfYear();

        if(aDate == bDate) {
            aDate = a.getYear();
            bDate = b.getYear();
            if(aDate == bDate) {
                return 1;
            }
        }

        return 0;

    }

    /**
     * finds a list of instructors/tandem-masters that also have matching dropzone to flight
     * @param skydivers
     * @param flight
     * @param time
     * @param passenger
     * @return ArrayList<Skydivers> of suitable instructors/tandem-masters
     */
    public ArrayList<Skydiver> findAvailableInstructor(ArrayList<Skydiver> skydivers, Flight flight, LocalDateTime time, Skydiver passenger, String type) {
        ArrayList<Skydiver> instructors = new ArrayList<Skydiver>();
        for(Skydiver i : skydivers) {
            if(type.equals("tandem")) {
                if((i.getLicence().equals("tandem-master")) && !(i.equals(passenger))) {
                    if(i.getDropzone().equals(flight.getDropzone())) {
                        instructors.add(i);
                    }
                }
            } else {
                if(!(i.equals(trainee))) {
                    if((i.getLicence().equals("tandem-master")) || (i.getLicence().equals("instructor"))) {
                        if(i.getDropzone().equals(flight.getDropzone())) {
                            instructors.add(i);
                        }
                    }
                }
            }
        }

        return instructors;
    }

}
