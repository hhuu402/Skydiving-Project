package unsw.skydiving;

public class Dropzone {
    private String name;

    /**
     * 
     * @param name of dropzone
     */
    public Dropzone(String name) {
        setName(name);
    }

    /**
     * 
     * @return dropzone name
     */
    public String getName() {
        return name;
    }

    /**
     * set dropzone name
     * @param name
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * check if two dropzones are the same object
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(getClass() != obj.getClass()) {
            return false;
        }
        
        Dropzone d = (Dropzone)obj;
        if(name.equals(d.name) == false) {
            return false;
        }

        return true;
    }
}
