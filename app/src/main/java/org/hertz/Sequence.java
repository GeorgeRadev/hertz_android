package org.hertz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Sequence {
    public String name;
    public String description;
    public String frequencies;
    public List<String> frequenciesList;

    public Sequence(String name, String description, String frequences) {
        this.name = name;
        this.description = description;
        this.frequencies = frequences;
        frequenciesList = frequenciesToList(frequences);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        return name.equals(sequence.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void validate() {
        if (name == null || name.length() <= 0) {
            throw new IllegalStateException("sequence name cannot be empty");
        }
        if (frequencies == null || frequencies.length() <= 0) {
            throw new IllegalStateException("frequencies cannot be empty");
        }
        frequenciesList = frequenciesToList(frequencies);
        frequencies = frequencesToString(frequenciesList);
    }

    public static List<String> frequenciesToList(String frequencies) {
        String[] tokens = frequencies.split("\\s");
        List<String> result = new ArrayList<>(tokens.length);

        for (String token : tokens) {
            token = token.trim();
            if (token.length() > 0) {
                try {
                    Double.parseDouble(token);
                } catch (Exception e) {
                    throw new IllegalArgumentException("not a number: " + token);
                }
                result.add(token);
            }
        }

        return result;
    }


    public static double[] frequenciesToDoubles(String frequencies) {
        List<String> frequenciesList = frequenciesToList(frequencies);
        double[] result = new double[frequenciesList.size()];
        int i = 0;
        for (String frequency : frequenciesList) {
            try {
                result[i++] = Double.parseDouble(frequency);
            } catch (Exception e) {
                // should be handled by frequenciesToList
            }
        }

        return result;
    }

    public static String frequencesToString(List<String> frequencies) {
        StringBuilder result = new StringBuilder();
        for (String frequency : frequencies) {
            if (frequency.length() > 0) {
                result.append(frequency);
                if (frequency.startsWith("+")) {
                    result.append(' ');
                } else {
                    result.append('\n');
                }
            }
        }
        return result.toString();
    }

    public static String formatSeconds(int seconds) {
        StringBuilder result = new StringBuilder(32);
        if (seconds < 0) {
            seconds = -seconds;
            result.append("- ");
        }
        int m = seconds / 60;
        if (m < 10) {
            result.append('0');
        }
        result.append(m);
        result.append(':');
        int s = seconds % 60;
        if (s < 10) {
            result.append('0');
        }
        result.append(s);

        return result.toString();
    }

    public static String formatMilliseconds(int millis) {
        int seconds = millis / 1000;
        return formatSeconds(seconds);
    }
}
