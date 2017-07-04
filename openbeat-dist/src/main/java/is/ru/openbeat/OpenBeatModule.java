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

package is.ru.openbeat;

import static com.google.common.collect.Lists.sortedCopy;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import is.ru.openbeat.gui.SwingRunner;
import is.ru.openbeat.pipeline.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class OpenBeatModule extends AbstractModule {
    protected void configure() {
        bind(IPipelineRunner.class).to(SwingRunner.class);

        bind(String.class).annotatedWith(Names.named("praat.dir")).toProvider(Configuration.of("praat/is"));
        bind(String.class).annotatedWith(Names.named("bml.host")).toProvider(Configuration.of("localhost"));
        bind(Integer.class).annotatedWith(Names.named("bml.port")).toProvider(Configuration.of(15000));

        bindConstant().annotatedWith(Names.named("kb.file")).to("database.yaml");
        bindConstant().annotatedWith(Names.named("freetts.stretch")).to(2F);
        bindConstant().annotatedWith(Names.named("fixedtiming.stretch")).to(0.37F);
    }

    @Provides
    @Singleton
    public List<INlpSource> listNlpSources(Set<INlpSource> nlpSources) {
        return sortedCopy(nlpSources, new SimpleNameComparator());
    }

    @Provides
    @Singleton
    public List<IBehaviorGenerator> listGenerators(Set<IBehaviorGenerator> generators) {
        return sortedCopy(generators, new SimpleNameComparator());
    }

    @Provides
    @Singleton
    public List<IBehaviorFilter> listFilters(Set<IBehaviorFilter> filters) {
        return sortedCopy(filters, new SimpleNameComparator());
    }

    @Provides
    @Singleton
    public List<ICompiler> listCompilers(Set<ICompiler> compilers) {
        return sortedCopy(compilers, new SimpleNameComparator());
    }

    @Provides
    @Singleton
    public List<IOutputWriter> listOutputWriters(Set<IOutputWriter> outputWriters) {
        return sortedCopy(outputWriters, new SimpleNameComparator());
    }

    @Provides
    @Singleton
    public List<ITimingSource> listTimingSources(Set<ITimingSource> timingSources) {
        return sortedCopy(timingSources, new SimpleNameComparator());
    }

    private class SimpleNameComparator implements Comparator<IPipe<?, ?>> {
        public int compare(IPipe<?, ?> o1, IPipe<?, ?> o2) {
            return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
        }
    }
}
