---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# Student Contact Manager Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

This Developer Guide (DG) has been adapted from the AB-3 developer guide found [here](https://se-education.org/addressbook-level3/DeveloperGuide.html).

GitHub Copilot (autocomplete feature) was used throughout the project to assist write Javadocs, Test Cases and implement some methods (by Tahsin, Sarji).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

Given below is a quick overview of main components and how they interact with each other.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2324S2-CS2103T-W08-3/tp/blob/master/src/main/java/scm/address/Main.java) and [`MainApp`](https://github.com/AY2324S2-CS2103T-W08-3/tp/blob/master/src/main/java/scm/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* implements its functionality using a concrete `{Component Name}Manager` class (which follows the corresponding API `interface` mentioned in the previous point.

For example, the `Logic` component defines its API in the `Logic.java` interface and implements its functionality using the `LogicManager.java` class which follows the `Logic` interface. Other components interact with a given component through its interface rather than the concrete class (reason: to prevent outside component's being coupled to the implementation of a component), as illustrated in the (partial) class diagram below.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2324S2-CS2103T-W08-3/tp/blob/master/src/main/java/scm/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` that is made up of parts (e.g.`CommandBox`, `ResultDisplay`, `PersonListPanel`, `StatusBarFooter`, etc.) All these, including the `MainWindow`, inherit from the abstract `UiPart` class which captures the commonalities between classes that represent parts of the visible GUI.

The `Ui` component uses the JavaFx UI framework. The layout of these UI parts are defined in matching `.fxml` files that are in the `src/main/resources/view` folder. For example, the layout of the [`MainWindow`](https://github.com/AY2324S2-CS2103T-W08-3/tp/blob/master/src/main/java/scm/address/ui/MainWindow.java) is specified in [`MainWindow.fxml`](https://github.com/AY2324S2-CS2103T-W08-3/tp/blob/master/src/main/resources/view/MainWindow.fxml)

The `Ui` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `Ui` relies on the `Logic` component to execute commands.
* depends on some classes in the `Model` component, as it displays the `Person` and `Schedule` objects residing in the `Model`.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2324S2-CS2103T-W08-3/tp/blob/master/src/main/java/scm/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, it is passed to an `AddressBookParser` object which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
2. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
3. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above (for simplicity), in the code it can take several interactions (between the command object and the `Model`) to achieve.
4. The result of the command execution is encapsulated as a `CommandResult` object which is returned back from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name e.g., `AddCommandParser`) which uses the other classes shown above to parse the user command and create a `XYZCommand` object (e.g., `AddCommand`) which the `AddressBookParser` returns back as a `Command` object.
* All `XYZCommandParser` classes (e.g., `AddCommandParser`, `DeleteCommandParser`, ...) inherit from the `Parser` interface so that they can be treated similarly where possible e.g, during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2324S2-CS2103T-W08-3/tp/blob/master/src/main/java/scm/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the contact manager data, i.e., all `Person` objects (which are contained in a `UniquePersonList` object), as well as all schedule list data (i.e., all `Schedule` objects, which are contained in a `ScheduleList` object).
* stores the currently 'selected' `Person` objects (e.g., results of a search query) as a separate _filtered_ list which is exposed to outsiders as an unmodifiable `ObservableList<Person>` that can be 'observed', e.g., the UI can be bound to this list so that the UI automatically updates when the data in the list change.
* stores the currently 'selected' `Schedule` objects in a manner analogous to the above 'selected' `Person` objects.
* stores a `UserPref` object that represents the user’s preferences. This is exposed to the outside as a `ReadOnlyUserPref` objects.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<box type="info" seamless>

**Note:** An alternative (arguably, a more OOP) model for the `AddressBook` part of `Model` is given below. It has a `Tag` list in the `AddressBook`, which `Person` references. This allows `AddressBook` to only require one `Tag` object per unique tag, instead of each `Person` needing their own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />

</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2324S2-CS2103T-W08-3/tp/blob/master/src/main/java/scm/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both contact manager data, user preference data, and schedule list data in JSON format, and read them back into corresponding objects.
* inherits from `AddressBookStorage`, `UserPrefStorage`, and `ScheduleStorage`, which means it can be treated as any one of these (if the functionality of only one is needed).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `scm.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Finding and Exporting feature

#### Implementation

The `FindAndExportCommand` feature is implemented to find users based on specified criteria and export their information. The command allows filtering of users by tags, name, and address, and exports the filtered list to a specified file, in a specified format.

This feature implements the following significant operations:

* `FindAndExportCommand#createPredicateForFiltering()`: This method creates a predicate for filtering users based on the provided tag, name, and address. It returns a predicate that can be used to filter the list of users.

* `FindAndExportCommand#execute()`: This method executes the command. It first updates the filtered person list in the model based on the predicate created by `createPredicateForFiltering()`. Then, it exports the filtered list to the specified file.

* `FindAndExportCommand#exportData()`: This method exports the filtered list of users to the specified file. It determines the file format based on the file extension and calls the appropriate method to export the data in that format.

* `FindAndExportCommand#exportDataAsJson()`: This method exports the filtered list of users to a JSON file. It uses `JsonAddressBookStorage` to save the list of users to the file.

* `FindAndExportCommand#exportDataAsCsv()`: This method exports the filtered list of users to a CSV file. It manually constructs the CSV data and writes it to the file.

The implementation of this feature follows Object-Oriented Programming (OOP) principles closely to prevent any one method from doing too many tasks. The logic of this feature is implemented in similar ways to how the application saves its own main save file.

Below is a sequence diagram on how the `FindAndExportCommand` feature works:

<puml src="diagrams/FindAndExportSequenceDiagram.puml" width="550" />

### Importing feature

#### Implementation

The import feature is implemented through the use of `JsonAddressBookStorage` . Given that the user of the application has a JSON file (containing contacts in a format similar to the save file of the application) or a CSV file (containing contacts in a format similar to how data is exported as a CSV file), they will be able to import such contacts by providing the filename of the JSON or CSV file. The implementation of the feature is somewhat similar to how the application natively reads its own contact manager data, and as such uses similar functions.

This feature implements the following significant operations, other than the ones it is overriding:

* `ImportCommand#retrievePersonsFromFile()`: Retrieves the `Person`s that are read from a given file and inserts them into a list of `JsonAdaptedPerson`.
* `ImportCommand#readPersons()`: Reads the `Person`s currently inside the file to be read. This command succeeds only if the application is able to read the file and if the file is in the correct JSON format.
* `ImportCommand#readPersonsFromCSV()`:  Reads the `Person`s currently inside the file to be read. This command succeeds only if the application is able to read the file and if the file is in the correct CSV format.

This command is implemented in the above manner in order to follow OOP principles more closely, more specifically to prevent any one method from doing too many tasks. Moreover, the reuse of `JsonAddressBookStorage` and other related classes is done in order to aid future development of the feature. Following the same spirit, the logic of this feature is implemented in similar ways to how the application reads its own main save file.

There is initially an alternative considered to refit the entire logic of the model and saving mechanism to fit this import feature. However, the current implementation is chosen over this due to the possibility of rewriting many pieces of unrelated code and of unknowingly breaking other features in the process. Another alternative that was not followed was to only use Jackson-based features to implement the import feature, in order to have more control over the code itself. However, as this feature should integrate with the exporting feature of the application, it became apparent that code reuse should be prioritised.

Below is a sequence diagram on how the import feature works:

<puml src="diagrams/ImportSequenceDiagram.puml" width="550" />

### Edit schedule feature

#### Implementation

The edit schedule feature is implemented through the use of `EditScheduleDescriptor` and `EditScheduleCommand`. Given a valid index to edit, this command will be able to edit the details of the `Schedule` in such index. The implementation of the feature is similar to that of `EditCommand`.

This feature implements the following operations, other than the methods that it is already overriding:

* `EditScheduleCommand#createEditedSchedule()`: Creates a new `Schedule` given an old schedule to edit as well as an `EditScheduleDescriptor`.

This command is implemented in the above manner to improve its adherence to OOP principles, as well as to allow it to have similarities to the implementation of `EditCommand`. This would allow it to be more extensible and supportive of further development.

### Delete schedule feature

The delete schedule feature is implemented through the use of `DeleteScheduleCommand`. Given a valid index found in the current list of filtered schedules, the command will be able to delete the `Schedule` in such index. The implementation of the feature is similar to that of `DeleteCommand`, in order to improve maintainability.

### Find schedule feature

#### Implementation

The find schedule feature is implemented through the use of `FindScheduleCommand`. Given a set of attributes (title, description, beforeDateTime, afterDateTime, duringDateTime) to search for, this command will be able to find all schedules that match the all attributes input by the user. The implementation of the feature is similar to that of `FindCommand`, where predicates are used to filter out the schedules. An attribute that is not specified by the user will be associated with a predicate that always returns true. A schedule is displayed if and only if it satisfies all the predicates.

This command is implemented in the above manner to improve its adherence to OOP principles, as well as to allow it to have similarities to the implementation of `FindCommand`. This would allow it to be more extensible and supportive of further development.

### Change theme feature

The change theme feature is implemented through the use of `ThemeCommand`. Given a valid theme (dark or light), the command will be able to change the `Theme` to the given theme name. The implementation of the feature is similar to that of `HelpCommand`, in order to be consistent with the codebase of the project. 

When this command is `executed`, it changes the GUI Setting by calling the `Model#setGuiSettings()` method (which changes the gui theme) and instantiates a `CommandResult` with `changeTheme` parameter being `True`. The `MainWindow`, being responsible for executing all commands, executes this command and checks whether the `CommandResult#isChangeTheme()` is `True`. When the condition is met, it executes the `MainWindow#handleChangeTheme()` to change `CSS` of the Views to effectively change the theme.


### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current contact manager state in its history.
* `VersionedAddressBook#undo()` — Restores the previous contact manager state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone contact manager state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial contact manager state, and the `currentStatePointer` pointing to that single contact manager state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the contact manager. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the contact manager after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted contact manager state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified contact manager state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the contact manager state will not be saved into the `addressBookStateList`.

</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous contact manager state, and restores the contact manager to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline reaches the end of diagram.

</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the contact manager to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest contact manager state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the contact manager, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all contact manager states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo executes:**

* **Alternative 1 (current choice):** Saves the entire contact manager.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command are correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, configuration, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [Configuration guide](Configuration.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* has a need to manage a significant number of contacts and events
* prefer desktop apps over other types
* can type fast
* prefers typing to mouse interactions
* is reasonably comfortable using CLI apps
* proficient with the use of a computer
* seeks an efficient way to manage a significant number of contacts within their academic and professional circles
* has a busy schedule
* desires an interactive and responsive app

**Value proposition**: manage contacts and schedules faster and more effectively than with a normal scheduling app.

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a/an ...            | I want to ...                                                    | So that I can ...                                                |
|----------|------------------------|------------------------------------------------------------------|------------------------------------------------------------------|
| `* * *`  | new user               | receive help messages and instructions for using the application | learn how to use its features effectively                        |
| `* * *`  | user                   | add a new contact                                                |                                                                  |
| `* * *`  | user                   | delete a contact                                                 | remove entries that I no longer need                             |
| `* * *`  | user                   | edit a contact                                                   | change a person's details                                        |
| `* * *`  | user                   | delete a contact                                                 | remove entries that I no longer need                             |
| `* * *`  | user                   | save contacts on the disk                                        | store details of all my contacts                                 |
| `* * *`  | user                   | list contacts                                                    | know what contacts I have                                        |
| `* * *`  | user                   | search for a contact by any criteria                             | find their information when I need it.                           |
| `* * *`  | user                   | import contacts                                                  | easily add multiple contacts at once from another source.        |
| `* * *`  | user                   | export contacts                                                  | easily integrate with existing data.                             |
| `*`      | user                   | have my information be secure                                    | so that my contacts are not leaked to others.                    |
| `*`      | busy user              | set reminders for specific contacts                              | connect with them better.                                        |
| `*`      | efficient user         | use keyboard shortcuts for frequently-used actions               | work more efficiently.                                           |
| `*`      | user                   | track the history of interactions with specific contacts         | personalize my communication and build stronger relationships.   |
| `* *`    | user                   | have a user-friendly interface                                   | easily navigate the application.                                 |
| `* * *`  | user with many friends | know which people are in which friend groups                     | keep track of my friend groups.                                  |
| `* * *`  | user                   | import/export my contact list in a common format                 | back up my data and export/import it from/to other applications. |
| `* *`    | user                   | change deadlines                                                 | manage my schedule more effectively.                             |
| `*`      | forgetful user         | use commands (possibly with aliases) that are easily remembered  | find the application easier to use.                              |
| `*`      | user                   | find the people I have not interacted with in a long time        | maintain a good relationship with them.                          |
| `*`      | user                   | look at the people I interact with the most                      | know who I spend the most time with.                             |
| `*`      | user                   | set data validation rules for certain fields                     | ensure the accuracy of my contact information.                   |
| `* *`    | user                   | make a clear schedule of what I will do in the future            | plan my schedule well.                                           |
| `*`      | user                   | set recurring tasks or reminders associated with contacts        | maintain the connections I have.                                 |

### Use cases

(For all use cases below, the **System** is the `Student Contact Manager` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Delete a person**

**MSS**

1.  User requests to list persons
2.  Student Contact Manager shows a list of persons
3.  User requests to delete a specific person in the list
4. Student Contact Manager deletes the person

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. Student Contact Manager shows an error message.

      Use case resumes at step 2.

**Use case: Finding by tag**

**MSS**

1. User requests to search for users by a specific tag.
2. The application displays a list of users who are tagged with the specified tag.
3. User reviews the list to find the desired contact(s).

Use case ends.

**Extensions**

* 1a. The user enters extra spaces before or after the command.

  * 1a1. The application automatically trims the extra spaces and processes the command.

    Use case resumes at step 2.

* 1b. The user specifies multiple tags or incorrect parameters.

  * 1b1. The application shows an error message and instructions for correct input format.

    Use case ends.

* 2a. No users are found with the given tag.

  * 2a1. The application notifies the user that no contacts were found with the specified tag.

    Use case ends.


**Use case: Finding users and exporting data**

**MSS**

1. User requests to search for users by a specific tag, and optionally, by name and/or address, and wishes to export the results to a specified file.
2. The application filters the users according to the given criteria and displays the filtered list.
3. The application exports the filtered list to the specified file.
4. The application notifies the user that the export was successful.

Use case ends.

**Extensions**

* 1a. The user includes extra spaces in the command.

    * 1a1. The application trims the extra spaces and processes the command.

      Use case resumes at step 2.

* 1b. The user does not specify a filename.

    * 1b1. A default filename is used as the specified file.

      Use case resumes at step 2.

* 2a. No users match the given criteria.

  * 2a1. The application alerts the user that no matching contacts were found.

    Use case ends.

* 3a. The user specifies an invalid filename or multiple filenames.

  * 3a1. The application shows an error message regarding the filename issue.

    Use case resumes at step 1.

**Use case: Importing contacts**

**MSS**

1. User requests to import contacts from one or multiple JSON files located in the `./data/` directory.
2. The application validates the existence and format of the specified file(s).
3. The application imports the contacts from the file(s) into the contact manager.
4. The application notifies the user that the import was successful.

Use case ends.

**Extensions**

* 1a. The user includes extra spaces in the command.

    * 1a1. The application trims the extra spaces and processes the command.

      Use case resumes at step 2.

* 2a. One or more specified files do not exist in the ./data/ directory.

  * 2a1. The application informs the user which files could not be found.

    Use case ends.

* 2b. One or more files are not in the correct JSON format.

  * 2b1. The application notifies the user which files have format issues.

    Use case ends.

**Use case: Adding a schedule**

**MSS**

1. User requests to add a schedule with the command add_schedule, specifying the title, description, start date and time, and end date and time.
2. The application adds the schedule to the system.
3. The application displays a message confirming the addition of the schedule.

Use case ends.

**Extensions**

* 1a. The user enters an invalid command format.

    * 1a1. The application shows an error message about the incorrect command format.

      Use case resumes at step 1.

* 1b. The user enters an end date and time that is before the start date and time.

    * 1b1. The application shows an error message about the incorrect time range.

      Use case resumes at step 1.

* 1c. The user tries to add a schedule that conflicts with an existing one.

    * 1c1. The application shows an error message about the schedule conflict.

      Use case resumes at step 1.

* 2a. The application encounters an error while saving the new schedule.

    * 2a1. The application alerts the user that the schedule could not be added.

Use case ends.

### Add Schedule Command Implementation

This section explains the implementation of the `AddScheduleCommand`.
Below is a sequence diagram that shows the interactions involved when a user adds a schedule through the application.

<puml src="diagrams/AddScheduleCommandSequenceDiagram.puml" width="280" />

As shown in the diagram, the `AddScheduleCommand` takes user input from the UI, parses it to create a schedule.
It interacts with the model to add the schedule to the system. A success message is then relayed back to the user.

**Use case: Editing a schedule**

**MSS**

1. User requests to edit a schedule in a particular index.
2. The application edits the wanted schedule and changes the fields given by the user.
3. The application displays a message confirming the edit.

Use case ends.

**Extensions**

* 2a. The current view does not have the index the user requested.

  * 2a1. The application displays an error message and no changes are made.
  
    Use case ends.
  
* 2b. The user does not give any fields for editing.

  * 2b1. The application displays an error message and no changes are made.

    Use case ends.

* 2c. The user provides invalid values for the fields to be edited.

  * 2c1. The application displays an error message and no changes are made.

    Use case ends.

**Use case: Listing schedules**

**MSS**

1. User requests to view all schedules currently stored.
2. The application displays all schedules that are currently stored.

Use case ends.

**Extensions**

* 2a. There are no schedules currently stored.

  * The application displays no schedules and informs the user.

    Use case ends.

**Deleting a schedule**

**MSS**

1. User requests to delete a schedule currently stored in the application with a currently available index.
2. The application deletes the desired schedule.
3. The application informs the user that the deletion is successful.

**Extensions**

* 2a. The current index is nonexistent in the current view.

  * 2a1. The application displays an error message and does not delete any schedule.

    Use case ends.

**Use case: Viewing schedules in a calendar**

**MSS**

1. User requests to view the calendar with the command `calendar_view`.
2. The application displays a calendar for the current month, with all scheduled events marked or listed under their respective dates.
3. User views the scheduled events on the calendar.

Use case ends.

**Extensions**

* 2a. The current month has no scheduled events.

    * 2a1. The application displays an empty calendar with no events marked.

      Use case ends.

* 2b. The user requests to view the calendar for a specific month.

    * 2b1. The application displays the calendar for the specified month with events marked.

      Use case ends.

### Non-Functional Requirements

1. Should work on any _mainstream OS_ as long as it has Java `11` or above installed.
2. Should be able to hold up to 1000 persons without a noticeable sluggishness in performance for typical usage.
3. Designed for users who can type fast, making command execution quicker than using a mouse.
4. Exporting and importing operations should be completed within a reasonable time frame, not exceeding 2 seconds per operation.
5. All other operations should be completed within 500ms, measured after the user has entered an input.
6. The application must ensure the security and privacy of private contact details during import and export operations. Data should be handled in a manner that prevents unauthorized access.

### Glossary

* **Mainstream OS**: Windows, Linux (and its various distributions), MacOS
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Tag**: A keyword or label assigned to a contact to categorize them based on certain criteria or characteristics. Tags allow users to filter and organize contacts more efficiently.
* **Command**: A specific instruction given by the user to the application to perform a particular operation, such as searching, exporting, or importing data.
* **JSON File**: A file format that uses human-readable text to store and transmit data objects consisting of attribute-value pairs. It's used for importing and exporting contact information in the application.
* **Filter Criteria**: The conditions or parameters (such as tag, name, or address) used to search for specific contacts within the application. These criteria help in refining search results to match the user's requirements more closely.
* **Export**: The process of saving data from the application to a file, which can then be used outside the application. This can be particularly useful for creating backups or for using the contact information in other software or services.
* **Import**: The process of adding or updating contacts in the application by loading them from an external file, typically in a structured format like JSON. This allows users to quickly populate the application with a large number of contacts.
* **Contact Manager**: The component or feature of the application that handles the storage, retrieval, organization, and modification of contact information. It is central to the application's functionality regarding managing contact details. In this context, this term also refers to the management of schedule information.
* **Data Directory**: A specific folder or location within the system where the application stores its data files, such as contact exports or imports. The `./data/` directory is an example where JSON files might be found for import operations.
--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</box>

### Launch and shutdown

1. Initial launch

   1. Download the jar file and copy into an empty folder

   2. Double-click the jar file Expected: Shows the GUI with a set of sample contacts. The window size may not be optimum.

2. Saving window preferences

   1. Resize the window to an optimum size. Move the window to a different location. Close the window.

   2. Re-launch the app by double-clicking the jar file.<br>
       Expected: The most recent window size and location is retained.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command. Multiple persons in the list.

   2. Test case: `delete 1`<br>
      Expected: First contact is deleted from the list. Details of the deleted contact shown in the status message. Timestamp in the status bar is updated.

   3. Test case: `delete 0`<br>
      Expected: No person is deleted. Error details shown in the status message. Status bar remains the same.

   4. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{explain how to simulate a missing/corrupted file, and the expected behavior}_

## Appendix: Planned Enhancements

Team size: 4

1. Make the `calendar_view` window output more precise: The current `calendar_view` command does not support events that extend beyond one day very well: an event is only displayed on the starting day of the event. We plan to make the event to have specific colour bars that are able to extend beyond one day, allowing the user to better visualise multi-day events better.
2. Allow for symbols inside person names, event titles, and event descriptions: this allows individuals to use their full legal name, as well as allows users to be more flexible with the content of their events titles and descriptions. We plan to allow for symbols such as `/`, `-`, and `@` inside the aforementioned fields to better accommodate users.
3. Allow for more variety on input formats: the use of `add` and `add_schedule` can be somewhat cumbersome as the user has to type out several fields in order for the application to accept the user input. We plan to allow for more flexibility on the required fields, such as by reducing the requirement to add a person to only a phone number or email. For schedules, we plan to have descriptions, start time, and end time to be optional fields.
4. Exporting people according to any searchable criteria: Currently, `find_and_export` only supports finding and exporting persons with the same tag. We plan to create another `export` command that would allow the exporting of individuals with any criteria, such as names, contact numbers, addresses, etc.
5. Overflow when adding schedules: There is an issue with regards to medium- and long-length titles for schedules. We plan to accommodate longer event titles by adding scrolling and text wrapping up to a certain length (e.g., 30 characters for titles, 200 characters for descriptions).
6. Overflow when adding contacts: there may be UI overflow issues when adding contacts that have long names, phone numbers, or addresses. We plan to fix this by both having text wrapping and limiting the length of such fields to reasonable lengths (e.g., 50 characters for names, 20 characters for phone numbers, and 200 characters for addresses).)
