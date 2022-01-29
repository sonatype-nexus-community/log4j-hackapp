package model;


import java.util.*;

public class Todo {

    String title;
    String id;
    Status status; 

    public Todo(String t, String i, Status s) {
		this.title=t;
		this.id=i;
		this.status=s;
	}

	public void toggleStatus() {
        this.status = isComplete() ? Status.ACTIVE : Status.COMPLETE;
    }

    public boolean isComplete() {
        return this.status == Status.COMPLETE;
    }

    public static Todo create(String title) {
        return new Todo(title, UUID.randomUUID().toString(), Status.ACTIVE);
    }

    public String toString() {
        return "todo(title="+title+", status="+status+")";
    }

	public String getId() {
		return id;
	}

	public void setTitle(String t) {
		this.title=t;
		
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status s) {
		this.status=s;
	}
}
