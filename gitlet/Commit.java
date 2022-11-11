package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/** Represents a commit object that can be serialized.
 * @author Neman Faiz
 */
public class Commit implements Serializable {

    /**Message associated with the commit. */
    private final String message;

    /**Time stamp of commit when created. */
    private final String timeStamp;

    /**Container that contains file
     * information associated with the commit
     * this maps name of file to id of blob. */
    protected HashMap<String, String> trackedFiles;

    /**Parent to the commit. (the previous commit) */
    private final String parent;

    /**SHA-1 id for the commit. */
    private final String commitId;

    /**Merge parent of the commit. (second parent) */
    private final String mergeParent;


    /**Creating a commit object.
     * @param commitMessage message of this commit
     * @param commitParent parent of this commit
     * @param commitTrackedFiles file info of this commit.
     * This commit object has no merge parent
     * and when this commit is created the time stamp is set to that
     * point of time with the following format. */
    protected Commit(String commitMessage, String commitParent,
                     HashMap<String, String> commitTrackedFiles) {
        this.message = commitMessage;
        this.trackedFiles = commitTrackedFiles;
        this.parent = commitParent;
        this.mergeParent = null;

        SimpleDateFormat dateTimeFormatter =
                new SimpleDateFormat("E LLL dd HH:mm:ss yyyy Z");
        this.timeStamp = dateTimeFormatter.format(new Date());

        this.commitId = makeSerial();

    }

    /**Creating a commit object.
     * @param commitMessage message of this commit
     * @param commitParent parent of this commit
     * @param commitMergeCommit merge parent of this commit
     * @param commitTrackedFiles file info of this commit.
     * This commit object has no merge parent
     * and when this commit is created the time stamp is set to that
     * point of time with the following format. */
    protected Commit(String commitMessage,
                     String commitParent, String commitMergeCommit,
                     HashMap<String, String> commitTrackedFiles) {
        this.message = commitMessage;
        this.trackedFiles = commitTrackedFiles;
        this.parent = commitParent;
        this.mergeParent = commitMergeCommit;

        SimpleDateFormat dateTimeFormatter =
                new SimpleDateFormat("E LLL dd HH:mm:ss yyyy Z");
        this.timeStamp = dateTimeFormatter.format(new Date());

        this.commitId = makeSerial();

    }

    /**Making a SHA-1 ID for this commit object.
     * @return the SHA-1 id for the commit object. */
    protected String makeSerial() {
        byte[] serial = Utils.serialize(this);
        return Utils.sha1((Object) serial);
    }

    /**@return the SHA-1 ID of this commit object. */
    protected String getCommitId() {
        return commitId;
    }

    /**@return the message associated with this commit object. */
    protected String getMessage() {
        return this.message;
    }

    /**@return the time stamp this commit was created. */
    protected String getTimeStamp() {
        return this.timeStamp;
    }

    /**@return the HashMap that contains
     * file info that associated with this commit.  */
    protected HashMap<String, String> getTrackedFiles() {
        return this.trackedFiles;
    }

    /**@return the parent of this commit. */
    protected String getParent() {
        return this.parent;
    }

    /**@return the merge parent of this commit. */
    protected String getMergeParent() {
        return this.mergeParent;
    }


    /**This prints out commit id, time stamp,
     *  and message of each commit. */
    protected void getGlobalLogPrint() {

        System.out.println("===");
        System.out.print("commit ");
        System.out.println(getCommitId());
        System.out.print("Date: ");
        System.out.println(getTimeStamp());
        System.out.println(getMessage());
        System.out.println();
    }
}
