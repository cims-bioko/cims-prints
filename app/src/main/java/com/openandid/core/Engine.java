package com.openandid.core;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import bmtafis.simple.AfisEngine;
import bmtafis.simple.Fingerprint;
import bmtafis.simple.Person;
import logic.Finger;

import static com.openandid.core.Fingerprints.makePrint;
import static com.openandid.core.Fingerprints.populatePrints;


public class Engine {

    private static final String TAG = "AFISEngine";

    private static final double BEST_MATCH_THRESHOLD = 70.0;

    public static boolean ready = true;

    private AfisEngine engine;
    private PersonCache cache;
    private float threshold;

    Engine(Context context, float threshold) {
        engine = new AfisEngine(context);
        this.threshold = threshold;
        engine.setThreshold(this.threshold);
        cache = new PersonCache();
    }

    public void populateCache(Map<String, Map<String, String>> candidates) {
        Log.i(TAG, "loading " + candidates.size() + " candidates");
        ready = false;
        cache.populate(candidates);
        ready = true;
    }

    void addToCache(String uuid, Map<String, String> templates) {
        cache.add(uuid, templates);
    }

    private double verifyInCache(Person p1, int afisId) {
        Person p2 = cache.getPerson(afisId);
        if (p2 == null) {
            throw new IllegalArgumentException("person with id " + afisId + " is not in the database");
        }
        return verify(p1, p2);
    }

    private double verify(Person p1, Person p2) {
        return engine.verify(p1, p2);
    }

    List<Match> getBestMatches(Map<String, String> templates) {

        ready = false;

        Set<Integer> hits = new HashSet<>();
        Map<Integer, Double> scores = new HashMap<>();
        Person wholePerson = new EphemeralPerson(templates);
        Map<String, Person> personPerFinger = new HashMap<>();

        // create single-finger persons for filtering
        for (Finger finger : Finger.enrolledValues()) {
            personPerFinger.put(finger.name(), new EphemeralPerson(finger, templates.get(finger.name())));
        }

        // identify potential matches based on all fingers
        List<Person> spikes = identify(wholePerson);
        Log.i(TAG, "identified " + spikes.size() + " candidates");
        for (Person p : spikes) {
            hits.add(p.getId());
            scores.put(p.getId(), 0.0);
        }

        engine.setThreshold(0.0f);

        // filter those potentials based on sum of verification scores for individual fingers
        List<Integer> filteredHits = new ArrayList<>();
        for (int id : hits) {
            double scoreSum = 0.0;
            for (Finger finger : Finger.enrolledValues()) {
                Person singleFingerPerson = personPerFinger.get(finger.name());
                scoreSum += verifyInCache(singleFingerPerson, id);
            }
            if (scoreSum >= BEST_MATCH_THRESHOLD) {
                filteredHits.add(id);
                scores.put(id, scoreSum);
            }
        }

        engine.setThreshold(threshold);

        // create match list from the filtered set, sorted on score
        List<Engine.Match> matches = new ArrayList<>();
        for (int id : filteredHits) {
            matches.add(new Match(cache.getPerson(id).getUuid(), scores.get(id)));
        }
        Collections.sort(matches);

        ready = true;

        Log.i(TAG, "returning " + matches.size() + " matches");

        return matches;
    }

    @SuppressWarnings("unchecked")
    private List<Person> identify(Person probe) {
        return cache.isEmpty() ? Collections.EMPTY_LIST : (List<Person>) engine.identify(probe, cache.toArray());
    }

    class Match implements Comparable<Match> {

        double score;
        String uuid;

        Match(String uuid, Double score) {
            this.score = score;
            this.uuid = uuid;
        }

        double getScore() {
            return score;
        }

        @Override
        public int compareTo(Match o) {
            return Double.compare(o.getScore(), getScore());
        }
    }
}

class PersonCache {

    private static final String TAG = PersonCache.class.getSimpleName();

    private static final AtomicInteger ID_GEN = new AtomicInteger();

    private Map<Integer, CachedPerson> candidates;

    PersonCache() {
        candidates = new LinkedHashMap<>();
    }

    private void clear() {
        candidates.clear();
    }

    boolean isEmpty() {
        return candidates.isEmpty();
    }

    CachedPerson getPerson(int afisId) {
        return candidates.get(afisId);
    }

    void add(String uuid, Map<String, String> templates) {
        try {
            int afisId = ID_GEN.getAndIncrement();
            CachedPerson person = new CachedPerson(afisId, uuid, templates);
            candidates.put(afisId, person);
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't add " + uuid, e);
        }
    }

    Person[] toArray() {
        return candidates.values().toArray(new Person[candidates.size()]);
    }

    void populate(Map<String, Map<String, String>> candidates) {
        clear();
        for (Map.Entry<String, Map<String, String>> entry : candidates.entrySet()) {
            try {
                add(entry.getKey(), entry.getValue());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}

class EphemeralPerson extends Person {

    private static final int DEFAULT_AFIS_ID = 999999999;

    private EphemeralPerson(int afisId) {
        setId(afisId);
    }

    EphemeralPerson(Map<String, String> templates) {
        this(DEFAULT_AFIS_ID, templates);
    }

    EphemeralPerson(int afisId, Map<String, String> templates) {
        this(afisId);
        populatePrints(getFingerprints(), templates, Finger.enrolledValues());
        if (templates.isEmpty()) {
            throw new IllegalArgumentException("No Valid Fingerprints found for " + DEFAULT_AFIS_ID);
        }
    }

    EphemeralPerson(Finger finger, String template) {
        this(DEFAULT_AFIS_ID);
        getFingerprints().add(makePrint(finger, template));
    }
}

class CachedPerson extends EphemeralPerson {

    private String uuid;

    CachedPerson(int afisId, String uuid, Map<String, String> templates) {
        super(afisId, templates);
        this.uuid = uuid;
    }

    String getUuid() {
        return uuid;
    }
}

class Fingerprints {

    private static final String TAG = Fingerprints.class.getSimpleName();

    static void populatePrints(List<Fingerprint> prints, Map<String, String> templates, Finger... fingers) {
        for (Finger finger : fingers) {
            String template = templates.get(finger.name());
            if (template != null) {
                try {
                    prints.add(makePrint(finger, template));
                } catch (Exception e) {
                    Log.e(TAG, String.format("Error parsing iso template: %s | %s | size(%s)", finger, e.toString(), template.length()));
                }
            }
        }
    }

    static Fingerprint makePrint(Finger finger, String template) {
        Fingerprint print = new Fingerprint();
        print.setIsoTemplate(hexDecode(template));
        print.setFinger(finger.afisValue());
        return print;
    }

    private static byte[] hexDecode(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}



