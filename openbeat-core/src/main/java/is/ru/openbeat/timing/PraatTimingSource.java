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

package is.ru.openbeat.timing;

import static com.google.common.collect.Maps.newHashMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.participation.ParticipationFrameworkBase;
import is.ru.openbeat.pipeline.ITimingSource;
import is.ru.openbeat.speech.WavePlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.Scanner;

/**
 * Class <tt>PraatTimingSource</tt> reads static word timings created with from <a
 * href="http://www.fon.hum.uva.nl/praat/">Praat</a>.
 * <p/>
 * Each timed word has a file ending with ".Sound" which contains the respective word timing. The name of the file is a
 * pointer to the word in the utterance which is being processed. If same word appears more than once in the utterance,
 * a suffix counter should be appended to the filename. E.g. if the word "car" appears twice, there should be a
 * "car.Sound" file and a "car2.Sound" file.
 * <p/>
 * The timing source is also speaker-aware. It adds a system-specific seperator char and the name of the speaker to the
 * directory which has been configured to store praat timings as well as the wave file containing the recorded speech.
 * <p/>
 * Note: the timing filenames are not case-sensitive.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
@Singleton
public class PraatTimingSource implements ITimingSource {
    private static final Logger log = LoggerFactory.getLogger(PraatTimingSource.class);

    private final Multimethod mm = new Multimethod(this);

    private final Provider<String> directoryPath;
    private final ParticipationFrameworkBase participationFrameworkBase;

    @Inject
    public PraatTimingSource(@Named("praat.dir") Provider<String> directoryPath,
                             ParticipationFrameworkBase participationFrameworkBase) {
        this.directoryPath = directoryPath;
        this.participationFrameworkBase = participationFrameworkBase;
    }

    public void play() {
        final String dir = directoryPath.get() + File.separator + participationFrameworkBase.getCurrentParticipationFramework().getSpeaker().getName();
        final String filename = dir + File.separator + "sentence.wav";

        final File speechFile = new File(filename);
        if (speechFile.exists()) {
            final WavePlayer wavePlayer = new WavePlayer(filename);
            final Thread thread = new Thread(wavePlayer);
            thread.start();
        } else {
            log.warn("'{}' does not exist, cannot play speech", filename);
        }
    }

    public Utterance process(Utterance utterance) {
        final String dir = directoryPath.get() + File.separator + participationFrameworkBase.getCurrentParticipationFramework().getSpeaker().getName();
        log.debug("Reading praat timings from '{}' directory", dir);

        final File directory = new File(dir);
        if (!directory.exists()) {
            log.warn("'{}' does not exist, cannot read praat timings", dir);
            return utterance;
        }

        final File[] soundFiles = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("Sound");
            }
        });

        final Map<String, Pair<Float, Float>> wordTimes = newHashMap();
        for (File soundFile : soundFiles) {
            final String filename = soundFile.getName();
            log.debug("Importing timings from {}", filename);
            try {
                final String wordToken = filename.substring(0, filename.lastIndexOf(".")).toLowerCase();
                final Pair<Float, Float> timings = fromFile(soundFile);
                log.debug("'{}' is timed at {}", wordToken, timings);
                wordTimes.put(wordToken, timings);
            } catch (FileNotFoundException e) {
                log.warn("File {} does not exist", filename);
            } catch (RuntimeException e) {
                log.warn("Cannot read timing information from {}", filename);
                log.warn("Exception", e);
            }
        }

        final Map<String, Integer> wordCounter = newHashMap();
        for (Clause clause : utterance.getClauses()) {
            handleArticulation(wordTimes, wordCounter, clause.getArticulations().getFirst());
            handleArticulation(wordTimes, wordCounter, clause.getArticulations().getSecond());
        }

        return utterance;
    }

    /**
     * Reads the begin time and the end time of a word from the given file.
     *
     * @param soundFile the file containing the timing information
     * @return a pair of timings, begin and end
     * @throws FileNotFoundException if the timing file does not exist
     */
    private Pair<Float, Float> fromFile(File soundFile) throws FileNotFoundException {
        final Scanner scanner = new Scanner(soundFile);
        // skip to line 4, which contains the begin time, read it, skip a line, read the end time, both as float
        scanner.nextLine();
        scanner.nextLine();
        scanner.nextLine();

        float t1 = Float.valueOf(scanner.nextLine());
        float t2 = Float.valueOf(scanner.nextLine());
        return Pair.of(t1, t2);
    }

    private void handleArticulation(Map<String, Pair<Float, Float>> wordTimes, Map<String, Integer> wordCounter,
                                    IArticulation articulation) {
        for (IFeatureStructure featureStructure : articulation.getPhrases()) {
            mm.match(wordTimes, wordCounter, featureStructure);
        }
    }

    @Multi
    private void handleConstituent(Map<String, Pair<Float, Float>> wordTimes, Map<String, Integer> wordCounter,
                                   Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(wordTimes, wordCounter, featureStructure);
        }
    }

    @Multi
    private void handleWord(Map<String, Pair<Float, Float>> wordTimes, Map<String, Integer> wordCounter, Word word) {
        if (!word.is(WordClassAttribute.PUNCTUATION)) {
            final String wordKey = word.getToken().toLowerCase();
            final Integer count;
            if (wordCounter.containsKey(wordKey)) {
                count = wordCounter.get(wordKey) + 1;
            } else {
                count = 1;
            }
            wordCounter.put(wordKey, count);

            final String timeKey = count > 1 ? wordKey + String.valueOf(count) : wordKey;
            if (wordTimes.containsKey(timeKey)) {
                final Pair<Float, Float> timings = wordTimes.get(timeKey);
                log.debug("Setting {} timing on {}", timings, word);
                word.setBeginTime(timings.getFirst());
                word.setEndTime(timings.getSecond());
            } else {
                log.warn("Do not have timing for {}", word);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
