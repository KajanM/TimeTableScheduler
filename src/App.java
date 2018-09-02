
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class App {

	private static String inputFileName;
	private static Set<Subject> subjectsToAssign;
	private static Set<Subject> subjectsAssigned;
	private static Set<RoomAndTime> availableRoomsAndTimes;
	private static Set<RoomAndTime> assignedRoomsAndTimes;

	private static Map<Subject, Set<RoomAndTime>> checkedSlotsPerSubject;

	private static boolean debugEnabled = true;

	public static void main(String[] args) {
		inputFileName = args[0];
		initialize();
		assignTimeAndRoom();
		outputResult();
	}

	private static void outputResult() {
		if (!subjectsToAssign.isEmpty()) {
			System.out.println("Invalid logic");
			return;
		}

		for (Subject subject : subjectsAssigned) {
			System.out.println(subject);
		}
	}

	private static void assignTimeAndRoom() {
		if (subjectsToAssign.isEmpty()) {
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
			if (forwardCheck(roomAndTime)) {
				availableRoomsAndTimes.remove(roomAndTime);
				assignedRoomsAndTimes.add(roomAndTime);
				subjectToAssign.setRoomAndTime(roomAndTime);
				subjectsToAssign.remove(subjectToAssign);
				subjectsAssigned.add(subjectToAssign);

				if (debugEnabled) {
					System.out.println("Successfully assigned subject " + subjectToAssign);
				}
				break;
			}
			if (checkedSlotsPerSubject.containsKey(subjectToAssign)) {
				checkedSlotsPerSubject.get(subjectToAssign).add(roomAndTime);
			} else {
				Set<RoomAndTime> slots = new HashSet<>();
				slots.add(roomAndTime);
				checkedSlotsPerSubject.put(subjectToAssign, slots);
			}
		}

		if (!subjectToAssign.isScheduled()) {
			backtrack();

//			if (blackList.containsKey(failedAssignment)) {
//				blackList.get(failedAssignment).add(failedAssignment.getRoomAndTime());
//			} else {
//				HashSet<RoomAndTime> set = new HashSet<>();
//				set.add(failedAssignment.getRoomAndTime());
//				blackList.put(failedAssignment, set);
//			}
		}

		assignTimeAndRoom();
	}

	private static boolean forwardCheck(RoomAndTime roomAndTime) {
		Set<RoomAndTime> effectiveAvailableRoomsAndTimes = new HashSet<>();
		effectiveAvailableRoomsAndTimes.addAll(availableRoomsAndTimes);
		for (Subject subject : subjectsToAssign) {
			if (Collections.disjoint(subject.getApplicableSlots(), effectiveAvailableRoomsAndTimes)) {
				return false;
			}
		}
		return true;
	}

	private static void backtrack() {
		Subject wrongAssignment = (Subject) subjectsAssigned.toArray()[subjectsAssigned.size() - 1];
		Iterator<Subject> iterator = subjectsAssigned.iterator();
		Subject subject;
		while (iterator.hasNext()) {
			subject = iterator.next();
			if (subject.equals(wrongAssignment)) {
				subjectsAssigned.remove(subject);
				subjectsToAssign.add(subject);

				availableRoomsAndTimes.add(subject.getRoomAndTime());
				assignedRoomsAndTimes.add(subject.getRoomAndTime());

				if (debugEnabled) {
					System.out.println("Backtrack: " + subject);
				}

				while (iterator.hasNext()) {
					subject = iterator.next();
					subjectsAssigned.remove(subject);
					subjectsToAssign.add(subject);

					availableRoomsAndTimes.add(subject.getRoomAndTime());
					assignedRoomsAndTimes.add(subject.getRoomAndTime());

					if (debugEnabled) {
						System.out.println("Backtrack: " + subject);
					}
				}
			}
		}
	}

	private static Subject getSubjectToAssign() {
		for (Subject subject : subjectsToAssign) {
			// TODO: add heuristic to get subject
			return subject;
		}
		return null;
	}

	private static void initialize() {
		subjectsToAssign = new LinkedHashSet<>();
		availableRoomsAndTimes = new HashSet<>();
		parseCSVData();
		subjectsAssigned = new LinkedHashSet<>();
		checkedSlotsPerSubject = new HashMap<>();
		assignedRoomsAndTimes = new HashSet<>();
	}

	private static void parseCSVData() {
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
				subjectsToAssign.add(subject);
			}

			if (debugEnabled) {
				System.out.println("parsing finished");
				System.out.println("Subjects to assign: " + subjectsToAssign);
				System.out.println("Rooms: " + rooms);
				System.out.println("********************************");
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
