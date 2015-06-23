package com.visural.retentionpolicy;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.joda.time.DateTime;

import static java.util.stream.Collectors.toSet;

/**
 *
 * @author richard
 */
public enum RetentionPolicy {

    years {
            @Override
            public Function<File, Comparable<?>> mapFunc(Collection<File> indexes, int keepCount) {
                return (File e) -> new DateTime(e.lastModified()).getYear();
            }
        },
    months {
            @Override
            public Function<File, Comparable<?>> mapFunc(Collection<File> indexes, int keepCount) {
                return (File e) -> new DateTime(e.lastModified()).getYear() * 12 + new DateTime(e.lastModified()).getMonthOfYear() - 1;
            }
        },
    weeks {
            @Override
            public Function<File, Comparable<?>> mapFunc(Collection<File> indexes, int keepCount) {
                return (File e) -> new DateTime(e.lastModified()).getYear() * 52 + new DateTime(e.lastModified()).getWeekOfWeekyear() - 1;
            }
        },
    days {
            @Override
            public Function<File, Comparable<?>> mapFunc(Collection<File> indexes, int keepCount) {
                return (File e) -> new DateTime(e.lastModified()).getYear() * 366 + new DateTime(e.lastModified()).getDayOfYear() - 1;
            }
        },
    hours {
            @Override
            public Function<File, Comparable<?>> mapFunc(Collection<File> indexes, int keepCount) {
                return (File e) -> new DateTime(e.lastModified()).getYear() * 366 * 24 + (new DateTime(e.lastModified()).getDayOfYear() - 1) * 24 + new DateTime(e.lastModified()).getHourOfDay() - 1;
            }
        },
    recent {
            @Override
            public Function<File, Comparable<?>> mapFunc(Collection<File> indexes, int keepCount) {
                return (File e) -> e.lastModified() + " " + e.getName();
            }
        };

    /**
     * Given a collection of index, returns the indexes that should be kept under this retention strategy.
     *
     * @param files
     * @param keepCount
     * @return
     */
    public Set<File> apply(Collection<File> files, int keepCount) {
        Multimap<Comparable<?>, File> grouped = Multimaps.index(files, mapFunc(files, keepCount));
        return grouped.keySet().stream()
            .sorted(Ordering.natural().reverse())
            .limit(keepCount)
            .map((Comparable<?> group) -> grouped.get(group).stream().sorted().findFirst().get())
            .collect(toSet());
    }

    public abstract Function<File, Comparable<?>> mapFunc(Collection<File> indexes, int keepCount);
}
