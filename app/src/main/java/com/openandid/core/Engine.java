package com.openandid.core;

import android.content.Context;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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


    private List<Person> candidates = null;
    private Map<Integer, Person> candidates_map = null;
    private Map<String, Integer> int_alias = null;
    private Map<Integer, String> string_alias = null;
    private AtomicInteger _id_generator = null;
    private float threshold;

    private final static String TAG = "AFISEngine";
    private AfisEngine mEngine;
    private static JSONParser parser = null;
    private double MATCH_THRESOLD = 70.0;

    public static boolean is_ready = true;

    public Engine(Context mContext, float threshold) {
        this.threshold = threshold;
        mEngine = new AfisEngine(mContext);
        mEngine.setThreshold(threshold);
        Log.i(TAG, "Engine Started");
        candidates = new ArrayList<>();
        candidates_map = new HashMap<>();

        int_alias = new HashMap<>();
        string_alias = new HashMap<>();
        _id_generator = new AtomicInteger();
    }

    public void add_candidate_to_cache(String uuid_in, Map<String, String> templates) {

        try {
            int _ref_id = _id_generator.getAndIncrement();
            Person p = map_to_person(_ref_id, templates);
            int_alias.put(uuid_in, _ref_id);
            string_alias.put(_ref_id, uuid_in);
            Log.i(TAG, "Adding uuid:" + uuid_in + " as _id" + Integer.toString(_ref_id));
            candidates.add(p);
            candidates_map.put(_ref_id, p);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't add " + uuid_in);
            throw new IllegalArgumentException(e.toString());
        }
    }

    public void cache_candidates(Map<String, Map<String, String>> candidates_in) {
        is_ready = false;
        candidates = new ArrayList<>();
        candidates_map = new HashMap<>();
        Log.i(TAG, "Loading Gallery with " + Integer.toString(candidates_in.size()) + " candidates.");
        for (String uuid_in : candidates_in.keySet()) {
            try {
                add_candidate_to_cache(uuid_in, candidates_in.get(uuid_in));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        is_ready = true;
    }

    //TODO kill
    public void add_candidate_to_db(int id, Person person, JSONObject ob) {
        candidates.add(person);
        String _id = Integer.toString(id);
        Controller.db_handle.save_user_to_db(_id, ob.toString());
    }

    public double verify_in_cache(String hex_template_1, String uuid) {
        int id = int_alias.get(uuid);
        Person p = candidates_map.get(id);
        if (p != null) {
            return verify(hex_template_1, p);
        }
        throw new IllegalArgumentException("UUID " + uuid + " is not in the database");
    }

    public double verify_in_cache(Person p1, String uuid) {
        int id = int_alias.get(uuid);
        return verify_in_cache(p1, id);
    }

    public double verify_in_cache(Person p1, int uuid) {
        Person p2 = candidates_map.get(uuid);
        if (p2 != null) {

            return verify(p1, p2);
        }
        throw new IllegalArgumentException("UUID " + uuid + " is not in the database");
    }

    public double verify(String hex_template_1, Person p2) {
        Person p1 = new Person();
        final Fingerprint f1 = new Fingerprint();
        f1.setFinger(Finger.ANY);
        f1.setIsoTemplate(hexStringToByteArray(hex_template_1));
        return verify(p1, p2);
    }


    public double verify(String hex_template_1, String hex_template_2) {
        return verify(hexStringToByteArray(hex_template_1), hexStringToByteArray(hex_template_2));
    }

    public double verify(byte[] template_1, byte[] template_2) {
        Person p1 = new Person();
        Person p2 = new Person();
        final Fingerprint f1 = new Fingerprint();
        final Fingerprint f2 = new Fingerprint();
        f1.setIsoTemplate(template_1);
        f2.setIsoTemplate(template_2);
        p1.setFingerprints(new ArrayList<Fingerprint>() {{
            add(f1);
        }});
        p2.setFingerprints(new ArrayList<Fingerprint>() {{
            add(f2);
        }});

        return mEngine.verify(p1, p2);
    }

    public double verify(Person p1, Person p2) {

        return mEngine.verify(p1, p2);
    }

    public List<Match> get_best_matches(Map<String, String> templates) {
        is_ready = false;
        Set<Integer> hits = new HashSet<>();
        Map<Integer, Double> scores = new HashMap<>();
        Map<Integer, Double> v_scores = new HashMap<>();
        List<String> fingers = new ArrayList<String>() {{
            add("left_thumb");
            add("right_thumb");
            add("left_index");
            add("right_index");
        }};
        Person whole_person = map_to_person(999999999, templates);
        Map<String, Person> p_hold = new HashMap<String, Person>();
        for (String f : fingers) {
            Map<String, String> f_hold = new HashMap<String, String>();
            f_hold.put(f, templates.get(f));
            p_hold.put(f, map_to_person(999999999, f_hold));
        }
        List<Person> spikes = identify(whole_person);
        Log.i(TAG, "Found " + Integer.toString(spikes.size()) + " candidates for inspection.");
        for (Person p : spikes) {
            hits.add(p.getId());
            scores.put(p.getId(), 0.0);
            v_scores.put(p.getId(), 0.0);
        }

        this.mEngine.setThreshold(0.0f);
        List<Integer> super_threshold = new ArrayList<Integer>();
        for (int id : hits) {
            //Log.i(TAG, "Verify whole for ID: " + Integer.toString(id) + " : " + Double.toString(whole_score));
            double score = 0.0;
            for (String f : fingers) {
                double f_score = verify_in_cache(p_hold.get(f), id);
                Log.i(TAG, "Verify finger " + "for id: " + Integer.toString(id) + " : " + Double.toString(f_score));
                score += f_score;
            }
            if (score >= MATCH_THRESOLD) {
                super_threshold.add(id);
                scores.put(id, score);
            }
        }

        mEngine.setThreshold(threshold);
        List<Engine.Match> matches = new ArrayList<Engine.Match>();

        for (int id : super_threshold) {
            String uuid = string_alias.get(id);
            double score = scores.get(id);
            matches.add(new Match(uuid, score));
        }
        Collections.sort(matches);
        Log.i(TAG, "Returning " + Integer.toString(matches.size()) + " matches.");
        is_ready = true;
        return matches;
    }

    public ArrayList<Person> identify_in_cohort(Person probe, ArrayList<Person> other_candidates) {
        return (ArrayList<Person>) mEngine.identify(probe, other_candidates.toArray(new Person[0]));
    }

    public ArrayList<Person> identify(Person probe) {
        if (candidates.size() > 0) {
            Log.i(TAG, "Starting Identify on gallery of " + Integer.toString(candidates.size()) + " candidates.");
            return (ArrayList<Person>) mEngine.identify(probe, candidates.toArray(new Person[0]));
        } else {
            Log.i(TAG, "No Cohort to Search Returning Premptive empty set");
            return new ArrayList<Person>();
        }
    }

    private void load_candidates_from_db() {
        Map<String, Person> db_candidates = Controller.db_handle.get_all_users();
        candidates = new ArrayList<Person>(db_candidates.values());
    }

    public static Person map_to_person(Integer uuid, Map<String, String> templates) {
        Person p = new Person();
        p.setId(uuid);
        List<Fingerprint> prints = new ArrayList<>();
        List<String> fingers = new ArrayList<String>() {{
            add("left_thumb");
            add("right_thumb");
            add("left_index");
            add("right_index");
        }};
        if (templates != null) {
            int c = 0;
            for (String finger : fingers) {
                String temp = templates.get(finger);
                if (temp != null) {
                    Fingerprint f = new Fingerprint();
                    try {
                        f.setIsoTemplate(hexStringToByteArray(temp));
                        f.setFinger(Finger.valueOf(finger.toUpperCase()));
                        prints.add(f);
                        c += 1;
                    } catch (Exception e) {
                        Log.e(TAG, String.format("Error parsing iso template: %s | %s | size(%s)", finger, e.toString(), temp.length()));
                        //throw new IllegalArgumentException(e.getMessage());
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

    public static Person json_string_to_person(String id, String json_in) {
        JSONObject person_json = null;// = new JSONObject();
        if (parser == null) {
            parser = new JSONParser();
        }
        try {
            person_json = (JSONObject) parser.parse(json_in);
        } catch (ParseException e) {
            Log.w(TAG, "failed to parse json", e);
        }
        Person p = new Person();
        if (person_json != null) {
            p.setId(Integer.parseInt(id));
            List<Fingerprint> prints = new ArrayList<>();
            if (person_json.get("left_thumb") != null) {
                Fingerprint f = new Fingerprint();
                f.setIsoTemplate(hexStringToByteArray((String) person_json.get("left_thumb")));
                f.setFinger(Finger.LEFT_THUMB);
                prints.add(f);
            }
            if (person_json.get("right_thumb") != null) {
                Fingerprint f = new Fingerprint();
                f.setIsoTemplate(hexStringToByteArray((String) person_json.get("right_thumb")));
                f.setFinger(Finger.RIGHT_THUMB);
                prints.add(f);
            }
            if (person_json.get("right_index") != null) {
                Fingerprint f = new Fingerprint();
                f.setIsoTemplate(hexStringToByteArray((String) person_json.get("right_index")));
                f.setFinger(Finger.RIGHT_INDEX);
                prints.add(f);
            }
            if (person_json.get("left_index") != null) {
                Fingerprint f = new Fingerprint();
                f.setIsoTemplate(hexStringToByteArray((String) person_json.get("left_index")));
                f.setFinger(Finger.LEFT_INDEX);
                prints.add(f);
            }
            p.setFingerprints(prints);
        }
        return p;


    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public class Match implements Comparable<Match> {

        public double score;
        public String uuid;

        public Match(String uuid, Double score) {
            this.score = score;
            this.uuid = uuid;
        }

        public double getScore() {
            return score;
        }

        @Override
        public int compareTo(Match o) {
            return Double.compare(o.getScore(), getScore());
        }

    }
}

