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

package is.ru.openbeat.freetts;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.NullAudioPlayer;
import com.sun.speech.freetts.audio.JavaStreamingAudioPlayer;
import is.ru.openbeat.pipeline.ITimingSource;

import java.util.List;

public class FreeTtsTimingModule extends AbstractModule {
    protected void configure() {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        
        final Multibinder<ITimingSource> multibinder = Multibinder.newSetBinder(binder(), ITimingSource.class);
        multibinder.addBinding().to(FreeTtsTimingSource.class);
    }

    @Named("freetts.timingvoice")
    @Provides
    @Singleton
    public Voice createVoice(TimingProcessor timingProcessor, @Named("freetts.stretch") Float durationStretch) {
        final VoiceManager voiceManager = VoiceManager.getInstance();
        final Voice voice = voiceManager.getVoices()[1];
        voice.setDurationStretch(durationStretch);

        voice.setAudioPlayer(new NullAudioPlayer());
        //voice.setAudioPlayer(new JavaStreamingAudioPlayer());
        voice.allocate(); // TODO: deallocate never gets called -- investigate if cleanup is needed

        final List<UtteranceProcessor> utteranceProcessors = (List<UtteranceProcessor>) voice.getUtteranceProcessors();
        utteranceProcessors.add(timingProcessor);

        return voice;
    }

    @Named("freetts.playbackvoice")
    @Provides
    @Singleton
    public Voice createPlaybackVoice(@Named("freetts.stretch") Float durationStretch) {
        final VoiceManager voiceManager = VoiceManager.getInstance();
        final Voice voice = voiceManager.getVoices()[1];
        voice.setDurationStretch(durationStretch);

        voice.setAudioPlayer(new JavaStreamingAudioPlayer());
        voice.allocate(); // TODO: deallocate never gets called -- investigate if cleanup is needed

        return voice;
    }

    @Provides
    @Singleton
    public TimingProcessor createProcessor() {
        return new TimingProcessor();
    }
}
