package gitlet;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {

    /** timestamp of commit. */
    private Date _timestamp;

    /** log message for commit. */
    private String _logmsg;

    /** repo/directory that the commit is part of. */
    private Repo _ref;

    /** uid for commit. */
    private String _uid;

    /** Hashmap containing files. */
    private HashMap<String, Blob> _files = new HashMap<>();

    /** Hashmap containing tracked files. */
    private HashMap<String, String> _trackedfiles = new HashMap<>();

    /** Commit parent id */
    private String _parenthash ;

    /** String contents of all blobs */
    private ArrayList<String> _filecontents;


    Commit (String msg, Timestamp time, String parent) {
        _logmsg = msg;
        _timestamp = time;
        _parenthash = parent;
    }


    Commit (String msg, Timestamp time, HashMap<String, Blob> files, String parent) {
        _logmsg = msg;
        _timestamp = time;
        _files = files;

        for (String file : _files.keySet()) {
            _filecontents.add(_files.get(file).getcontents());
        }
        _parenthash = parent;
    }

    /** set timestamp for commit. */
    public void settimestamp(Timestamp _timestamp) {
        this._timestamp = _timestamp;
    }

    /** get files. */
    public HashMap<String, Blob> getfiles() {
        return _files;
    }

    /** get tracked files. */
    public HashMap<String, String> gettrackedfiles() {
        return _trackedfiles;
    }

    /** get hashid for commit. */
    public String gethash() {
        if (_logmsg.equals("initial commit")) {
            ArrayList<Object> shalist = new ArrayList<>();
            shalist.add(_logmsg);
            shalist.add("buffer");
            return Utils.sha1(shalist);
        }
        ArrayList<Object> shalist = new ArrayList<>();
        shalist.add(_logmsg);
        shalist.add(_filecontents);
        shalist.add(_parenthash);
        return Utils.sha1(shalist);
    }

    /** set files. */
    public void setfiles(HashMap<String, Blob> _files) {
        this._files = _files;
    }

    /** set tracked files. */
    public void settrackedfiles(HashMap<String, String> _trackedfiles) {
        this._trackedfiles = _trackedfiles;
    }
}
