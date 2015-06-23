package com.visural.retentionpolicy;

import com.google.common.base.Splitter;
import com.visural.retentionpolicy.RetentionPolicyApplier.Action;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

/**
 *
 * @author richard
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage:");
            System.out.println("        java -jar RetentionPolicy.jar [policy] [action] [folder] [filename regex]\n");
            System.out.println("  e.g.  java -jar RetentionPolicy.jar months=12,weeks=4,days=7,recent=5 rm /home/user/backups .*[.]tar[.]gz");
        } else {
            Map<RetentionPolicy, Integer> policy = parsePolicy(args[0]);
            Action action = Action.valueOf(args[1]);
            File folder = new File(args[2]);
            String regex = args[3];
            RetentionPolicyApplier applier = new RetentionPolicyApplier(policy);
            applier.apply(folder, regex, action);
        }
    }

    private static Map<RetentionPolicy, Integer> parsePolicy(String arg) {
        Map<RetentionPolicy, Integer> policy = new HashMap<>();
        newArrayList(Splitter.on(",").trimResults().omitEmptyStrings().split(arg)).forEach((String s) -> {
            List<String> kv = newArrayList(Splitter.on("=").omitEmptyStrings().trimResults().split(s));
            checkState(kv.size() == 2, s + " is not a valid retention policy setting");
            RetentionPolicy p = RetentionPolicy.valueOf(kv.get(0));
            int amount = Integer.parseInt(kv.get(1));
            checkState(amount > 0, s + " : value should be > 0");
            policy.put(p, amount);
        });
        return policy;
    }
}
