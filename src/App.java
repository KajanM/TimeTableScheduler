
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class App {

	private static Set<Subject> subjectsToSchedule;
	private static Set<Subject> subjectsScheduled;
	private static Set<RoomAndTime> availableRoomsAndTimes;
	private static Set<RoomAndTime> assignedRoomsAndTimes;

	private static boolean debugEnabled = true;

	public static void main(String[] args) {
		System.out.println("Debug enabled: " + debugEnabled + "\n");
		String inputFileName = args[0];
		initialize();
		parseCSVData(inputFileName);
		assignPriority();
		assignTimeAndRoom();
		outputResult();
	}

	private static void assignPriority() {
		if(subjectsToSchedule.isEmpty()) {
			System.out.println("Initial subject list is empty. Existing");
			return;
		}
		int priority;
		for(Subject subject : subjectsToSchedule) {
			priority = 0;
			priority += subject.getApplicableSlots().size();
			if(subject.isCompulsory()) {
				priority++;
			}
			subject.setPriority(priority);
		}
	}

	private static void outputResult() {
		if (!subjectsToSchedule.isEmpty()) {
			System.out.println("Invalid logic");
			return;
		}

		for (Subject subject : subjectsScheduled) {
			System.out.println(subject);
		}
	}

	private static void assignTimeAndRoom() {
		if (subjectsToSchedule.isEmpty()) {
			return;
		}

		Subject subjectToAssign = getSubjectToAssign();

		Set<RoomAndTime> applicableRoomsAndTimes = subjectToAssign.getApplicableSlots();

		for (RoomAndTime roomAndTime : applicableRoomsAndTimes) {
			if (!availableRoomsAndTimes.contains(roomAndTime)) {
				if (debugEnabled) {
					System.out.println("\nRoomAndTime already taken: " + roomAndTime);
					System.out.println("Available slots: " + availableRoomsAndTimes + "\n");
				}
				continue;
			}
			if (forwardCheck(subjectToAssign, roomAndTime)) {
				availableRoomsAndTimes.remove(roomAndTime);
				assignedRoomsAndTimes.add(roomAndTime);
				if(subjectToAssign.isCompulsory()) {
					for(RoomAndTime slot : availableRoomsAndTimes) {
						if(slot.getTime().equals(roomAndTime.getTime())) {
							availableRoomsAndTimes.remove(slot);
							assignedRoomsAndTimes.add(slot);
						}
					}
				}
				assignedRoomsAndTimes.add(roomAndTime);
				subjectToAssign.setRoomAndTime(roomAndTime);
				subjectsToSchedule.remove(subjectToAssign);
				subjectsScheduled.add(subjectToAssign);

				if (debugEnabled) {
					System.out.println("Successfully scheduled " + subjectToAssign);
					displayScheduledSubjects();
				}
				break;
			}
		}

		if (!subjectToAssign.isScheduled()) {
			backtrack();
		}

		assignTimeAndRoom();
	}

	private static void displayScheduledSubjects() {
		for(Subject subject : subjectsScheduled) {
			System.out.println(subject.getName() + " | " + subject.getRoomAndTime().getRoom() + " | " + subject.getRoomAndTime().getTime());
		}
		System.out.println();
	}

	private static void displaySubjectsToAssign() {
		System.out.println("Subjects to schedule\n==========");
		for(Subject subject : subjectsToSchedule) {
			System.out.println(subject);
		}
	}

	private static boolean forwardCheck(Subject subjectToAssign, RoomAndTime roomAndTime) {
		Set<RoomAndTime> effectiveAvailableRoomsAndTimes = new HashSet<>();
		effectiveAvailableRoomsAndTimes.addAll(availableRoomsAndTimes);
		effectiveAvailableRoomsAndTimes.remove(roomAndTime);
		
		if(subjectToAssign.isCompulsory()) {
			for(RoomAndTime slot : effectiveAvailableRoomsAndTimes) {
				if(slot.getTime().equals(roomAndTime.getTime())) {
					effectiveAvailableRoomsAndTimes.remove(slot);
				}
			}
		}
		
		Set<Subject> subjectsToForwardCheck = new HashSet<>();
		subjectsToForwardCheck.addAll(subjectsToSchedule);
		subjectsToForwardCheck.remove(subjectToAssign);
		for (Subject subject : subjectsToForwardCheck) {
			if (Collections.disjoint(subject.getApplicableSlots(), effectiveAvailableRoomsAndTimes)) {
				if(debugEnabled) {
					System.out.println("Can't assign " + roomAndTime + " to " + subjectToAssign.getName() + " due to forward check constraint");
				}
				return false;
			}
		}
		return true;
	}

	private static void backtrack() {
		if(subjectsScheduled.isEmpty()) {
			System.out.println("Unable to find the solution, terminating the program");
			System.exit(0);
		}
		Subject wrongAssignment = (Subject) subjectsScheduled.toArray()[subjectsScheduled.size() - 1];
		Iterator<Subject> iterator = subjectsScheduled.iterator();
		Subject subject;
		while (iterator.hasNext()) {
			subject = iterator.next();
			if (subject.equals(wrongAssignment)) {
				subjectsScheduled.remove(subject);
				subjectsToSchedule.add(subject);

				availableRoomsAndTimes.add(subject.getRoomAndTime());
				if(subject.isCompulsory()) {
					for(RoomAndTime slot : assignedRoomsAndTimes) {
						if(slot.getTime().equals(subject.getRoomAndTime().getTime())) {
							assignedRoomsAndTimes.remove(slot);
							availableRoomsAndTimes.add(slot);
						}
					}
				}
				assignedRoomsAndTimes.remove(subject.getRoomAndTime());

				if (debugEnabled) {
					System.out.println("Backtrack: " + subject);
				}

				while (iterator.hasNext()) {
					subject = iterator.next();
					subjectsScheduled.remove(subject);
					subjectsToSchedule.add(subject);

					availableRoomsAndTimes.add(subject.getRoomAndTime());
					assignedRoomsAndTimes.remove(subject.getRoomAndTime());

					if (debugEnabled) {
						System.out.println("Backtrack: " + subject);
					}
				}
				break;
			}
		}
	}

	private static Subject getSubjectToAssign() {
		Subject maxPrioritySubject = (Subject) subjectsToSchedule.toArray()[0];
		for (Subject subject : subjectsToSchedule) {
			if(subject.getPriority() < maxPrioritySubject.getPriority()) {
				maxPrioritySubject = subject;
			}
		}
		return maxPrioritySubject;
	}

	private static void initialize() {
		subjectsToSchedule = new LinkedHashSet<>();
		availableRoomsAndTimes = new HashSet<>();
		subjectsScheduled = new LinkedHashSet<>();
		assignedRoomsAndTimes = new HashSet<>();
	}

	private static void parseCSVData(String inputFileName) {
		Set<String> rooms = new HashSet<>();

		try {
			String[] data;

			List<String> input = Files.readAllLines(Paths.get(App.class.getResource(inputFileName).getPath()),
					StandardCharsets.UTF_8);

			String roomsLine = input.get(input.size() - 1);
			for (String room : roomsLine.split(",")) {
				rooms.add(room.trim());
			}
			input.remove(roomsLine);

			for (String line : input) {
				if(line.trim().startsWith("#")) {
					//ignore comment
					continue;
				}
				if(line.trim().isEmpty()) {
					continue;
				}
				data = line.split(",");

				Subject subject;
				subject = new Subject(data[0].trim(), data[1].trim().equals("c"));

				RoomAndTime roomAndTime;
				for (int i = 2; i < data.length; i++) {
					for (String room : rooms) {
						roomAndTime = new RoomAndTime(room, data[i].trim());
						subject.addApplicableSlot(roomAndTime);
						availableRoomsAndTimes.add(roomAndTime);
					}
				}
				subjectsToSchedule.add(subject);
			}

			if (debugEnabled) {
				System.out.println("parsing " + inputFileName + " finished");
				displaySubjectsToAssign();
				System.out.println("Available rooms\n==========\n" + rooms);
				System.out.println();
			}

		} catch (FileNotFoundException e) {
			System.out.println("Error: no such input file");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error: could not parse the input file");
			e.printStackTrace();
		}
	}
}
