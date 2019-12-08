package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** Repository/Tree class for Gitlet, the tiny stupid version-control system.
 *  @author Varun Jadia
 */

public class Repo implements Serializable {

    /** tracks sha code of last/head commit.*/
    private String _lastcommit;

    /**tracks files in stage. */
    private HashMap<String, Blob> _stagefiles = new HashMap<>();

    /** Hashmap of branch names to their head commits. */
    private HashMap<String, String> _branchmap = new HashMap<>();

    /** Tracks current branch by its name. */
    private String _currbranch;

    /** Tracks file to be removed in next commit. */
    private ArrayList<String> _rmfilenames;

    /**Tracks all commit ids. */
    private HashSet<String> _commitids;

    /**initialize a new repo. RETURN*/
    public Repo init() {

        Commit initcom = new Commit("initial commit",
                "Thu Jan 1 00:00:00 1970 -0800", null);

        File commits = new File(".gitlet/commits");
        commits.mkdir();
        File staging = new File(".gitlet/staging");
        staging.mkdir();

        _stagefiles = new HashMap<String, Blob>();
        _commitids = new HashSet<>();
        _lastcommit = initcom.gethash();

        String hash = initcom.gethash();
        File comm = new File(".gitlet/commits/" + hash);
        Utils.writeContents(comm, Utils.serialize(initcom));
        _commitids.add(hash);
        _branchmap.put("master", hash);
        _currbranch = "master";
        return this;
    }

    /**add method for gitlet. FILENAME*/
    public void add(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.print("File does not exist.");
            System.exit(0);
        }

        if (_rmfilenames == null) {
            _rmfilenames = new ArrayList<>();
        }
        if (_rmfilenames.contains(filename)) {
            _rmfilenames.remove(filename);
        }

        Blob fileblob = new Blob(filename);
        String hashfile = fileblob.getshacode();
        Commit lastcommit = Utils.readObject(
                new File(".gitlet/commits/" + _lastcommit), Commit.class);

        for (String name : lastcommit.getfiles().keySet()) {
            if (hashfile.equals(lastcommit.getfiles().get(name).getshacode())) {
                if (_stagefiles.containsKey(name)) {
                    String code = lastcommit.getfiles().get(name).getshacode();
                    File remove = new File(".gitlet/staging" + code);
                    remove.delete();
                }
                return;
            }
        }

