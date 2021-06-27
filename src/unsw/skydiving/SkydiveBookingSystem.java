package unsw.skydiving;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Skydive Booking System for COMP2511.
 *
 * A basic prototype to serve as the "back-end" of a skydive booking system. Input
 * and output is in JSON format.
 *
 * @author Xinyi Sun
 * 
 *
 */


public class SkydiveBookingSystem {
    /**
     * Constructs a skydive booking system. Initially, the system contains no flights, skydivers, jumps or dropzones
     */

    private ArrayList<Skydiver> skydiversList; //full list of all skydivers
    private ArrayList<Flight> flightList; //full list of flights
    private ArrayList<Dropzone> dropzoneList; //full list of dropzones
    private ArrayList<Jump> jumpList; //full list of jumps

    public SkydiveBookingSystem() {
        this.skydiversList = new ArrayList<Skydiver>();
        this.flightList = new ArrayList<Flight>();
        this.dropzoneList = new ArrayList<Dropzone>();
        this.jumpList = new ArrayList<Jump>();
    }

    private void processCommand(JSONObject json) {

        switch (json.getString("command")) {

        case "flight":
            String id = json.getString("id");
            int maxload = json.getInt("maxload");
            LocalDateTime starttime = LocalDateTime.parse(json.getString("starttime"));
            LocalDateTime endtime = LocalDateTime.parse(json.getString("endtime"));
            String dropzone = json.getString("dropzone");

            //create new dropzone and add to dropzoneList if it has not been seen before
            newDropzone(dropzone, dropzoneList);

            //create flight, add to list of flights
            Flight flight  = new Flight(id, maxload, starttime, endtime, dropzone);
            this.flightList.add(flight);
            break;

        case "skydiver":
            String name = json.getString("skydiver");
            String licence = json.getString("licence");

            Skydiver skydiver = new Skydiver(name, licence);

            //if skydiver has dropzone, add it to skydiver.dropzone
            if((licence.equals("instructor") || licence.equals("tandem-master"))){
                String diver_dropzone = json.getString("dropzone");
                skydiver.addDropzone(diver_dropzone);

                //if this dropzone hasn't been seen before, add it to dropzoneList
                newDropzone(diver_dropzone, dropzoneList);
            }

            //add to list of skydivers
            this.skydiversList.add(skydiver);

            break;

        case "request":
            String type = json.getString("type");
            String request_id = json.getString("id");
            LocalDateTime request_starttime = LocalDateTime.parse(json.getString("starttime"));

            //create new jump
            Jump jump = new Jump(request_id, type, request_starttime);

            if(type.equals("fun")) {
                //use jsonarray to get all array elements and conver them to ArrayList<string>
                JSONArray jsonArray = json.getJSONArray("skydivers");
                ArrayList<String> funSkydivers = jsonToArrayList(jsonArray);
                
                for(String i : funSkydivers) {
                    //findSkydiver on skydiversList
                    Skydiver funDiver = findSkydiver(i, skydiversList);

                    //add skydiver
                    if(funDiver != null) {
                        jump.addFunDiver(funDiver);
                    }
            
                }
                
                //register request
                bookForFun(jump, request_starttime);

                break;
                

            } else if(type.equals("tandem")){
                String passengerName = json.getString("passenger");
                Skydiver passenger = findSkydiver(passengerName, skydiversList);

                jump.addPassenger(passenger);

                //find a flight for this jump
                jump.assignFlight(flightList, type, request_starttime, 2);
                
                if(jump.getFlight() == null) {
                    //cannot find a flight for this jump, submit rejection
                    JSONObject result = returnRejected();
                    System.out.println(result.toString(2));
                    break;

                }

                //register request
                bookForOtherTypes(type, jump, request_starttime, passenger);

                break;

            } else if(type.equals("training")){
                String traineeName = json.getString("trainee");
                Skydiver trainee = findSkydiver(traineeName, skydiversList);

                jump.addTrainee(trainee);

                //find a flight for this jump
                jump.assignFlight(flightList, type, request_starttime, 2);

                if(jump.getFlight() == null) {
                    //cannot find a flight for this jump, submit rejection
                    JSONObject result = returnRejected();
                    System.out.println(result.toString(2));
                    break;   
                }
                
                //register request
                bookForOtherTypes(type, jump, request_starttime, trainee);

                break;
            }
        
        case "change":
            /**
             * for "change", I will
             * 1. save the jump to be changed as "oldJump", create a new jump "newJump" for the new jump
             * 2. save the index of oldJump on jumpList
             * 3. remove oldJump's skydiver(s)' booking and from jumpList
             * 4. attempt to add newJump
             * 5. if newJump cannot be added, add oldJump and put it back into jumpList at index
             */
            //step 1
            Jump removeThisJump = findJumpByJumpId(json.getString("id"));
            if(removeThisJump == null) {
                break;
            }
            Jump oldJump = removeThisJump;

            //step 2
            int index = 0;
            for(Jump j : jumpList) {
                if(j.getId().equals(removeThisJump.getId())) {
                    break;
                }
                index++;
            }

            //step 3
            Flight changeFlight = removeThisJump.getFlight();
            int changeNumPeople = removeThisJump.calculateNumPeople();
            changeFlight.removeFromLoad(changeNumPeople);
            
            removeBooking(removeThisJump);

            this.jumpList.remove(removeThisJump);

            //4
            String change_type = json.getString("type");
            String change_id = json.getString("id");
            LocalDateTime change_starttime = LocalDateTime.parse(json.getString("starttime"));
            Jump newJump = new Jump(change_id, change_type, change_starttime);

            if(change_type.equals("fun")) {
                JSONArray jsonArray = json.getJSONArray("skydivers");
                ArrayList<String> funSkydivers = jsonToArrayList(jsonArray);
                for(String i : funSkydivers) {
                    //findSkydiver
                    Skydiver funDiver = findSkydiver(i, skydiversList);

                    //add skydiver
                    if(funDiver != null) {
                        newJump.addFunDiver(funDiver);
                    }
            
                }
                
                int i = changeBookForFun(newJump, change_starttime, "true");
                if(i == 0) {
                    //failed to change booking
                    //5
                    removeBooking(newJump);

                    i = changeBookForFun(oldJump, oldJump.getTime(), "false");
                    this.jumpList.add(index, oldJump);

                }

                

            } else if(change_type.equals("tandem")){
                String passengerName = json.getString("passenger");
                Skydiver passenger = findSkydiver(passengerName, skydiversList);

                //add jump
                newJump.addPassenger(passenger);
                newJump.assignFlight(flightList, change_type, change_starttime, 2);
                
                int i = changeBookForOtherTypes(change_type, newJump, change_starttime, passenger, "true");
                if(i == 0 || (newJump.getFlight() == null)) {
                    //failed to change booking
                    //5
                    removeBooking(newJump);

                    i = (changeBookForOtherTypes(oldJump.getType(), oldJump, oldJump.getTime(), oldJump.getPassenger(), "false"));
                    this.jumpList.add(index, oldJump);
                }


            } else if(change_type.equals("training")){
                String traineeName = json.getString("trainee");
                Skydiver trainee = findSkydiver(traineeName, skydiversList);
                //add jump
                newJump.addTrainee(trainee);
                newJump.assignFlight(flightList, change_type, change_starttime, 2);

                int i = changeBookForOtherTypes(change_type, newJump, change_starttime, trainee, "true");
                if(i == 0 || (newJump.getFlight() == null)) {
                    //failed to change booking
                    //5
                    removeBooking(newJump);
                    i = changeBookForOtherTypes(oldJump.getType(), oldJump, oldJump.getTime(), oldJump.getTrainee(), "false");
                    this.jumpList.add(index, oldJump);
                }

            }

            break;

        case "cancel":
            String cancel_id = json.getString("id");
            
            Jump cancelThis = findJumpByJumpId(cancel_id);

            //removes number of skydivers from the associated flight
            Flight thisFlight = cancelThis.getFlight();
            int numPeopleOnJump = cancelThis.calculateNumPeople();
            thisFlight.removeFromLoad(numPeopleOnJump);

            //remove this jump from skydiver's schedule
            removeBooking(cancelThis);
            
            this.jumpList.remove(cancelThis);
        
            break;

        case "jump-run":
            JSONObject result = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            String jr_id = json.getString("id");
            
            ArrayList<Jump> foundJumps = findJumpsByFlightId(jr_id, jumpList);
            ArrayList<Jump> printJumps = returnAllFunSorted(foundJumps);
            
            if(!printJumps.isEmpty()) {
                JSONObject resultFun = new JSONObject();
                ArrayList<String> funJumperNames = new ArrayList<String>();
                for(Jump j : printJumps) {
                    for(Skydiver s : j.getSkydivers()) {
                        funJumperNames.add(s.getName());
                    }
                    
                }
                Collections.sort(funJumperNames);
                JSONArray jsonJumps = new JSONArray(funJumperNames);
                
                result.put("skydivers", jsonJumps);
                resultFun.put("skydivers", jsonJumps);
                jsonArray.put(resultFun); 

            }

            printJumps = returnJumpByType(foundJumps, "training");
            JSONObject resultTraining = new JSONObject();
            for(Jump j : printJumps) {
                
                if((j.getInstructor() != null) && (j.getTrainee() != null)) {
                    result.put("instructor", (j.getInstructor()).getName());
                    result.put("trainee", (j.getTrainee()).getName());

                    resultTraining.put("instructor", (j.getInstructor()).getName());
                    resultTraining.put("trainee", (j.getTrainee()).getName());
                    jsonArray.put(resultTraining);
                }
                
            }

            printJumps = returnJumpByType(foundJumps, "tandem");
            JSONObject resultTandem = new JSONObject();
            for(Jump j : printJumps) {
                if((j.getPassenger() != null) && (j.getTandemMaster() != null)) {
                    result.put("passenger", (j.getPassenger()).getName());
                    result.put("jump-master", (j.getTandemMaster()).getName());
                    resultTandem.put("passenger", (j.getPassenger()).getName());
                    resultTandem.put("jump-master", (j.getTandemMaster()).getName());

                    jsonArray.put(resultTandem);
                }
            }

            System.out.println(jsonArray.toString(2));

            break;
        }
    }

