// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsMadness.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.Vector3f;

public class FlockComponent implements Component {

    public float speed = 5f;
    public float searchRadius = 10f;
    public Vector3f flockCentre = Vector3f.zero();
    public Vector3f flockAvoid = Vector3f.zero();
}