        File stage = new File(".gitlet/staging/" + hashfile);
        _stagefiles.put(filename, fileblob);
        Utils.writeObject(stage, fileblob);
    }

    /**commit method. MESSAGE TIME*/
    public void commit(String message, String time) {
        if (message.equals("")) {
            System.out.print("Please enter a commit message.");
            System.exit(0);
        }

        if (_stagefiles == null) {
            System.out.print("No changes added to the commit.");
            System.exit(0);
        }

        Commit lastcommit = Utils.readObject(
                new File(".gitlet/commits/" + _lastcommit), Commit.class);
        Commit comm = new Commit(message, time,
                lastcommit.getfiles(), _lastcommit);

        if (comm.gethash().equals(_lastcommit)) {
            System.out.print("No changes added to the commit.");
            System.exit(0);
        }

        for (String filename : comm.getfiles().keySet()) {
            if (_stagefiles.containsKey(filename)) {
                comm.getfiles().remove(filename);
                comm.getfiles().put(filename, _stagefiles.get(filename));
            }
            if (_rmfilenames.contains(filename)) {
                comm.getfiles().remove(filename);
            }
        }

        for (String filename : _stagefiles.keySet()) {
            if (!comm.getfiles().containsKey(filename)) {
                comm.getfiles().put(filename, _stagefiles.get(filename));
                String code = _stagefiles.get(filename).getshacode();
                File delstage = new File(".gitlet/staging/" + code);
                delstage.delete();
            }
        }

        File addcomm = new File(".gitlet/commits/" + comm.gethash());
        Utils.writeObject(addcomm, comm);

        String commhash = comm.gethash();
        _stagefiles.clear();
        _lastcommit = comm.gethash();
        _branchmap.put(_currbranch, commhash);
        _commitids.add(commhash);
    }

    /**java gitlet.Main checkout -- [file name]. FILENAME*/
    public void checkout1(String filename) {
        Commit lastcommit = Utils.readObject(
                new File(".gitlet/commits/" + _lastcommit), Commit.class);
        Blob blb = null;
        if (lastcommit.getfiles().containsKey(filename)) {
            blb = lastcommit.getfiles().get(filename);
        } else {
            System.out.print("File does not exist in that commit.");
            System.exit(0);
        }
        File overwrite = new File(filename);
        Utils.writeContents(overwrite, blb.getcontents());
    }

    /** java gitlet.Main
     * checkout [commit id] -- [file name]. COMMITID FILENAME*/
    public void checkout2(String commitid, String filename) {
        File checkcomm = new File(".gitlet/commits/" + commitid);
        Commit lastcommit = null;
        if (checkcomm.exists()) {
            lastcommit = Utils.readObject(checkcomm, Commit.class);
        } else {
            System.out.print("No commit with that id exists.");
            System.exit(0);
        }
        Blob blb = null;
        if (lastcommit.getfiles().containsKey(filename)) {
            blb = lastcommit.getfiles().get(filename);
        } else {
            System.out.print("File does not exist in that commit.");
            System.exit(0);
        }
        File overwrite = new File(filename);
        Utils.writeContents(overwrite, blb.getcontents());
    }

    /**java gitlet.Main checkout [branch name]. BRANCHNAME*/
    public void checkout3(String branchname) {
        if (!_branchmap.containsKey(branchname)) {
            System.out.print("No such branch exists.");
            System.exit(0);
        } else if (_currbranch.equals(branchname)) {
            System.out.print("No need to checkout to the current branch.");
            System.exit(0);
        }
        Commit currhead = Utils.readObject(
                new File(".gitlet/commits/" + _lastcommit), Commit.class);

        Commit lastcommit = Utils.readObject(
                new File(".gitlet/commits/" + _branchmap.get(branchname)), Commit.class);

        HashMap<String, Blob> commfiles = lastcommit.getfiles();
        HashMap<String, Blob> currheadfiles = currhead.getfiles();

        for (String file : commfiles.keySet()) {
            File plstage = new File(file);
            if (!currheadfiles.containsKey(file) && plstage.exists()) {
                System.out.print("There is an untracked file in the way; delete it or add it first.");
                System.exit(0);
            }
        }

        for (String file : currheadfiles.keySet()) {
            File overwrite = new File(file);
            if (!commfiles.containsKey(file)) {
                Utils.restrictedDelete(overwrite);
            }
        }

        for (String file : commfiles.keySet()) {
            String contents = commfiles.get(file).getcontents();
            File overwrite = new File(file);
            Utils.writeContents(overwrite, contents);
        }

        _currbranch = branchname;
        _lastcommit = _branchmap.get(_currbranch);

        for (String filename : _stagefiles.keySet()) {
            File delstage = new File(".gitlet/staging/" + _stagefiles.get(filename).getshacode());
            delstage.delete();
        }
        _stagefiles.clear();
    }

    /**prints commit logs. */
    public void log() {
        String counter = _lastcommit;
        while (counter != null) {
            Commit lastcommit = Utils.readObject(
                    new File(".gitlet/commits/" + counter), Commit.class);
            System.out.println("===");
            System.out.println("commit " + lastcommit.gethash());
            System.out.println("Date: " + lastcommit.gettimestamp());
            System.out.println(lastcommit.getlogmsg());
            System.out.println();
            counter = lastcommit.getparenthash();
        }
    }

    /** rm function for gitlet. FILENAME*/
    public void rm(String filename) {
        Commit lastcommit = Utils.readObject(
                new File(".gitlet/commits/" + _lastcommit), Commit.class);
        if (!_stagefiles.containsKey(filename)
                && !lastcommit.getfiles().containsKey(filename)) {
            System.out.print("No reason to remove the file.");
            System.exit(0);
        }

        File remove = new File(".gitlet/staging" + filename);
        _stagefiles.remove(filename);
        remove.delete();

        if (lastcommit.getfiles().containsKey(filename)) {
            if (_rmfilenames == null) {
                _rmfilenames = new ArrayList<>();
            }
            _rmfilenames.add(filename);
            File rm = new File(filename);
            Utils.restrictedDelete(rm);
        }
    }
    /** global log. */
    public void globallog() {
        File commitdir = new File(".gitlet/commits/");
        for (File file : commitdir.listFiles()) {
            Commit comm = Utils.readObject(file, Commit.class);
            System.out.println("===");
            System.out.println("commit " + comm.gethash());
            System.out.println("Date: " + comm.gettimestamp());
            System.out.println(comm.getlogmsg());
            System.out.println();
        }
    }

    /** find function. MSG*/
    public void find(String msg) {
        ArrayList<String> vals = new ArrayList<>();
        File commitdir = new File(".gitlet/commits/");
        for (File file : commitdir.listFiles()) {
            Commit comm = Utils.readObject(file, Commit.class);
            if (comm.getlogmsg().equals(msg)) {
                System.out.println(comm.gethash());
                vals.add(comm.gethash());
            }
        }
        if (vals.isEmpty()) {
            System.out.print("Found no commit with that message.");
        }
    }

    /** gitlet status. */
    public void status() {
        System.out.println("=== Branches ===");
        for (String branch : _branchmap.keySet()) {
            if (branch.equals(_currbranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String filename : _stagefiles.keySet()) {
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String filename : _rmfilenames) {
            System.out.println(filename);
        }
        System.out.println();
        System.out.print("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    /** creates a new branch on the commit tree.BRANCHNAME */
    public void branch(String branchname) {
        if (_branchmap.containsKey(branchname)) {
            System.out.print("A branch with that name already exists.");
            System.exit(0);
        }
        _branchmap.put(branchname, _lastcommit);
    }

    /** rm-branch.BRANCHNAME*/
    public void rmbranch(String branchname) {
        if (!_branchmap.containsKey(branchname)) {
            System.out.print("A branch with that name does not exist.");
            System.exit(0);
        } else if (branchname.equals(_currbranch)) {
            System.out.print("Cannot remove the current branch.");
            System.exit(0);
        }

        _branchmap.remove(branchname);
    }

    /** reset.COMMITID*/
    public void reset(String commitid) {
        if (!_commitids.contains(commitid)) {
            System.out.print("No commit with that id exists.");
            System.exit(0);
        }
        File commfile = new File(".gitlet/commits/" + commitid);
        Commit comm = Utils.readObject(commfile, Commit.class);

        Commit currhead = Utils.readObject(
                new File(".gitlet/commits/" + _lastcommit), Commit.class);

        HashMap<String, Blob> commfiles = comm.getfiles();
        HashMap<String, Blob> currheadfiles = currhead.getfiles();

        for (String file : commfiles.keySet()) {
            File plstage = new File(file);
            if (!currheadfiles.containsKey(file) && plstage.exists()) {
                System.out.print("There is an untracked file "
                        + "in the way; delete it or add it first.");
                System.exit(0);
            }
        }

        for (String filename : currheadfiles.keySet()) {
            if (!commfiles.containsKey(filename)) {
                File overwrite = new File(filename);
                if (overwrite.exists()) {
                    Utils.restrictedDelete(overwrite);
                }
            }
        }

        for (String filename : commfiles.keySet()) {
            checkout1(filename);
        }

        for (String filename : _stagefiles.keySet()) {
            File delstage = new File(".gitlet/staging/"
                    + _stagefiles.get(filename).getshacode());
            delstage.delete();
        }
        _stagefiles.clear();
    }

    /**merge function for gitlet.BRANCHNAME*/
    public void merge(String branchname) {
        if (!_stagefiles.isEmpty() && !_rmfilenames.isEmpty()) {
            System.out.print("You have uncommitted changes.");
            System.exit(0);
        }

        if (!_branchmap.containsKey(branchname)) {
            System.out.print("A branch with that name does not exist.");
            System.exit(0);
        }

        if (branchname.equals(_currbranch)) {
            System.out.print("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /**helper function for finding ancestor given two branch heads. */
    public String ancestor(String branch1, String branch2) {
        Commit commit1 = Utils.readObject(new File(".gitlet/commits/" + branch1), Commit.class);
        Commit commit2 = Utils.readObject(new File(".gitlet/commits/" + branch2), Commit.class);

//        boolean check = true;
//
//        for (int i = 0, j = 0, k = 0, l = 0; check;) {
//            if (commit1.getparenthash() == null || commit2.getparenthash() == null) {
//                return commit1.gethash();
//            }
//            if (!commit1.getparenthash().equals(commit2.getparenthash())) {
//                i++;
//                if (i < j && i < k && i < l) {
//                    check = false;
//                }
//            }
//            if (commit1.getmergeparenthash() != null && commit2.getmergeparenthash() != null) {
//                if (commit1.getmergeparenthash().equals(commit2.getmergeparenthash())) {
//                    j++;
//                }
//                if (j < i && j < k && j < l) {
//                    check = false;
//                }
//            }
//            if (commit2.getmergeparenthash() != null) {
//                if(commit2.getmergeparenthash().equals(commit1.getparenthash())) {
//                    k++;
//                }
//                if (k < i && k < l && k < j) {
//                    check = false;
//                }
//            }
//            if (commit1.getmergeparenthash() != null) {
//                if (commit1.getmergeparenthash().equals(commit2.getparenthash())) {
//                    l++;
//                }
//                if (l < i && l < k && l < j) {
//                    check = false;
//                }
//            }
//
//        }

        while (!commit1.getparenthash().equals(commit2.getparenthash())) {
            commit1 = Utils.readObject(new File(".gitlet/commits/" + commit1.getparenthash()), Commit.class);
            commit2 = Utils.readObject(new File(".gitlet/commits/" + commit2.getparenthash()), Commit.class);
        }
        return commit1.getparenthash();
    }
}