    /**
     * This function adds new dropzone to list of dropzones (dropzonelist) if it is not on the list
     * @param dropzone is the new dropzone to be examined
     * @param dropzoneList is the list of registered dropzones
     */
    public void newDropzone(String dropzone, ArrayList<Dropzone> dropzoneList) {
        int check = 0;
        for(Dropzone i : dropzoneList) {
            if(dropzone.equals(i.getName())) {
                check = 1;
                break;
            }
        }
        
        if(check == 0) {
            Dropzone newDropzone = new Dropzone(dropzone);
            this.dropzoneList.add(newDropzone);
        }
    }

    /**
     * Given list of skydivers, find the corresponding one by name
     * @param name of the skydiver to be found
     * @param skydiverList is full the list of skydivers
     * @return the found skydiver or null
     */
    public Skydiver findSkydiver(String name, ArrayList<Skydiver> skydiverList) {
        for(Skydiver i : skydiverList) {
            if(name.equals(i.getName())) {
                return i;
            }
        }
        return null;
    }

    /**
     * finds a list of jumps with flight id 'id'
     * @param id the FLIGHT id of the flight you want to find
     * @param jumpList the full list of registered jumps
     * @return ArrayList of jumps found
     */
    private ArrayList<Jump> findJumpsByFlightId(String id, ArrayList<Jump> jumpList) {
        ArrayList<Jump> foundJumps = new ArrayList<Jump>();

        for(Jump j : jumpList) {
            Flight jumpFlight = j.getFlight();
            String jumpFlightId = jumpFlight.getId();
            if(jumpFlightId.equals(id)) {
                foundJumps.add(j);
            }
        }

        return foundJumps;
    }

