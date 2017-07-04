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

package is.ru.openbeat.participation;

import com.google.common.base.Nullable;
import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.newHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Class <tt>ParticipationFramework</tt> keeps track of state for a group of participants in a scene. Only one
 * <tt>speaker</tt> can be speaking at a time with other being <tt>hearers</tt> or the ones who are <tt>addressed</tt>
 * explicitly.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public class ParticipationFramework {
    private static final Logger log = LoggerFactory.getLogger(ParticipationFramework.class);

    private final Map<String, Participant> participants = newHashMap();
    private Participant speaker;
    private Participant addressee;

    public ParticipationFramework() {
    }

    public void addParticipant(String participantName) {
        participants.put(participantName, new Participant(participantName));
    }

    public void removeParticipant(String participantName) {
        final Participant removed = participants.get(participantName);
        if (speaker.equals(removed)) {
            speaker = null;
        }
        if (addressee.equals(removed)) {
            addressee = null;
        }
        participants.remove(participantName);
    }

    public void setSpeakerAddressing(String speakerName, String addresseeName) {
        if (speaker != null) {
            speaker.setState(Participant.State.HEARER);
        }

        if (addressee != null) {
            addressee.setState(Participant.State.HEARER);
        }

        final Participant newSpeaker = participants.get(speakerName);
        if (newSpeaker != null) {
            speaker = newSpeaker;
            speaker.setState(Participant.State.SPEAKER);
        }

        final Participant newAddressee = participants.get(addresseeName);
        if (newAddressee != null) {
            addressee = newAddressee;
            addressee.setState(Participant.State.ADDRESSEE);
        }
    }

    public void setSpeaker(String speakerName) {
        if (addressee != null) {
            addressee.setState(Participant.State.HEARER);
        }
        if (speaker != null) {
            speaker.setState(Participant.State.ADDRESSEE);
            addressee = speaker;
        } else {
            addressee = null;
        }

        if (speakerName != null) {
            final Participant newSpeaker = participants.get(speakerName);
            if (newSpeaker != null) {
                log.debug("Setting {} as speaker", speakerName);
                speaker = newSpeaker;
                speaker.setState(Participant.State.SPEAKER);
            }
        } else {
            speaker = null;
        }
    }

    public Participant getSpeaker() {
        return speaker;
    }

    public Participant getAddressee() {
        return addressee;
    }

    public Iterable<Participant> getHearers() {
        return filter(participants.values(), new Predicate<Participant>() {
            public boolean apply(@Nullable Participant input) {
                return !input.equals(speaker);
            }
        });
    }

    @Override
    public String toString() {
        return "ParticipationFramework{" +
            "participants=" + participants +
            ", speaker=" + speaker +
            ", addressee=" + addressee +
            '}';
    }
}
