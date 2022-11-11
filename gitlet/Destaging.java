package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** Represents Staging Area for Removal that can be serialized.
 * @author Neman Faiz
 */
public class Destaging implements Serializable {

    /** Hashmap for staging area for removal where
     * name of file is mapped to blob of the file. */
    private final HashMap<String, String> stagedForRemoval;

    /**Detaging class constructor.
     * creating an object of stage for Removal
     * @param  remove HashMap for addition */
    protected Destaging(HashMap<String, String> remove) {

        this.stagedForRemoval = remove;

    }

    /**Mapping file name to blob to be removed.
     * @param fileName name of the file to be removed
     * @param blob blob of the file. */
    protected void stageForRemoval(String fileName, String blob) {
        this.stagedForRemoval.put(fileName, blob);
    }


}
