# Gitlet Design Document

**Name**: Neman Faiz

## Classes and Data Structures

### Main.java
This class is the entry point of the program.
It takes argument from terminal and parse them. It identifies each command for gitlet, construct Repository object and call its associated method from Repository class.
This is  where all commands like init, add, commit, log, checkout, global-log, find, status, branch, rm-branch and merge are running from where it makes each command functional.

###Staging.java
This class represent objects that are staged for addition. 

####Fields
1. private HashMap<String, String> stagedForAdd --> hashmap that stores fileName and blob

###Destaging.java
This class represent objects that are staged for removal.

####Fields
1. private ArrayList<String> stagedForRemove --> Arraylist that keep name of files that need to be removed

###Commit.java
This class represent commit objects

###Fields
1. private String message --> String that represent message of a commit
2. private String timeStamp --> String that represent time stamp of a commit made
3. private String parent --> String that represent commit name of previous commit (parent commit)
4. private String serial --> String that represent commit id
5. private HashMap<String, String> trackedFiles --> Hashmap that represent name file and blob 
6. HashMap<String, String> serialToBlob --> possible variable that may be used to store commit id and blob associate with it


### Repository.java
This class represents a Repository.
There are two sets of methods. private and public methods.
Public methods represent the commands we use in gitlet.
Private methods represent the helper functions to gitlet commands.




#### Fields

1. Static Variables 
   1. static final File currentDirectory --> A pointer to current directory where I like to set my Repository in
   2. static File gitDir --> A pointer to the directory named .gitlet inside current working directory 
   3. static File stagingAreaDir --> A pointer to the directory named StagingArea inside git directory
   4. static File stagingDir --> A pointer to the directory named Staging directory inside Staging Area directory where Staging class objects are persisted
   5. static File destagingDir -->  A pointer to the directory named Detaging directory inside Staging Area directory where Destaging class objects are persisted
   6. static File commitDir --> A pointer to the directory named Commits inside git directory where Commit class objects are persisted
   7. static File blobDir --> A pointer to the directory named Blob inside git directory where content of file exist
   8. static File logDir --> A pointer to the directory named Log inside git directory where I may store list of logs from different branches
   9. static File branchDir -> A pointer to the directory named Branches inside git directory. Inside this directory I have file named Head where I update the head with branch and a files for each branch. 
2. Instance Variables
   1. private String Head --> A string that stores name of the branch. Mainly, it points to the branch that we are working on, where on branch linked list we are.
   2. private String branch --> A string that stores the commit id of last commit. Mainly, it points to the commit that we are on. 
   3. private Staging staging --> Staging class instance that indicates the files in Staging Area to be Added
   4. private Destaging destaging --> Destaging class instance that indicates the files in Staging Area to be Removed
   5. HashMap<String, File> untrackedFiles --> HashMap that stores the name and blob path of files that are untracked.


## Algorithms

### Main.java
####main(String[] args): 
This is the entry point of the program. 
I start this method by constructing a Repository object.
- First I check if any argument is given, if not exiting
- Then I have series of if statements where I grab the first argument given
to identify each command then call appropiate method from Repository class
to run command, make that command functional.


###Staging.java
####public Staging(HashMap<String, String> stagedForAdd)
1. This constructor takes a hashmap and assign it to its class hashmap instance

####public void addToStaging(String fileName, String blob)
1. This method maps fileName to a blob
   1. for example --> hello.txt maps to blob0

####public HashMap<String, String> getStagedForAdd()
1. This method returns the class instance (stagedForAdd) hashmap

###Destaging.java
####public Destaging(ArrayList<String> stagedForRemove)
1. This constructor set parameter arraylist to class instance arrayList

####public void addToDestaging(String fileName)
1. Adding names of Files that staged for removals.

####public ArrayList<String> getStagedForRemove()
1. Returns the class instance (stagedForRemove)  arraylist


### Repository.java
####public Repository(): 
initializing Head and branch instance variable

* Head stores the branch that we are working on
* branch stores the id of the last commit we made.

####public void init()
As long as another .gitlet directory does not exist then
making directories for each static Dir variable then calling the making initial commit method
to make an initial commit

If a .gitlet directory exists then exiting with a message.

####private void initialCommit()
Helper function to the init command.

First creating a new commit object with msg -> initial commit, parent being null and empty hashmap
  
   

## Persistence

### story [text]
- When a new line of story is appended to the story, we concatenate it with existing lines from `story.txt` and write the updated story back to `story.txt`.

### dog [name] [breed] [age]
- For each dog, we write the object to a file called `name` in `.capers/dog/`.

### birthday [name]
- Read out the dog with `name` from its corresponding file in `.capers/dog/`.
- Increment the age of the dog instance by 1.
- Write the dog instance back to its corresponding file in `.capers/dog/`.


## Design Diagram

![Capers Design Diagram](capers-design.png)