package gitlet;
import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;

/** Represents a Repository for the Gitlet.
 * All the commands take place here.
 * Objects an file can be serialized.
 * @author Neman Faiz
 */
public class Repository implements Serializable {
    /**String instance represent the head.
     * This head stores current branch. */
    private final String head;
    /**String instance represent the branch.
     * This branch stores the last Commit made. */
    private String branch;
    /**This represents object of staging class.
     * Here we store files and their blobs
     * to staged them for Addition. */
    private Staging staging;
    /**This represents objects of Destaging class.
     * Here we store files and their blobs
     * to staged them for Removal.*/
    private Destaging destaging;
    /**Represents the current working directory. */
    private final File currentDirectory = new File(".");
    /**Represents the main Gitlet directory. */
    private static final File GIT_DIR = new File("./.gitlet");
    /**Commit subdirectory to Git meta directory.
     * where we store Commit objects. */
    private static final File COMMIT_DIR = new File("./.gitlet/commits");
    /**Staging Area subdirectory to Git meta directory.
     * where we keep track of Files Staged for Addition
     * and file Staged for Removal. */
    private static final File STAGING_AREA_DIR =
            new File("./.gitlet/stagingArea");

    /**Staging subdirectory to Staging Area.
     * where we store Staging objects. */
    private static final File STAGING_DIR =
            new File("./.gitlet/stagingArea/staging");

    /**Destaging subdirectory to Staging Area.
     * where we store Destaging objects. */
    private static final File DESTAGING_DIR =
            new File("./.gitlet/stagingArea/destaging");


    /**Blob subdirectory to Git meta directory.
     * where we store the content of file with
     * SHA-1 name where we stored in commit staging objects. */
    private static final File BLOB_DIR = new File("./.gitlet/blobs");

    /**Branch subdirectory to Git meta directory.
     * where we store _Head file and branch files. */
    private static final File BRANCH_DIR = new File("./.gitlet/branches");

    /**Represents repository object.
     * Initializing instance variable head and branch. */
    public Repository() {
        this.head = "head";
        this.branch = "master";
    }

