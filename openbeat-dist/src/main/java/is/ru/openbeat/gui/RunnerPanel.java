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

package is.ru.openbeat.gui;

import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.openbeat.knowledge.IKnowledgeBase;
import is.ru.openbeat.knowledge.IKnowledgePerson;
import is.ru.openbeat.knowledge.IKnowledgeScene;
import is.ru.openbeat.model.Pair;
import is.ru.openbeat.model.Utterance;
import is.ru.openbeat.participation.ParticipationFrameworkBase;
import is.ru.openbeat.pipeline.*;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import static javax.swing.SwingUtilities.invokeLater;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

@Singleton
public class RunnerPanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(RunnerPanel.class);

    private static final String DEFAULT_STATUS_TEXT = "Ready";

    private final ParticipationFrameworkBase participationFrameworkBase;

    private JComboBox nlp;
    private JComboBox timing;
    private JComboBox compiler;
    private JComboBox outputWriter;

    private List<Pair<IBehaviorGenerator, JCheckBox>> generatorCheckBoxes = newArrayList();
    private List<Pair<IBehaviorFilter, JCheckBox>> filterCheckBoxes = newArrayList();

    private JTextArea input;
    private JTextArea output;

    private JTextArea status;

    private JComboBox scene;
    private JComboBox speaker;

    @Inject
    public RunnerPanel(final List<INlpSource> nlpSources, final List<ITimingSource> timingSources,
                       final List<IBehaviorGenerator> generators, final List<IBehaviorFilter> filters,
                       final List<ICompiler> compilers, final List<IOutputWriter> outputWriters,
                       final IKnowledgeBase knowledgeBase,
                       final ParticipationFrameworkBase participationFrameworkBase) {
        super(new MigLayout("wrap 2", "[] [grow]", "[grow] []"));
        this.participationFrameworkBase = participationFrameworkBase;

        // left panel
        add(new JPanel(new MigLayout("wrap", "[grow]")) {{
            add(new JPanel(new MigLayout(null, "[grow]")) {{
                setBorder(BorderFactory.createTitledBorder("Scene"));

                add(scene = new JComboBox(knowledgeBase.getAllScenes().toArray()) {{
                    setEditable(false);
                    setEnabled(knowledgeBase.getAllScenes().size() > 1);

                    addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            participationFrameworkBase.setCurrentScene(getScene());
                            speaker.removeAllItems();
                            for (IKnowledgePerson person : getScene().getParticipants()) {
                                speaker.addItem(person);
                            }
                        }
                    });
                }}, "grow");
            }}, "grow");
            participationFrameworkBase.setCurrentScene(getScene());

            add(new JPanel(new MigLayout()) {{
                setBorder(BorderFactory.createTitledBorder("Sources"));

                add(new JLabel("NLP"));
                add(nlp = new JComboBox(nlpSources.toArray()) {{
                    setEditable(false);
                }}, "grow, wrap");

                add(new JLabel("Timing"));
                add(timing = new JComboBox(timingSources.toArray()) {{
                    setEditable(false);
                }}, "grow, wrap");

                add(new JLabel("Compiler"));
                add(compiler = new JComboBox(compilers.toArray()) {{
                    setEditable(false);
                }}, "grow, wrap");

                add(new JLabel("Writer"));
                add(outputWriter = new JComboBox(outputWriters.toArray()) {{
                    setEditable(false);
                }}, "grow, wrap");
            }});

            add(new JScrollPane(new JPanel(new MigLayout("wrap")) {{
                for (IBehaviorGenerator generator : generators) {
                    final JCheckBox checkBox = new JCheckBox(prettyName(generator.getClass().getSimpleName()));
                    checkBox.setSelected(false);

                    generatorCheckBoxes.add(Pair.of(generator, checkBox));
                    add(checkBox);
                }
            }}) {{
                setBorder(BorderFactory.createTitledBorder("Behavior generators"));
            }}, "height 250, growx");

            add(new JScrollPane(new JPanel(new MigLayout("wrap")) {{
                for (IBehaviorFilter filter : filters) {
                    final JCheckBox checkBox = new JCheckBox(prettyName(filter.getClass().getSimpleName()));
                    checkBox.setSelected(false);

                    filterCheckBoxes.add(Pair.of(filter, checkBox));
                    add(checkBox);
                }
            }}) {{
                setBorder(BorderFactory.createTitledBorder("Behavior filters"));
            }}, "height 250, growx");

            add(new JScrollPane(status = new JTextArea(DEFAULT_STATUS_TEXT) {{
                setLineWrap(true);
                setEditable(false);
                setFont(new Font(getFont().getName(), getFont().getStyle(), getFont().getSize() - 3));
            }}) {{
                setBorder(BorderFactory.createTitledBorder("Status"));
            }}, "growx");
        }}, "top, width 200");

        // right panel
        add(new JPanel(new MigLayout("wrap")) {{
            add(new JPanel(new MigLayout("wrap", "[grow]", "[] [grow]")) {{
                setBorder(BorderFactory.createTitledBorder("Input"));

                add(new JLabel("Speaker"), "split 2");
                add(speaker = new JComboBox() {{
                    setEditable(false);
                    setEnabled(getScene().getParticipants().size() > 1);

                    for (IKnowledgePerson person : getScene().getParticipants()) {
                        addItem(person);
                    }

                    addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            participationFrameworkBase.getParticipationFramework(getScene()).setSpeaker(
                                getSpeaker().getId());
                        }
                    });
                }});
                participationFrameworkBase.getParticipationFramework(getScene()).setSpeaker(getSpeaker().getId());

                add(new JScrollPane(input = new JTextArea() {{
                    setLineWrap(true);
                }}), "grow");
            }}, "height 50%, width max");

            add(new JScrollPane(output = new RSyntaxTextArea() {{
                setLineWrap(true);
                setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
            }}) {{
                setBorder(BorderFactory.createTitledBorder("Output"));
            }}, "height 50%, width max");
        }}, "top, grow");

        // bottom panel
        add(new JPanel() {{
            add(new JButton("Reset model") {{
                setToolTipText("Resets the discourse model of all NLP sources");
                addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        invokeLater(new Runnable() {
                            public void run() {
                                for (INlpSource nlpSource : nlpSources) {
                                    nlpSource.clearState();
                                }

                                status.setText(DEFAULT_STATUS_TEXT);
                            }
                        });
                    }
                });
            }});

            add(new JButton("Compile") {{
                setToolTipText("Compiles the inputed text into the output textarea");
                addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            compile();
                        } catch (final RuntimeException ex) {
                            invokeLater(new Runnable() {
                                public void run() {
                                    status.setText(ex.getMessage());
                                }
                            });
                            log.warn("Could not compile", ex);
                        }
                    }
                });
            }});

            add(new JButton("Write output") {{
                setToolTipText("Reads compiled output from the output textarea and writes it to the output writer");
                addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            final String compiledOutput = output.getText().trim();
                            getOutputWriter().process(compiledOutput);

                            getTimingSource().play();

                            invokeLater(new Runnable() {
                                public void run() {
                                    output.setText(compiledOutput);
                                    status.setText("Wrote output");
                                }
                            });
                        } catch (final RuntimeException ex) {
                            invokeLater(new Runnable() {
                                public void run() {
                                    status.setText(ex.getMessage());
                                }
                            });
                            log.warn("Could not write output", ex);
                        }
                    }
                });
            }});

            add(new JButton("Compile and write") {{
                setToolTipText(
                    "Compiles the inputed text into the output textarea and automatically writes it to the output writer");
                addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            compileAndWrite();
                        } catch (final RuntimeException ex) {
                            invokeLater(new Runnable() {
                                public void run() {
                                    status.setText(ex.getMessage());
                                }
                            });
                            log.warn("Could not compile and write", ex);
                        }
                    }
                });
            }});
        }}, "span 2, gapbefore push");
    }

    private void compile() {
        final long t1 = System.currentTimeMillis();

        final String s = input.getText().trim();
        log.debug("Sending '{}' through (dry run) pipeline", s);

        // perform linguistic analysis on text
        Utterance utterance = getNlpSource().process(s);

        // generate behaviors
        for (Pair<IBehaviorGenerator, JCheckBox> pair : generatorCheckBoxes) {
            if (pair.getSecond().isSelected()) {
                utterance = pair.getFirst().process(utterance);
            }
        }

        // filter behaviors
        for (Pair<IBehaviorFilter, JCheckBox> pair : filterCheckBoxes) {
            if (pair.getSecond().isSelected()) {
                utterance = pair.getFirst().process(utterance);
            }
        }

        // get timings
        utterance = getTimingSource().process(utterance);

        // compile output
        final String compiledOutput = getCompiler().process(utterance);

        // log runtime (in milliseconds) to status
        final long t2 = System.currentTimeMillis();

        invokeLater(new Runnable() {
            public void run() {
                output.setText(compiledOutput);
                status.setText(String.format("Compiled text in %d ms", (t2 - t1)));
            }
        });
    }

    private void compileAndWrite() {
        final long t1 = System.currentTimeMillis();

        final String s = input.getText().trim();
        log.debug("Sending '{}' through (run) pipeline", s);

        // perform linguistic analysis on text
        Utterance utterance = getNlpSource().process(s);

        // generate behaviors
        for (Pair<IBehaviorGenerator, JCheckBox> pair : generatorCheckBoxes) {
            if (pair.getSecond().isSelected()) {
                utterance = pair.getFirst().process(utterance);
            }
        }

        // filter behaviors
        for (Pair<IBehaviorFilter, JCheckBox> pair : filterCheckBoxes) {
            if (pair.getSecond().isSelected()) {
                utterance = pair.getFirst().process(utterance);
            }
        }

        // get timings
        utterance = getTimingSource().process(utterance);

        // compile output
        final String compiledOutput = getCompiler().process(utterance);

        // log runtime (in milliseconds) to status
        final long t2 = System.currentTimeMillis();

        getOutputWriter().process(compiledOutput);

        getTimingSource().play();

        invokeLater(new Runnable() {
            public void run() {
                output.setText(compiledOutput);
                status.setText(String.format("Compiled text and wrote output in %d ms", (t2 - t1)));
            }
        });
    }

    private IKnowledgeScene getScene() {
        return (IKnowledgeScene) scene.getSelectedItem();
    }

    private IKnowledgePerson getSpeaker() {
        return (IKnowledgePerson) speaker.getSelectedItem();
    }

    private INlpSource getNlpSource() {
        return (INlpSource) nlp.getSelectedItem();
    }

    private ITimingSource getTimingSource() {
        return (ITimingSource) timing.getSelectedItem();
    }

    private ICompiler getCompiler() {
        return (ICompiler) compiler.getSelectedItem();
    }

    private IOutputWriter getOutputWriter() {
        return (IOutputWriter) outputWriter.getSelectedItem();
    }

    private String prettyName(String s) {
        s = withoutEnd(s, "Source");
        s = withoutEnd(s, "Timing");
        s = withoutEnd(s, "Compiler");
        s = withoutEnd(s, "Generator");
        s = withoutEnd(s, "Filter");
        s = withoutEnd(s, "Behavior");
        return s;
    }

    private String withoutEnd(String s, String end) {
        if (s.endsWith(end)) {
            return s.substring(0, s.indexOf(end));
        }
        return s;
    }

    // TODO: see if this can render better with JComboBox on Window (sucks on Linux) -- JComboBox.setRenderer
    private class PrettyListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            // setText(prettyName(value.toString()));
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
