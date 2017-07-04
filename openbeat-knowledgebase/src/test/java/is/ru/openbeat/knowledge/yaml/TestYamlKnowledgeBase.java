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

package is.ru.openbeat.knowledge.yaml;

import atunit.AtUnit;
import atunit.Container;
import atunit.MockFramework;
import atunit.Unit;
import atunit.guice.GuiceContainer;
import atunit.mockito.MockitoFramework;
import static com.google.common.collect.ImmutableMap.of;
import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import is.ru.openbeat.behavior.Arm;
import is.ru.openbeat.behavior.GestureBehavior;
import is.ru.openbeat.knowledge.*;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Class <tt>TestYamlKnowledgeBase</tt> tests the knowledge base methods with a Yaml input
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson), gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
@RunWith(AtUnit.class)
@Container(GuiceContainer.class)
@MockFramework(MockitoFramework.class)
public class TestYamlKnowledgeBase extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(TestYamlKnowledgeBase.class);

    @Inject
    @Unit
    private KnowledgeBase kb;

    // TODO: this is ugly, refactor
    protected void configure() {
        install(new YamlKnowledgeModule() {
            @Override
            public Reader createReader(@Named("kb.file") String filename) throws FileNotFoundException {
                return new StringReader("types:\n" +
                    "- klass: person\n" +
                    "  name: professional\n" +
                    "  features:\n" +
                    "  - name: role\n" +
                    "    type: SYM\n" +
                    "    typical: real\n" +
                    "  - name: age\n" +
                    "    type: NUM\n" +
                    "    typical: 65\n" +
                    "- klass: personality\n" +
                    "  name: teacher\n" +
                    "  features:\n" +
                    "  - name: role\n" +
                    "    type: SYM\n" +
                    "    typical: real\n" +
                    "  - name: age\n" +
                    "    type: NUM\n" +
                    "    typical: 45\n" +
                    "- klass: vehicle\n" +
                    "  name: auto\n" +
                    "  features:\n" +
                    "  - name: role\n" +
                    "    type: SYM\n" +
                    "    typical: color" +
                    "\n" +
                    "instances:\n" +
                    "- id: Stairs1\n" +
                    "  instanceOf: Stairs\n" +
                    "  attributes:\n" +
                    "    shape: Spiral\n" +
                    "- id: Texture1\n" +
                    "  instanceOf: Shader\n" +
                    "  attributes:\n" +
                    "    type: Texture\n" +
                    "    bits: 16\n" +
                    "- id: Box1\n" +
                    "  instanceOf: Container\n" +
                    "  attributes:\n" +
                    "    type: Box\n" +
                    "    shape: square\n" +
                    "    contents: Chocolates\n" +
                    "- id: Punk1\n" +
                    "  instanceOf: professional\n" +
                    "  attributes:\n" +
                    "    role: Actor\n" +
                    "    type: Virtual\n" +
                    "    age: 25\n" +
                    "    hype: Popular\n" +
                    "- id: House1\n" +
                    "  instanceOf: Home\n" +
                    "  attributes:\n" +
                    "    size: Large\n" +
                    "    type: House\n" +
                    "- id: Siggraph1\n" +
                    "  instanceOf: Conference\n" +
                    "  attributes:\n" +
                    "    name: Siggraph\n" +
                    "    distance: Here\n" +
                    "    year: 2001\n" +
                    "- id: Truck1\n" +
                    "  instanceOf: Auto\n" +
                    "  attributes:\n" +
                    "    type: Truck\n" +
                    "    color: Blue\n" +
                    "- id: Wgnl\n" +
                    "  instanceOf: Sign\n" +
                    "  attributes:\n" +
                    "    name: W.G.N.L.\n" +
                    "    label: Wgnl\n" +
                    "- id: car1\n" +
                    "  instanceOf: auto\n" +
                    "  attributes:\n" +
                    "    role: saw\n" +
                    "    type: red\n" +
                    "\n" +
                    "scenes:\n" +
                    "- id: MIT\n" +
                    "  objects:\n" +
                    "  - ML1\n" +
                    "  - Truck1\n" +
                    "  - Alan1\n" +
                    "- id: Local\n" +
                    "  objects:\n" +
                    "  - House1\n" +
                    "  - Texture1\n" +
                    "  persons:\n" +
                    "  - id: Rob\n" +
                    "    role: PARTICIPANT\n" +
                    "  - id: Kelly\n" +
                    "    role: BYSTANDER\n" +
                    "\n" +
                    "gestures:\n" +
                    "- type: iconic\n" +
                    "  value: square\n" +
                    "  arms:\n" +
                    "  - handshape: box\n" +
                    "    trajectory: box\n" +
                    "    type: RIGHT\n" +
                    "  - handshape: box\n" +
                    "    trajectory: box\n" +
                    "    type: LEFT\n" +
                    "- type: metamorphic\n" +
                    "  value: optimized\n" +
                    "  arms:\n" +
                    "  - handshape: virtual\n" +
                    "    trajectory: virtual\n" +
                    "    type: RIGHT\n" +
                    "  - handshape: virtual\n" +
                    "    trajectory: virtual\n" +
                    "    type: LEFT\n" +
                    "- type: iconic\n" +
                    "  value: saw\n" +
                    "  arms:\n" +
                    "  - handshape: why\n" +
                    "    trajectory: why\n" +
                    "    type: RIGHT" +
                    "");
            }
        });
        bindConstant().annotatedWith(Names.named("kb.file")).to("NOT-USED");
    }

    @Test
    public void testYamlKnowledgeBase() {
        assertThat(kb.getInput().getTypes(), Matchers.<IKnowledgeType>hasItem(
            new KnowledgeType("professional", "person", Lists.<IKnowledgeFeature>newArrayList(
                new KnowledgeFeature("age", "65", KnowledgeFeature.FeatureType.NUM)))));
        assertThat(kb.getInput().getInstances(),
            Matchers.<IKnowledgeInstance>hasItem(new KnowledgeInstance("Stairs", "Stairs1", of("shape", "Spiral"))));
        assertThat(kb.getInput().getScenes(),
            Matchers.<IKnowledgeScene>hasItem(new KnowledgeScene("MIT", newArrayList("ML1, Truck1, Alan1"))));
        assertThat(kb.getInput().getGestures(), Matchers.<IGesture>hasItem(new GestureBehavior("iconic", "square",
            Lists.<IArm>newArrayList(new Arm("box", "box", Arm.ArmType.RIGHT),
                new Arm("box", "box", Arm.ArmType.LEFT)))));

        log.debug("Best instance match: {}", kb.getBestInstanceMatch("Spiral"));
        assertThat(kb.getBestInstanceMatch("Spiral"), is(Matchers.<IKnowledgeInstance>equalTo(
            new KnowledgeInstance("Stairs", "Stairs1", of("shape", "Spiral")))));

        log.debug("Surprising value: {}", kb.getInstance("Punk1").getSurprisingValue(kb));
        assertThat(kb.getInstance("Punk1").getSurprisingValue(kb), is("Actor"));

        log.debug("Compact gesture: {}", kb.getCompactGesture("square"));
        assertThat(kb.getCompactGesture("square"), Matchers.is(Matchers.<IGesture>equalTo(
            new GestureBehavior("iconic", "square", Lists.<IArm>newArrayList(new Arm("box", "box", IArm.ArmType.RIGHT),
                new Arm("box", "box", IArm.ArmType.LEFT)), IGesture.GestureType.BOTH))));

        log.debug("Observable: {}", kb.isObservable("Local", "House1"));
        assertThat(kb.isObservable("Local", "House1"), is(true));
    }

    @Test
    public void testVehicleClass() {
        assertThat(kb.getInput().getTypes(), Matchers.<IKnowledgeType>hasItem(
            new KnowledgeType("auto", "vehicle", Lists.<IKnowledgeFeature>newArrayList(
                new KnowledgeFeature("role", "color", KnowledgeFeature.FeatureType.SYM)))));

        assertThat(kb.getInput().getInstances(),
            Matchers.<IKnowledgeInstance>hasItem(new KnowledgeInstance("auto", "car1", of("role", "saw"))));
        assertThat(kb.getInput().getGestures(), Matchers.<IGesture>hasItem(new GestureBehavior("iconic", "saw",
            Lists.<IArm>newArrayList(new Arm("why", "why", Arm.ArmType.RIGHT)))));

        log.debug("Best instance match: {}", kb.getBestInstanceMatch("saw"));
        assertThat(kb.getBestInstanceMatch("saw"), is(Matchers.<IKnowledgeInstance>equalTo(
            new KnowledgeInstance("auto", "car1", of("role", "saw")))));

        log.debug("ID: {}", kb.getBestInstanceMatch("saw").getId());
        assertThat(kb.getBestInstanceMatch("saw").getId(), is("car1"));
        
        log.debug("Suprising value: {}", kb.getInstance("car1").getSurprisingValue(kb));
        assertThat(kb.getInstance("car1").getSurprisingValue(kb), is("saw"));

        log.debug("Compact gesture: {}", kb.getCompactGesture("saw"));
        assertThat(kb.getCompactGesture("saw"), Matchers.is(Matchers.<IGesture>equalTo(
            new GestureBehavior("iconic", "saw", Lists.<IArm>newArrayList(new Arm("why", "why", IArm.ArmType.RIGHT)),
                    IGesture.GestureType.RIGHT))));

    }
}
