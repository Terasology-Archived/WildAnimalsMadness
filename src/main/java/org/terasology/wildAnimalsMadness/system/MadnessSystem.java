/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.wildAnimalsMadness.system;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.StaticSound;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.CollectiveBehaviorComponent;
import org.terasology.logic.behavior.CollectiveInterpreter;
import org.terasology.logic.behavior.GroupMindComponent;
import org.terasology.logic.behavior.GroupTagComponent;
import org.terasology.logic.behavior.Interpreter;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.Group;
import org.terasology.logic.behavior.asset.GroupData;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.registry.In;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.utilities.Assets;
import org.terasology.wildAnimalsMadness.components.FlockComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class MadnessSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(MadnessSystem.class);
    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;

    private final Map<String, Group> groupsFromAssets = new HashMap<>();
    private final Map<String, GroupData> groups = new HashMap<>();
    private final Map<String, EntityRef> hives = new HashMap<>();

    @Override
    public void initialise() {
        /**
         * Loads all Group (.group) assets registered
         */
        List<ResourceUrn> uris = Lists.newArrayList();
        uris.addAll(new ArrayList<>(assetManager.getAvailableAssets(StaticSound.class)));

        for (ResourceUrn uri : assetManager.getAvailableAssets(Group.class)) {
            try {
                Optional<Group> asset = assetManager.getAsset(uri, Group.class);
                asset.ifPresent(group -> groupsFromAssets.put(group.getGroupData().getGroupLabel(), group));
            } catch (RuntimeException e) {
                logger.info("Failed to load groups.", uri, e);
            }
        }

    }

    /**
     * Manually populates the groups map using GroupData
     * Group "magenta" is loaded from disk if the correspondent
     * file (magenta.group) is available. This serves as a
     * test for the new group asset as well.
     */
    @Command(shortDescription = "Initialise test data")
    public String loadTestData() {
        groups.put("yellow", new GroupData("yellow", false, "Behaviors:critter"));
        logger.info("Group: yellow registered");
        groups.put("cyan", new GroupData("cyan", true, "Behaviors:critter"));
        logger.info("Group: cyan registered");
        if((!groupsFromAssets.isEmpty()) && groupsFromAssets.containsKey("magenta")) {
            groups.put("magenta", groupsFromAssets.get("magenta").getGroupData());
            logger.info("Group: magenta loaded from disk");

        } else {
            groups.put("magenta", new GroupData("magenta", true, "Behaviors:critter"));
            logger.info("Group: magenta registered");
        }

        groups.put("black", new GroupData("black", false, "flock"));
        logger.info("Group: black registered");

        //Creates all necessary hives
        initHives();
        return "All systems online. Hives registered: " + hives.size();
    }

    /**
     * First Group Test:
     * Objective: assign the same behavior to entities in the same group.
     * This test uses only the group identifier from the GroupTag component,
     * which means the group formation is dynamic: all group members are
     * identified by their tag at the moment of the behavior assignment.
     * This is a way of passively creating groups,
     * i.e., identifying group members and acting upon external stimuli.
     * From the game perspective, there are (i) entities tagged as
     * belonging to a group X and (ii) a Group asset that defines
     * the characteristics for group X (through GroupData).
     * Restrictions: identical behavior does not mean identical actions.
     * Actions can be determined by probabilistic events, and even with
     * identical behavior trees each entity will have its own probability
     * roll.
     * Conditions: for observable results, use in conjunction with yellowDeers.
     * Entities in the same group are located by a specific group tag
     * component. This test embeds the posterior development on group
     * identity.
     * @return success message
     */
    @Command(shortDescription = "First group test: assigns the 'critter' behavior to multiple entities tagged in the 'yellow' group.")
    public String groupTestOne() {
        GroupData groupData = groups.get("yellow");
        assignBehaviorToAll(groupData.getGroupLabel(), groupData.getBehavior(), "magentaDeerSkin");
        return "Your should have a magenta deer party by now.";
    }


    /**
     * Second Group Test:
     * Objective: assign the same behavior change to entities in the same group.
     * This test uses groups that require the HiveMind component. When a group
     * possesses requires a HiveMind, a new Hive entity is created when the
     * system is initialised. A Hive is able to process stimuli individually
     * and propagate their effects to group members, acting as a mediator
     * between the environment and the group members.
     * In this test, the members of a Hive are accessed and (i) have a common behavior
     * assigned to them, and (ii) have this same behavior modified by a external
     * command. Note that in this case the same could be accomplished without the
     * use of a Hive entity (see First Group Test).
     * Restrictions: identical behavior changes still do not mean identical actions.
     * Actions can be determined by probabilistic events, and even with
     * identical behavior trees each entity will have its own probability
     * roll.
     * Conditions: use in conjunction with cyanDeers. Entities in the same group are
     * located by a specific group tag component. This test embeds the posterior
     * development on group identity.
     * @return success message
     */
    @Command(shortDescription = "Second group test: coordinated behavior changes. Processes the same behavior change for multiple entities tagged in the 'magenta' group.")
    public String groupTestTwo() {
        EntityRef hiveEntity = hives.get("cyan");
        populateHive(hiveEntity, "yellowDeerSkin");
        updateSpeedToAll(hiveEntity);

        return "Entities in the hive: " + hives.get("cyan").getComponent(GroupMindComponent.class).groupMembers.size() + ". Run, deers, run.";
    }

    /**
     * Third Group Test:
     * Objective: assign the same BT to multiple actors at once. The idea is to cover
     * scenarios where synchronized behavior change is not enough. In order for it to
     * be possible, the core engine logic package was extended with a CollectiveBehaviorComponent
     * class (and respective tree runner/interpreter).
     * Restrictions: identical behavior should be observed, with few exceptions
     * (such as random neighbor move in 'Behaviors:critter').     *
     * Conditions: use in conjunction with yellowDeers.
     * Entities in the same group are located by a specific
     * group tag component. This test embeds the posterior development on group identity.
     * @return success message
     */
    @Command(shortDescription = "Third group test: coordinated behavior. Uses the extended CollectiveBehaviorComponent to assign a single BT to multiple actors. Actors are created from entities tagged in the 'magenta' group.")
    public String groupTestThree() {
        EntityRef hiveEntity = hives.get("magenta");
        populateHive(hiveEntity, null);

        Set<Actor> hiveActors = getActorsFromHive(hiveEntity);

        if(!hiveActors.isEmpty()) {

            BehaviorTree groupBT = assetManager.getAsset("Behaviors:critter", BehaviorTree.class).get();

            if (null != groupBT) {
                CollectiveBehaviorComponent collectiveBehaviorComponent = new CollectiveBehaviorComponent();

                if (hiveEntity.hasComponent(CollectiveBehaviorComponent.class) && hiveEntity.getComponent(CollectiveBehaviorComponent.class).tree != groupBT) {
                    collectiveBehaviorComponent = hiveEntity.getComponent(CollectiveBehaviorComponent.class);
                }

                collectiveBehaviorComponent.tree = groupBT;
                collectiveBehaviorComponent.collectiveInterpreter = new CollectiveInterpreter(hiveActors);
                collectiveBehaviorComponent.collectiveInterpreter.setTree(groupBT);

                hiveEntity.saveComponent(collectiveBehaviorComponent);
            }
        }

        return "Your should be **really** happy if this works.";
    }

    /**
     * Fourth Group Test:
     * Objective: restore the original behavior state of an entity
     * (before it joined a group). The idea is to restore not only
     * the original BT, but the exact node/state in which the entity
     * was.
     * Conditions: for (better) observable results, follow this script:
     * - spawn cyanDeers;
     * - run the first group test;
     * - run the fourth group test.
     * Restrictions: the entity skin is not restored on purpose.
     * Entities in the same group are located by a specific
     * group tag component. This test embeds the posterior development on group identity.
     * @return success message
     */
    @Command(shortDescription = "Fourth group test: behavior states. Returns an entity to its original behavior state (before joining a group).")
    public String groupTestFour() {
        recoverBehaviorBackup("yellow");
        return "Friend. Girlfriend. Boyfriend. Everything has an end. Pizza doesn't.";
    }

    /**
     * Fifth Group Test:
     * Objective: test the flocking behavior. Black deers are assigned
     * a FlockComponent and a flock behavior. The related system makes sure
     * that the flock parameters are updated whenever a new entity receives
     * a FlockComponent. Flocking behavior should make the entities converge
     * to the group center (randomized for emergence emulation).
     * Restrictions: this is very early work. The entities still don't keep
     * a minimal distance from each other. Since the flocking algorithm
     * uses a random reference to establish the group center, flocks
     * tend to get stable in one location until a new entity joins the flock.
     * This test is composed by two steps: in the first one the user spawns a few
     * black deers. Executing the command creates the flock. In the second step,
     * the user spawns an additional black deer, and re-running the command
     * updated the flock.
     * @return success message
     */
    @Command(shortDescription = "Fifth group test: flocking. Here all the black deers become a flock.")
    public String groupTestFive() {
        GroupData groupData = groups.get("black");
        assignComponentBehaviorToAll(groupData.getGroupLabel(), groupData.getBehavior(), new FlockComponent(), "cyanDeerSkin");
        return "Cozy, just like hell.";
    }


    /**
     * Nuke Command:
     * Objective: clean-up a saved game, removing all group-related
     * entities.
     * @return success message
     */
    @Command(shortDescription = "Clean-up.")
    public String nuke() {
        for (EntityRef entityRef : entityManager.getEntitiesWith(GroupMindComponent.class)) {
            entityRef.destroy();
        }

        for (EntityRef entityRef : entityManager.getEntitiesWith(GroupTagComponent.class)) {
            entityRef.destroy();
        }

        return "The hives are dead. LONG LIVE THE PHALANX";
    }

    @Command(shortDescription = "Clean-up sentient entities.")
    public String terminate() {
        for (EntityRef entityRef : entityManager.getEntitiesWith(BehaviorComponent.class)) {
            entityRef.destroy();
        }

        return "They will not be back.";
    }

    private void initHives() {
        if(!groups.isEmpty()) {
            for( String groupLabel : groups.keySet()) {
                if(groups.get(groupLabel).needsHive) {
                    EntityRef hiveEntity = entityManager.create("hiveEntity");
                    GroupMindComponent hivemindComponent = hiveEntity.getComponent(GroupMindComponent.class);
                    hivemindComponent.groupLabel = groupLabel;
                    hivemindComponent.behavior = groups.get(groupLabel).getBehavior();
                    hiveEntity.saveComponent(hivemindComponent);
                    hives.put(groupLabel,hiveEntity);
                }
            }
        }

    }


    /**
     * Assign the same behavior to all entities with the same group label.
     *
     * @param groupLabel
     * @param behavior
     * @param newGroupSkin
     */
    private void assignBehaviorToAll(String groupLabel, String behavior, @Nullable String newGroupSkin) {
        for (EntityRef entityRef : entityManager.getEntitiesWith(GroupTagComponent.class)) {
            if (entityRef.getComponent(GroupTagComponent.class).groups.contains(groupLabel)) {
                assignBehaviorToEntity(entityRef, behavior, newGroupSkin);
            }
        }
    }

    /**
     * Assign the same behavior and a specific component to all entities with the same group label.
     *
     * @param groupLabel
     * @param behavior
     * @param component
     * @param newGroupSkin
     */
    private void assignComponentBehaviorToAll(String groupLabel, String behavior, Component component, @Nullable String newGroupSkin) {
        for (EntityRef entityRef : entityManager.getEntitiesWith(GroupTagComponent.class)) {
            if (entityRef.getComponent(GroupTagComponent.class).groups.contains(groupLabel)) {
                if(!entityRef.hasComponent(component.getClass())) {
                    entityRef.saveComponent(component);
                }

                assignBehaviorToEntity(entityRef, behavior, newGroupSkin);
            }
        }
    }

    private void assignBehaviorToEntity(EntityRef entityRef, String behavior, @Nullable String newGroupSkin) {

        if(entityRef.hasComponent(SkeletalMeshComponent.class) && null != newGroupSkin) {
            SkeletalMeshComponent skeletalComponent = entityRef.getComponent(SkeletalMeshComponent.class);
            skeletalComponent.material = Assets.getMaterial(newGroupSkin).get();
            entityRef.saveComponent(skeletalComponent);
        }

        BehaviorTree groupBT = assetManager.getAsset(behavior, BehaviorTree.class).get();

        if(null != groupBT) {
            BehaviorComponent behaviorComponent = new BehaviorComponent();

            if (entityRef.hasComponent(BehaviorComponent.class)) {
                behaviorComponent = entityRef.getComponent(BehaviorComponent.class);

                GroupTagComponent groupTagComponent = entityRef.getComponent(GroupTagComponent.class);

                groupTagComponent.backupBT = behaviorComponent.tree;
                groupTagComponent.backupRunningState = new Interpreter(behaviorComponent.interpreter);
                entityRef.saveComponent(groupTagComponent);
            }

            behaviorComponent.tree = groupBT;
            behaviorComponent.interpreter = new Interpreter(new Actor(entityRef));
            behaviorComponent.interpreter.setTree(groupBT);

            entityRef.saveComponent(behaviorComponent);
        }

    }

    private void populateHive(String groupLabel, String newSkin) {
        if(hives.containsKey(groupLabel)) {
            logger.info("Hive: " + groupLabel + "was found");
            populateHive(hives.get(groupLabel), newSkin);
        }
    }

    private void populateHive(EntityRef hive, @Nullable String newSkin) {
        GroupMindComponent groupMindComponent = hive.getComponent(GroupMindComponent.class);
        for (EntityRef entityRef : entityManager.getEntitiesWith(GroupTagComponent.class)) {
            GroupTagComponent groupTagComponent = entityRef.getComponent(GroupTagComponent.class);
            if (groupTagComponent.groups.contains(groupMindComponent.groupLabel)) {
                if(null != newSkin) {
                    assignBehaviorToEntity(entityRef, groupMindComponent.behavior, newSkin);
                }
                groupMindComponent.groupMembers.add(entityRef);
            }
        }
        hive.saveComponent(groupMindComponent);
        logger.info("Hive: " + groupMindComponent.groupLabel + " populated with "
                + groupMindComponent.groupMembers.size() + " members.");
    }

    private void updateSpeedToAll(EntityRef hiveEntity) {
        if(hiveEntity.hasComponent(GroupMindComponent.class)) {
            GroupMindComponent groupMindComponent = hiveEntity.getComponent(GroupMindComponent.class);

            if(!groupMindComponent.groupMembers.isEmpty()) {
                for (EntityRef entityRef : groupMindComponent.groupMembers) {
                    CharacterMovementComponent characterMovementComponent = entityRef.getComponent(CharacterMovementComponent.class);
                    characterMovementComponent.speedMultiplier = 2.5f;
                    entityRef.saveComponent(characterMovementComponent);
                }
            }
        }

    }

    private Set<Actor> getActorsFromHive(EntityRef hiveEntity) {
        Set<Actor> hiveActors = new HashSet<>();
        GroupMindComponent groupMindComponent = hiveEntity.getComponent(GroupMindComponent.class);

        if(!groupMindComponent.groupMembers.isEmpty()) {
            for (EntityRef entityRef : groupMindComponent.groupMembers) {
                Actor actor = new Actor(entityRef);
                hiveActors.add(actor);
            }
        }

        return hiveActors;

    }

    private void recoverBehaviorBackup(String groupLabel) {
        for (EntityRef entityRef : entityManager.getEntitiesWith(GroupTagComponent.class)) {
            GroupTagComponent groupTagComponent = entityRef.getComponent(GroupTagComponent.class);

            if (groupTagComponent.groups.contains(groupLabel)) {

                if((null != groupTagComponent.backupBT) && (null != groupTagComponent.backupRunningState)) {

                    if(entityRef.hasComponent(BehaviorComponent.class)) {
                        entityRef.removeComponent(BehaviorComponent.class);

                        BehaviorComponent behaviorComponent = new BehaviorComponent();
                        behaviorComponent.tree = groupTagComponent.backupBT;
                        behaviorComponent.interpreter = groupTagComponent.backupRunningState;
                        behaviorComponent.interpreter.setTree(groupTagComponent.backupBT);

                        entityRef.saveComponent(behaviorComponent);

                    }
                }

            }
        }
    }
}
