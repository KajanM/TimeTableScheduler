
import java.util.HashSet;
import java.util.Set;

public class Subject {

	private final String name;
	private final boolean compulsory;
	private RoomAndTime roomAndTime;
	
	private int priority;

	private final Set<RoomAndTime> applicableSlots;

	public Subject(String name, boolean compulsory) {
		this.name = name;
		this.compulsory = compulsory;

		applicableSlots = new HashSet<RoomAndTime>();
	}

	public String getName() {
		return name;
	}

	public boolean isCompulsory() {
		return compulsory;
	}

	public void addApplicableSlot(RoomAndTime applicableRoomAndTime) {
		applicableSlots.add(applicableRoomAndTime);
	}

	public Set<RoomAndTime> getApplicableSlots() {
		return applicableSlots;
	}

	public RoomAndTime getRoomAndTime() {
		return roomAndTime;
	}

	public boolean isScheduled() {
		return roomAndTime != null;
	}

	public void setRoomAndTime(RoomAndTime roomAndTime) {
		this.roomAndTime = roomAndTime;
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public void incrementPriorityByOne() {
		priority++;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicableSlots == null) ? 0 : applicableSlots.hashCode());
		result = prime * result + (compulsory ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Subject other = (Subject) obj;
		if (applicableSlots == null) {
			if (other.applicableSlots != null)
				return false;
		} else if (!applicableSlots.equals(other.applicableSlots))
			return false;
		if (compulsory != other.compulsory)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Subject [name=" + name + ", compulsory=" + compulsory + ", roomAndTime=" + roomAndTime + ", priority="
				+ priority + ", applicableSlots=" + applicableSlots + "]";
	}

}
