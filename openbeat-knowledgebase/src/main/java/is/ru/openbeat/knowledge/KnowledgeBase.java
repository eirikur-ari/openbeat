/*
 * OpenBEAT
 *
 * Arni Hermann Reynisson     arnir06@ru.is
 * Eirikur Ari Petursson      eirikurp06@ru.is
 * Gudleifur Kristjansson     gudleifur05@ru.is
 * Hannes Hogni Vilhjalmsson  hannes@ru.is
 *
 * Copyright(c) 2009 Center for Analysis and Design of Intelligent Agents
 *                   Reykjavik University
 *                   All rights reserved
 *
 *                   http://cadia.ru.is/
 *
 * Based on BEAT, Copyright(c) 2000-2001 by MIT Media Lab,
 * developed by Hannes Vilhjalmsson, Timothy Bickmore, Yang Gao and Justine Cassell
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of its copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package is.ru.openbeat.knowledge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import static com.google.common.collect.Maps.newHashMap;
import com.google.common.collect.Iterables;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.filter;
import com.google.common.base.Predicate;
import com.google.common.base.Nullable;
import static is.ru.openbeat.behavior.Arm.ArmType;

import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Class <tt>KnowledgeBase</tt> implements a domain knowledge base for OpenBEAT.
 *
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
@Singleton
public class KnowledgeBase implements IKnowledgeBase {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeBase.class);

    private final KnowledgeBaseHolder input;

    @Inject
    public KnowledgeBase(KnowledgeBaseHolder input) {
        this.input = input;
    }

    public KnowledgeBaseHolder getInput() {
        return input;
    }

    public List<IKnowledgeInstance> getAllInstances() {
        return input.getInstances();
    }

    public List<IKnowledgeScene> getAllScenes() {
        return input.getScenes();
    }

    public List<IKnowledgeType> getAllTypes() {
        return input.getTypes();
    }

    /**
     * Returns the instance in the knowledge base that has values that correspond most closely to the words found in the
     * passed description. null is returned if there is no clear best match or if there is no match at all.
     *
     * @param description is the value of the instance attribute
     * @return best knowledge instance match if it's found, else null
     */
    public IKnowledgeInstance getBestInstanceMatch(String description) {
        log.debug("Finding best instance match for '{}'", description);
        int maxMatches = 0;
        final Map<IKnowledgeInstance, Integer> hits = newHashMap();
        IKnowledgeInstance bestInstance = null;
        for (IKnowledgeInstance instance : input.getInstances()) {
            hits.put(instance, 0);
            for (String value : instance.getAllValues()) {
                log.trace("Looping over: {}", value);
                if (!value.isEmpty() && description.contains(value)) {
                    log.debug("{} matched the description", value);
                    hits.put(instance, hits.get(instance) + 1);
                }
            }

            if (hits.get(instance) > maxMatches) {
                log.debug("Assigning new best instance match {}", instance);
                bestInstance = instance;
                maxMatches = hits.get(instance);
            }
        }

        final int max = maxMatches;
        // count the number of instances with maxMatches
        int bestCount = size(filter(hits.values(), new Predicate<Integer>() {
            public boolean apply(@Nullable Integer input) {
                return input.equals(max);
            }
        }));

        log.debug("Best instance count: {}", bestCount);
        log.debug("Best instance: {}", bestInstance);
        if (bestCount < 2 && bestInstance != null) {
            log.debug("Returning best instance {}", bestInstance);
            return bestInstance;
        } else {
            return null;
        }
    }

    /**
     * Returns the gesture as a single compact gesture in a form suitable for a single behavior suggestion element.
     *
     * @param value of the gesture instance
     * @return gesture if it's found, otherwise null
     */
    public IGesture getCompactGesture(String value) {
        final IGesture gesture = getGesture(value);
        if (gesture != null) {
            final List<IArm> arms = gesture.getArms();
            boolean left = false;
            boolean right = false;

            for (IArm arm : arms) {
                if (ArmType.LEFT.equals(arm.getType())) {
                    left = true;
                } else if (ArmType.RIGHT.equals(arm.getType())) {
                    right = true;
                }
            }

            if (left && right) {
                gesture.setGestureType(IGesture.GestureType.BOTH);
            } else if (left && !right) {
                gesture.setGestureType(IGesture.GestureType.LEFT);
            } else if (right && !left) {
                gesture.setGestureType(IGesture.GestureType.RIGHT);
            }
        }
        return gesture;
    }

    public IGesture getGesture(String id) {
        for (IGesture gesture : input.getGestures()) {
            if (gesture.getValue().equals(id)) {
                return gesture;
            }
        }
        return null;
    }

    public IKnowledgeInstance getInstance(String id) {
        for (IKnowledgeInstance instance : input.getInstances()) {
            if (instance.getId().equals(id)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Returns true if an object with an id of objectId is found inside the scene with id of sceneId, returns false
     * otherwise.
     *
     * @param sceneId  the id of the scene
     * @param objectId the id of the object
     * @return true if object is observable within scene, otherwise false
     */
    public boolean isObservable(String sceneId, String objectId) {
        final IKnowledgeScene scene = getScene(sceneId);
        return scene != null && scene.containsObject(objectId);
    }

    private IKnowledgeScene getScene(String id) {
        for (IKnowledgeScene scene : input.getScenes()) {
            if (scene.getId().equals(id)) {
                return scene;
            }
        }
        return null;
    }
}
