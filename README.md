# WildAnimalsMadness

## Purpose

This module is a showcase for the GSoC 2019 project "Collective Behavior"

## Contents

The module currently explores group structures, collective behavior structures (modifications made to the core API) and group assignments.

### Components

The module contains two different components:

* `GroupTagModule`: used to tag an entity that belongs to a named group.
* `HivemindComponent`: used to share a behavior tree among multiple actors.

More details on how these components work can be found in the [related blog post](https://casals.io/code/gsoc-reaching-second-milestone/).

### Creatures 

There are four new creatures in this module (CMYK Deers), all having `WildAnimals:greenDeer` as a parent. They can be spawned through the in-game terminal:

* `spawnPrefab cyanDeer`: possesses a static animated behavior (`WildAnimalsMadness:still.behavior`) and is tagged as members of the group `magenta`.
* `spawnPrefab yellowDeer`: does not possess a behavior component and is tagged as members of the group `magenta`.
* `spawnPrefab magentaDeer`: possesses a `Behavior:critter` behavior (to be modified in future tests).
* `spawnPrefab blackDeer`: possesses a `Behavior:critter` behavior (to be modified in future tests).

### Assets

There is a new asset `Group` used to compile all group information when the game is loaded (this allows the user to define/work with groups using JSON files only, similarly to behaviors and prefabs). 

### Commands 
The main system is composed of five commands:

* **First Group Test:**
     * **Command:** `groupTestOne`
     * **Objective:** assign the same behavior to entities in the same group and test dynamic rendering (all deers become magenta after assigned to the respective group).
     * **Restrictions:** identical behavior does not mean identical actions. Actions can be determined by probabilistic events, and even with identical behavior trees each entity will have its own probability roll.
     * **Conditions:** for observable results, use in conjunction with `yellowDeers`.

* **Second Group Test:**
     * **Command:** `groupTestTwo`
     * **Objective:** assign the same behavior change to entities in the same group  
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
        * spawn `cyanDeers`;
        * run the first group test;
        * run the fourth group test.
     * **Restrictions:** the entity skin is not restored on purpose.
        
* **Fifth Group Test:**
     * **Command:** `groupTestFive`
     * **Objective:** restore group parameters from a file.
     * **Issues:** permission rights on modules/WildAnimalsMadness folder. Needs further investigation.     

### Dependencies

This module depend on PRs [3708 (core engine)](https://github.com/MovingBlocks/Terasology/pull/3708) and [31 (WildAnimals)](https://github.com/Terasology/WildAnimals/pull/31).          
