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
package org.terasology.wildAnimalsMadness.assets;

import org.terasology.assets.AssetData;

/**
 * Every group is described by a GroupData asset.
 */
public class GroupData implements AssetData {

    public String groupLabel;
    public Boolean needsHivemind;
    public String behavior;

    public GroupData() {
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public void setGroupLabel(String groupLabel) {
        this.groupLabel = groupLabel;
    }

    public Boolean getNeedsHivemind() {
        return needsHivemind;
    }

    public void setNeedsHivemind(Boolean needsHivemind) {
        this.needsHivemind = needsHivemind;
    }

    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

}
