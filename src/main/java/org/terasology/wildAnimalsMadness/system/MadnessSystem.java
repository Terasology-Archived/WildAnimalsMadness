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

import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.CollectiveBehaviorComponent;
import org.terasology.logic.behavior.CollectiveInterpreter;
import org.terasology.logic.behavior.Interpreter;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.utilities.Assets;
import org.terasology.wildAnimalsMadness.components.GroupTagComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.registry.In;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.wildAnimalsMadness.components.HivemindComponent;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class MadnessSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;

    @Override
    public void initialise() {

    }


    /**
     * First Group Test:
     * Objective: assign the same behavior to entities in the same group
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
    @Command(shortDescription = "First group test: assigns the 'critter' behavior to multiple entities tagged in the 'magenta' group.")
    public String groupTestOne() {
        assignBehaviorToAll("magenta", "Behaviors:critter", "magentaDeerSkin");
        return "Your should have a magenta deer party by now.";
    }


    /**
     * Second Group Test:
     * Objective: assign the same behavior change to entities in the same group     *
     * Restrictions: identical behavior changes still do not mean identical actions.
     * Actions can be determined by probabilistic events, and even with
     * identical behavior trees each entity will have its own probability
     * roll.
     * Conditions: for observable results, use in conjunction with cyanDeers
     * or run groupTestOne first. Entities in the same group are located by a specific
     * group tag component. This test embeds the posterior development on group identity.
     * @return success message
     */
    @Command(shortDescription = "Second group test: coordinated behavior changes. Processes the same behavior change for multiple entities tagged in the 'magenta' group.")
    public String groupTestTwo() {
        EntityRef hivemindEntity = populateHivemind();
        updateSpeedToAll(hivemindEntity);

        return "Run, deers, run.";
    }

    /**
     * Third Group Test:
     * Objective: assign the same BT to multiple actors at once. The idea is to cover
     * scenarios where synchronized behavior change is not enough. In order for it to
     * be possible, the core engine logic package was extended with a CollectiveBehaviorComponent
     * class (and respective tree runner/interpreter).
     * Restrictions: identical behavior should be observed, with few exceptions
     * (such as random neighbor move in 'Behaviors:critter').     *
     * Conditions: for observable results, use in conjunction with yellowDeers.
     * Entities in the same group are located by a specific
     * group tag component. This test embeds the posterior development on group identity.
     * @return success message
     */
    @Command(shortDescription = "Third group test: coordinated behavior. Uses the extended CollectiveBehaviorComponent to assign a single BT to multiple actors. Actors are created from entities tagged in the 'magenta' group.")
    public String groupTestThree() {
        EntityRef hivemindEntity = populateHivemind();

        Set<Actor> hiveActors = getActorsFromHive(hivemindEntity);

        if(!hiveActors.isEmpty()) {

            BehaviorTree groupBT = assetManager.getAsset("Behaviors:critter", BehaviorTree.class).get();

            if (null != groupBT) {
                CollectiveBehaviorComponent collectiveBehaviorComponent = new CollectiveBehaviorComponent();

                if (hivemindEntity.hasComponent(CollectiveBehaviorComponent.class) && hivemindEntity.getComponent(CollectiveBehaviorComponent.class).tree != groupBT) {
                    collectiveBehaviorComponent = hivemindEntity.getComponent(CollectiveBehaviorComponent.class);
                }

                collectiveBehaviorComponent.tree = groupBT;
                collectiveBehaviorComponent.collectiveInterpreter = new CollectiveInterpreter(hiveActors);
                collectiveBehaviorComponent.collectiveInterpreter.setTree(groupBT);

                hivemindEntity.saveComponent(collectiveBehaviorComponent);
            }
        }

        return "Your should be **really** happy if this works.";
    }

    /**
     * Use after groupTestOne
     * @return
     */
    @Command(shortDescription = "Fourth group test: behavior states.")
    public String groupTestFour() {
        recoverBehaviorBackup("magenta");
        return "Friend. Girlfriend. Boyfriend. Everything has an end. Pizza doesn't.";
    }

//    @Command(shortDescription = "Fifth group test: identities for groups/members. Replace with managed asset.")
//    public String groupTestFour()

    @Command(shortDescription = "Clean-up.")
    public String nuke() {
        for (EntityRef entityRef : entityManager.getEntitiesWith(HivemindComponent.class)) {
            entityRef.destroy();
        }

        for (EntityRef entityRef : entityManager.getEntitiesWith(GroupTagComponent.class)) {
            entityRef.destroy();
        }

        return "The hive is dead. LONG LIVE THE PHALANX";
    }

    private void assignBehaviorToAll(String groupLabel, String behavior, @Nullable String newGroupSkin) {
        for (EntityRef entityRef : entityManager.getEntitiesWith(GroupTagComponent.class)) {
            GroupTagComponent groupTagComponent = entityRef.getComponent(GroupTagComponent.class);

            if (groupTagComponent.groupLabel.equalsIgnoreCase(groupLabel)) {

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
        }
    }

    private EntityRef populateHivemind() {
        EntityRef hivemindEntity = entityManager.create("hivemindEntity");

        HivemindComponent hivemindComponent = hivemindEntity.getComponent(HivemindComponent.class);

        if(null != hivemindComponent.groupLabel) {
            for (EntityRef entityRef : entityManager.getEntitiesWith(GroupTagComponent.class)) {
                GroupTagComponent groupTagComponent = entityRef.getComponent(GroupTagComponent.class);

                if (groupTagComponent.groupLabel.equalsIgnoreCase(hivemindComponent.groupLabel)) {
                    hivemindComponent.groupMembers.add(entityRef);
                }
            }
        }
        hivemindEntity.saveComponent(hivemindComponent);

        return hivemindEntity;
    }

    private void updateSpeedToAll(EntityRef groupEntity) {
        if(groupEntity.hasComponent(HivemindComponent.class)) {
            HivemindComponent hivemindComponent = groupEntity.getComponent(HivemindComponent.class);

            if(!hivemindComponent.groupMembers.isEmpty()) {
                for (EntityRef entityRef : hivemindComponent.groupMembers) {
                    CharacterMovementComponent characterMovementComponent = entityRef.getComponent(CharacterMovementComponent.class);
                    characterMovementComponent.speedMultiplier = 2.5f;
                    entityRef.saveComponent(characterMovementComponent);
                }
            }
        }

    }

    private Set<Actor> getActorsFromHive(EntityRef hivemindEntity) {
        Set<Actor> hiveActors = new HashSet<>();
        HivemindComponent hivemindComponent = hivemindEntity.getComponent(HivemindComponent.class);

        if(!hivemindComponent.groupMembers.isEmpty()) {
            for (EntityRef entityRef : hivemindComponent.groupMembers) {
                Actor actor = new Actor(entityRef);
                hiveActors.add(actor);
            }
        }

        return hiveActors;

    }

    private void recoverBehaviorBackup(String groupLabel) {
        for (EntityRef entityRef : entityManager.getEntitiesWith(GroupTagComponent.class)) {
            GroupTagComponent groupTagComponent = entityRef.getComponent(GroupTagComponent.class);

            if (groupTagComponent.groupLabel.equalsIgnoreCase(groupLabel)) {

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
