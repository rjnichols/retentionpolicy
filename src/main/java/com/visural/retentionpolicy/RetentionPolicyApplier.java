package com.visural.retentionpolicy;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;
import static com.visural.retentionpolicy.RetentionPolicyApplier.Action.echoKeep;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

/**
 *
 * @author Richard Nichols
 */
public class RetentionPolicyApplier {

    public enum Action {

        rm,
        echo,
        log,
        echoKeep
    }

    private static final Logger log = LoggerFactory.getLogger(RetentionPolicyApplier.class);

    private final Map<RetentionPolicy, Integer> policies;

    public RetentionPolicyApplier(Map<RetentionPolicy, Integer> policies) {
        checkNotNull(policies);
        checkState(!policies.isEmpty(), "Need to specify a retention policy.");

        this.policies = policies;
        Integer recentCount = policies.get(RetentionPolicy.recent);
        if (recentCount == null || recentCount < 1) {
            log.warn("Retention strategy does not have a recent count >= 1. This is usually not desirable.");
        }
    }

    public void apply(File folder, String filenameRegex, Action action) {
        checkState(folder != null && folder.exists() && folder.isDirectory(), folder + " is not a valid directory to process.");

        Pattern regex = Pattern.compile(filenameRegex);
        Set<File> files = asList(folder.listFiles()).stream()
            .filter(f -> regex.matcher(f.getName()).matches())
            .collect(toSet());

        Set<File> keep = newHashSet();

        for (Map.Entry<RetentionPolicy, Integer> strategy : policies.entrySet()) {
            keep.addAll(strategy.getKey().apply(files, strategy.getValue()));
        }

        if (action == echoKeep) {
            keep.stream().sorted().forEach(k -> System.out.println(k.getName()));
        } else {
            Set<File> expired = difference(files, keep);
            for (File e : expired) {
                switch (action) {
                    case log:
                        log.info("File '{}' has expired due to retention policy {}", e, policies);
                        break;
                    case echo:
                        System.out.println(e.getName());
                        break;
                    case rm:
                        log.info("Deleting expired file '{}' due to retention policy {}", e, policies);
                        checkState(e.delete(), "Unable to delete file: " + e);
                        break;
                }
            }
        }

    }

}
