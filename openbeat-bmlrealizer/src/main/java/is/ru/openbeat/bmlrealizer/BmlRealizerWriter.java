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

package is.ru.openbeat.bmlrealizer;

import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import is.ru.openbeat.model.Pair;
import is.ru.openbeat.participation.Participant;
import is.ru.openbeat.participation.ParticipationFrameworkBase;
import is.ru.openbeat.pipeline.IOutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

@Singleton
class BmlRealizerWriter implements IOutputWriter {
    private static final Logger log = LoggerFactory.getLogger(BmlRealizerWriter.class);

    private final ParticipationFrameworkBase participationFrameworkBase;

    private Provider<String> host;
    private Provider<Integer> port;

    @Inject
    BmlRealizerWriter(ParticipationFrameworkBase participationFrameworkBase, @Named("bml.host") Provider<String> host,
                      @Named("bml.port") Provider<Integer> port) {
        this.participationFrameworkBase = participationFrameworkBase;
        this.host = host;
        this.port = port;
    }

    public Void process(String s) {
        try {
            unsafeProcess(s);
        } catch (IOException e) {
            log.warn("Cannot send BML to {}:{}", host.get(), port.get());
            log.debug("Stack trace", e);
            throw new RuntimeException(String.format("Cannot send BML block to %s:%d", host.get(), port.get()), e);
        }

        return null;
    }

    private void unsafeProcess(String bml) throws IOException {
        log.debug("Creating socket to {}:{}", host.get(), port.get());
        final Socket socket = new Socket(host.get(), port.get());
        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

        log.debug("Writing data");
        final Participant speaker = participationFrameworkBase.getCurrentParticipationFramework().getSpeaker();
        final StringBuilder sb = new StringBuilder();
        if (speaker != null) {
            sb.append(speaker.getName());
        }
        sb.append("|BEAT|");
        sb.append(toAsciiSet(bml));
        bos.write(sb.toString().getBytes("UTF-8"));

        log.debug("Flushing output stream");
        bos.flush();

        log.debug("Closing socket");
        socket.close();
    }

    private String toAsciiSet(String s) {
        for (Pair<String, String> ch : of(Pair.of("Á", "A"), Pair.of("Ð", "D"), Pair.of("É", "E"), Pair.of("Í", "I"),
            Pair.of("Ó", "O"), Pair.of("Ú", "U"), Pair.of("Ý", "Y"), Pair.of("Þ", "Th"), Pair.of("Æ", "Ae"), Pair.of("Ö", "O")))
        {
            s = s.replaceAll(ch.getFirst(), ch.getSecond());
            s = s.replaceAll(ch.getFirst().toLowerCase(), ch.getSecond().toLowerCase());
        }
        return s;
    }

    private <T> List<T> of(T... ts) {
        return newArrayList(ts);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
