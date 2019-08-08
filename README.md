# WildAnimalsMadness

## Purpose

This module is a showcase for the GSoC 2019 project "Collective Behavior". Please see the "Testing" section at the end of this file for instructions on how to proceed.

## Contents

The module currently explores group structures, collective behavior structures (modifications made to the core API) and group assignments.

### Grouping entities

Entities can be grouped in two differend manners: **groups** and **hives**.

#### Groups:

* Entities possess a common attribute (group label).
* Entities can possess the same behavior mechanism, but each with its individual state. 
* Behavior is processed individually (by each entity). 
* From the behavioral perspective, groups are used in scenarios where the entities should have the same behavior mechanism and eventually have their behavior affected in the same manner, but not necessarily perform identical actions. 
* Entities can belong to multiple groups at the same time.

#### Hives

* Entities possess a common attribute (group label).
* Entities must possess the same behavior mechanism and state. 
* Behavior is processed by the hive.
* From the behavioral perspective, hives are used in scenarios where entities must act the same way (in unison).
* While at this moment there's no mechanism restricting entities to belong to multiple hives at the same time, it doesn't make sense from a behavior perspective: the entity will adopt the behavior enforced by the last hive joined.

### Components

The module contains two different components for group behavior:

* `GroupTagModule`: used to tag entities that belong to one or more groups. This component possesses a list of group identifiers and a "memory" mechanism, comprised of a `BehaviorTree` and an `Interpreter`. These are used to store the original Behavior Tree and the respective state in which the entity was right before joning a group or hive.

* `HiveMindComponent`: used to enforce unison behavior over a hive of entities. It is designed to work in coordination with the `GroupModule` (at this point, only to use the memory mechanism). It also possesses an identifier (used for unique identification), a behavior marker (which allows eventual behavior changes in a simpler manner) and a set of group members (entities).

There is also an additional component from the `Behaviors` module used especifically by this module:

* `Behaviors:CollectiveBehavior`: used by `HiveMindComponent` to share a behavior tree among multiple actors. 

