import java.util.HashSet;
import java.util.Set;

public class TestCases {
	public static void onlyApplicableTimeIsAssigned(Set<Subject> subjectsScheduled) {
		for(Subject subject : subjectsScheduled) {
			if(!subject.getApplicableSlots().contains(subject.getRoomAndTime())) {
				System.out.println("Failed: Assigned time is not applicable for the subject " + subject);
				return;
			}
		}
		System.out.println("Passed: Assigned time is one of given applicable times");
	}
	
	public static void noSubjectsAreScheduledToSameRoomAndTime(Set<Subject> subjectsScheduled) {
		Set<RoomAndTime> scheduledSlots = new HashSet<>();
		for (Subject subject : subjectsScheduled) {
			if (scheduledSlots.contains(subject.getRoomAndTime())) {
				System.out.println("Failed: two or more subjects are scheduled at " + subject.getRoomAndTime());
				return;
			} else {
				scheduledSlots.add(subject.getRoomAndTime());
			}
		}
		System.out.println("Passed: No subjects are assigned to same room and time");
	}

	public static void noSubjectsAreScheduledAtCompulsorySubjectTime(Set<Subject> subjectsScheduled) {
		Set<String> compulsorySubjectTimes = new HashSet<>();
		for (Subject subject : subjectsScheduled) {
			if (subject.isCompulsory()) {
				if (compulsorySubjectTimes.contains(subject.getRoomAndTime().getTime())) {
					System.out.println("Failed: two or more compulsory subjects are scheduled at same time "
							+ subject.getRoomAndTime().getTime());
					return;
				} else {
					compulsorySubjectTimes.add(subject.getRoomAndTime().getTime());
				}
			}
		}

		for (Subject subject : subjectsScheduled) {
			if (!subject.isCompulsory()) {
				if (compulsorySubjectTimes.contains(subject.getRoomAndTime().getTime())) {
					System.out.println("Failed: two or more optional subjects are scheduled at time "
							+ subject.getRoomAndTime().getTime() + " which is also scheduled for a compulsory subject");
					return;
				}
			}
		}
		System.out.println("Passed: No other subjects assigned at a compulsory subject time");
	}
}
