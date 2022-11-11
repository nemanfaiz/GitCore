package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** Represents Staging Area for Addition that can be serialized.
 * @author Neman Faiz
 */
public class Staging implements Serializable {

    /** Hashmap for staging area for addition where
     * name of file is mapped to blob of the file. */
    private HashMap<String, String> stagedForAddition;

    /**Staging class constructor.
     * creating an object of stage for addition
     * @param  add HashMap for addition */
    protected Staging(HashMap<String, String> add) {
        this.stagedForAddition = add;

    }

    /**@return HashMap instance variable - stageForAddition. */
    protected HashMap<String, String> getStagedForAddition() {
        return stagedForAddition;
    }



}
