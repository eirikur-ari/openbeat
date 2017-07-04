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
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.spi.ProviderInstanceBinding;
import is.ru.openbeat.Configuration;
import is.ru.openbeat.model.Pair;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

@Singleton
public class ConfigurationPanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationPanel.class);

    private List<Pair<Pair<Named, JTextField>, Configuration<?>>> configurations = newArrayList();

    @Inject
    public ConfigurationPanel(final Injector injector) {
        super(new MigLayout(null, "[grow]"));

        add(new JPanel(new MigLayout("wrap 2", "[grow] [grow]")) {{
            setBorder(BorderFactory.createTitledBorder("Properties"));

            for (Map.Entry<Key<?>, Binding<?>> entry : injector.getBindings().entrySet()) {
                if (Named.class.equals(entry.getKey().getAnnotationType())) {
                    if (ProviderInstanceBinding.class.isAssignableFrom(entry.getValue().getClass())) {
                        final ProviderInstanceBinding<?> providerInstanceBinding = (ProviderInstanceBinding<?>) entry.getValue();
                        final Provider<?> provider = providerInstanceBinding.getProviderInstance();
                        if (Configuration.class.isAssignableFrom(provider.getClass())) {
                            final Named named = (Named) entry.getKey().getAnnotation();
                            final Configuration<?> configuration = (Configuration<?>) provider;

                            add(new JLabel(named.value()), "width 50%");

                            final JTextField field = new JTextField(String.valueOf(configuration.get()));
                            add(field, "width 50%");

                            configurations.add(Pair.<Pair<Named, JTextField>, Configuration<?>>of(Pair.of(named, field),
                                configuration));
                        }
                    }
                }
            }
        }}, "wrap, grow");

        add(new JButton("Update configurations") {{
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (Pair<Pair<Named, JTextField>, Configuration<?>> pair : configurations) {
                        final Named named = pair.getFirst().getFirst();
                        final JTextField field = pair.getFirst().getSecond();
                        final Configuration<?> configuration = pair.getSecond();

                        try {
                            configuration.set(field.getText());
                        } catch (RuntimeException ex) {
                            log.warn("Cannot set '{}' for configuration '{}'", field.getText(), named.value());
                            log.warn("Exception", e);
                        }
                    }
                }
            });
        }}, "gapbefore push");
    }
}
