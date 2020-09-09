// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsMadness.actions;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.GroupMindComponent;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.nui.properties.Range;

@BehaviorAction(name = "set_mad_speed")
public class SetMadSpeedAction extends BaseAction {

    @Range(max = 10f)
    private float speedMultiplier;

    @Override
    public void construct(Actor actor) {
        if (actor.hasComponent(GroupMindComponent.class)) {
            GroupMindComponent hivemindComponent = actor.getComponent(GroupMindComponent.class);

            if (!hivemindComponent.groupMembers.isEmpty()) {
                for (EntityRef entityRef : hivemindComponent.groupMembers) {
                    CharacterMovementComponent characterMovementComponent =
                            entityRef.getComponent(CharacterMovementComponent.class);
                    characterMovementComponent.speedMultiplier = speedMultiplier;
                    entityRef.saveComponent(characterMovementComponent);
                }
            }
        }
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        return BehaviorState.SUCCESS;
    }
}