    /**
     * finds jump by id in jumpList
     * @param id of jump to be found
     * @return jump, or null if not found
     */
    public Jump findJumpByJumpId(String id) {
        for(Jump j : this.jumpList) {
            String jId = j.getId();
            if(jId.equals(id)) {
                return j;
            }
        }
    
        return null;
    }

    /**
     * parses jsonArray to ArrayList<String>
     * @param jsonArray
     * @return ArrayList<String>
     */
    private ArrayList<String> jsonToArrayList(JSONArray jsonArray) {
        ArrayList<String> funSkydivers = new ArrayList<String>();
        for(int i = 0; i < jsonArray.length(); i++) {
            funSkydivers.add(jsonArray.get(i).toString());
        }
    
        return funSkydivers;
    }
    
    /**
     * Attempt to create a booking for a fun jump
     * @param j = jump
     * @param start = request's "starttime"
     */
    private void bookForFun(Jump j, LocalDateTime start) {
        int size = (j.getSkydivers()).size();
        j.assignFlight(this.flightList, j.getType(), start, size);
    
        if(j.getFlight() == null) {
            JSONObject result = returnRejected();
            System.out.println(result.toString(2));
        }
    
        int canArrive = 0;
        for(Skydiver i : j.getSkydivers()) {
            canArrive += canSkydiverArrive(i, j.getTime(), j.getFlight(), j.getType());
        }
    
        if(canArrive == 0) {
            //skydiver(s) can join skydive (no schedule clash)
            LocalDateTime newStart = (j.getFlight()).getStarttime();
            LocalDateTime newEnd = (j.getFlight()).getEndtime();
            for(Skydiver i : j.getSkydivers()) {
                if(i.getLicence().equals("student")) {
                    i.removeBooking();
                    i.addNewBooking(newStart, newEnd);
                } else {
                    i.removeBooking();
                    newEnd = newEnd.plusMinutes(10);
                    i.addNewBooking(newStart, newEnd);
                }
                
            }
            this.jumpList.add(j);
            JSONObject result = returnSuccess(j);
            System.out.println(result.toString(2));
        } else {
    
            JSONObject result = returnRejected();
            System.out.println(result.toString(2));
        }
    }