More details on how these components work can be found in the [related blog post](https://casals.io/code/gsoc-reaching-second-milestone/).

This module also possess an alpha version of *flocking*, which is used to test emergent behavior (behavior that emerges from a group but does not necessarily requires a unison behavior among its members). The flocking implementation is composed by:

* `flock.behavior`, a behavior tree description;
* `FlockMoveAction`, used to re-define the movement targets for entities belonging to a flock;
* `FlockComponent`, used to hold all data related to flocking; and
* `FlockSystem`, which ensures that the flock is updated whenever a new entity joins it. All flocking data is also shared and updated between the flock members.


### Creatures 

There are four new creatures in this module (CMYK Deers), all having `WildAnimals:greenDeer` as a parent. They can be spawned through the in-game terminal:

* `spawnPrefab cyanDeer`: possesses a static animated behavior (`WildAnimalsMadness:still.behavior`) and is tagged as a member of the groups `magenta` and `cyan`.
* `spawnPrefab yellowDeer`: does not possess a behavior component and is tagged as a member of the group `yellow`.
* `spawnPrefab magentaDeer`: possesses a `WildAnimalsMadness:still.behavior` behavior component and is tagged as a member of the group `magenta`.
* `spawnPrefab blackDeer`: possesses a `WildAnimalsMadness:still.behavior` behavior component and is tagged as a member of the group `black`.

### Assets

There is a new asset `Group` used to compile all group information when the game is loaded (this allows the user to define/work with groups using JSON files only, similarly to behaviors and prefabs). **Requires: [MovingBlocks/Terasology PR#3717](https://github.com/MovingBlocks/Terasology/pull/3717)**.

### Commands 
The main system is composed of seven commands:

**Load test data:**
   * **Command:** `loadTestData`
   * **Objective:** Test the new Group asset and populate the test data pool.

* **First Group Test:**
     * **Command:** `groupTestOne`
     * **Objective:** assign the same behavior to entities in the same group. This test uses only the group identifier from the GroupTag component, which means the group formation is dynamic: all group members are identified by their tag at the moment of the behavior assignment. This is a way of passively creating groups, i.e., identifying group members and acting upon external stimuli. From the game perspective, there are (i) entities tagged as belonging to a group X and (ii) a Group asset that defines the characteristics for group X (through GroupData).
     * **Restrictions:** identical behavior does not mean identical actions. Actions can be determined by probabilistic events, and even with identical behavior trees each entity will have its own probability roll.
     * **Conditions:** for observable results, use in conjunction with `yellowDeers`.

* **Second Group Test:**
     * **Command:** `groupTestTwo`
     * **Objective:** assign the same behavior change to entities in the same group. This test uses groups that require the HiveMind component. When a group possesses requires a HiveMind, a new Hive entity is created when the system is initialised. A Hive is able to process stimuli individually and propagate their effects to group members, acting as a mediator between the environment and the group members. In this test, the members of a Hive are accessed and (i) have a common behavior assigned to them, and (ii) have this same behavior modified by a external command. Note that in this case the same could be accomplished without the use of a Hive entity (see First Group Test).  
     * **Restrictions:** identical behavior changes still do not mean identical actions. Actions can be determined by probabilistic events, and even with identical behavior trees each entity will have its own probability roll.
     * **Conditions:** for observable results, use in conjunction with `cyanDeers` or run `groupTestOne` first. 

* **Third Group Test:**
     * **Command:** `groupTestThree`
     * **Objective:** assign the same BT to multiple actors at once. The idea is to cover scenarios where synchronized behavior change is not enough. In order for it to be possible, the core engine logic package was extended with a `CollectiveBehaviorComponent` class (and respective tree runner/interpreter).
     * **Restrictions:** identical behavior should be observed, with few exceptions (such as random neighbor move in `Behaviors:critter`).
     * **Conditions:** for observable results, use in conjunction with `yellowDeers`.
       
* **Fourth Group Test:**
     * **Command:** `groupTestFour`
     * **Objective:** restore the original behavior state of an entity (before it joined a group). The idea is to restore not only the original BT, but the exact node/state in which the entity was.
     * **Conditions:** for (better) observable results, follow this script:
        * spawn `yellowDeers`;
        * run the first group test;
        * run the fourth group test.
     * **Restrictions:** the entity skin is not restored on purpose.

* **Fifth Group Test:**
     * **Command:** `groupTestFive`
     * **Objective:** test the flocking behavior. Black deers are assigned a `FlockComponent` and a `flock` behavior. The related system makes sure that the flock parameters are updated whenever a new entity receives a `FlockComponent`. Flocking behavior should make the entities converge to the group center (randomized for emergence emulation).
     * **Restrictions:** this is very early work. The entities still don't keep a minimal distance from each other. Since the flocking algorithm uses a random reference to establish the group center, flocks tend to get stable in one location until a new entity joins the flock. This test is composed by two steps: in the first one the user spawns a few black deers. Executing the command creates the flock. In the second step, the user spawns an additional black deer, and re-running the command updated the flock.
        
* **Nuke:**
     * **Command:** `nuke`
     * **Objective:** destroy all entities containing the components `GroupTagComponent` and `HiveMindComponent`.

### Dependencies

This module depend on PRs [3708 (core engine)](https://github.com/MovingBlocks/Terasology/pull/3708) and [31 (WildAnimals)](https://github.com/Terasology/WildAnimals/pull/31).

### Testing

To make sure your tests run smoothly:

* If you are using a saved game, make sure to run the `nuke` command first. This will ensure no unwanted group entities will interfere with the tests.
* You **must** run the command `loadTestData` once before starting the tests. This command emulates the group asset loading from the disk. Hives are created here according to each group requirement, but not populated.
* Please be aware of the restrictions listed for each of the test commands, and feel free to report any issues (feedback is always welcome).
