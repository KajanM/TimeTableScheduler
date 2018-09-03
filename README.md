GitHub repo: https://github.com/KajanM/TimeTableScheduler

# CS3612 - Intelligent Systems
## Assignment: Time_Tabling_With_CSP
## To run the program
1. navigate to the `executable` directory which has `scheduler.jar`
1. ensure the input, output `csv` files are in the same directory
1. open terminal from that directory
1. run `java -jar scheduler.jar <input_file_name> <output_file_name>` 
(do not include `.csv` extension)

## Sample output of the program
```
[kajan@kajan executable]$ java -jar scheduler.jar test-input-2 test-output
Debug enabled: false

Found Linux OS, setting path separator to /
Input file contents
# 6 subjects, 6 available slots

S1, o, M1, M2, M3, T1
S2, o, M1, M2, M3, T2
S3, o, M1, M2, M3, T3
S4, o, M3
S5, c, M2
S6, o, M1
R1

Parsing input csv file completed

Assigning priority to subjects based on `Minimum Remaining Value` and `Degree` heuristics.

Recursive scheduling job started at Mon Sep 03 06:46:30 IST 2018

Scheduling job completed at Mon Sep 03 06:46:30 IST 2018
Scheduling process took 15 milli seconds

Results
S4 | compulsory: false | R1 | M3
S6 | compulsory: false | R1 | M1
S5 | compulsory: true | R1 | M2
S1 | compulsory: false | R1 | T1
S2 | compulsory: false | R1 | T2
S3 | compulsory: false | R1 | T3

Running test cases to verify all constraints are met...
Passed: No subjects are assigned to same room and time
Passed: No other subjects assigned at a compulsory subject time
Passed: Assigned time is one of given applicable times
[kajan@kajan executable]$ 
```
## Modeling the problem

### Variables

1. `Subject` object representing a subject that need to be scheduled with the following fields
    *   name 
    *   compulsory or not
    *   scheduled room and time (represented by a single object `RoomAndTime`)
    *   priority (to apply heuristics when selecting which subject to schedule next/first)
    *	set of applicable slots
    
1. `RoomAndTime` object to hold combination of room and time. Since both room and time need to be assigned for a subject they are combined to form a single object to apply `OOP` concepts.

1. `subjectsToSchedule` - a `Set`(_no duplicates_) of `Subject`s to be scheduled.
1. `subjectsScheduled` - _Ordered_ `set` of `Subject`s that are scheduled so far.
1. `availableRoomsAndTimes` - `Set` of `RoomAndTime` objects that are available.
1. `assignedRoomsAndTimes`- `Set` of `RoomAndTime` objects that are scheduled.  

### Domain
Domain for each variable is extracted from the input `csv` file.
Input file should be of the following format for correct processing.
```
#comments are ignored from parsing

Subject_1, c, M1, M3, Tu2
Subject_2, o, Tu1, W1, Th2
Subject_3, c, M1, M3, W1
.
.
.
Subject_n, o, M3, Th2
R1, R2, R3
```

### Initializing
1. While parsing the input `csv` file, create `RoomAndTime` and `Subject` objects.
1. add all `Subject`s to `subjectsToSchedule`
1. add all `RoomAndTime` to `availableRoomsAndTimes`
1. initialize `subjectsScheduled` as empty `set`
1. initialize `assignedRoomsAndTimes` as empty `set`

### Assigning a time slot to a subject
This is a recursive operation, i.e this operation calls itself until there is no `Subject` to assign or __infinite loop__ is detected.
1. get a `Subject` from `subjectsToSchedule`
1. iterate through each of the applicable timeslot for the selected `Subject`
1. check if the applicable slot is `consistent` to current assignment
1. forward check to see if the assignment will fail any of the remaining `Subject` to schedule
1. once a `consistent` slot is assigned to the `Subject` move it to the `subjectsScheduled` `set`
1. update `availableRoomsAndTimes`, `assignedRoomsAndTimes` accordingly
1. if no consistent value is found then `backtrack`

### Checking consistency
When selecting a `RoomAndTime` to assign to a `Subject` following constraints are checked.

1. A given subjects can be assigned only to one of the possible time slots given for that subject.
1. No other subject is assigned to a time that is already assigned to a compulsory subject regardless of room.
1. Two subjects cannot be assigned to the same room if they are assigned to the same time slot.

Test cases are also written to ensure the end result satisfies above constraints. They are availabe at `TestCases.java`. Below is a snippet of the code.
```
public static void onlyApplicableTimeIsAssigned(Set<Subject> subjectsScheduled) {
	for(Subject subject : subjectsScheduled) {
		if(!subject.getApplicableSlots().contains(subject.getRoomAndTime())) {
			System.out.println("Failed: Assigned time is not applicable for the subject " + subject);
			return;
		}
	}
	System.out.println("Passed: Assigned time is one of given applicable times");
}
```
### Forward checking
Before assigning a consistent `RoomAndTime` value to a `Subject`, the logic ensures that remaining `Subject`s to be scheduled has atleast one `applicable` `RoomAndTime` which is also availabe.

### Backtracking
When the logic finds there is no consistent `RoomAndTime` to assign to a `Subject`, it removes the cause of assignment from `subjectsScheduled` and put into `subjectsToSchedule` and update other relevant parameters(`availableRoomsAndTimes`, ``assignedRoomsAndTimes`)

### Heuristics
Priority is assigned to each `Subject` based on following heuristics. `Subject` with smaller priority value is considered to have higer priority.

#### Minimum Remaining Values
`Subject` with the fewest number of applicable slots is chosen for next assignment.

#### Least-Constraining Value
Priority values of compulsory `Subject`s are increased by one to give higer priority to optional `Subject`s.