    /**
     * Attempt to create a booking for a request for non-fun type jumps
     * @param type of jump
     * @param jump
     * @param time is request's "starttime"
     * @param passOrTrainee is either jump.passenger or jump.trainee, depending on type
     */
    private void bookForOtherTypes(String type, Jump jump, LocalDateTime time, Skydiver passOrTrainee) {
        Skydiver instructor = null;
        //time = request_starttime
    
        ArrayList<Skydiver> instructorsList = jump.findAvailableInstructor(this.skydiversList, jump.getFlight(), time, passOrTrainee, type);
        for(Skydiver i : instructorsList) {
            if(canSkydiverArrive(i, time, jump.getFlight(), type) == 0) {
                if(type.equals("tandem")) {
                    instructor = i;
                    jump.addTandemMaster(instructor);   
                } else {
                    instructor = i;
                    jump.addInstructor(instructor);
                   
                }

                break;
            }

        }
        
        if(instructor != null) {
            LocalDateTime start = (jump.getFlight()).getStarttime();
            LocalDateTime end = ((jump.getFlight()).getEndtime());
    
            instructor.removeBooking();
            passOrTrainee.removeBooking();

            if(type.equals("tandem")) {
                
                end = instructor.setEndTimeWithPack(end);
                instructor.addNewBooking(start, end);

                end = passOrTrainee.setEndTimeWithPack(end);
                passOrTrainee.addNewBooking(start, end);
    
            } else {
                end = instructor.setEndTimeWithPack(end);
                end = end.plusMinutes(15);
                instructor.addNewBooking(start, end);
                
                end = passOrTrainee.setEndTimeWithPack(end);
                end = end.plusMinutes(15);
                passOrTrainee.addNewBooking(start, end);
            }
    
            this.jumpList.add(jump);
            JSONObject result = returnSuccess(jump);
            System.out.println(result.toString(2));
    
        } else {

            JSONObject result = returnRejected();
            System.out.println(result.toString(2));
            
        }
    }
    
    /**
     * Removes a jump booking from jumpList and deletes its jump duration all relevant skydiver's schedule
     * @param j
     */
    private void removeBooking(Jump j) {
        if(j.getType() == null) {
            return;
        }

        HashMap<LocalDateTime, LocalDateTime> searchBookings = new HashMap<LocalDateTime, LocalDateTime>();

        switch(j.getType()) {
        
        case "fun":
            for(Skydiver i : j.getSkydivers()) {
                searchBookings = i.getBookedTimes();
                if(searchBookings.isEmpty()) {
                    continue;
                } else {
                    i.removeBooking();
                }
                
            }

            break;

        case "tandem":
            Skydiver tandemMaster = j.getTandemMaster();
            Skydiver passenger = j.getPassenger();

            if(tandemMaster == null || passenger == null) {
                break;
            } else {
                searchBookings = tandemMaster.getBookedTimes();
                if(!searchBookings.isEmpty()) {           
                    tandemMaster.removeBooking();
                    break;
                }

                searchBookings = passenger.getBookedTimes();
                if(!searchBookings.isEmpty()) {

                    passenger.removeBooking();
                    break;
                }
            }

            break;

        case "training":
            Skydiver trainee = j.getTrainee();
            Skydiver instructor = j.getInstructor();

            searchBookings = trainee.getBookedTimes();
            trainee.removeBooking();


            searchBookings = instructor.getBookedTimes();
            instructor.removeBooking();
            
            break;
        }

        return;
    }
    

