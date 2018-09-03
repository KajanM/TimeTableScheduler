
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;

public class App {

	private static Set<Subject> subjectsToSchedule;
	private static Set<Subject> subjectsScheduled;
	private static Set<RoomAndTime> availableRoomsAndTimes;
	private static Set<RoomAndTime> assignedRoomsAndTimes;

	private static boolean debugEnabled = true;
	private static String pathSeparator;

	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.out.println("Please specify input, output files name");
				return;
			}
			System.out.println("Debug enabled: " + debugEnabled + "\n");
			if (SystemUtils.IS_OS_WINDOWS) {
				pathSeparator = "\\";
				System.out.println("Found Windows OS, setting path separator to " + pathSeparator);
			} else {
				pathSeparator = "/";
				System.out.println("Found Linux OS, setting path separator to " + pathSeparator);
			}

			if (args[0].trim().isEmpty()) {
				System.out.println("Invalid input file name");
				return;
			}
			String inputFileName = args[0].trim() + ".csv";

			initialize();
			parseCSVData(inputFileName);
			assignPriority();
			long startTime = System.currentTimeMillis();
			System.out.println("Recursive scheduling job started at " + new Date() + "\n");
			assignTimeAndRoom();
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime);
			System.out.println("Scheduling process took " + duration + " milli seconds");
			
			if (args[1].trim().isEmpty()) {
				System.out.println("Invalid output file name");
				return;
			}
			String outputFileName = args[1].trim() + ".csv";
			if (!debugEnabled) {
				System.out.println("\nResults");
				displayScheduledSubjects();
			}
			writeResultToCsv(outputFileName);
			System.out.println("\nRunning test cases to verify all constraints are met...");

			TestCases.noSubjectsAreScheduledToSameRoomAndTime(subjectsScheduled);
			TestCases.noSubjectsAreScheduledAtCompulsorySubjectTime(subjectsScheduled);
			TestCases.onlyApplicableTimeIsAssigned(subjectsScheduled);
		} catch (Exception e) {
			System.out.println("Could not solve...");
			if (debugEnabled) {
				e.printStackTrace();
			}
			System.out.println("Remaining subjects to solve ");
			if (debugEnabled) {
				for (Subject subject : subjectsToSchedule) {
					System.out.print(subject.getName() + " ");
					System.out.print(subject.isCompulsory() + " ");
					for (RoomAndTime applicableSlot : subject.getApplicableSlots()) {
						System.out.print(applicableSlot.getTime() + "@" + applicableSlot.getRoom() + " ");
					}
					System.out.println();
				}
			}
			displayUnableToSolveMessage();
		}
	}

	private static void assignPriority() {
		if (subjectsToSchedule.isEmpty()) {
			System.out.println("Initial subject list is empty. Existing");
			return;
		}
		System.out.println(
				"Assigning priority to subjects based on `Minimum Remaining Value` and `Degree` heuristics.\n");
		int priority;
		for (Subject subject : subjectsToSchedule) {
			priority = 0;
			priority += subject.getApplicableSlots().size();
			if (subject.isCompulsory()) {
				priority++;
			}
			subject.setPriority(priority);
		}
	}

	private static void writeResultToCsv(String outputFileName) {
		PrintWriter pw;
		try {
			String jarpath = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String outputpath = Paths.get(jarpath).getParent().toString() + pathSeparator + outputFileName;
			if (debugEnabled) {
				System.out.println("\nWriting to file: " + outputpath);
			}
			pw = new PrintWriter(new File(Paths.get(outputpath).toString()));
			StringBuilder sb = new StringBuilder();
			for (Subject subject : subjectsScheduled) {
				sb.append(subject.getName());
				sb.append(" ,");
				sb.append(subject.getRoomAndTime().getTime());
				sb.append(" ,");
				sb.append(subject.getRoomAndTime().getRoom());
				sb.append("\n");
			}
			pw.write(sb.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(outputFileName + " is not found");
			e.printStackTrace();
		}
	}

	private static void assignTimeAndRoom() {
		if (subjectsToSchedule.isEmpty()) {
			System.out.println("Scheduling job completed at " + new Date());
			return;
		}

		Subject subjectToAssign = getSubjectToAssign();
		if (debugEnabled) {
			System.out.println("Attempting to schedule " + subjectToAssign);
		}

		Set<RoomAndTime> applicableRoomsAndTimes = subjectToAssign.getApplicableSlots();
		Set<RoomAndTime> effectiveAvailableRoomsAndTimes = new HashSet<>();
		for (RoomAndTime roomAndTime : applicableRoomsAndTimes) {
			if (!availableRoomsAndTimes.contains(roomAndTime)) {
				if (debugEnabled) {
					System.out
							.println("RoomAndTime already taken: " + roomAndTime + ". Attempting next applicable slot");
				}
				continue;
			}
			if (subjectToAssign.isCompulsory()) {
				if (debugEnabled) {
					System.out.println("Available room and time: " + availableRoomsAndTimes);
				}
				effectiveAvailableRoomsAndTimes = new HashSet<>();
				Set<String> allocatedTimes = new HashSet<>();
				for (RoomAndTime assignedRoomAndTime : assignedRoomsAndTimes) {
					allocatedTimes.add(assignedRoomAndTime.getTime());
				}
				for (RoomAndTime availableRoomAndTime : availableRoomsAndTimes) {
					if (!allocatedTimes.contains(availableRoomAndTime.getTime())) {
						effectiveAvailableRoomsAndTimes.add(availableRoomAndTime);
					} else {
						if (debugEnabled) {
							System.out.println("Another subject is assigned at time " + availableRoomAndTime.getTime());
							System.out.println("Ignoring " + availableRoomAndTime + " from available time slot "
									+ " since " + subjectToAssign.getName() + " is compulsory");
						}
					}
				}
				if (debugEnabled) {
					System.out.println("Effective available room and time: " + effectiveAvailableRoomsAndTimes);
				}
				if (effectiveAvailableRoomsAndTimes.isEmpty()) {
					continue;
				}
				if (!effectiveAvailableRoomsAndTimes.contains(roomAndTime)) {
					continue;
				}
			} else {
				effectiveAvailableRoomsAndTimes.addAll(availableRoomsAndTimes);
			}
			if (forwardCheck(subjectToAssign, roomAndTime)) {
				availableRoomsAndTimes.remove(roomAndTime);
				assignedRoomsAndTimes.add(roomAndTime);
				Set<RoomAndTime> temp = new HashSet<>();
				temp.addAll(availableRoomsAndTimes);
				if (subjectToAssign.isCompulsory()) {
					for (RoomAndTime slot : temp) {
						if (slot.getTime().equals(roomAndTime.getTime())) {
							availableRoomsAndTimes.remove(slot);
							assignedRoomsAndTimes.add(slot);
						}
					}
				}
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
		if(subjectsScheduled.isEmpty()) {
			System.out.println("No subjects are scheduled yet");
		}
		for (Subject subject : subjectsScheduled) {
			System.out.println(subject.getName() + " | compulsory: " + subject.isCompulsory() + " | "
					+ subject.getRoomAndTime().getRoom() + " | " + subject.getRoomAndTime().getTime());
		}
	}

	private static boolean forwardCheck(Subject subjectToAssign, RoomAndTime roomAndTime) {
		Set<RoomAndTime> effectiveAvailableRoomsAndTimes = new HashSet<>();

		if (subjectToAssign.isCompulsory()) {
			for (RoomAndTime slot : availableRoomsAndTimes) {
				if (!slot.getTime().equals(roomAndTime.getTime())) {
					effectiveAvailableRoomsAndTimes.add(slot);
				}
			}
			for (RoomAndTime slot : assignedRoomsAndTimes) {
				if (slot.getTime().equals(roomAndTime.getTime())) {
					effectiveAvailableRoomsAndTimes.remove(slot);
				}
			}
		} else {
			effectiveAvailableRoomsAndTimes.addAll(availableRoomsAndTimes);
		}
		if (effectiveAvailableRoomsAndTimes.isEmpty()) {
			if (debugEnabled) {
				System.out.println("Can't assign " + roomAndTime + " to " + subjectToAssign.getName()
						+ " due to forward check constraint");
			}
			return false;
		}

		Set<Subject> subjectsToForwardCheck = new HashSet<>();
		subjectsToForwardCheck.addAll(subjectsToSchedule);
		subjectsToForwardCheck.remove(subjectToAssign);
		for (Subject subject : subjectsToForwardCheck) {
			if (Collections.disjoint(subject.getApplicableSlots(), effectiveAvailableRoomsAndTimes)) {
				if (debugEnabled) {
					System.out.println("Can't assign " + roomAndTime + " to " + subjectToAssign.getName()
							+ " due to forward check constraint");
				}
				return false;
			}
		}
		if (debugEnabled) {
			System.out.println("Forward check passed " + subjectToAssign.getName() + " " + roomAndTime);
		}
		return true;
	}

	private static void backtrack() {
		if (subjectsScheduled.isEmpty()) {
			if (getSubjectToAssign() == null || getSubjectToAssign().getPriority() > 100) {
				displayUnableToSolveMessage();
			}
			getSubjectToAssign().incrementPriorityByOne();
			return;
		}
		Subject wrongAssignment = (Subject) subjectsScheduled.toArray()[subjectsScheduled.size() - 1];
		
		wrongAssignment.incrementPriorityByOne();
		Iterator<Subject> iterator = subjectsScheduled.iterator();
		Subject subject;
		while (iterator.hasNext()) {
			subject = iterator.next();
			if (subject.equals(wrongAssignment)) {
				if (debugEnabled) {
					System.out.println("Removing " + subject.getName() + " from scheduled category");
				}
				subjectsScheduled.remove(subject);
				subjectsToSchedule.add(subject);

				availableRoomsAndTimes.add(subject.getRoomAndTime());
				if (subject.isCompulsory()) {
					for (RoomAndTime slot : assignedRoomsAndTimes) {
						if (slot.getTime().equals(subject.getRoomAndTime().getTime())) {
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

	private static void displayUnableToSolveMessage() {
		System.out.println("Unable to find the solution, terminating the program");
		System.out.println("Partial assignment");
		displayScheduledSubjects();
		System.out.println("Please ensure the problem is solvable");
		System.out.println(
				"If solvable, please consider filing an issue at https://github.com/KajanM/TimeTableScheduler");
		System.exit(0);
	}

	private static Subject getSubjectToAssign() {
		Subject maxPrioritySubject = (Subject) subjectsToSchedule.toArray()[0];
		for (Subject subject : subjectsToSchedule) {
			if (subject.getPriority() < maxPrioritySubject.getPriority()) {
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

			String jarpath = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String inputpath = Paths.get(jarpath).getParent().toString() + pathSeparator + inputFileName;
			if (debugEnabled) {
				System.out.println("Reading from file: " + inputpath);
			}
			List<String> input = Files.readAllLines(Paths.get(inputpath), StandardCharsets.ISO_8859_1);

			System.out.println("Input file contents");
			for (String line : input) {
				System.out.println(line);
			}

			String roomsLine = input.get(input.size() - 1);
			for (String room : roomsLine.split(",")) {
				rooms.add(room.trim());
			}
			input.remove(roomsLine);

			for (String line : input) {
				if (line.trim().startsWith("#")) {
					// ignore comment
					continue;
				}
				if (line.trim().isEmpty()) {
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

			System.out.println("\nParsing input csv file completed\n");
		} catch (FileNotFoundException e) {
			System.out.println("Error: no such input file");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error: could not parse the input file");
			e.printStackTrace();
		}
	}
}
