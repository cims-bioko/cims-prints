package com.openandid.core;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import bmtafis.simple.AfisEngine;
import bmtafis.simple.Finger;
import bmtafis.simple.Fingerprint;
import bmtafis.simple.Person;


public class Engine {

    private static final String TAG = "AFISEngine";
    private static final List<String> FINGERS = new ArrayList<String>() {{
        add("left_thumb");
        add("right_thumb");
        add("left_index");
        add("right_index");
    }};
    private static final double MATCH_THRESOLD = 70.0;

    public static boolean ready = true;

    private List<Person> candidates = null;
    private Map<Integer, Person> candidatesMap = null;
    private Map<String, Integer> intAlias = null;
    private Map<Integer, String> stringAlias = null;
    private AtomicInteger idGenerator = null;
    private float threshold;

    private AfisEngine mEngine;

    public Engine(Context mContext, float threshold) {
        this.threshold = threshold;
        mEngine = new AfisEngine(mContext);
        mEngine.setThreshold(threshold);
        Log.i(TAG, "Engine Started");
        candidates = new ArrayList<>();
        candidatesMap = new HashMap<>();
        intAlias = new HashMap<>();
        stringAlias = new HashMap<>();
        idGenerator = new AtomicInteger();
    }

    void addCandidateToCache(String uuid, Map<String, String> templates) {
        try {
            int _ref_id = idGenerator.getAndIncrement();
            Person p = mapToPerson(_ref_id, templates);
            intAlias.put(uuid, _ref_id);
            stringAlias.put(_ref_id, uuid);
            Log.i(TAG, "Adding uuid:" + uuid + " as _id" + Integer.toString(_ref_id));
            candidates.add(p);
            candidatesMap.put(_ref_id, p);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't add " + uuid);
            throw new IllegalArgumentException(e.toString());
        }
    }

    public void cacheCandidates(Map<String, Map<String, String>> candidates) {
        Log.i(TAG, "Loading Gallery with " + Integer.toString(candidates.size()) + " candidates.");
        ready = false;
        this.candidates = new ArrayList<>();
        candidatesMap = new HashMap<>();
        for (String uuid : candidates.keySet()) {
            try {
                addCandidateToCache(uuid, candidates.get(uuid));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        ready = true;
    }

    private double verifyInCache(Person p1, int uuid) {
        Person p2 = candidatesMap.get(uuid);
        if (p2 != null) {
            return verify(p1, p2);
        }
        throw new IllegalArgumentException("UUID " + uuid + " is not in the database");
    }

    private double verify(Person p1, Person p2) {
        return mEngine.verify(p1, p2);
    }

    List<Match> getBestMatches(Map<String, String> templates) {

        ready = false;

        Set<Integer> hits = new HashSet<>();
        Map<Integer, Double> scores = new HashMap<>();
        Person whole_person = mapToPerson(999999999, templates);
        Map<String, Person> p_hold = new HashMap<>();

        for (String finger : FINGERS) {
            Map<String, String> f_hold = new HashMap<>();
            f_hold.put(finger, templates.get(finger));
            p_hold.put(finger, mapToPerson(999999999, f_hold));
        }

        List<Person> spikes = identify(whole_person);
        Log.i(TAG, "Found " + Integer.toString(spikes.size()) + " candidates for inspection.");
        for (Person p : spikes) {
            hits.add(p.getId());
            scores.put(p.getId(), 0.0);
        }

        this.mEngine.setThreshold(0.0f);
        List<Integer> super_threshold = new ArrayList<>();
        for (int id : hits) {
            double score = 0.0;
            for (String finger : FINGERS) {
                double f_score = verifyInCache(p_hold.get(finger), id);
                Log.i(TAG, "Verify finger for id: " + Integer.toString(id) + " : " + Double.toString(f_score));
                score += f_score;
            }
            if (score >= MATCH_THRESOLD) {
                super_threshold.add(id);
                scores.put(id, score);
            }
        }

        mEngine.setThreshold(threshold);
        List<Engine.Match> matches = new ArrayList<>();
        for (int id : super_threshold) {
            String uuid = stringAlias.get(id);
            double score = scores.get(id);
            matches.add(new Match(uuid, score));
        }
        Collections.sort(matches);
        Log.i(TAG, "Returning " + Integer.toString(matches.size()) + " matches.");

        ready = true;

        return matches;
    }

    private ArrayList<Person> identify(Person probe) {
        if (candidates.size() > 0) {
            Log.i(TAG, "Starting Identify on gallery of " + Integer.toString(candidates.size()) + " candidates.");
            return (ArrayList<Person>) mEngine.identify(probe, candidates.toArray(new Person[0]));
        } else {
            Log.i(TAG, "No Cohort to Search Returning Premptive empty set");
            return new ArrayList<>();
        }
    }

    private static Person mapToPerson(Integer uuid, Map<String, String> templates) {

        Person p = new Person();
        p.setId(uuid);
        List<Fingerprint> prints = new ArrayList<>();

        if (templates != null) {
            int c = 0;
            for (String finger : FINGERS) {
                String temp = templates.get(finger);
                if (temp != null) {
                    Fingerprint f = new Fingerprint();
                    try {
                        f.setIsoTemplate(hexToBytes(temp));
                        f.setFinger(Finger.valueOf(finger.toUpperCase()));
                        prints.add(f);
                        c += 1;
                    } catch (Exception e) {
                        Log.e(TAG, String.format("Error parsing iso template: %s | %s | size(%s)", finger, e.toString(), temp.length()));
                    }
                }
            }
            if (c == 0) {
                throw new IllegalArgumentException("No Valid Fingerprints found for " + uuid);
            }
            p.setFingerprints(prints);
            return p;
        }

        return null;
    }

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
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