    /**
     * given list of jumps, find all "fun" type jumps and sort them from largest to smallest
     * if it is the same size, then will organise by first registered, second registered...etc
     * @param jumpList is the list of jumps
     * @return sorted ArrayList<jump> of all "fun" types
     */
    private ArrayList<Jump> returnAllFunSorted(ArrayList<Jump> jumpList) {
        ArrayList<Jump> funJumps = new ArrayList<Jump>();
        for(Jump j : jumpList) {
            if((j.getType()).equals("fun")) {
                funJumps.add(j);
            }
        }

        ArrayList<Jump> funJumpsSorted = new ArrayList<Jump>();
        int count = 0;
        int prevCount = 0;
        Jump largest = null;
        while(funJumpsSorted.size() > jumpList.size()) {
            for(Jump j : funJumps) {
                if(count < j.funDiverSize()) {
                    prevCount = count;
                    count = j.funDiverSize();
                    largest = j;
                }
            }
            funJumpsSorted.add(largest);

            funJumps.remove(largest);

            count = prevCount;
            prevCount = 0;
        }

        return funJumps;
    }

    /**
     * given list of jumps, return only jumps by type (should be sorted in order of 1st, 2nd...etc to register)
     * @param jumps - list of jumps
     * @param type - the type of jump to search for
     * @return ArrayList of found jumps of type 'type'
     */
    private ArrayList<Jump> returnJumpByType(ArrayList<Jump> jumps, String type) {
        ArrayList<Jump> returnArray = new ArrayList<Jump>();
        for(Jump j : jumps) {
            if(j.getType().equals(type)) {
                returnArray.add(j);
            }
        }
        return returnArray;
    }

    /**
     * canSkydiverArrive returns an int > 0 if given skydiver's schedule clashes with new booking time
     * and so cannot join the skydive
     * @param skydiver
     * @param requestTime
     * @param f
     * @param type
     * @return int count > 0 if schedule has a clash, == 0 if no clashes
     */

    private int canSkydiverArrive(Skydiver skydiver, LocalDateTime requestTime, Flight f, String type) {
        LocalDateTime predictEnd = f.getEndtime();

        int isThereBooking = (skydiver.getBookedTimes()).size();
        if(isThereBooking == 0) {
            return 0;
        }

        if(skydiver.getLicence().equals("student")) {
            if(type.equals("training")) {
                predictEnd = predictEnd.plusMinutes(15);
            }
        } else {
            predictEnd = predictEnd.plusMinutes(10);
            if(type.equals("training")) {
                predictEnd = predictEnd.plusMinutes(15);
            }
        }

        HashMap<LocalDateTime, LocalDateTime> allBooked = skydiver.getBookedTimes();
        int count = 0;
        for(LocalDateTime t : allBooked.keySet()) {
            if(checkIfSameDate(t, requestTime) == 1) {
                LocalDateTime bookedStart = t;
                LocalDateTime bookedEnd = allBooked.get(t);

                if(bookedStart.isBefore(requestTime)) {
                    if(bookedEnd.isBefore(requestTime)) {
                        continue;
                    } else if(bookedEnd.isEqual(requestTime)) {
                        continue;
                    } else {
                        count++;
                    }
                } else if(bookedStart.isEqual(requestTime)) {
                    return 1;
                } else {
                    if(bookedStart.isAfter(predictEnd) || bookedStart.isEqual(predictEnd)) {
                        continue;
                    } else {
                        count++;
                    }
                }
            }
        }

        return count;

    }