    /**Represents the init command.
     * Creates a new Gitlet version-control system in the current directory.
     * This will automatically start with one commit
     * a commit that contains no files and has the commit message initial commit
     * in the default branch master. */
    public void init() throws IOException {

        if (!GIT_DIR.exists()) {
            GIT_DIR.mkdir();
            STAGING_AREA_DIR.mkdir();
            STAGING_DIR.mkdir();
            DESTAGING_DIR.mkdir();
            COMMIT_DIR.mkdir();
            BLOB_DIR.mkdir();
            BRANCH_DIR.mkdir();

            initialCommit();


        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
    }
    /**Represents helper function to init command.
     * making a commit and updating commit in the branch and head. */
    private void initialCommit() throws IOException {

        Utils.join(BRANCH_DIR, head).createNewFile();

        Commit initialCommit = new Commit("initial commit",
                null, new HashMap<>());

        makeCommit(initialCommit);

        updateheadOfCommit();

    }

    /**Helper function that makes a commit in the commit directory.
     * @param newCommit the commit to be made. */
    private void makeCommit(Commit newCommit) throws IOException {
        newCommit.makeSerial();
        String commitSerial = newCommit.getCommitId();
        File commitPath = Utils.join(COMMIT_DIR, commitSerial);

        if (!commitPath.exists()) {
            commitPath.createNewFile();
        }

        Utils.writeObject(commitPath, newCommit);

        putCommitInBranch(commitSerial, branch);

    }

    /**Helper function that write the commit id into the current branch.
     * @param commitId SHA-1 id of commit.
     * @param branchName the name of given branch. */
    private void putCommitInBranch(String commitId,
                                   String branchName) throws IOException {

        try {
            File masterPath = Utils.join(BRANCH_DIR, branchName);
            masterPath.createNewFile();

            FileWriter fileWriter = new FileWriter(masterPath);
            BufferedWriter str = new BufferedWriter(fileWriter);
            str.write(commitId);
            str.close();

        } catch (IOException excp) {
            return;
        }

    }

    /**Helper function that write the name of current branch into head file. */
    private void updateheadOfCommit() {
        try {
            File headPath = Utils.join(BRANCH_DIR, head);
            headPath.createNewFile();

            FileWriter fileWriter = new FileWriter(headPath);
            BufferedWriter str = new BufferedWriter(fileWriter);
            str.write(branch);
            str.close();

        } catch (IOException excp) {
            return;
        }
    }



    /**Helper function that reads the string content from a File.
     * Note: I store the name of branch and commit id in txt files,
     * I couldn't use the function provided in Utils class effectively,
     * so I researched and found Buffer Reader class and examples.
     * I wanted to make a note about this.
     * @param path string representation of the path to read a string from.
     * @return the string that it read from the file. */
    private String readStringsOfFile(String path) {
        String reader = null;
        String str = "";
        File filePath = new File(path);

        try (BufferedReader buffer =
                     new BufferedReader(new FileReader(filePath))) {

            while ((reader = buffer.readLine()) != null) {
                str += reader;
            }
            return str;
        } catch (IOException excp) {
            return "error";
        }
    }

    /**Helper function that reads the current branch from
     * head file. */
    private void readBranch() {

        String branchPath = "./.gitlet/branches/" + head;

        if (new File(branchPath).exists()) {
            branch = readStringsOfFile(branchPath);
        }

    }

    /**Represents the add command.
     * @param fileName the name file to add. */
    public void add(String fileName) throws IOException {

        readBranch();

        if (!new File(fileName).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        File destagingPath = Utils.join(DESTAGING_DIR, fileName);
        if (destagingPath.exists()) {
            destagingPath.delete();
        }

        File currFile = null;
        for (File find : currentDirectory.listFiles()) {
            if (find.getName().equals(fileName)) {
                currFile = find;
            }
        }

        byte[] fileContent = Utils.readContents(currFile);
        String blob = Utils.sha1((Object) fileContent);
        Commit currCommit = getCommit();


        if (currCommit.getTrackedFiles().get(fileName) != null
                && currCommit.getTrackedFiles()
                .get(fileName).equals(blob)) {

            System.exit(0);
        }

        File blobPath = Utils.join(BLOB_DIR, blob);
        blobPath.createNewFile();
        Utils.writeContents(blobPath, (Object) fileContent);


        staging = new Staging(new HashMap<>());
        staging.getStagedForAddition().put(fileName, blob);

        File stagingPath = Utils.join(STAGING_DIR, fileName);
        stagingPath.createNewFile();
        Utils.writeObject(stagingPath, staging);

    }

    /**Helper function that handles the errors for commit command.
     * @param message the message of the commit. */
    private void handleCommitErrors(String message) {

        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);

        } else if (Objects
                .requireNonNull(STAGING_DIR.listFiles()).length == 0
                && Objects
                .requireNonNull(DESTAGING_DIR.listFiles()).length == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);

        }
    }

    /**Helper function that handle the staging Area for the commit.
     * @param trackedFiles map of fileName to blob container.
     * @return the updated map of fileName to blob. */
    private HashMap<String, String> handleStagingAreaForCommit(
            HashMap<String, String> trackedFiles) {
        for (File file : Objects.requireNonNull(DESTAGING_DIR.listFiles())) {
            trackedFiles.remove(file.getName());
            file.delete();
        }

        for (File file : Objects
                .requireNonNull(STAGING_DIR.listFiles())) {
            if (file.exists()) {

                Staging currStaging = Utils.readObject(file, Staging.class);

                if (currStaging.getStagedForAddition()
                        .containsKey(file.getName())) {

                    trackedFiles.put(file.getName(),
                            currStaging.getStagedForAddition()
                                    .get(file.getName()));

                    file.delete();
                }
            }

        }

        return trackedFiles;
    }


    /**Represents the Commit command.
     * @param message the message of the commit. */
    public void commit(String message)
                throws IOException {

        readBranch();

        handleCommitErrors(message);

        Commit currCommit = getCommit();
        HashMap<String, String> trackedFiles = new
                HashMap<>(currCommit.getTrackedFiles());

        trackedFiles = handleStagingAreaForCommit(trackedFiles);


        Commit newCommit = new Commit(message,
                                currCommit.getCommitId(), trackedFiles);

        makeCommit(newCommit);

    }

    /**Helper function that return the last commit from the current branch. */
    private Commit getCommit() throws IOException {

        String headBranch = readStringsOfFile("./.gitlet/branches/" + head);

        String commitName = readStringsOfFile("./.gitlet/branches/"
                + headBranch);
        File commitID = Utils.join(COMMIT_DIR, commitName);


        return Utils.readObject(commitID, Commit.class);
    }


    /**Represents the log command. */
    public void log() throws IOException {

        Commit currCommit = getCommit();

        while (currCommit != null) {

            System.out.println("===");
            System.out.println("commit " + currCommit.getCommitId());
            System.out.println("Date: " + currCommit.getTimeStamp());
            System.out.println(currCommit.getMessage());
            System.out.println();

            if (currCommit.getParent() != null) {
                File parentPath = Utils.join(COMMIT_DIR,
                        currCommit.getParent());
                currCommit = Utils.readObject(parentPath, Commit.class);
            } else {
                break;
            }
        }

    }

    /**Represents the checkout command.
     * @param args possible arguments for checkout. */
    public void checkout(String... args) throws IOException {
        if (args.length == 3) {
            checkoutFileName(args[2]);

        } else if (args.length == 4) {
            checkoutCommitId(args[1], args[3]);
        } else if (args.length == 2) {
            checkoutBranch(args[1]);
        }

    }

    /**Helper function to checkout command.
     * when there is only file name is given.
     * @param fileName the name of the file to checkout. */
    private void checkoutFileName(String fileName) throws IOException {

        Commit currCommit = getCommit();
        HashMap<String, String> currMap = currCommit.getTrackedFiles();

        if (STAGING_DIR.listFiles().length != 0) {
            System.exit(0);
        }

        if (!currMap.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }


        File currDir = Utils.join(currentDirectory, fileName);
        if (currDir.exists()) {
            Utils.restrictedDelete(currDir);
        }

        File blobPath = Utils.join(BLOB_DIR, currMap.get(fileName));

        currDir.createNewFile();
        Utils.writeContents(currDir, Utils.readContents(blobPath));

    }

    /**Helper function to checkout command.
     * when there is commit id and file name is given.
     * @param commitId the SHA-1 id of given id
     * @param fileName the name of file for the command. */
    private void checkoutCommitId(String commitId, String fileName)
            throws IOException {

        File commitFile = null;

        for (File file : COMMIT_DIR.listFiles()) {


            if (file.getName().equals(commitId)
                    || file.getName().contains(commitId)) {

                commitFile = file;
            }

        }


        if (commitFile == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);

        } else {

            Commit commitObj = Utils.readObject(commitFile, Commit.class);


            if (commitObj == null) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);


            } else if (!commitObj.getTrackedFiles().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);


            } else {

                File currDir = Utils.join(currentDirectory, fileName);

                if (currDir.exists()) {
                    Utils.restrictedDelete(currDir);
                }

                HashMap<String, String> currMap = commitObj.getTrackedFiles();

                File blobPath = Utils.join(BLOB_DIR, currMap.get(fileName));

                currDir.createNewFile();
                Utils.writeContents(currDir, Utils.readContents(blobPath));

            }

        }

    }



    /**Represents the rm command.
     * @param fileName the name of file to remove. */
    public void rm(String fileName) throws IOException {

        File stagingPath = Utils.join(STAGING_DIR, fileName);
        Commit currCommit = getCommit();
        HashMap<String, String> commitMap = currCommit.getTrackedFiles();


        if (stagingPath.exists()) {

            Staging stage = Utils.readObject(stagingPath, Staging.class);

            String blob = stage.getStagedForAddition().get(fileName);

            File blobPath = Utils.join(BLOB_DIR, blob);
            blobPath.delete();
            stagingPath.delete();

        } else if (commitMap.containsKey(fileName)) {


            destaging = new Destaging(new HashMap<>());
            destaging.stageForRemoval(fileName, commitMap.get(fileName));

            File deStagingPath = Utils.join(DESTAGING_DIR, fileName);
            deStagingPath.createNewFile();
            Utils.writeObject(deStagingPath, destaging);


            File cwd = Utils.join(currentDirectory, fileName);
            cwd.delete();


            commitMap.remove(fileName);


        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }


    }

    /**Represents the global-log command. */
    public void globalLog() {

        for (File file : COMMIT_DIR.listFiles()) {
            String serial = file.getName();
            File commitPath = Utils.join(COMMIT_DIR, serial);

            Commit curr = Utils.readObject(commitPath, Commit.class);
            curr.getGlobalLogPrint();
        }
    }

    /**Represents the find command.
     * @param message given message to find. */
    public void find(String message) {

        int commitCounter = 0;

        for (File file : COMMIT_DIR.listFiles()) {
            String serial = file.getName();
            File commitPath = Utils.join(COMMIT_DIR, serial);

            Commit curr = Utils.readObject(commitPath, Commit.class);

            if (curr.getMessage().equals(message)) {
                System.out.println(curr.getCommitId());
                commitCounter += 1;
            }
        }

        if (commitCounter == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }


    }

    /**Represents the status command. */
    public void status() throws IOException {

        if (!GIT_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        String headName = readStringsOfFile("./.gitlet/branches/" + head);

        System.out.println("=== Branches ===");
        System.out.println("*" + headName);
        for (String fileName : Objects
                .requireNonNull(Utils.plainFilenamesIn(BRANCH_DIR))) {
            if (!fileName.equals(headName) && !fileName.equals("head")) {
                System.out.println(fileName);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String fileName : Objects
                .requireNonNull(Utils.plainFilenamesIn(STAGING_DIR))) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String fileName : Objects
                .requireNonNull(Utils.plainFilenamesIn(DESTAGING_DIR))) {
            System.out.println(fileName);
        }
        System.out.println();

        handleStatusModification();

        System.out.println("=== Untracked Files ===");
        for (File file : Objects.requireNonNull(currentDirectory.listFiles())) {
            String fileName = file.getName();
            if (fileName.endsWith(".txt")) {

                Commit currCommit = getCommit();

                byte[] fileContent = Utils.readContents(file);
                String blob = Utils.sha1((Object) fileContent);

                File blobPath = Utils.join(BLOB_DIR, blob);
                File stagingPath = Utils.join(STAGING_DIR, fileName);


                if (!currCommit.getTrackedFiles().containsKey(fileName)
                        && !stagingPath.exists()) {
                    System.out.println(fileName);
                }
            }
        }
        System.out.println();


    }

    /**Helper function to status command.
     * This handles modification done to the files and prints them. */
    private void handleStatusModification() throws IOException {
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (File file : Objects.requireNonNull(currentDirectory.listFiles())) {
            String fileName = file.getName();

            if (fileName.endsWith(".txt")) {

                File stagingPath = Utils.join(STAGING_DIR, fileName);
                File destagingPath = Utils.join(DESTAGING_DIR, fileName);
                Commit currCommit = getCommit();

                byte[] fileContent = Utils.readContents(file);
                String blob = Utils.sha1((Object) fileContent);

                if (currCommit.getTrackedFiles().containsKey(fileName)
                        && !currCommit.getTrackedFiles()
                        .get(fileName).equals(blob)
                        && !stagingPath.exists() && !destagingPath.exists()) {
                    System.out.println(fileName + " (modified)");
                }
            }
        }

        for (File file : STAGING_DIR.listFiles()) {
            if (file.exists()
                    && !Utils.join(currentDirectory,
                    file.getName()).exists()) {
                System.out.println(file.getName() + " (deleted)");
            }
        }

        for (String fileName : getCommit().getTrackedFiles().keySet()) {

            File destagingPath = Utils.join(DESTAGING_DIR, fileName);
            if (!destagingPath.exists()
                    && getCommit().getTrackedFiles()
                    .containsKey(fileName)
                    && !Utils.join(currentDirectory, fileName).exists()) {
                System.out.println(fileName + " (deleted)");
            }
        }


        System.out.println();
    }


    /**Represents the branch command.
     * @param  branchName the name of given branch. */
    public void branch(String branchName) throws IOException {

        File branchPath = Utils.join(BRANCH_DIR, branchName);
        if (branchPath.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        Commit commit = getCommit();

        String commitSerial = commit.getCommitId();
        File commitPath = Utils.join(COMMIT_DIR, commitSerial);

        if (!commitPath.exists()) {
            commitPath.createNewFile();
        }

        Utils.writeObject(commitPath, commit);

        putCommitInBranch(commit.getCommitId(), branchName);

    }

    /**Helper of to the checkout command.
     * when we want to switch to the given branch.
     * @param branchName the name of given branch. */
    private void checkoutBranch(String branchName) throws IOException {

        handleCheckoutErrors(branchName);

        String branchCommitName = readStringsOfFile("./.gitlet/branches/"
                + branchName);
        Commit checkoutCommit = Utils.readObject(
                Utils.join(COMMIT_DIR, branchCommitName), Commit.class);

        Commit currCommit = getCommit();

        handleUntrackedForCheckout(currCommit, checkoutCommit);


        if (checkoutCommit != null) {

            for (String fileName : checkoutCommit.getTrackedFiles().keySet()) {

                String serial = checkoutCommit.getTrackedFiles().get(fileName);
                File blobPath = Utils.join(BLOB_DIR, serial);

                byte[] fileContent = Utils.readContents(blobPath);
                Utils.writeContents(new File(fileName), (Object) fileContent);

            }
        }

        for (File file : Objects.requireNonNull(currentDirectory.listFiles())) {
            String fileName = file.getName();

            if (fileName.endsWith(".txt")) {


                if (currCommit.getTrackedFiles().containsKey(fileName)
                        && !checkoutCommit.getTrackedFiles()
                        .containsKey(fileName)) {
                    file.delete();
                }
            }
        }


        for (File file : Objects.requireNonNull(STAGING_DIR.listFiles())) {
            file.delete();
        }

        updatehead(branchName);

    }

    /**Helper function to checkout command.
     * Handles expected errors of checkout command.
     * @param branchName the name of given branch. */
    private void handleCheckoutErrors(String branchName) {

        File branchPath = Utils.join(BRANCH_DIR, branchName);

        String headBranch = readStringsOfFile("./.gitlet/branches/" + head);


        if (!branchPath.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);


        } else if (headBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
    }

    /**Helper function to checkout command.
     * Reports if there are any untracked files.
     * @param currCommit the current commit
     * @param givenCommit the given commit. */
    private void handleUntrackedForCheckout(Commit currCommit,
                                            Commit givenCommit) {

        for (File file : Objects.requireNonNull(currentDirectory.listFiles())) {
            String fileName = file.getName();

            if (fileName.endsWith(".txt")) {

                File stagingPath = Utils.join(STAGING_DIR, fileName);

                if (!stagingPath.exists()
                        && !currCommit.getTrackedFiles().containsKey(fileName)
                        && givenCommit.getTrackedFiles()
                        .containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }

    /**Helper function to checkoutBranch.
     * Here we update the head when we switch to given branch.
     * @param branchName the name of given branch. */
    private void updatehead(String branchName) {
        try {
            branch = branchName;
            File headPath = Utils.join(BRANCH_DIR, head);

            FileWriter fileWriter = new FileWriter(headPath);
            BufferedWriter str = new BufferedWriter(fileWriter);
            str.write(branchName);
            str.close();

        } catch (IOException excp) {
            return;
        }
    }


    /**Represents the rm-branch command.
     * @param branchName the name of the given branch. */
    public void removeBranch(String branchName) {


        File branchPath = Utils.join(BRANCH_DIR, branchName);

        String headBranch = readStringsOfFile("./.gitlet/branches/" + head);


        if (!branchPath.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);

        } else if (headBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        branchPath.delete();
    }

    /**Represents the reset command.
     * @param commitId SHA-1 id of the commit. */
    public void reset(String commitId) throws IOException {

        File commitPath = Utils.join(COMMIT_DIR, commitId);
        if (!commitPath.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit currCommit = getCommit();
        Commit givenCommit = Utils.readObject(commitPath, Commit.class);

        handleUntrackedForCheckout(currCommit, givenCommit);


        if (givenCommit != null) {


            for (String fileName : givenCommit.getTrackedFiles().keySet()) {

                String serial = givenCommit.getTrackedFiles().get(fileName);
                File blobPath = Utils.join(BLOB_DIR, serial);

                byte[] fileContent = Utils.readContents(blobPath);
                Utils.writeContents(new File(fileName), (Object) fileContent);

            }
        }

        for (File file : Objects.requireNonNull(currentDirectory.listFiles())) {
            String fileName = file.getName();

            if (fileName.endsWith(".txt")) {


                if (currCommit.getTrackedFiles().containsKey(fileName)
                        && !givenCommit.getTrackedFiles()
                        .containsKey(fileName)) {
                    file.delete();
                }
            }
        }


        for (File file : Objects.requireNonNull(STAGING_DIR.listFiles())) {
            file.delete();
        }

        updateBranch(commitId);
    }

    /**Helper function to reset.
     * Here we update the branch to the last recent commit.
     * @param commitId SHA-1 id of the commit. */
    private void updateBranch(String commitId) {
        try {

            String branchName = readStringsOfFile("./.gitlet/branches/" + head);
            File branchPath = Utils.join(BRANCH_DIR, branchName);

            FileWriter fileWriter = new FileWriter(branchPath);
            BufferedWriter str = new BufferedWriter(fileWriter);
            str.write(commitId);
            str.close();

        } catch (IOException excp) {
            return;
        }
    }

    /**Helper function Merge command.
     * Handles expected errors of merge command and,
     * reports if there are any untracked files.
     * @param branchName the name of given branch. */
    private void handleMergeErrors(String branchName) throws IOException {
        File branchPath = Utils.join(BRANCH_DIR, branchName);

        String headBranch = readStringsOfFile("./.gitlet/branches/" + head);


        if (headBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        if (!branchPath.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        if (Objects.requireNonNull(STAGING_DIR.listFiles()).length != 0
                || Objects.requireNonNull(DESTAGING_DIR.listFiles()).length
                != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }


        String branchCommit = readStringsOfFile("./.gitlet/branches/"
                + branchName);

        Commit givenCommit = Utils.readObject(
                Utils.join(COMMIT_DIR, branchCommit), Commit.class);

        Commit currCommit = getCommit();


        for (File file : Objects.requireNonNull(currentDirectory.listFiles())) {
            String fileName = file.getName();

            if (fileName.endsWith(".txt")) {

                File stagingPath = Utils.join(STAGING_DIR, fileName);

                if (!stagingPath.exists()
                        && !currCommit.getTrackedFiles()
                        .containsKey(fileName)
                        && givenCommit.getTrackedFiles()
                        .containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

    }

    /**Helper function Merge command.
     * when there is no common ancestor or
     * when given branch is the ancestor.
     * @param branchName the name of given branch
     * @param headCommit the current commit
     * @param givenCommit the given commit. */
    private void handleNoSplitPoint(String branchName, Commit headCommit,
                                    Commit givenCommit) throws IOException {

        Commit temp1 = getCommit();
        while (temp1.getParent() != null) {
            if (givenCommit.getCommitId().equals(temp1.getCommitId())) {
                System.out.println("Given branch is an ancestor "
                        + "of the current branch.");
                System.exit(0);
            }
            File parentPath = Utils.join(COMMIT_DIR, temp1.getParent());
            Commit parentCommit = Utils.readObject(parentPath, Commit.class);
            temp1 = parentCommit;
        }

        String tempCommit = readStringsOfFile("./.gitlet/branches/"
                + branchName);

        File tempPath = Utils.join(COMMIT_DIR, tempCommit);
        Commit temp2 = Utils.readObject(tempPath, Commit.class);

        while (temp2.getParent() != null) {
            if (headCommit.getCommitId().equals(temp2.getCommitId())) {
                checkoutBranch(branchName);
                System.out.println("Current branch fast-forwarded.");
                System.exit(0);
            }


            File parentPath = Utils.join(COMMIT_DIR, temp2.getParent());
            Commit parentCommit = Utils.readObject(parentPath, Commit.class);
            temp2 = parentCommit;
        }
    }

    /**Helper function to Merge command.
     * this find the split point between two branches.
     * @param headCommit the current commit
     * @param givenCommit the given commit.
     * @return SHA-1 id of two branch common ancestor. */
    private String findSplitPoint(Commit headCommit, Commit givenCommit) {
        HashMap<String, Integer> headMap = new HashMap<>();
        HashMap<String, Integer> givenMap = new HashMap<>();
        Commit temp1 = headCommit;
        int dist = 0;
        while (temp1.getParent() != null) {
            File parentPath = Utils.join(COMMIT_DIR, temp1.getParent());
            Commit parentCommit = Utils.readObject(parentPath, Commit.class);
            dist = dist + 1;
            int depth = dist;
            headMap.put(parentCommit.getCommitId(), depth);
            if (temp1.getMergeParent() != null) {
                headMap.put(temp1.getMergeParent(), depth);
            }
            temp1 = parentCommit;
        }
        Commit temp2 = givenCommit;
        int dist2 = 0;
        while (temp2.getParent() != null) {
            File parentPath = Utils.join(COMMIT_DIR, temp2.getParent());
            Commit parentCommit = Utils.readObject(parentPath, Commit.class);
            dist2 = dist2 + 1;
            int depth = dist2;
            givenMap.put(parentCommit.getCommitId(), depth);
            if (temp2.getMergeParent() != null) {
                givenMap.put(temp2.getMergeParent(), depth);
            }
            temp2 = parentCommit;
        }
        String splitPoint = null;

        HashMap<String, Integer> minMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : headMap.entrySet()) {
            if (givenMap.containsKey(entry.getKey())) {
                minMap.put(entry.getKey(), entry.getValue());
            }
        }
        int min = Collections.min(minMap.values());
        for (Map.Entry<String, Integer> entry : minMap.entrySet()) {
            if (min == entry.getValue()) {
                splitPoint = entry.getKey();
            }
        }
        if (splitPoint == null) {
            for (String commitId : headMap.keySet()) {
                if (givenMap.containsKey(commitId)) {
                    splitPoint = commitId;
                }
            }
        }
        return splitPoint;
    }


    /**Helper function to Merge command.
     * This will stage files for addition when called.
     * @param givenBlob blob of the given commit
     * @param givenContent content of given commit
     * @param fileName name of the file in CWD. */
    private void handleMergeStaging(String givenBlob, String givenContent,
                                    String fileName) throws IOException {

        File cwd = Utils.join(currentDirectory, fileName);
        if (!cwd.exists()) {
            cwd.createNewFile();
        }

        Utils.writeContents(cwd, givenContent);
        staging = new Staging(new HashMap<>());

        staging.getStagedForAddition().put(fileName, givenBlob);

        File stagingPath = Utils.join(STAGING_DIR, fileName);
        stagingPath.createNewFile();
        Utils.writeObject(stagingPath, staging);
    }

    /**Helper function to Merge command.
     * This will stage files for Removal when called.
     * @param headBlob blob of the current commit
     * @param fileName name of the file. */
    private void handleMergeDestaging(String headBlob, String fileName)
            throws IOException {

        destaging = new Destaging(new HashMap<>());
        destaging.stageForRemoval(fileName, headBlob);
        File deStagingPath = Utils.join(DESTAGING_DIR, fileName);
        deStagingPath.createNewFile();
        Utils.writeObject(deStagingPath, destaging);

        File cwd = Utils.join(currentDirectory, fileName);
        cwd.delete();
    }

    /**Helper function to Merge command.
     * When there is a conflict between files at the time of merging.
     * @param fileName name of file in CWD
     * @param givenContent content of given commit
     * @param givenBlob blob of given blob
     * @param headContent content of current commit. */
    private void handleMergeConflict(String fileName, String givenContent,
                                     String givenBlob, String headContent)
            throws IOException {
        File cwd = Utils.join(currentDirectory, fileName);
        String text;
        if (givenContent == null && givenBlob == null) {
            text = "<<<<<<< HEAD\n" + headContent + "=======\n"
                    + ">>>>>>>\n";
        } else {
            text = "<<<<<<< HEAD\n" + headContent + "=======\n"
                    + givenContent + ">>>>>>>\n";
        }
        cwd.createNewFile();
        Utils.writeContents(cwd, text);
        add(fileName);
        System.out.println("Encountered a merge conflict.");
    }


    /**Helper function to Merge command.
     * Conditions for handling files when merging.
     * As we go through all files in given commit, current commit and commit
     * at split point where we decide which files to keep, stage for addition,
     * stage for removal, modified when there is a conflict.
     * @param allCommitFiles list of all files in head, given, and split commit.
     * @param headCommit commit in current branch
     * @param givenCommit commit in given branch
     * @param splitCommit commit at the split point. */
    private void handleMergeCondition(HashSet<String> allCommitFiles,
                                      Commit headCommit,
                                      Commit givenCommit,
                                      Commit splitCommit)
            throws IOException {
        for (String fileName : allCommitFiles) {
            String headBlob = null,   headContent = null;
            String givenBlob = null,  givenContent = null;
            String splitBlob = null,  splitContent = null;
            if (headCommit.trackedFiles.containsKey(fileName)) {
                headBlob = headCommit.getTrackedFiles().get(fileName);
                File blobPath = Utils.join(BLOB_DIR, headBlob);
                headContent = Utils.readContentsAsString(blobPath);
            }
            if (givenCommit.trackedFiles.containsKey(fileName)) {
                givenBlob = givenCommit.getTrackedFiles().get(fileName);
                File blobPath = Utils.join(BLOB_DIR, givenBlob);
                givenContent = Utils.readContentsAsString(blobPath);
            }
            if (splitCommit.trackedFiles.containsKey(fileName)) {
                splitBlob = splitCommit.getTrackedFiles().get(fileName);
                File blobPath = Utils.join(BLOB_DIR, splitBlob);
                splitContent = Utils.readContentsAsString(blobPath);
            }
            if (splitBlob != null && givenBlob != null
                    && splitBlob.equals(headBlob)
                    && !splitBlob.equals(givenBlob)) {
                handleMergeStaging(givenBlob, givenContent, fileName);
            } else if (splitBlob != null && headBlob != null
                    && splitBlob.equals(givenBlob)
                    && !splitBlob.equals(headBlob)) {
                continue;
            } else if (splitBlob != null && headBlob == null
                    && givenBlob == null) {
                continue;
            } else if (splitBlob != null && givenBlob == null
                    && splitBlob.equals(headBlob)) {
                handleMergeDestaging(headBlob, fileName);
            } else if (headBlob == null && givenBlob != null
                    && givenBlob.equals(splitBlob)) {
                continue;
            } else if (splitBlob == null && headBlob == null
                    && givenBlob != null) {
                handleMergeStaging(givenBlob, givenContent, fileName);
            } else if (splitBlob == null && headBlob != null
                    && givenBlob == null) {
                continue;
            } else {
                handleMergeConflict(fileName, givenContent,
                        givenBlob, headContent);
            }
        }
    }



    /**Represents the Merge command.
     * @param branchName the name of branch we are merging our commit to. */
    public void merge(String branchName) throws IOException {
        handleMergeErrors(branchName);


        String branchCommit = readStringsOfFile("./.gitlet/branches/"
                + branchName);

        File givenPath = Utils.join(COMMIT_DIR, branchCommit);
        Commit givenCommit = Utils.readObject(givenPath, Commit.class);

        Commit headCommit = getCommit();
        handleNoSplitPoint(branchName, headCommit, givenCommit);


        String splitPoint = findSplitPoint(headCommit, givenCommit);

        File splitPointPath = Utils.join(COMMIT_DIR, splitPoint);
        Commit splitCommit = Utils.readObject(splitPointPath, Commit.class);


        HashSet<String> allCommitFiles = new HashSet<>();
        allCommitFiles.addAll(headCommit.getTrackedFiles().keySet());
        allCommitFiles.addAll(givenCommit.getTrackedFiles().keySet());
        allCommitFiles.addAll(splitCommit.getTrackedFiles().keySet());

        handleMergeCondition(allCommitFiles,
                headCommit, givenCommit, splitCommit);


        String currBranch = readStringsOfFile("./.gitlet/branches/" + head);
        mergeCommit("Merged " + branchName + " into " + currBranch + ".",
                        givenCommit.getCommitId());

    }

    /**Helper function to Merge command.
     * This is where we update the commit to have mergeParent
     * as a result of merge.
     * @param message the message of the commit
     * @param mergeParent the second parent of commit as result of merging */
    public void mergeCommit(String message, String mergeParent)
                throws IOException {

        readBranch();

        handleCommitErrors(message);
        Commit currCommit = getCommit();
        HashMap<String, String> trackedFiles =
                new HashMap<>(currCommit.getTrackedFiles());

        trackedFiles = handleStagingAreaForCommit(trackedFiles);

        Commit newCommit = new Commit(message, currCommit.getCommitId(),
                                        mergeParent, trackedFiles);

        makeCommit(newCommit);
    }


}
