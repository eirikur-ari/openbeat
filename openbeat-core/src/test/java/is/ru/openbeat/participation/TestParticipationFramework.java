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

import org.junit.Test;

public class TestParticipationFramework {
    @Test
    public void testPrevious() {
        final ParticipationFramework participationFramework = new ParticipationFramework();

        participationFramework.addParticipant("NED1");
        participationFramework.addParticipant("PETER1");
        participationFramework.addParticipant("OLAF1");
        System.out.println(participationFramework);

        participationFramework.setSpeakerAddressing("NED1", "PETER1");
        System.out.println(participationFramework);

        participationFramework.setSpeaker("PETER1");
        System.out.println(participationFramework);

        participationFramework.setSpeaker("NED1");
        System.out.println(participationFramework);

        participationFramework.setSpeaker("PETER1");
        System.out.println(participationFramework);

        participationFramework.setSpeaker("OLAF1");
        System.out.println(participationFramework);

        participationFramework.setSpeaker("NED1");
        System.out.println(participationFramework);

        participationFramework.setSpeakerAddressing("PETER1", "OLAF1");
        System.out.println(participationFramework);

        System.out.println(participationFramework.getHearers());

        /*
{NED1=<< NED1 [0] >>, OLAF1=<< OLAF1 [0] >>, PETER1=<< PETER1 [0] >>}
{NED1=<< NED1 [2] >>, OLAF1=<< OLAF1 [0] >>, PETER1=<< PETER1 [1] >>}
{NED1=<< NED1 [1] >>, OLAF1=<< OLAF1 [0] >>, PETER1=<< PETER1 [2] >>}
{NED1=<< NED1 [2] >>, OLAF1=<< OLAF1 [0] >>, PETER1=<< PETER1 [1] >>}
{NED1=<< NED1 [1] >>, OLAF1=<< OLAF1 [0] >>, PETER1=<< PETER1 [2] >>}
{NED1=<< NED1 [0] >>, OLAF1=<< OLAF1 [2] >>, PETER1=<< PETER1 [1] >>}
{NED1=<< NED1 [2] >>, OLAF1=<< OLAF1 [1] >>, PETER1=<< PETER1 [0] >>}
{NED1=<< NED1 [0] >>, OLAF1=<< OLAF1 [1] >>, PETER1=<< PETER1 [2] >>}
         */
    }
}