    /**
     * Same as BookForFun but returns an int to indicate either booking success or failure
     * @param j for jump
     * @param start
     * @return 0 for failed to book, 1 for success in booking
     */
    private int changeBookForFun(Jump j, LocalDateTime start, String print) {
        int size = (j.getSkydivers()).size();
        j.assignFlight(this.flightList, j.getType(), start, size);
    
        if(j.getFlight() == null) {
            if(print.equals("true")) {
                JSONObject result = returnRejected();
                System.out.println(result.toString(2));
            }
            return 0;
        }
    
        int canArrive = 0;
        for(Skydiver i : j.getSkydivers()) {
            canArrive += canSkydiverArrive(i, j.getTime(), j.getFlight(), j.getType());
        }
    
        if(canArrive == 0) {

            //skydiver(s) can join skydive (no schedule clash)
            LocalDateTime newStart = (j.getFlight()).getStarttime();
            LocalDateTime newEnd = (j.getFlight()).getEndtime();
            for(Skydiver i : j.getSkydivers()) {
                if(i.getLicence().equals("student")) {
                    i.removeBooking();
                    i.addNewBooking(newStart, newEnd);
                } else {
                    i.removeBooking();
                    newEnd = newEnd.plusMinutes(10);
                    i.addNewBooking(newStart, newEnd);
                }
                
            }

            
            this.jumpList.add(j);
            if(print.equals("true")) {
                JSONObject result = returnSuccess(j);
                System.out.println(result.toString(2));
            }
            return 1; //success
        } else {
            if(print.equals("true")) {
                JSONObject result = returnRejected();
                System.out.println(result.toString(2));
            }
            return 0; //failure
        }
    }

    /**
     * Same as BookingForOtherTypes but returns an int to indicate either booking success or failure
     * @param type
     * @param jump
     * @param time
     * @param passOrTrainee
     * @return 0 for failure, 1 for success
     */
    private int changeBookForOtherTypes(String type, Jump jump, LocalDateTime time, Skydiver passOrTrainee, String print) {
        Skydiver instructor = null;
    
        ArrayList<Skydiver> instructorsList = jump.findAvailableInstructor(this.skydiversList, jump.getFlight(), time, passOrTrainee, type);
        for(Skydiver i : instructorsList) {
            if(canSkydiverArrive(i, time, jump.getFlight(), type) == 0) {
                if(type.equals("tandem")) {
                    instructor = i;
                    jump.addTandemMaster(i);
                                 
                } else {
                    instructor = i;
                    jump.addInstructor(i);
                   
                }

                break;
            }

        }
        
        if(instructor != null) {
            LocalDateTime start = (jump.getFlight()).getStarttime();
            LocalDateTime end = ((jump.getFlight()).getEndtime());

            instructor.removeBooking();
            passOrTrainee.removeBooking();
    
            if(type.equals("tandem")) {
                end = instructor.setEndTimeWithPack(end);
                instructor.addNewBooking(start, end);
    
               
                end = passOrTrainee.setEndTimeWithPack(end);
                passOrTrainee.addNewBooking(start, end);
    
            } else {
                end = instructor.setEndTimeWithPack(end);
                end = end.plusMinutes(15);
                instructor.addNewBooking(start, end);

                end = passOrTrainee.setEndTimeWithPack(end);
                end = end.plusMinutes(15);
                passOrTrainee.addNewBooking(start, end);
            }

            if(print.equals("true")) {
                JSONObject result = returnSuccess(jump);
            System.out.println(result.toString(2));
            }
            this.jumpList.add(jump);
            return 1; //success
    
        } else {
            if(print.equals("true")) {
                JSONObject result = returnRejected();
                System.out.println(result.toString(2));
            }
            return 0; //failure
            
        }
    }

    /**
     * check if 2 LocalDateTime objects share the same date
     * @param a
     * @param b
     * @return 1 if they have the same date, 0 for different dates
     */

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
     * Outputs command reigster success for JSON
     * @param jump
     * @return
     */
    private JSONObject returnSuccess(Jump jump) {
        JSONObject result = new JSONObject();

        result.put("flight", jump.getFlight().getId());
        result.put("dropzone", jump.getFlight().getDropzone());
        result.put("status", "success");

        return result;
    }

    /**
     * Outputs command reigster rejection for JSON
     * @return
     */
    private JSONObject returnRejected() {
        JSONObject result = new JSONObject();

        result.put("status", "rejected");

        return result;
    }

    public static void main(String[] args) {
        SkydiveBookingSystem system = new SkydiveBookingSystem();

        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (!line.trim().equals("")) {
                JSONObject command = new JSONObject(line);
                system.processCommand(command);
            }
        }
        sc.close();
    }

}
