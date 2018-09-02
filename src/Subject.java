
import java.util.HashSet;
import java.util.Set;

public class Subject {

	private final String name;
	private final boolean compulsory;
	private RoomAndTime roomAndTime;

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

	@Override
	public String toString() {
		return "Subject [name=" + name + ", compulsory=" + compulsory + ", roomAndTime=" + roomAndTime
				+ ", applicableSlots=" + applicableSlots + "]";
	}

}
